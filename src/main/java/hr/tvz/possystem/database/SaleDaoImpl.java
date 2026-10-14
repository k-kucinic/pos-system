package hr.tvz.possystem.database;

import hr.tvz.possystem.PosSystemApplication;
import hr.tvz.possystem.constants.FiscalStatus;
import hr.tvz.possystem.exceptions.InsufficientStockException;
import hr.tvz.possystem.fisc.*;
import hr.tvz.possystem.model.Product;
import hr.tvz.possystem.model.Sale;
import hr.tvz.possystem.model.SaleItem;
import hr.tvz.possystem.model.User;
import hr.tvz.possystem.printer.PosReceiptPrinter;
import javafx.collections.ObservableList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static hr.tvz.possystem.database.DatabaseUtil.connectToDatabase;
import static hr.tvz.possystem.database.DatabaseUtil.logger;
import static hr.tvz.possystem.fisc.XmlUtil.buildLocalSignatureSeed;
import static hr.tvz.possystem.fisc.ZkiPlaceholder.toHex;

public class SaleDaoImpl implements SaleDao {
    private ProductDao productDao = new ProductDaoImpl();
    private UserDao userDao = new UserDaoImpl();
    private static final String privateKeyPath = "C:\\Users\\Katarina\\OneDrive - Zagreb University of Applied Science\\Desktop\\faks\\sesti semestar\\zavrsni\\pos-system\\certs\\pos-local.p12";


    /**
     * {@inheritDoc}
     */
    @Override
    public void createSaleAfterRefund(List<SaleItem> remainingProducts, BigDecimal total, String notes, Connection connection) {
        int saleId = -1;
        String sqlQuery = "INSERT INTO sales(date_time, total_amount, payment_method, user_id, notes, is_refunded, created_at)" +
                " values(?, ?, ?, ?, ?, ?, ?) ";
        PreparedStatement preparedStatement;
        try {
            preparedStatement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setDate(1, Date.valueOf(LocalDate.now()));
            preparedStatement.setBigDecimal(2, total);
            preparedStatement.setString(3, "cash");
            User user = PosSystemApplication.getAppUser();
            preparedStatement.setInt(4, user.getId());
            if (!notes.isBlank()) {
                preparedStatement.setString(5, notes);
            } else {
                preparedStatement.setString(5, null);
            }
            preparedStatement.setBoolean(6, false);
            Timestamp timestamp = Timestamp.from(Instant.now());
            preparedStatement.setTimestamp(7, timestamp);
            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();

            if (rs.next()) {
                saleId = rs.getInt(1);
            }
            if (saleId != -1) {
                insertSaleItems(remainingProducts, connection, saleId);
                try {
                    PosReceiptPrinter.printTheReceipt(new Sale(saleId, timestamp.toLocalDateTime(), total, "novčanice", user, false, timestamp.toLocalDateTime(), remainingProducts));
                } catch (Exception e) {
                    logger.error("Greška printera: " + e.getMessage());
                }

            }
        } catch (SQLException e) {
            logger.error(e.getClass().getName() + ": " + e.getMessage());
        }

    }

    /**
     * Sprema kupljene artikle u tablicu {@code sale_items} za zadani račun. Poziva se prilikom obrade standardne
     * prodaje i zapisa nove prodaje preostalih artikala nakon povrata.
     *
     * @param remainingProducts lista artikala koji se spremaju na račun
     * @param connection        aktivna veza prema bazi podataka
     * @param saleId            identifikacijska oznaka računa kojem artikli pripadaju
     */
    protected void insertSaleItems(List<SaleItem> remainingProducts, Connection connection, int saleId) throws SQLException {
        String insertIntoSaleItems = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, tax_id) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement psItem = connection.prepareStatement(insertIntoSaleItems);

