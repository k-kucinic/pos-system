package hr.tvz.possystem.controllers.admin.sales;

import hr.tvz.possystem.controllers.admin.refunds.RefundHistoryController;
import hr.tvz.possystem.database.SaleDao;
import hr.tvz.possystem.database.SaleDaoImpl;
import hr.tvz.possystem.model.Sale;
import hr.tvz.possystem.model.SaleItem;
import hr.tvz.possystem.printer.PosReceiptPrinter;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

/**
 * Upravlja sučeljem za prikaz svih obavljenih prodaja u aplikaciji. Omogućuje pregled svih proizvoda po kupnji.
 * Omogućuje ispis kopije računa.
 */
public class AllSalesController {

    @FXML
    private DatePicker dateTimeTo;
    @FXML
    private DatePicker dateTimeFrom;
    @FXML
    private TableView<Sale> salesTableView;
    @FXML
    private TableColumn<Sale, Integer> idTableColumn;
    @FXML
    private TableColumn<Sale, String> dateTableColumn;
    @FXML
    private TableColumn<Sale, String> timeTableColumn;
    @FXML
    private TableColumn<Sale, String> userTableColumn;
    @FXML
    private TableColumn<Sale, String> notesTableColumn;
    @FXML
    private TableColumn<Sale, String> refundedTableColumn;
    @FXML
    private TableColumn<Sale, String> totalAmountTableColumn;
    private final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy.");
    private final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");
    @FXML
    private TableView<SaleItem> saleItemsTableView;
    @FXML
    private TableColumn<SaleItem, Integer> saleItemIdTableColumn;
    @FXML
    private TableColumn<SaleItem, String> barcodeSaleItemTableView;
    @FXML
    private TableColumn<SaleItem, String> saleItemProductColumn;
    @FXML
    private TableColumn<SaleItem, Integer> quantityTableColumn;
    @FXML
    private TableColumn<SaleItem, String> unitPriceTableColumn;
    @FXML
    private TableColumn<SaleItem, String> totalPriceSaleItemColumn;
    @FXML
    private TableColumn<SaleItem, String> isSaleItemRefundedTableColumn;
    @FXML
    private TableColumn<SaleItem, String> taxRateTableColumn;
    @FXML
    private Button close;
    @FXML
    private Button showAllSalesButton;
    @FXML
    private Button printTheReceiptButton;

    private final SaleDao saleDao = new SaleDaoImpl();

    /**
     * Omogućava filtriranje prodaja prema datumu.
     * Koristi vrijednosti iz DatePicker-a za slanje upita bazi podataka.
     */
    public void search() {

        if (dateTimeFrom.getValue() != null && dateTimeTo.getValue() != null) {
            showAllSalesButton.setVisible(true);
            LocalDate dateFrom = dateTimeFrom.getValue();
            LocalDate dateTo = dateTimeTo.getValue();
            if (dateTo.isAfter(dateFrom)) {

                List<Sale> salesByDate = saleDao.getSalesByDate(dateFrom, dateTo);
                if (salesByDate.isEmpty()) {
                    salesTableView.setPlaceholder(new Label("Nema rezultata za prikaz"));
                }
                salesTableView.setItems(FXCollections.observableArrayList(saleDao.getSalesByDate(dateFrom, dateTo)));

            }

        }
    }

    /**
     * Inicijalizira prikaz prodaja u TableView nakon učitavanja FXML-a.
     * Postavlja podatke, definira cellValueFactory za sve kolone.
     */
    public void initialize() {
        salesTableView.setItems(FXCollections.observableArrayList(saleDao.getAllSalesFromDatabase()));
        idTableColumn.setCellValueFactory(sale -> new ReadOnlyObjectWrapper<>(sale.getValue().getId()));
        dateTableColumn.setCellValueFactory(sale -> new ReadOnlyStringWrapper(sale.getValue().getCreatedAt().format(DATE_FORMATTER)));
        timeTableColumn.setCellValueFactory(sale -> new ReadOnlyStringWrapper(sale.getValue().getCreatedAt().format(TIME_FORMATTER)));
        userTableColumn.setCellValueFactory(sale -> new ReadOnlyObjectWrapper<>(sale.getValue().getUser().getUsername()));
        notesTableColumn.setCellValueFactory(saleStringCellDataFeatures -> new ReadOnlyStringWrapper(saleStringCellDataFeatures.getValue().getNotes()));
        refundedTableColumn.setCellValueFactory(sale -> new ReadOnlyStringWrapper(isRefunded(sale.getValue().isRefunded())));
        totalAmountTableColumn.setCellValueFactory(sale -> new ReadOnlyStringWrapper(sale.getValue().getTotalAmount() + " €"));
        salesTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        RefundHistoryController.setCellValueFactoryForSaleItems(saleItemIdTableColumn, barcodeSaleItemTableView, saleItemProductColumn, quantityTableColumn, unitPriceTableColumn, totalPriceSaleItemColumn, isSaleItemRefundedTableColumn, taxRateTableColumn);
        salesTableView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldSelection, newSelection) -> {
            if (oldSelection != null) {
                saleItemsTableView.setVisible(false);
                saleItemsTableView.getItems().clear();
            }
        });


    }

    /**
     * Prikazuje artikle za odabranu prodaju iz tablice. Poziva se klikom na gumb "Prikaz artikala".
     */

    public void showSaleItems() {
        if (salesTableView.getSelectionModel().isEmpty()) return;
        close.setVisible(true);
        printTheReceiptButton.setVisible(true);
        List<SaleItem> saleItems = saleDao.getSaleItemsBySaleId(salesTableView.getSelectionModel().getSelectedItem());
        saleItemsTableView.setItems(FXCollections.observableArrayList(saleItems));
        saleItemsTableView.setVisible(true);


    }

    /**
     * Sakriva tablicu sa artiklima. Poziva se klikom na gumb "Zatvori".
     */
    public void hide() {
        saleItemsTableView.setVisible(false);
        close.setVisible(false);
        printTheReceiptButton.setVisible(false);
    }

    /**
     * Služi za stiliziranje informacije o tome je li artikl vraćen.
     *
     * @param isRefunded isRefunded {@code true} ako je artikl vraćen, inače {@code false}
     * @return "Da" ako je artikl vraćen, inače "Ne"
     */
    public static String isRefunded(boolean isRefunded) {
        if (isRefunded) {
            return "Da";
        } else {
            return "Ne";
        }
    }

    /**
     * Služi za poništavanje filtriranja prodaja po datumu te u TableView postavlja sve prodaje iz baze podataka.
     */
    public void showAllSales() {
        salesTableView.setItems(FXCollections.observableArrayList(saleDao.getAllSalesFromDatabase()));
        showAllSalesButton.setVisible(false);
    }

    /**
     * Služi za printanje kopije računa. Račun se printa sa dodatnom bilješkom "Kopija računa". Poziva se klikom na gumb
     * "Ispis računa".
     */
    public void printTheReceipt() {
        if (salesTableView.getSelectionModel().isEmpty()) return;
        Sale sale = salesTableView.getSelectionModel().getSelectedItem();
        sale.setSaleItems(saleItemsTableView.getItems());
        sale.setNotes("Kopija računa " + sale.getId() + "\n");
        try {
            PosReceiptPrinter.printTheReceipt(sale);
        } catch (Exception e) {
            logger.error("Greška printera: " + e.getMessage());
        }


    }
}
