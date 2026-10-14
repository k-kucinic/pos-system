package hr.tvz.possystem.database;

import hr.tvz.possystem.model.TaxRate;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

public class TaxRateDaoImpl implements TaxRateDao{
    /**
     * {@inheritDoc}
     */
    @Override
    public List<TaxRate> getTaxRatesFromDatabase() {
        List<TaxRate> taxRates = new ArrayList<>();
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String getTaxRatesString = "SELECT * FROM tax_rates";
            Statement statement = connection.createStatement();
            statement.execute(getTaxRatesString);
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                Integer categoryId = resultSet.getInt("id");
                String name = resultSet.getString("name");
                BigDecimal rate = resultSet.getBigDecimal("rate");
                Timestamp createdAt = resultSet.getTimestamp("created_at");
                taxRates.add(new TaxRate(categoryId, name, rate, createdAt));
            }
            return taxRates;


        } catch (IOException | SQLException e) {
            logger.error(e.getMessage());
        }
        return taxRates;

    }
}
