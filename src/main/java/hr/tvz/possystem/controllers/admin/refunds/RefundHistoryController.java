package hr.tvz.possystem.controllers.admin.refunds;

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
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static hr.tvz.possystem.controllers.admin.sales.AllSalesController.isRefunded;

/**
 * Upravlja sučeljem za prikaz povijesti izvršenih povrata i vraćenih artikala. Omogućuje
 * filtraciju po datumu. Podatke o povratima i vraćenim artiklima prikazuje u dva TableView-a.
 */
public class RefundHistoryController {

    @FXML
    private DatePicker dateFromValue;
    @FXML
    private DatePicker dateToValue;
    @FXML
    private TableView<Refund> refundsTableView;
    @FXML
    private TableColumn<Refund, String> dateColumn;
    @FXML
    private TableColumn<Refund, String> timeColumn;
    @FXML
    private TableColumn<Refund, String> cashierColumn;
    @FXML
    private TableColumn<Refund, String> totalRefundColumn;
    @FXML
    private Button showAllRefundsButton;
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


    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy.");
    public static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");
    private RefundsDao refundsDao = new RefundsDaoImpl();
    private SaleDao saleDao = new SaleDaoImpl();

    /**
     * Inicijalizira prikaz povrata u TableView nakon učitavanja FXML-a.
     * Postavlja podatke, definira cellValueFactory za sve kolone.
     */

    public void initialize() {
        dateColumn.setCellValueFactory(refund -> new ReadOnlyStringWrapper(refund.getValue().getCreatedAt().toLocalDateTime().format(DATE_FORMATTER)));
        timeColumn.setCellValueFactory(refund -> new ReadOnlyStringWrapper(refund.getValue().getCreatedAt().toLocalDateTime().format(TIME_FORMATTER)));
        cashierColumn.setCellValueFactory(refund -> new ReadOnlyStringWrapper(refund.getValue().getProcessedBy().getUsername()));
        totalRefundColumn.setCellValueFactory(refund -> new ReadOnlyStringWrapper(refund.getValue().getRefundAmount().toString() + " €"));
        refundsTableView.setItems(FXCollections.observableArrayList(refundsDao.getAllRefundsFromDatabase()));
        refundsTableView.getColumns().forEach(column -> column.setSortable(false));
        refundsTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        setCellValueFactoryForSaleItems(saleItemIdTableColumn, barcodeSaleItemTableView, saleItemProductColumn, quantityTableColumn, unitPriceTableColumn, totalPriceSaleItemColumn, isSaleItemRefundedTableColumn, taxRateTableColumn);

    }

