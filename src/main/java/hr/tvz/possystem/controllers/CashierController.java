package hr.tvz.possystem.controllers;

import hr.tvz.possystem.database.ProductDao;
import hr.tvz.possystem.database.ProductDaoImpl;
import hr.tvz.possystem.database.SaleDao;
import hr.tvz.possystem.database.SaleDaoImpl;
import hr.tvz.possystem.model.Product;
import hr.tvz.possystem.model.SaleItem;
import hr.tvz.possystem.printer.PosReceiptPrinter;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

/**
 * Upravlja glavnim sučeljem blagajničkog dijela aplikacije gdje se vrši prodaja artikala i ispis računa.
 */
public class CashierController {

    @FXML
    private TableView<SaleItem> cartItemsTableView;
    @FXML
    private TableColumn<SaleItem, String> productNameColumn;
    @FXML
    private TableColumn<SaleItem, Integer> productQuantityColumn;
    @FXML
    private TableColumn<SaleItem, BigDecimal> priceColumn;
    @FXML
    private TableColumn<SaleItem, BigDecimal> priceTotalColumn;
    @FXML
    private TextField searchByIdTextField;
    @FXML
    private Label total;
    @FXML
    private Label discountLabel;
    @FXML
    private Label totalGrandLabel;
    @FXML
    private ListView<Product> saleItemsAutoCompleteListView;

    private boolean isSaleCommitted = false;
    @FXML
    private TextField receivedAmount;
    private final ProductDao productDao = new ProductDaoImpl();
    private final SaleDao saleDao = new SaleDaoImpl();
    private ObservableList<SaleItem> saleItemsFromCart = FXCollections.observableArrayList();

    /**
     * Postavlja testne artikle u listu koja se koristi u tablici.
     * Koristi se isključivo za testiranje metoda za izračun ukupne cijene i popusta.
     *
     * @param items testni (mock) artikli nad kojima se vrše testovi
     */
    public void setSaleItemsFromCart(ObservableList<SaleItem> items) {
        this.saleItemsFromCart = items;
    }