        for (SaleItem saleItem : remainingProducts) {
            psItem.setInt(1, saleId);
            psItem.setInt(2, saleItem.getProduct().getId());
            psItem.setInt(3, saleItem.getQuantity());
            psItem.setBigDecimal(4, saleItem.getUnitPrice());
            psItem.setInt(5, saleItem.getProduct().getTaxRate().getId());
            psItem.addBatch();
        }
        psItem.executeBatch();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void createSale(ObservableList<SaleItem> saleItems, BigDecimal total, String notes) {
        int saleId = -1;
        String jir = null;
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            for (SaleItem saleItem : saleItems) {
                try {
                    productDao.lowerStockQuantity(saleItem.getQuantity(), saleItem.getProduct(), connection);
                } catch (InsufficientStockException e) {
                    logger.error(e.getClass().getName() + ": " + e.getMessage());
                }
            }
            Sale sale = new Sale();
            String sqlQuery = "INSERT INTO sales(date_time, total_amount, payment_method, user_id, notes, is_refunded, created_at)" +
                    " values(?, ?, ?, ?, ?, ?, ?) ";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setDate(1, Date.valueOf(LocalDate.now()));
            preparedStatement.setBigDecimal(2, total);
            sale.setTotalAmount(total);
            preparedStatement.setString(3, "cash");
            sale.setPaymentMethod("novčanice");
            preparedStatement.setInt(4, PosSystemApplication.getAppUser().getId());
            sale.setUser(PosSystemApplication.getAppUser());
            preparedStatement.setString(5, notes);
            preparedStatement.setBoolean(6, false);
            Timestamp now = Timestamp.from(Instant.now());
            preparedStatement.setTimestamp(7, now);
            sale.setCreatedAt(now.toLocalDateTime());
            preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            sale.setSaleItems(saleItems);
            if (rs.next()) {
                saleId = rs.getInt(1);
                sale.setId(saleId);
                FiscalInvoice fiscalInvoice = FiscalInvoice.fromSale(sale);
                String seed = buildLocalSignatureSeed(fiscalInvoice, "POS1", "POS1");
                var keyMaterial = KeystoreLoader.loadPkcs12(privateKeyPath, "changeit", "pos-local", "changeit");
                PrivateKey privateKey = keyMaterial.privateKey();
                PublicKey publicKey = keyMaterial.certificate().getPublicKey();
                byte[] signature = LocalSigner.sign(seed.getBytes(StandardCharsets.UTF_8), privateKey);
                String signatureB64 = Base64.getEncoder().encodeToString(signature);
                boolean verifyOk = LocalSigner.verify(seed.getBytes(StandardCharsets.UTF_8), signature, publicKey);
                if (!verifyOk) {
                    logger.warn("Local signature verification FAILED for saleId=" + saleId);
                }
                byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(signature);
                String demoJir = toHex(sha256);

                String zki = ZkiPlaceholder.generatePlaceholder(
                        fiscalInvoice.issuerOib(),
                        fiscalInvoice.issueDateTime().format(SoapXmlBuilder.F),
                        fiscalInvoice.invoiceNumber(),
                        "POS1",
                        "POS1",
                        fiscalInvoice.totalAmount()
                );

                Long fiscId = saveFiscalizationRecord(saleId, zki, null, connection);

                if (verifyOk) {
                    jir = demoJir;
                    updateFiscalizationSuccess(fiscId, jir, "LOCAL_SIG:" + signatureB64, connection, FiscalStatus.OK);

                    logger.info("LOCAL fiscalization OK, JIR=" + demoJir);
                } else {
                    updateFiscalizationFail(fiscId, "local-signature-verify-failed", "LOCAL_SIG:" + signatureB64, connection);
                    logger.warn("LOCAL fiscalization FAIL (verify=false)");
                }
            }


            if (saleId != -1) {
                try {
                    PosReceiptPrinter.printTheReceipt(sale);
                } catch (Exception e) {

                    logger.error("Greška printera " + e.getMessage());
                }
                insertSaleItems(saleItems, connection, saleId);


            }


        } catch (SQLException | IOException e) {
            logger.error(e.getClass().getName() + ": " + e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }


    }


    public static String getJirBySaleId(Integer saleID) {
        String jir = null;
        try (Connection connection = connectToDatabase()) {
            String getJirSqlString = "SELECT fiscalization.jir FROM sales LEFT JOIN fiscalization ON fiscalization.sale_id=sales.id WHERE sale_id=?;";
            PreparedStatement preparedStatement = connection.prepareStatement(getJirSqlString);
            preparedStatement.setInt(1, saleID);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            if (resultSet.next()) {
                jir = resultSet.getString("jir");
            }


        } catch (IOException | SQLException e) {
            logger.error(e.getMessage());
        }
        return jir;
    }