    /**
     * Postavlja cellValueFactory za sve kolone TableView-a za artikle. Poziva se pri inicijalizaciji sučelja za povrate
     * i račune.
     *
     * @param saleItemIdTableColumn         kolona koja predstavlja jedinstveni identifikator artikla
     * @param barcodeSaleItemTableView      kolona koja predstavlja barkod artikla
     * @param saleItemProductColumn         kolona koja predstavlja naziv artikla
     * @param quantityTableColumn           kolona koja predstavlja količinu artikla
     * @param unitPriceTableColumn          kolona koja predstavlja jediničnu cijenu artikla
     * @param totalPriceSaleItemColumn      kolona koja predstavlja ukupnu cijenu artikla
     * @param isSaleItemRefundedTableColumn kolona koja označava je li artikl vraćen
     * @param taxRateTableColumn            kolona koja predstavlja poreznu stopu artikla
     */
    public static void setCellValueFactoryForSaleItems(TableColumn<SaleItem, Integer> saleItemIdTableColumn, TableColumn<SaleItem, String> barcodeSaleItemTableView, TableColumn<SaleItem, String> saleItemProductColumn, TableColumn<SaleItem, Integer> quantityTableColumn, TableColumn<SaleItem, String> unitPriceTableColumn, TableColumn<SaleItem, String> totalPriceSaleItemColumn, TableColumn<SaleItem, String> isSaleItemRefundedTableColumn, TableColumn<SaleItem, String> taxRateTableColumn) {
        saleItemIdTableColumn.setCellValueFactory(saleItem -> new ReadOnlyObjectWrapper<>(saleItem.getValue().getProduct().getId()));
        barcodeSaleItemTableView.setCellValueFactory(saleItem -> new ReadOnlyStringWrapper(saleItem.getValue().getProduct().getBarcode()));
        saleItemProductColumn.setCellValueFactory(saleItem -> new ReadOnlyStringWrapper(saleItem.getValue().getProduct().getName()));
        quantityTableColumn.setCellValueFactory(saleItem -> new ReadOnlyObjectWrapper<>(saleItem.getValue().getQuantity()));
        unitPriceTableColumn.setCellValueFactory(saleItem -> new ReadOnlyStringWrapper(saleItem.getValue().getUnitPrice() + " €"));
        totalPriceSaleItemColumn.setCellValueFactory(saleItem -> new ReadOnlyStringWrapper(saleItem.getValue().getUnitPrice().multiply(new BigDecimal(saleItem.getValue().getQuantity())) + " €"));
        isSaleItemRefundedTableColumn.setCellValueFactory(saleItem -> new ReadOnlyStringWrapper(isRefunded(saleItem.getValue().isRefunded())));
        taxRateTableColumn.setCellValueFactory(saleItem -> new ReadOnlyStringWrapper(saleItem.getValue().getProduct().getTaxRate().getRate().intValue() + "%"));
    }

    /**
     * Omogućava filtriranje povrata prema datumu.
     * Koristi vrijednosti iz DatePicker komponenti za slanje upita bazi podataka.
     */
    public void searchByDate() {
        if (dateFromValue.getValue() != null && dateToValue.getValue() != null) {
            showAllRefundsButton.setVisible(true);
            LocalDate dateFrom = dateFromValue.getValue();
            LocalDate dateTo = dateToValue.getValue();
            if (dateTo.isAfter(dateFrom)) {

                List<Refund> refundsByDate = refundsDao.getRefundsByDate(dateFrom, dateTo).stream()
                        .sorted(Comparator.comparing(Refund::getCreatedAt).reversed())
                        .collect(Collectors.toList());

                if (refundsByDate.isEmpty()) {
                    refundsTableView.setPlaceholder(new Label("Nema rezultata za prikaz"));
                }
                refundsTableView.setItems(FXCollections.observableArrayList(refundsByDate));

            }

        }
    }

    /**
     * Poništava filtraciju po datumu i prikazuje sve povrate. Poziva se klikom na gumb "Prikaži sve".
     */
    public void showAllRefunds() {
        refundsTableView.setItems(FXCollections.observableArrayList(refundsDao.getAllRefundsFromDatabase()));
        showAllRefundsButton.setVisible(false);
    }

    /**
     * Prikazuje vraćene artikle za odabrani povrat iz tablice. Poziva se klikom na gumb "Prikaz artikala".
     */

    public void showRefundItems() {
        if (refundsTableView.getSelectionModel().isEmpty()) {
            return;
        }
        close.setVisible(true);
        Optional<Sale> saleOptional = saleDao.getSaleById(refundsTableView.getSelectionModel().getSelectedItem().getSale().getId());
        if (saleOptional.isPresent()) {
            saleItemsTableView.setItems(FXCollections.observableArrayList(saleDao.getSaleItemsByRefund(saleOptional.get())));
            saleItemsTableView.setVisible(true);
        }


    }

    /**
     * Sakriva tablicu sa artiklima. Poziva se klikom na gumb "Zatvori".
     */

    public void hide() {
        saleItemsTableView.setVisible(false);
        saleItemsTableView.setItems(FXCollections.observableArrayList(new ArrayList<>()));
        close.setVisible(false);

    }
}
