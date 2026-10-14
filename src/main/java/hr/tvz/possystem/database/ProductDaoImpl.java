package hr.tvz.possystem.database;

import hr.tvz.possystem.exceptions.InsufficientStockException;
import hr.tvz.possystem.model.Category;
import hr.tvz.possystem.model.Product;
import hr.tvz.possystem.model.TaxRate;
import hr.tvz.possystem.model.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

public class ProductDaoImpl implements ProductDao {
    private UserDao userDao = new UserDaoImpl();


    /**
     * {@inheritDoc}
     */
    @Override
    public List<Product> getAllProductsFromDatabase() {
        List<Product> products = new ArrayList<>();
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String getProductsString = """
                    SELECT\s
                        products.*,
                        categories.name AS category_name,
                        tax_rates.name AS tax_name,
                        tax_rates.rate
                    FROM\s
                        products
                    LEFT JOIN categories ON products.category_id = categories.id
                    LEFT JOIN tax_rates ON products.tax_id = tax_rates.id;
                    """;
            Statement statement = connection.createStatement();
            statement.execute(getProductsString);
            ResultSet resultSet = statement.getResultSet();


            while (resultSet.next()) {
                products.add(mapResultSetToProduct(resultSet));
            }

            return products;
        } catch (SQLException | IOException e) {
            logger.error(e.getMessage());
            return products;
        }

    }


    /**
     * {@inheritDoc}
     */
    @Override

    public List<Product> productNamesFromBarcode(String partOfBarcode) {
        List<Product> allProductsString = new ArrayList<>();
        try (Connection connection = DatabaseUtil.connectToDatabase()) {

            String findByBarcodeString = """
                    SELECT\s
                        products.*,
                        categories.name AS category_name,
                        tax_rates.name AS tax_name,
                        tax_rates.rate
                    FROM\s
                        products
                    LEFT JOIN categories ON products.category_id = categories.id
                    LEFT JOIN tax_rates ON products.tax_id = tax_rates.id

                    WHERE products.barcode LIKE ? and products.is_active=1;
                    """;
            PreparedStatement preparedStatement = connection.prepareStatement(findByBarcodeString);
            preparedStatement.setString(1, "%" + partOfBarcode + "%");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                allProductsString.add(mapResultSetToProduct(resultSet));
            }
            return allProductsString;


        } catch (SQLException | IOException e) {
            logger.error(e.getMessage());
        }
        return allProductsString;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Product> productFromBarcode(String barcode) {
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String findByBarcodeString = """
                    SELECT\s
                        products.*,
                        categories.name AS category_name,
                        tax_rates.name AS tax_name,
                        tax_rates.rate
                    FROM\s
                        products
                    LEFT JOIN categories ON products.category_id = categories.id
                    LEFT JOIN tax_rates ON products.tax_id = tax_rates.id

                    WHERE products.barcode=?;
                    """;
            PreparedStatement preparedStatement = connection.prepareStatement(findByBarcodeString);
            preparedStatement.setString(1, barcode);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapResultSetToProduct(resultSet));
            } else {
                return Optional.empty();
            }

        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Product> getProductById(Integer id) {
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String findByBarcodeString = """
                    SELECT\s
                        products.*,
                        categories.name AS category_name,
                        tax_rates.name AS tax_name,
                        tax_rates.rate
                    FROM\s
                        products
                    LEFT JOIN categories ON products.category_id = categories.id
                    LEFT JOIN tax_rates ON products.tax_id = tax_rates.id

                    WHERE products.id=?;
                    """;
            PreparedStatement preparedStatement = connection.prepareStatement(findByBarcodeString);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapResultSetToProduct(resultSet));
            } else {
                return Optional.empty();
            }

        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Mapira jedan redak {@link ResultSet} objekta u instancu klase {@link Product}.
     *
     * @param resultSet rezultat izvršenog SQL upita koji sadrži podatke o proizvodu
     * @return {@link Product} objekt temeljen na podacima iz rezultata
     * @throws SQLException ako dođe do pogreške pri čitanju podataka iz baze
     * @throws IOException  ako dođe do pogreške prilikom spajanja na bazu podataka u {@link DatabaseUtil}
     */
    private Product mapResultSetToProduct(ResultSet resultSet) throws SQLException, IOException {
        Integer id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        String barcode = resultSet.getString("barcode");
        Integer categoryId = resultSet.getInt("category_id");
        String categoryName = resultSet.getString("category_name");
        Category category = new Category(categoryId, categoryName);
        Integer taxRateId = resultSet.getInt("tax_id");
        String taxName = resultSet.getString("tax_name");
        BigDecimal taxRate = resultSet.getBigDecimal("rate");
        TaxRate tax = new TaxRate(taxRateId, taxName, taxRate);
        BigDecimal price = resultSet.getBigDecimal("price");
        Integer stockQuantity = resultSet.getInt("stock_quantity");
        BigDecimal discount = resultSet.getBigDecimal("discount");
        boolean isActive = resultSet.getBoolean("is_active");
        Integer createdBy = resultSet.getInt("created_by");
        Optional<User> createdByUser = userDao.getUserById(createdBy);
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");

        return new Product.ProductBuilder()
                .withId(id).withName(name)
                .withBarcode(barcode)
                .withCategory(category)
                .withTaxRate(tax)
                .withPrice(price)
                .withStockQuantity(stockQuantity)
                .withDiscount(discount)
                .withIsActive(isActive)
                .withCreatedBy(createdByUser.orElseGet(null))
                .withCreatedAt(createdAt).withUpdatedAt(updatedAt).build();
    }

    /**
     * Služi smanjivanju količine proizvoda u skladištu. Koristi se pri kreiranju nove prodaje.
     *
     * @param quantity   broj koji označava koji označava umanitelj količine
     * @param product    proizvod kojemu se smanjuje količina
     * @param connection veza iz metode za kreiranje nove prodaje
     * @throws SQLException ukoliko se dogodi greška u radu s bazom podataka
     * @throws InsufficientStockException ukoliko je {@param quantity} manji od trenutnih zaliha proizvoda
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void lowerStockQuantity(Integer quantity, Product product, Connection connection) throws SQLException, InsufficientStockException {
        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException("Nema dovoljno zaliha za proizvod: " + product.getName());
        }

        String lowerQuantityString = """
                    UPDATE products
                    SET stock_quantity = stock_quantity - ?
                    WHERE id = ?;
                """;

        PreparedStatement preparedStatement = connection.prepareStatement(lowerQuantityString);
        preparedStatement.setInt(1, quantity);
        preparedStatement.setInt(2, product.getId());
        preparedStatement.executeUpdate();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void restoreStockQuantityAfterRefund(Integer quantity, Product product, Connection connection) throws SQLException {

        String lowerQuantityString = """
                    UPDATE products
                    SET stock_quantity = stock_quantity + ?
                    WHERE id = ?;
                """;
        PreparedStatement preparedStatement = connection.prepareStatement(lowerQuantityString);
        preparedStatement.setInt(1, quantity);
        preparedStatement.setInt(2, product.getId());
        preparedStatement.executeUpdate();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Integer addProductToDatabase(Product product) {
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String insertNewProductString = "INSERT INTO products(barcode," +
                    " category_id, " + "created_by,  discount, " +
                    "is_active, name, price, stock_quantity, " +
                    "created_at, tax_id)" +
                    " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
            PreparedStatement preparedStatement = connection.
                    prepareStatement(insertNewProductString);
            preparedStatement.setString(1, product.getBarcode());
            preparedStatement.setInt(2, product.getCategory().getId());
            preparedStatement.setInt(3, product.getCreatedBy().getId());
            preparedStatement.setBigDecimal(4, product.getDiscount());
            preparedStatement.setBoolean(5, product.isActive());
            preparedStatement.setString(6, product.getName());
            preparedStatement.setBigDecimal(7, product.getPrice());
            preparedStatement.setInt(8, product.getStockQuantity());
            preparedStatement.setTimestamp(9, product.getCreatedAt());
            preparedStatement.setInt(10, product.getTaxRate().getId());
            preparedStatement.executeUpdate();
            return 1;

        } catch (SQLIntegrityConstraintViolationException e) {
            logger.error(e.getClass().getName() + ": " + e.getMessage());
            return 0;
        } catch (IOException | SQLException e) {
            logger.error(e.getClass().getName() + ": " + e.getMessage());
            return 2;
        }

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setProductAsInactive(Integer id) {
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String setInactiveString = "UPDATE products SET is_active=0, updated_at = CURRENT_TIMESTAMP WHERE id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(setInactiveString);
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();

        } catch (IOException | SQLException e) {
            logger.error(e.getMessage());
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void editProduct(Product product) {
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String setInactiveString = "UPDATE products SET name=?, price=?, " +
                    "discount=?, " +
                    "updated_at = NOW() WHERE id=?";
            PreparedStatement preparedStatement = connection.
                    prepareStatement(setInactiveString);
            preparedStatement.setString(1, product.getName());
            preparedStatement.setBigDecimal(2, product.getPrice());
            preparedStatement.setBigDecimal(3, product.getDiscount());
            preparedStatement.setInt(4, product.getId());
            preparedStatement.executeUpdate();

        } catch (IOException | SQLException e) {
            logger.error(e.getMessage());
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addQuantityToProduct(Integer id, Integer stockQuantity) {
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String addQuantityString = "UPDATE products SET stock_quantity=?, " +
                    "updated_at = NOW() WHERE id=?";
            PreparedStatement preparedStatement = connection.
                    prepareStatement(addQuantityString);
            preparedStatement.setInt(1, stockQuantity);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
            return true;

        } catch (IOException | SQLException e) {
            logger.error(e.getMessage());
            return false;
        }

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkQuantity(Integer productId, Integer quantity) {
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM products WHERE id = ? AND stock_quantity >= ?;");
            preparedStatement.setInt(1, productId);
            preparedStatement.setInt(2, quantity);
            preparedStatement.execute();
            ResultSet resultSet = preparedStatement.getResultSet();
            return resultSet.next();

        } catch (IOException | SQLException exception) {
            logger.error(exception.getMessage());
            return false;
        }
    }
}
