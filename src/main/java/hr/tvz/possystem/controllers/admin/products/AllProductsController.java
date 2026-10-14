package hr.tvz.possystem.controllers.admin.products;

import hr.tvz.possystem.database.ProductDao;
import hr.tvz.possystem.database.ProductDaoImpl;
import hr.tvz.possystem.model.Category;
import hr.tvz.possystem.model.Product;
import hr.tvz.possystem.model.User;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

/**
 * Upravlja administratorskim sučeljem za prikaz i upravljanje svim proizvodima iz baze podataka. Sve proizvode iz baze
 * podataka prikazuju se u TableView-u.
 */
public class AllProductsController {

    @FXML
    private TableView<Product> productTableView;
    @FXML
    private TableColumn<Product, Integer> idTableColumn;

    @FXML
    private TableColumn<Product, String> nameTableColumn;
    @FXML
    private TableColumn<Product, String> barcodeTableColumn;
    @FXML
    private TableColumn<Product, Category> categoryTableColumn;
    @FXML
    private TableColumn<Product, BigDecimal> taxRateTableColumn;
    @FXML
    private TableColumn<Product, BigDecimal> priceTableColumn;
    @FXML
    private TableColumn<Product, Integer> stockQuantityTableColumn;
    @FXML
    private TableColumn<Product, BigDecimal> discountTableColumn;
    @FXML
    private TableColumn<Product, Boolean> isActiveTableColumn;

    @FXML
    private TableColumn<Product, String> createdAtTableColumn;
    @FXML
    private TableColumn<Product, String> updatedAtTableColumn;
    @FXML
    private TextField searchTextField;
    private ProductDao productDao = new ProductDaoImpl();
    private List<Product> allProducts = productDao.getAllProductsFromDatabase();
    public static final DateTimeFormatter DT_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    private ContextMenu contextMenu;

