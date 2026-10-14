package hr.tvz.possystem.database;

import hr.tvz.possystem.PosSystemApplication;
import hr.tvz.possystem.model.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

public class RefundsDaoImpl implements RefundsDao {
    ProductDao productDao = new ProductDaoImpl();
    SaleDao saleDao = new SaleDaoImpl();
    UserDao userDao = new UserDaoImpl();


    /**
     * {@inheritDoc}
     */
    @Override
    public void insertNewRefundIntoDatabase(Refund refund, List<SaleItem> saleItems, List<SaleItem> refundedItems) {
        Connection connection = null;
        try {
            connection = DatabaseUtil.connectToDatabase();
            connection.setAutoCommit(false);

            String sqlString = "INSERT INTO refunds(sale_id, refund_amount, processed_by, reason) VALUES (?, ?, ?, ?)";
            try (PreparedStatement psRefund = connection.prepareStatement(sqlString)) {
                psRefund.setInt(1, refund.getSale().getId());
                psRefund.setBigDecimal(2, refund.getRefundAmount());
                psRefund.setInt(3, PosSystemApplication.getAppUser().getId());
                psRefund.setString(4, refund.getReason());
                psRefund.executeUpdate();
            }

            String updateSale = "UPDATE sales SET is_refunded = 1 WHERE id = ?";
            try (PreparedStatement psSale = connection.prepareStatement(updateSale)) {
                psSale.setInt(1, refund.getSale().getId());
                psSale.executeUpdate();
            }

            returnItems(connection, refundedItems);
            saleDao.createSaleAfterRefund(saleItems, calculateTotal(saleItems), "Preostali artikli nakon povrata, broj narudžbe: " + refund.getSale().getId(), connection);

            connection.commit();


        } catch (IOException | SQLException e) {
            logger.error("Greška kod povrata: " + e.getMessage());
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error(rollbackEx.getMessage());
                }
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeEx) {
                    logger.error("Greška kod zatvaranja konekcije: " + closeEx.getMessage());
                }
            }
        }
    }

    /**
     * Označava zadane artikle kao vraćene u bazi podataka postavljanjem vrijednosti stupca {@code is_refunded} na 1 u
     * tablici {@code sale_items}. Vraća količine tih artikala u skladište.
     *
     * @param connection aktivna veza prema bazi podataka
     * @param saleItems  lista artikala koji se označavaju kao vraćeni
     * @throws SQLException ako dođe do pogreške pri radu s bazom podataka
     */
    private void returnItems(Connection connection, List<SaleItem> saleItems) throws SQLException {
        for (SaleItem item : saleItems) {
            String updateSql = "UPDATE sale_items SET is_refunded = 1 WHERE id = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                updateStmt.setInt(1, item.getIdBeforeRefund());
                updateStmt.executeUpdate();
            }
            productDao.restoreStockQuantityAfterRefund(item.getQuantity(), item.getProduct(), connection);
        }
    }

    /**
     * Računa ukupnu cijenu preostalih artikala nakon povrata.
     * @param saleItems preostali artikli nakon povrata
     * @return ukupna cijena preostalih artikala
     */
    private BigDecimal calculateTotal(List<SaleItem> saleItems) {
        BigDecimal total = BigDecimal.ZERO;
        for (SaleItem saleItem : saleItems) {
            BigDecimal itemTotal = saleItem.getUnitPrice().multiply(BigDecimal.valueOf(saleItem.getQuantity()));
            total = total.add(itemTotal);
        }
        return total;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Refund> getAllRefundsFromDatabase() {
        List<Refund> refunds = new ArrayList<>();
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String getRefundsSqlString = "SELECT * FROM refunds";
            Statement statement = connection.createStatement();
            statement.execute(getRefundsSqlString);
            ResultSet refundsResultSet = statement.getResultSet();
            mapResultSetToRefund(refundsResultSet, refunds);

            return refunds;

        } catch (IOException | SQLException e) {
            logger.error(e.getMessage());
            return refunds;
        }

    }

    /**
     * Mapira podatke iz {@link ResultSet} objekta u listu instanci klase {@link Refund}. Puni postojeću listu povrata
     * podacima iz baze podataka.
     *
     * @param resultSet rezultat izvršenog SQL upita koji sadrži podatke o povratima
     * @param refunds prethodno inicijalizirana lista koja će se popuniti objektima {@link Refund}

     */
    private void mapResultSetToRefund(ResultSet resultSet, List<Refund> refunds) {
        try {
            while (resultSet.next()) {
                Integer id = resultSet.getInt("id");
                Optional<Sale> saleOptional = saleDao.getSaleById(resultSet.getInt("sale_id"));
                BigDecimal refundAmount = resultSet.getBigDecimal("refund_amount");
                Optional<User> processedBy = userDao.getUserById(resultSet.getInt("processed_by"));
                String reason = resultSet.getString("reason");
                Timestamp createdAt = resultSet.getTimestamp("created_at");
                if (saleOptional.isPresent() && processedBy.isPresent()) {
                    Refund refund = new Refund(id, saleOptional.get(), refundAmount, processedBy.get(), reason, createdAt);
                    refunds.add(refund);
                }


            }
        } catch (SQLException | IOException e) {
            logger.error(e.getMessage());
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Refund> getRefundsByDate(LocalDate dateFrom, LocalDate dateTo) {
        List<Refund> refundsByDate = new ArrayList<>();
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM refunds WHERE created_at BETWEEN ? AND ?");
            preparedStatement.setTimestamp(1, Timestamp.valueOf(dateFrom.atStartOfDay()));
            preparedStatement.setTimestamp(2, Timestamp.valueOf(dateTo.atTime(23, 59, 59)));
            preparedStatement.executeQuery();
            ResultSet resultSet = preparedStatement.getResultSet();
            while (resultSet.next()) {
                mapResultSetToRefund(resultSet, refundsByDate);
            }

        } catch (IOException | SQLException e) {
            logger.error(e.getMessage());
        }
        return refundsByDate;

    }
}
