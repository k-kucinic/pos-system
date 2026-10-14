package hr.tvz.possystem.controllers;

import hr.tvz.possystem.PosSystemApplication;
import hr.tvz.possystem.database.RefundsDao;
import hr.tvz.possystem.database.RefundsDaoImpl;
import hr.tvz.possystem.database.SaleDao;
import hr.tvz.possystem.database.SaleDaoImpl;
import hr.tvz.possystem.model.Refund;
import hr.tvz.possystem.model.Sale;
import hr.tvz.possystem.model.SaleItem;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

/**
 * Upravlja korisničkim sučeljem za povrat artikala s računa unosom identifikacijskog broja računa.
 * Prikazuje sve artikle sa računa dostupne za povrat i omogućuje korisniku djelomičan ili puni povrat.
 * <p>
 * Sučelje sadrži dvije tablice: prva prikazuje originalne artikle s računa, a druga prikazuje artikle odabrane za povrat.
 */

public class RefundsController {


    @FXML
    private TableView<SaleItem> cartItemsTableView;
    @FXML
    private TableColumn<SaleItem, String> productNameColumn;
    @FXML
    private TableColumn<SaleItem, Integer> productQuantityColumn;
    @FXML
    private TableColumn<SaleItem, String> priceTotalColumn;
    @FXML
    private TableView<SaleItem> cartItemsTableViewRefund;
    @FXML
    private TableColumn<SaleItem, String> productNameColumnRefund;
    @FXML
    private TableColumn<SaleItem, Integer> productQuantityColumnRefund;
    @FXML
    private TableColumn<SaleItem, String> priceTotalColumnRefund;
    private Optional<Sale> sale;
    private boolean dialogOpen = false;

    @FXML
    private Label dateInfoLabel;
    @FXML
    private Label timeInfoLabel;
    @FXML
    private Label carshierInfoLabel;
    @FXML
    private TextArea reasonTextArea;

    @FXML
    private TextField searchASaleByIDTextField;
    private final SaleDao saleDao = new SaleDaoImpl();
    private final RefundsDao refundsDao = new RefundsDaoImpl();