    /**
     * Inicijalizira {@code CellValueFactory} za sve kolone i prilagođava njihove širine
     * prema širini tablice.
     * Omogućuje uređivanje količine artikala u tablici i brisanje artikala
     * pritiskom tipke Backspace ili Delete.
     */
    public void initialize() {

        productNameColumn.setCellValueFactory(product -> new ReadOnlyStringWrapper(product.getValue().getProduct().getName()));
        productQuantityColumn.setCellValueFactory(product -> new ReadOnlyObjectWrapper<>(product.getValue().getQuantity()));
        priceColumn.setCellValueFactory(product -> new ReadOnlyObjectWrapper<>(product.getValue().getProduct().getPrice()));
        priceTotalColumn.setCellValueFactory(product -> new ReadOnlyObjectWrapper<>(product.getValue().getUnitPrice().multiply(BigDecimal.valueOf(product.getValue().getQuantity()))));
        saleItemsAutoCompleteListView.setVisible(false);
        cartItemsTableView.setEditable(true);
        productQuantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        productQuantityColumn.setOnEditCommit(event -> {
            SaleItem item = event.getRowValue();
            int newQuantity = event.getNewValue();
            if (canProductBeAdded(item.getProduct().getId(), newQuantity)) {
                item.setQuantity(Math.max(newQuantity, 1));
                cartItemsTableView.refresh();
                refreshTotalLabels();
            } else {
                item.setQuantity(1);
                cartItemsTableView.refresh();
                showAlert("Nedovoljno zaliha za proizvod " + item.getProduct().getName(), Alert.AlertType.ERROR);
            }
        });

        cartItemsTableView.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double width = newWidth.doubleValue();
            productNameColumn.setPrefWidth(width * 0.40);
            productQuantityColumn.setPrefWidth(width * 0.15);
            priceColumn.setPrefWidth(width * 0.20);
            priceTotalColumn.setPrefWidth(width * 0.25);
        });
        cartItemsTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        cartItemsTableView.setOnKeyPressed(event -> {
            if ((event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE)) {
                if (LoginController.authorizeCashier()) {
                    var selected = cartItemsTableView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        cartItemsTableView.getItems().remove(selected);
                        refreshTotalLabels();
                        cartItemsTableView.getSelectionModel().clearSelection();
                    }
                }
            }
        });


    }

    /**
     * Omogućava dodavanje proizvoda iz ListView-a u TableView dvostrukim klikom na odabrani proizvod.
     *
     * @param event klik miša na redak u ListView
     */
    public void addSaleItemByClick(MouseEvent event) {

        if (saleItemsAutoCompleteListView.getSelectionModel().isEmpty()) {
            return;
        }
        if (event.getClickCount() == 2) {
            Product saleItem = saleItemsAutoCompleteListView.getSelectionModel().getSelectedItem();
            addSaleItemToTable(saleItem);
            searchByIdTextField.clear();
            saleItemsAutoCompleteListView.setVisible(false);

        }


    }

    /**
     * Uklanja artikl iz TableView-a. Poziva se klikom na gumb "Ukloni". Zahtjeva autorizaciju.
     */
    public void removeSaleItemFromCart() {
        if (LoginController.authorizeCashier()) {
            SaleItem selectedProduct = cartItemsTableView.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                cartItemsTableView.getItems().remove(selectedProduct);
            } else {
                return;
            }
            refreshTotalLabels();
            cartItemsTableView.getSelectionModel().clearSelection();
        }

    }

    /**
     * Dodaje proizvod u TableView na temelju njegovog barkoda unesenog u tekstualno polje. Poziva se klikom
     * na tipku Enter u tekstualnom polju.
     */
    public void addProduct() {
        String barCode = searchByIdTextField.getText();
        Optional<Product> productOptional = productDao.productFromBarcode(barCode);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            addSaleItemToTable(product);
        }
    }

    /**
     * Dodaje proizvod u {@code TableView} s artiklima.
     * Ako proizvod već postoji u tablici, povećava mu količinu.
     *
     * @param product proizvod koji se dodaje u {@code TableView}
     */
    private void addSaleItemToTable(Product product) {
        Optional<SaleItem> existingItem = saleItemsFromCart.stream()
                .filter(item -> item.getProduct().getBarcode().equals(product.getBarcode()))
                .findFirst();
        if (existingItem.isPresent()) {
            if (canProductBeAdded(existingItem.get().getProduct().getId(), existingItem.get().getQuantity() + 1)) {
                existingItem.get().setQuantity(existingItem.get().getQuantity() + 1);
            } else {
                showAlert("Nedovoljno zaliha za proizvod " + existingItem.get().getProduct().getName(), Alert.AlertType.ERROR);
            }

        } else {
            if (canProductBeAdded(product.getId(), 1)) {
                SaleItem newItem = new SaleItem.SaleItemBuilder()
                        .withProduct(product)
                        .withQuantity(1)
                        .withUnitPrice(product.getPrice())
                        .withTotalPrice(product.getPrice())
                        .withRefunded(false)
                        .build();
                saleItemsFromCart.add(newItem);
            } else {
                showAlert("Nedovoljno zaliha za proizvod " + product.getName(), Alert.AlertType.ERROR);
            }


        }

        if (cartItemsTableView.getItems() != saleItemsFromCart) {
            cartItemsTableView.setItems(saleItemsFromCart);
        }
        cartItemsTableView.refresh();
        searchByIdTextField.clear();
        refreshTotalLabels();
    }

    /**
     * Provjerava ima li dovoljno zaliha proizvoda u bazi podataka
     *
     * @param productId jedinstvena identifikacijska oznaka proizvoda
     * @param quantity  željena količina proizvoda
     * @return {@code true} ako je dovoljno zaliha proizvoda u bazi podataka, inače {@code false}
     */
    private boolean canProductBeAdded(Integer productId, Integer quantity) {
        return productDao.checkQuantity(productId, quantity);

    }

    /**
     * Osvježava labele za prikaz cijene, popusta i konačne cijene. Poziva se nakon dodavanja ili uklanjanja
     * artikla iz tablice.
     */
    private void refreshTotalLabels() {
        total.setText(calculateTotal() + " €");
        discountLabel.setText(totalDiscount() + " €");
        totalGrandLabel.setText(calculateTotal().subtract(totalDiscount()) + " €");
    }

    /**
     * Računa ukupnu cijenu svih artikala iz tablice.
     *
     * @return ukupnu cijenu svih artikala bez popusta
     */

    public BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (SaleItem saleItem : saleItemsFromCart) {
            BigDecimal itemTotal = saleItem.getUnitPrice().multiply(BigDecimal.valueOf(saleItem.getQuantity()));
            total = total.add(itemTotal);
        }
        return total;
    }

    /**
     * Računa ukupan popust svih artikala iz tablice.
     *
     * @return ukupan popust svih artikala
     */


    public BigDecimal totalDiscount() {
        BigDecimal totalDiscount = BigDecimal.ZERO;
        for (SaleItem saleItem : saleItemsFromCart) {
            if (saleItem.getQuantity() > 1) {
                totalDiscount = totalDiscount.add(saleItem.getProduct().getDiscount().multiply(BigDecimal.valueOf(saleItem.getQuantity())));
            } else {
                totalDiscount = totalDiscount.add(saleItem.getProduct().getDiscount());
            }

        }
        return totalDiscount;
    }

    /**
     * Prikazuje popis proizvoda za automatsko dovršavanje unosa u polju za barkod.
     * Ako je unos prazan, lista se skriva. Ne reagira na strelice ili tipku Enter.
     * Poziva se tipkanjem barkoda u tekstualno polje.
     *
     * @param event događaj tipkovnice koji pokreće pretragu
     */
    public void itemsAutoComplete(KeyEvent event) {
        if (event.getCode().isArrowKey() || event.getCode() == KeyCode.ENTER)
            return;


        String input = searchByIdTextField.getText().trim().toLowerCase();
        if (input.isEmpty()) {
            saleItemsAutoCompleteListView.setVisible(false);
            return;
        }
        List<Product> productsString = productDao.productNamesFromBarcode(input);
        if (!productsString.isEmpty()) {
            saleItemsAutoCompleteListView.setVisible(true);
            saleItemsAutoCompleteListView.setItems(FXCollections.
                    observableArrayList(productsString));
        }

    }


    /**
     * Omogućuje navigaciju kroz ListView s prijedlozima proizvoda pomoću strelica gore i dolje.
     * Dodaje odabrani proizvod pritiskom na Enter.
     *
     * @param event događaj pritiska tipke koji određuje navigaciju ili potvrdu unosa
     */
    public void selectItemFromListView(KeyEvent event) {
        if (!saleItemsAutoCompleteListView.isVisible() || saleItemsAutoCompleteListView.getItems().isEmpty()) {
            return;
        }
        if (event.getCode() == KeyCode.DOWN) {
            int currentIndex = saleItemsAutoCompleteListView.getSelectionModel().getSelectedIndex();
            int nextIndex = currentIndex + 1;
            if (currentIndex == -1 && !saleItemsAutoCompleteListView.getItems().isEmpty()) {
                saleItemsAutoCompleteListView.getSelectionModel().selectFirst();
            } else if (nextIndex < saleItemsAutoCompleteListView.getItems().size()) {
                saleItemsAutoCompleteListView.getSelectionModel().select(nextIndex);
                saleItemsAutoCompleteListView.scrollTo(nextIndex);
            }
            if (nextIndex < saleItemsAutoCompleteListView.getItems().size()) {
                saleItemsAutoCompleteListView.getSelectionModel().select(nextIndex);
                saleItemsAutoCompleteListView.scrollTo(nextIndex);
            }
        } else if (event.getCode() == KeyCode.UP) {
            int prevIndex = saleItemsAutoCompleteListView.getSelectionModel().getSelectedIndex() - 1;
            if (prevIndex >= 0) {
                saleItemsAutoCompleteListView.getSelectionModel().select(prevIndex);
                saleItemsAutoCompleteListView.scrollTo(prevIndex);
            }
        } else if (event.getCode() == KeyCode.ENTER) {
            Product selected = saleItemsAutoCompleteListView.getSelectionModel().getSelectedItem();
            if (selected != null) {

                String barcode = selected.getBarcode();

                searchByIdTextField.setText(barcode);
                addProduct();
                saleItemsAutoCompleteListView.setVisible(false);
            }
        }


    }

    /**
     * Finalizira prodaju provjerom unesenog iznosa i izračunom povrata.
     * Ako je iznos ispravan i dovoljan, račun se sprema u bazu, a korisniku se prikazuje povrat.
     * U suprotnom se prikazuju odgovarajuće poruke o pogrešci.
     */
    public void completeSale() {
        String input = receivedAmount.getText().trim();

        if (input.isEmpty()) {
            showAlert("Nije unesen primljeni iznos!", Alert.AlertType.ERROR);
            return;
        }

        BigDecimal receivedMoney;

        try {
            receivedMoney = new BigDecimal(input);
        } catch (NumberFormatException e) {
            showAlert("Pogrešan unos primljenog iznosa!", Alert.AlertType.ERROR);
            return;
        }

        BigDecimal total = calculateTotal().subtract(totalDiscount());

        if (receivedMoney.compareTo(total) < 0) {
            showAlert("Nedovoljan iznos.", Alert.AlertType.ERROR);
            return;
        }

        BigDecimal change = receivedMoney.subtract(total);
        showAlert("Za vratiti: " + change + " €", Alert.AlertType.INFORMATION);

        saleDao.createSale(saleItemsFromCart, total, null);
        if (!PosReceiptPrinter.isPrinterConnected("POS-58(copy of 1)")) {
            showAlert("Printer nije uključen. račun će se ispisati kada se printer uključi", Alert.AlertType.ERROR);
        }
        this.isSaleCommitted = true;
        cancel();
    }

    /**
     * Poništava trenutnu prodaju i vraća korisničko sučelje u početno stanje.
     * Ako prodaja još nije dovršena (račun nije spremljen), za poništavanje je potrebna administratorska autorizacija.
     * Koristi se u slučajevima kada kupac odustane od kupnje prije plaćanja.
     * Nakon dovršene kupnje (commit), metoda samo čisti polja bez potrebe za autorizacijom.
     */
    public void cancel() {
        if (!this.isSaleCommitted) {
            if (!LoginController.authorizeAdmin()) {
                return;
            }
        }

        saleItemsFromCart.clear();
        cartItemsTableView.refresh();
        searchByIdTextField.clear();
        saleItemsAutoCompleteListView.setVisible(false);
        total.setText("0,00 €");
        discountLabel.setText("0,00 €");
        totalGrandLabel.setText("0,00 €");
        receivedAmount.clear();
    }

    /**
     * Prikazuje dijaloški prozor za različite situacije, kod prikaza pogrešaka o primljenom iznosu, kod
     *
     * @param message   poruka koja se prikazuje u dijaloškom prozoru
     * @param alertType tip dijaloškog prozora
     */

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(message);
        alert.setHeaderText(message);
        try {
            Image icon = new Image(new FileInputStream("src/main/resources/images/icon.png"));
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/icon.png"))));

            stage.getIcons().add(icon);

        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());
        }
        alert.showAndWait();
    }


}