    private void updateFiscalizationSuccess(Long id, String jir, String responseXml, Connection c, FiscalStatus fiscalStatus) throws SQLException {
        try (var ps = c.prepareStatement("""
                    UPDATE fiscalization SET status=?, jir=?, response_xml=?, last_attempt_at=NOW() WHERE id=?
                """)) {
            ps.setString(1, fiscalStatus.toString());
            ps.setString(2, jir);
            ps.setString(3, responseXml);
            ps.setLong(4, id);
            ps.executeUpdate();
        }
    }

    private void updateFiscalizationFail(Long id, String error, String responseXml, Connection c) throws SQLException {
        try (var ps = c.prepareStatement("""
                    UPDATE fiscalization SET status='FAILED', error_msg=?, response_xml=?, last_attempt_at=NOW() WHERE id=?
                """)) {
            ps.setString(1, error);
            ps.setString(2, responseXml);
            ps.setLong(3, id);
            ps.executeUpdate();
        }
    }


    private Long saveFiscalizationRecord(int saleId,
                                         String zki,
                                         String requestXml,
                                         Connection connection) throws SQLException {
        final String requestId = java.util.UUID.randomUUID().toString();

        final String sql = """
                INSERT INTO fiscalization
                    (sale_id, zki, status, request_xml, request_id, created_at)
                VALUES
                    (?, ?, ?, ?, ?, NOW())
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, saleId);
            ps.setString(2, zki);
            ps.setString(3, "PENDING");
            ps.setString(4, requestXml);
            ps.setString(5, requestId);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Sale> getSaleById(Integer saleId) {
        try (Connection connection = DatabaseUtil.connectToDatabase()) {

            String saleQuery = "SELECT * FROM sales WHERE id = ?";
            PreparedStatement saleStmt = connection.prepareStatement(saleQuery);
            saleStmt.setInt(1, saleId);
            ResultSet saleRs = saleStmt.executeQuery();

            if (!saleRs.next()) return Optional.empty();

            Sale sale = new Sale();
            sale.setId(saleId);

            sale.setDateTime(saleRs.getTimestamp("date_time").toLocalDateTime());
            sale.setTotalAmount(saleRs.getBigDecimal("total_amount"));
            sale.setPaymentMethod(saleRs.getString("payment_method"));
            sale.setPaymentMethod(saleRs.getString("payment_method"));
            sale.setRefunded(saleRs.getBoolean("is_refunded"));
            sale.setCreatedAt(saleRs.getTimestamp("created_at").toLocalDateTime());


            int userId = saleRs.getInt("user_id");
            sale.setUser(userDao.getUserById(userId).orElse(null));

            List<SaleItem> saleItems = new ArrayList<>();
            sale.setSaleItems(saleItems);
            return Optional.of(sale);

        } catch (IOException | SQLException e) {
            logger.error("Error fetching sale: " + e.getMessage(), e);
            return Optional.empty();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<SaleItem> getSaleItemsBySaleId(Sale sale) {
        Integer saleID = sale.getId();
        List<SaleItem> saleItems = new ArrayList<>();
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String sqlString = """
                    SELECT\s
                        si.id AS sale_item_id,
                        si.sale_id,
                        si.product_id,
                        si.quantity,
                        si.unit_price,
                        si.total_price,
                        si.is_refunded,

                        p.name AS product_name,
                        p.barcode,
                        p.price AS product_price,
                        p.discount,
                        p.tax_id,

                        t.name AS tax_name,
                        t.rate AS tax_rate

                    FROM sale_items si
                    JOIN products p ON si.product_id = p.id
                    LEFT JOIN tax_rates t ON p.tax_id = t.id
                    WHERE si.sale_id = ?;
                    """;
            PreparedStatement preparedStatement = connection.prepareStatement(sqlString);
            preparedStatement.setInt(1, saleID);
            preparedStatement.executeQuery();
            ResultSet resultSet = preparedStatement.getResultSet();
            while (resultSet.next()) {
                SaleItem saleItem = new SaleItem.SaleItemBuilder().build();
                saleItem.setId(resultSet.getInt("sale_item_id"));
                saleItem.setSale(sale);
                saleItem.setProduct(productDao.getProductById(resultSet.getInt("product_id")).orElse(new Product.ProductBuilder().build()));
                saleItem.setQuantity(resultSet.getInt("quantity"));
                saleItem.setUnitPrice(resultSet.getBigDecimal("unit_price"));
                saleItem.setRefunded(resultSet.getBoolean("is_refunded"));
                saleItem.setTotalPrice(resultSet.getBigDecimal("total_price"));
                saleItems.add(saleItem);

            }
            return saleItems;


        } catch (IOException | SQLException e) {
            logger.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return saleItems;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Sale> getAllSalesFromDatabase() {
        List<Sale> allSales = new ArrayList<>();
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String saleQuery = "SELECT * FROM sales ORDER BY created_at DESC";
            PreparedStatement saleStmt = connection.prepareStatement(saleQuery);
            ResultSet saleRs = saleStmt.executeQuery();
            while (saleRs.next()) {
                mapResultSetToSales(saleRs, allSales);
            }
            return allSales;
        } catch (IOException | SQLException e) {
            logger.error(e.getClass().getName() + ": " + e.getMessage());
            return allSales;
        }

    }

    /**
     * Mapira podatke iz {@link ResultSet} objekta u instancu klase {@link Sale} i dodaje je u zadanu listu računa.
     *
     * @param saleRs   rezultat SQL upita koji sadrži podatke o računu
     * @param allSales lista u koju se dodaje novi {@link Sale} objekt
     */
    private void mapResultSetToSales(ResultSet saleRs, List<Sale> allSales) throws SQLException, IOException {
        Sale sale = new Sale();
        sale.setId(saleRs.getInt("id"));
        sale.setDateTime(saleRs.getTimestamp("date_time").toLocalDateTime());
        sale.setTotalAmount(saleRs.getBigDecimal("total_amount"));
        sale.setPaymentMethod(saleRs.getString("payment_method"));
        sale.setNotes(saleRs.getString("notes"));
        sale.setRefunded(saleRs.getBoolean("is_refunded"));
        sale.setCreatedAt(saleRs.getTimestamp("created_at").toLocalDateTime());
        int userId = saleRs.getInt("user_id");
        sale.setUser(userDao.getUserById(userId).orElse(null));

        List<SaleItem> saleItems = new ArrayList<>();
        sale.setSaleItems(saleItems);
        allSales.add(sale);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Sale> getSalesByDate(LocalDate dateFrom, LocalDate dateTo) {
        List<Sale> salesByDate = new ArrayList<>();
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM sales WHERE date_time BETWEEN ? and ?");
            preparedStatement.setTimestamp(1, Timestamp.valueOf(dateFrom.atStartOfDay()));
            preparedStatement.setTimestamp(2, Timestamp.valueOf(dateTo.atTime(23, 59, 59)));
            preparedStatement.executeQuery();
            ResultSet resultSet = preparedStatement.getResultSet();
            while (resultSet.next()) {
                mapResultSetToSales(resultSet, salesByDate);
            }

        } catch (IOException | SQLException e) {
            logger.error(e.getClass().getName() + ": " + e.getMessage());
        }
        return salesByDate;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<SaleItem> getSaleItemsByRefund(Sale sale) {
        List<SaleItem> refundedSaleItems = new ArrayList<>();
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String getRefundedSaleItemsString = "SELECT * FROM sale_items WHERE sale_id=? AND IS_refunded=1;";
            PreparedStatement preparedStatement = connection.prepareStatement(getRefundedSaleItemsString);
            preparedStatement.setInt(1, sale.getId());
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            while (resultSet.next()) {
                SaleItem saleItem = new SaleItem.SaleItemBuilder().build();
                saleItem.setId(resultSet.getInt("sale_id"));
                saleItem.setSale(sale);
                saleItem.setProduct(productDao.getProductById(resultSet.getInt("product_id")).orElse(new Product.ProductBuilder().build()));
                saleItem.setQuantity(1);
                saleItem.setUnitPrice(resultSet.getBigDecimal("unit_price"));
                saleItem.setRefunded(resultSet.getBoolean("is_refunded"));
                saleItem.setTotalPrice(resultSet.getBigDecimal("total_price"));
                refundedSaleItems.add(saleItem);

            }
            return refundedSaleItems;

        } catch (SQLException | IOException e) {
            logger.error(e.getClass().getName() + ": " + e.getMessage());
            return refundedSaleItems;
        }
    }

}
