package hr.tvz.possystem.database;

import hr.tvz.possystem.model.Role;
import hr.tvz.possystem.model.User;


import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static hr.tvz.possystem.database.DatabaseUtil.connectToDatabase;
import static hr.tvz.possystem.database.DatabaseUtil.logger;

public class UserDaoImpl implements UserDao {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getUsersFromDataBase() {
        List<User> users = new ArrayList<>();
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String baseSqlQuery = """
                    SELECT u.id AS user_id, u.username, u.password_hash, u.is_active, u.created_at,
                           r.id AS role_id, r.name AS role_name
                    FROM users u
                    JOIN user_roles ur ON u.id = ur.user_id
                    JOIN roles r ON ur.role_id = r.id;""";
            Statement statement = connection.createStatement();
            statement.execute(baseSqlQuery);
            ResultSet resultSet = statement.getResultSet();
            mapResultSetToUsers(resultSet, users);
        } catch (SQLException | IOException e) {
            logger.error(e.getMessage());
        }

        return users.stream().sorted(Comparator.comparingInt(User::getId)).collect(Collectors.toList());


    }

    /**
     * Mapira podatke iz {@link ResultSet} objekta u listu instanci klase {@link User}.
     * Ako korisnik ima više uloga, metoda ih povezuje u isti objekt korisnika.
     *
     * @param resultSet rezultat SQL upita koji sadrži podatke o korisnicima i njihovim ulogama
     * @param users     lista u koju se dodaju mapirani korisnici
     * @throws SQLException ako dođe do pogreške pri čitanju podataka iz rezultata upita
     */
    private void mapResultSetToUsers(ResultSet resultSet, List<User> users) throws SQLException {
        Map<Integer, User> userMap = new HashMap<>();

        while (resultSet.next()) {
            int userId = resultSet.getInt("user_id");

            User user = userMap.get(userId);
            if (user == null) {
                String username = resultSet.getString("username");
                String passwordHash = resultSet.getString("password_hash");
                boolean isActive = resultSet.getBoolean("is_active");
                Timestamp createdAt = resultSet.getTimestamp("created_at");

                user = new User.UserBuilder()
                        .withId(userId)
                        .withUsername(username)
                        .withRoles(new ArrayList<>())
                        .withActive(isActive)
                        .withCreatedAt(createdAt)
                        .withPasswordHash((passwordHash))
                        .build();
                user.setRoles(new ArrayList<>());
                userMap.put(userId, user);
            }


            int roleId = resultSet.getInt("role_id");
            String roleName = resultSet.getString("role_name");
            Role role = new Role(roleId, roleName);

            user.getRoles().add(role);
        }

        users.addAll(userMap.values());
    }


    /**
     * {@inheritDoc}
     */
    @Override

    public void addUserToDatabase(User user) {
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String insertUserSql = "INSERT INTO users (username, password_hash, is_active, created_at) "
                    + "VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertUserSql);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPasswordHash());
            preparedStatement.setBoolean(3, user.isActive());
            preparedStatement.setTimestamp(4, user.getCreatedAt());
            preparedStatement.executeUpdate();
            Optional<Integer> userId = getUserIdByUsername(user.getUsername());
            if (userId.isPresent()) {
                if (user.getRoles().isEmpty()) {
                    logger.error("Nema uloga u bazi");
                }
                for (Role r : user.getRoles()) {
                    int role_id = 0;
                    if (r.getName().compareTo("admin") == 0) {
                        role_id = 1;
                    } else if (r.getName().compareTo("cashier") == 0) {
                        role_id = 2;
                    }
                    String inserUserRolesSql = "INSERT INTO user_roles (user_id, role_id) " +
                            "values(?,?)";
                    PreparedStatement preparedStatementRole = connection.prepareStatement(inserUserRolesSql);
                    preparedStatementRole.setInt(1, userId.get());
                    preparedStatementRole.setInt(2, role_id);


                    preparedStatementRole.executeUpdate();
                }
            }


        } catch (SQLException | IOException ex) {
            String message = "An error occurred while saving data to the database!";
            logger.error(message, ex);
        }

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Integer> getUserIdByUsername(String username) throws SQLException, IOException {
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String preparedStatementStringSql = "SELECT id from users where username=?";
            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementStringSql);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(resultSet.getInt("id"));
            }


        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> getUserById(Integer id) {
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String preparedStatementStringSql = """
                    SELECT\s
                        users.*,
                        roles.id AS role_id,
                        roles.name AS role_name
                    FROM users
                    LEFT JOIN user_roles ON users.id = user_roles.user_id
                    LEFT JOIN roles ON user_roles.role_id = roles.id
                    WHERE users.id = ?;
                    """;
            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementStringSql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String username = resultSet.getString("username");
                String passwordHash = resultSet.getString("password_hash");
                boolean isActive = resultSet.getBoolean("is_active");
                Timestamp createdAt = resultSet.getTimestamp("created_at");

                User user = new User.UserBuilder().withId(id).withUsername(username).withPasswordHash(passwordHash).withRoles(new ArrayList<>()).withActive(isActive).withCreatedAt(createdAt).build();
                user.setRoles(new ArrayList<>());


                int roleId = resultSet.getInt("role_id");
                String roleName = resultSet.getString("role_name");
                Role role = new Role(roleId, roleName);

                user.getRoles().add(role);
                return Optional.of(user);
            } else {
                return Optional.empty();
            }


        } catch (IOException | SQLException e) {
            logger.error(e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<User> getUserByUsername(String username) {
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String preparedStatementStringSql = """
                    SELECT users.*, roles.id AS role_id, roles.name AS role_name
                    FROM users
                    LEFT JOIN user_roles ON users.id = user_roles.user_id
                    LEFT JOIN roles ON user_roles.role_id = roles.id
                    WHERE users.username = ?;
                    """;
            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementStringSql);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Optional<User> userOptional = Optional.of(getUserFromResultSetByUsername(resultSet));
                if (userOptional.isPresent()) {
                    while (resultSet.next()) {
                        userOptional.get().getRoles().add(getRoleFromResultSet(resultSet));

                    }
                }

                return userOptional;
            } else {
                return Optional.empty();
            }


        } catch (IOException | SQLException e) {
            logger.error(e.getMessage());
        }
        return Optional.empty();
    }

    private Role getRoleFromResultSet(ResultSet resultSet) throws SQLException {
        int roleId = resultSet.getInt("role_id");
        String roleName = resultSet.getString("role_name");
        return new Role(roleId, roleName);

    }

    protected User getUserFromResultSetByUsername(ResultSet resultSet) throws SQLException {
        Integer id = resultSet.getInt("id");
        String passwordHash = resultSet.getString("password_hash");
        boolean isActive = resultSet.getBoolean("is_active");
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        String username = resultSet.getString("username");

        User user = new User.UserBuilder()
                .withId(id)
                .withUsername(username)
                .withPasswordHash(passwordHash)
                .withRoles(new ArrayList<>())
                .withActive(isActive)
                .withCreatedAt(createdAt)
                .build();


        int roleId = resultSet.getInt("role_id");
        String roleName = resultSet.getString("role_name");
        Role role = new Role(roleId, roleName);

        user.getRoles().add(role);
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Role> getRolesFromDatabase() {
        List<Role> roles = new ArrayList<>();
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String getRolesString = "SELECT * FROM roles";
            Statement statement = connection.createStatement();
            statement.execute(getRolesString);
            ResultSet resultSet = statement.getResultSet();


            mapResultSetToRoles(resultSet, roles);
            return roles;
        } catch (SQLException | IOException e) {
            logger.error(e.getMessage());
            return roles;
        }
    }

    /**
     * Mapira podatke iz {@link ResultSet} objekta u listu instanci klase {@link Role}.
     *
     * @param resultSet rezultat SQL upita koji sadrži podatke o korisnicima i njihovim ulogama
     * @param roles     lista u koju se dodaju mapirani korisnici
     * @throws SQLException ako dođe do pogreške pri čitanju podataka iz rezultata upita
     */
    private void mapResultSetToRoles(ResultSet resultSet, List<Role> roles) throws SQLException {
        while (resultSet.next()) {
            Integer id = resultSet.getInt("id");
            String username = resultSet.getString("name");
            Role role = new Role(id, username);
            roles.add(role);

        }
    }

    /**
     * Provjerava postoji li u bazi podataka korisnik sa određenim korisničkim imenom
     *
     * @param username korisničko ime koje se provjerava
     * @return {@code true} ako je korisničko ime zauzeto, inače {@code false}
     */
    public boolean isUsernameTaken(String username) {
        String usernameSql = "SELECT * FROM users WHERE username=?";
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            PreparedStatement preparedStatement = connection.prepareStatement(usernameSql);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();


        } catch (SQLException | IOException e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    public static String getAdminPasswordHash() {
        final String sql = "SELECT password_hash FROM users JOIN user_roles ON " +
                "user_roles.role_id=users.id WHERE user_roles.role_id = 1 LIMIT 1;";
        try (Connection connection = connectToDatabase();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password_hash");
            }
        } catch (SQLException | IOException e) {
            logger.error("Greška pri dohvaćanju admin lozinke: " + e.getMessage());
        }
        return null;
    }

    /**
     * Deaktivira korisnika u bazi podataka
     *
     * @param id identifikacijska oznaka korisnika
     *           * @return {@code true} ako je korisnik uspješno deaktiviran, inače {@code false}
     */
    @Override
    public boolean deactivateUser(Integer id) {
        try (Connection connection = DatabaseUtil.connectToDatabase()) {
            String deactivateUsersString = "UPDATE users SET is_active=0 " +
                    "where id=?";
            PreparedStatement preparedStatement = connection
                    .prepareStatement(deactivateUsersString);
            preparedStatement.setInt(1, id);
            preparedStatement.execute();
            return true;
        } catch (IOException | SQLException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * Služi za pretragu korisnika po korisničkom imenu.
     *
     * @param username dio korisničkog imena
     * @return listu korisnika koji sadržavaju {@code username}
     */
    @Override
    public List<User> getUsersByUserName(String username) {
        List<User> usersThatContainUsername = new ArrayList<>();
        try (Connection connection = connectToDatabase()) {
            String preparedStatementStringSql = """
                    SELECT users.*, roles.id AS role_id, roles.name AS role_name
                    FROM users
                    LEFT JOIN user_roles ON users.id = user_roles.user_id
                    LEFT JOIN roles ON user_roles.role_id = roles.id
                    WHERE users.username LIKE ?;
                    """;
            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementStringSql);
            preparedStatement.setString(1, "%" + username + "%");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                usersThatContainUsername.add(getUserFromResultSetByUsername(resultSet));
            }
            return usersThatContainUsername;

        } catch (SQLException | IOException e) {
            logger.error(e.getMessage());
        }
        return usersThatContainUsername;
    }

}