    /**
     * Postavlja podatke, definira cellValueFactory za sve kolone i prilagođava širine kolona prema širini tablice.
     */
    public void initialize() {
        productNameColumn.setCellValueFactory(product -> new ReadOnlyStringWrapper(product.getValue().getProduct().getName()));
        productQuantityColumn.setCellValueFactory(product -> new ReadOnlyObjectWrapper<>(product.getValue().getQuantity()));
        priceTotalColumn.setCellValueFactory(product -> new ReadOnlyObjectWrapper<>(product.getValue().getUnitPrice().multiply(BigDecimal.valueOf(product.getValue().getQuantity())) + " €"));
        productNameColumnRefund.setCellValueFactory(product -> new ReadOnlyStringWrapper(product.getValue().getProduct().getName()));
        productQuantityColumnRefund.setCellValueFactory(product -> new ReadOnlyObjectWrapper<>(product.getValue().getOriginalQuantity()));
        priceTotalColumnRefund.setCellValueFactory(product -> new ReadOnlyStringWrapper(product.getValue().getUnitPrice().multiply(BigDecimal.valueOf(product.getValue().getQuantity())) + " €"));
        cartItemsTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        cartItemsTableViewRefund.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        cartItemsTableView.setPlaceholder(new Label(""));
        cartItemsTableViewRefund.setPlaceholder(new Label(""));
        cartItemsTableView.setPrefWidth(250);
        cartItemsTableViewRefund.setPrefWidth(250);
        cartItemsTableView.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double width = newWidth.doubleValue();
            productNameColumn.setPrefWidth(width * 0.685);
            productQuantityColumn.setPrefWidth(width * 0.15);
            priceTotalColumn.setPrefWidth(width * 0.15);
        });

        cartItemsTableViewRefund.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double width = newWidth.doubleValue();
            productNameColumnRefund.setPrefWidth(width * 0.685);
            productQuantityColumnRefund.setPrefWidth(width * 0.15);
            priceTotalColumnRefund.setPrefWidth(width * 0.15);
        });
        reasonTextArea.setVisible(false);


    }

    /**
     * Pretražuje bazu podataka prema identifikacijskom broju računa unesenom u TextField. Ako račun postoji,
     * njegovi artikli se prikazuju u prvoj tablici sučelja. Metoda se poziva pritiskom na tipku Enter nakon
     * unosa ID-a računa.
     */

    public void searchForSale() {
        String saleIdString = searchASaleByIDTextField.getText();
        int saleId;
        if (!saleIdString.isBlank()) {
            try {
                saleId = Integer.parseInt(saleIdString);
            } catch (NumberFormatException e) {

                return;
            }
            Optional<Sale> saleOptional = saleDao.getSaleById(saleId);
            if (saleOptional.isPresent()) {
                if(!saleOptional.get().isRefunded()){
                    saleOptional.get().setSaleItems(saleDao.getSaleItemsBySaleId(saleOptional.get()));
                    List<SaleItem> nonRefundedItems = new ArrayList<>();

                    nonRefundedItems = saleOptional.get().getSaleItems().stream().filter(saleItem -> !saleItem.isRefunded()).collect(Collectors.toList());

                    cartItemsTableView.setItems(FXCollections.observableArrayList(nonRefundedItems));
                    cartItemsTableViewRefund.setItems(FXCollections.observableArrayList(new ArrayList<>()));

                    this.sale = saleOptional;
                    dateInfoLabel.setText("datum prodaje: " + saleOptional.get().getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")));
                    timeInfoLabel.setText("vrijeme: " + saleOptional.get().getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")));
                    if (Optional.ofNullable(saleOptional.get().getUser()).isPresent()) {
                        carshierInfoLabel.setText("blagajnik: " + saleOptional.get().getUser().getUsername());
                    }
                    showOrHideLabels(true);
                }
                else{
                    alreadyRefundedMessage(saleIdString);
                    searchASaleByIDTextField.clear();
                }



            } else {
                message(String.valueOf(saleId));
                searchASaleByIDTextField.clear();

            }

        }
    }

    /**
     * Prikazuje ili skriva informacije o računu, vremenu, blagajniku i razlogu povrata.
     *
     * @param trueOrFalse ako je {@code true}, elementi se prikazuju; ako je {@code false}, skrivaju se
     */

    public void showOrHideLabels(boolean trueOrFalse) {

        dateInfoLabel.setVisible(trueOrFalse);
        timeInfoLabel.setVisible(trueOrFalse);
        carshierInfoLabel.setVisible(trueOrFalse);
        reasonTextArea.setVisible(trueOrFalse);


    }

    /**
     * Premješta artikl iz tablice s proizvodima koji ostaju kupcu u tablicu artikala koji se vraćaju. Ako je količina
     * artikla veća od jedan, otvara se dijaloški prozor za odabir potpune ili djelomične količine za povrat.
     *
     * @see RefundQuantityController
     */

    public void moveToRefund() {
        if (dialogOpen) return;

        SaleItem selectedItem = cartItemsTableView.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            if (selectedItem.getQuantity() == 1) {
                selectedItem.setOriginalQuantity(1);
                selectedItem.setIdBeforeRefund(selectedItem.getId());
                cartItemsTableView.getItems().remove(selectedItem);
                cartItemsTableViewRefund.getItems().add(selectedItem);
            } else {
                dialogOpen = true;
                showRefundQuantityDialog(selectedItem).ifPresent(refundQuantity -> {
                    if (selectedItem.getQuantity() - refundQuantity == 0) {
                        selectedItem.setOriginalQuantity(selectedItem.getQuantity());
                        selectedItem.setIdBeforeRefund(selectedItem.getId());
                        cartItemsTableView.getItems().remove(selectedItem);
                        cartItemsTableViewRefund.getItems().add(selectedItem);
                    } else {
                        selectedItem.setOriginalQuantity(selectedItem.getQuantity());
                        selectedItem.setIdBeforeRefund(selectedItem.getId());
                        SaleItem refundItem = selectedItem.cloneWithQuantity(refundQuantity);
                        selectedItem.setQuantity(selectedItem.getQuantity() - refundQuantity);

                        refundItem.setRefunded(true);
                        cartItemsTableViewRefund.getItems().add(refundItem);

                    }

                    cartItemsTableView.refresh();
                    cartItemsTableViewRefund.refresh();
                });
                dialogOpen = false;
            }
        }
        cartItemsTableView.getSelectionModel().clearSelection();
        cartItemsTableViewRefund.getSelectionModel().clearSelection();
        cartItemsTableView.refresh();
        cartItemsTableViewRefund.refresh();
    }

    /**
     * Otvara dijaloški prozor u kojem korisnik odabire količinu artikla za djelomični povrat.
     *
     * @param selectedItem artikl za koji se unosi količina povrata
     * @return količina artikla koju korisnik želi vratiti, ako nije odabrana vraća se prazna vrijednost
     */

    private Optional<Integer> showRefundQuantityDialog(SaleItem selectedItem) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hr/tvz/possystem/cashier/refund-quantity.fxml"));
            Parent root = loader.load();

            RefundQuantityController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Povrat količine");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            controller.setDialogStage(dialogStage);
            controller.setMaxQuantity(selectedItem.getQuantity());
            try {
                Image icon = new Image(new FileInputStream("src/main/resources/images/icon.png"));
                dialogStage.getIcons().add(icon);
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage());
            }

            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            int result = controller.getSelectedQuantity();
            return result > 0 ? Optional.of(result) : Optional.empty();

        } catch (IOException e) {
            logger.error("Greška kod otvaranja dijaloga: " + e.getMessage());
            return Optional.empty();
        }
    }


    /**
     * Sprema informacije o povratu u bazu podataka ukoliko se ispravno unese administratorska lozinka.
     */
    public void makeARefund() {
        if (cartItemsTableViewRefund.getItems().isEmpty()) {
            return;

        }
        if (!PosSystemApplication.getMultiRoles()) {
            if (!LoginController.authorizeAdmin()) {
                return;
            }
        }

        if (this.sale.isPresent()) {
            List<SaleItem> saleItemsForRefund = cartItemsTableViewRefund.getItems();
            List<SaleItem> itemsToKeep = cartItemsTableView.getItems();
            Refund refund = new Refund();
            refund.setCreatedAt(Timestamp.from(Instant.now()));
            refund.setSale(this.sale.get());
            if (!reasonTextArea.getText().isBlank()) {
                refund.setReason(reasonTextArea.getText());
            } else {
                refund.setReason("Reason");
            }

            refund.setRefundAmount(totalRefundAmount(saleItemsForRefund));
            refundsDao.insertNewRefundIntoDatabase(refund, itemsToKeep, saleItemsForRefund);

            searchASaleByIDTextField.clear();
            cartItemsTableViewRefund.getItems().clear();
            cartItemsTableView.getItems().clear();
            showOrHideLabels(false);

        }

    }

    /**
     * Računa ukupan iznos povrata na temelju cijena i količina artikala.
     *
     * @param saleItems lista artikala koji se vraćaju
     * @return ukupan iznos povrata
     */

    private BigDecimal totalRefundAmount(List<SaleItem> saleItems) {
        BigDecimal total = BigDecimal.ZERO;
        for (SaleItem saleItem : saleItems) {
            total = total.add(saleItem.getUnitPrice().multiply(BigDecimal.valueOf(saleItem.getOriginalQuantity())));

        }
        return total;
    }

    /**
     * Prikazuje dijaloški prozor s upozorenjem da račun s unesenim ID-om ne postoji.
     *
     * @param message ID računa koji nije pronađen
     */

    public void message(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Ne postoji račun sa ID-om: " + message);
        alert.setHeaderText("Ne postoji račun sa ID-om: " + message);
        alert.showAndWait();
    }
    /**
     * Prikazuje dijaloški prozor s upozorenjem da račun s unesenim ID-om ne postoji.
     *
     * @param message ID računa koji nije pronađen
     */

    public void alreadyRefundedMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Narudžba sa ID-om " + message+ " već je vraćena.");
        alert.setHeaderText("Narudžba sa ID-om " + message+ " već je vraćena");
        alert.showAndWait();
    }
}
