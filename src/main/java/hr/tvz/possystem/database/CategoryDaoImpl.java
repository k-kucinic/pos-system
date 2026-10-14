package hr.tvz.possystem.database;

import hr.tvz.possystem.model.Category;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

/**
 * Implementacija sučelja {@link CategoryDao} koja omogućuje pristup i upravljanje podacima o kategorijama u
 * bazi podataka. Sve metode koriste try-with-resources za sigurno rukovanje resursima.
 * Hvataju se iznimke {@link SQLException} i {@link IOException} koje se bilježe u logger datoteku.
 *
 * @see CategoryDao
 */
public class CategoryDaoImpl implements CategoryDao {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Category> getAllCategoriesFromDatabase() {
        List<Category> categories = new ArrayList<>();
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String getCategoriesString = "SELECT * FROM categories";
            Statement statement = connection.createStatement();
            statement.execute(getCategoriesString);
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                Integer categoryId = resultSet.getInt("id");
                String name = resultSet.getString("name");
                Timestamp createdAt = resultSet.getTimestamp("created_at");
                Timestamp updatedAt = resultSet.getTimestamp("updated_at");
                categories.add(new Category(categoryId, name, createdAt, updatedAt));
            }
            return categories;


        } catch (IOException | SQLException e) {
            logger.error(e.getMessage());
        }
        return categories;

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Integer> insertNewCategoryToDatabase(Category category) {
        Optional<Integer> categoryId = Optional.empty();
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String insertCategoryString = "INSERT INTO categories(name, created_at, updated_at)" +
                    " VALUES(?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertCategoryString,
                    Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, category.getName());
            preparedStatement.setTimestamp(2, category.getCreatedAt());
            preparedStatement.setTimestamp(3, category.getUpdatedAt());
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();

            if (resultSet.next()) {
                categoryId = Optional.of(resultSet.getInt(1));
            }
            return categoryId;


        } catch (IOException | SQLException e) {
            logger.error(e.getMessage());
            return Optional.empty();
        }

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Category> getCategoryByName(String categoryString) {
        Optional<Category> category = Optional.empty();
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String findCategoryByName = "SELECT * FROM categories WHERE name LIKE ?";
            PreparedStatement preparedStatement = connection.prepareStatement(findCategoryByName);
            preparedStatement.setString(1, categoryString);

            preparedStatement.executeQuery();
            ResultSet resultSet = preparedStatement.getResultSet();
            if (resultSet.next()) {
                Integer id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                Timestamp createdAt = resultSet.getTimestamp("created_at");
                Timestamp updatedAt = resultSet.getTimestamp("updated_at");
                category = Optional.of(new Category(id, name, createdAt, updatedAt));

            }
            return category;

        } catch (IOException | SQLException e) {
            logger.error(e.getMessage());
            return category;
        }
    }
}