    /**
     * Inicijalizira prikaz proizvoda u TableView-u nakon učitavanja FXML-a.
     * Postavlja podatke, definira cellValueFactory za sve kolone, prilagođava širine kolona
     * prema širini tablice i dodaje kontekstni izbornik na retke.
     * Postavlja otvaranje ContextMenu-a klikom na desni klik u retku TableView-a.
     */
    public void initialize() {

        productTableView.setItems(FXCollections.observableArrayList(allProducts));

        idTableColumn.setCellValueFactory(product -> new ReadOnlyObjectWrapper<>(product.getValue().getId()));
        nameTableColumn.setCellValueFactory(product -> new ReadOnlyStringWrapper(product.getValue().getName()));
        barcodeTableColumn.setCellValueFactory(product -> new ReadOnlyStringWrapper(product.getValue().getBarcode()));
        categoryTableColumn.setCellValueFactory(product -> new ReadOnlyObjectWrapper<>(product.getValue().getCategory()));
        taxRateTableColumn.setCellValueFactory(product -> new ReadOnlyObjectWrapper<>(product.getValue().getTaxRate().getRate()));
        priceTableColumn.setCellValueFactory(product -> new ReadOnlyObjectWrapper<>(product.getValue().getPrice()));
        stockQuantityTableColumn.setCellValueFactory(product -> new ReadOnlyObjectWrapper<>(product.getValue().getStockQuantity()));
        discountTableColumn.setCellValueFactory(product -> new ReadOnlyObjectWrapper<>(product.getValue().getDiscount()));
        isActiveTableColumn.setCellValueFactory(product -> new ReadOnlyObjectWrapper<>(product.getValue().isActive()));
        createdAtTableColumn.setCellValueFactory(product -> new ReadOnlyObjectWrapper<>(product.getValue().getCreatedAt().toLocalDateTime().format(DT_FORMAT)));
        updatedAtTableColumn.setCellValueFactory(product -> new ReadOnlyObjectWrapper<>(product.getValue().getUpdatedAt().toLocalDateTime().format(DT_FORMAT)));
        productTableView.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double width = newWidth.doubleValue();
            idTableColumn.setPrefWidth(width * 0.04);
            nameTableColumn.setPrefWidth(width * 0.15);
            barcodeTableColumn.setPrefWidth(width * 0.11);
            categoryTableColumn.setPrefWidth(width * 0.09);
            taxRateTableColumn.setPrefWidth(width * 0.06);
            priceTableColumn.setPrefWidth(width * 0.08);
            stockQuantityTableColumn.setPrefWidth(width * 0.08);
            discountTableColumn.setPrefWidth(width * 0.06);
            isActiveTableColumn.setPrefWidth(width * 0.06);
            createdAtTableColumn.setPrefWidth(width * 0.11);
            updatedAtTableColumn.setPrefWidth(width * 0.10);
        });

        productTableView.setRowFactory(categoryTableView1 -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(mouseEvent -> {
                if (!row.isEmpty() && mouseEvent.getButton() == MouseButton.SECONDARY) {
                    contextMenu.show(productTableView, mouseEvent.getSceneX(), mouseEvent.getSceneY());
                }
            });
            return row;
        });
    }

    /**
     * Označava odabrani proizvod kao neaktivan nakon potvrde korisnika.
     * Ako korisnik potvrdi deaktivaciju u dijaloškom prozoru, proizvod se ažurira u bazi podataka
     * te se tablica proizvoda osvježi.
     * Poziva se klikom na "Ukloni" iz ContextMenu-a.
     */
    public void setInactive() {
        Product selectedProduct = productTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            boolean confirmation = confirmDeleting(selectedProduct);
            if (confirmation) {
                productDao.setProductAsInactive(selectedProduct.getId());
                refreshProducts();
            }

        }
    }

    /**
     * Prikazuje dijaloški prozor u kojem korisnik potvrđuje deaktivaciju proizvoda.
     *
     * @param product proizvod koji se treba deaktivirati
     * @return {@code true} ako korisnik potvrdi, {@code false} ako odustane od deaktivacije
     */

    private boolean confirmDeleting(Product product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Želite li ukloniti proizvod iz prodaje: " + product.getName());
        alert.setHeaderText("Potvrdite uklanjanje proizvoda ");
        return yesOrCancelButton(alert);
    }

    /**
     * Postavlja gumbove u dijaloški prozor te vraća rezultat akcije korisnika.
     *
     * @param alert dijaloški prozor kojemu se dodavaju gumbovi
     * @return {@code true} ako je kliknut gumb "Da", {@code false} ako je kliknut gumb "Otkaži"
     */

    public static boolean yesOrCancelButton(Alert alert) {
        ButtonType saveButton = new ButtonType("Da", ButtonBar.ButtonData.YES);
        ButtonType cancelButton = new ButtonType("Otkaži", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(saveButton, cancelButton);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            ButtonType buttonType = result.get();
            return buttonType == saveButton;
        } else {
            return false;
        }
    }

    /**
     * Otvara modalni dijaloški prozor za uređivanje odabranog proizvoda.
     * Ako je proizvod odabran, učitava odgovarajući FXML, postavlja podatke
     * u polja za unos i prikazuje prozor. Nakon zatvaranja prozora, lista proizvoda
     * se osvježava.
     * Poziva se klikom na "Uredi" iz ContextMenu-a.
     */

    public void edit() {
        Product selectedProduct = productTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hr/tvz/possystem/admin/product/edit-pop-up-view.fxml"));
            Parent root = loader.load();

            EditPopUPController controller = loader.getController();
            controller.setEditingProduct(selectedProduct);
            controller.fillTheTextFields();
            Stage stage = new Stage();
            stage.setTitle("Uredi proizvod");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            try {
                Image icon = new Image(new FileInputStream("src/main/resources/images/icon.png"));
                stage.getIcons().add(icon);
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage());
            }
            stage.showAndWait();
            refreshProducts();
            productTableView.setItems(FXCollections.observableArrayList(this.allProducts));

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Osvježava listu proizvoda iz baze podataka u TableView-u.
     * Poziva se nakon uređivanja, deaktiviranja ili dodavanja količine proizvoda u skladištu.
     */
    private void refreshProducts() {
        this.allProducts = productDao.getAllProductsFromDatabase();
    }

    /**
     * Filtrira listu proizvoda u tablici na temelju naziva ili barkoda proizvoda unesenih u
     * tekstualno polje za pretraživanje.
     */
    public void searchByNameOrId() {
        String search = searchTextField.getText().toLowerCase();
        List<Product> filtered = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(search) ||
                        p.getBarcode().toLowerCase().contains(search))
                .collect(Collectors.toList());
        productTableView.setItems(FXCollections.observableArrayList(filtered));

    }

    /**
     * Otvara modalni dijaloški prozor za dodavanje zalihe proizvoda.
     * Ako je proizvod odabran, učitava odgovarajući FXML i prikazuje prozor.
     * Nakon zatvaranja prozora, lista proizvoda se osvježava.
     * Poziva se klikom na "Dodaj zalihe" iz ContextMenu-a.
     */

    public void addStockQuantity() {
        Product selectedProduct = productTableView.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hr/tvz/possystem/admin/product/add-quantity-dialog-view.fxml"));
            Parent root = loader.load();

            AddQuantityController controller = loader.getController();
            controller.setSelectedProduct(selectedProduct);
            Stage stage = new Stage();
            stage.setTitle("Unos zalihe");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            try {
                Image icon = new Image(new FileInputStream("src/main/resources/images/icon.png"));
                stage.getIcons().add(icon);
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage());
            }
            stage.showAndWait();
            refreshProducts();
            productTableView.setItems(FXCollections.observableArrayList(this.allProducts));

        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }
}
