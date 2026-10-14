package hr.tvz.possystem.controllers.admin.users;

import hr.tvz.possystem.controllers.admin.products.AllProductsController;
import hr.tvz.possystem.database.UserDao;
import hr.tvz.possystem.database.UserDaoImpl;
import hr.tvz.possystem.model.Role;
import hr.tvz.possystem.model.User;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Upravlja korisničkim sučeljem za pregled svih korisnika. Svi korisinici iz baze učitavaju se u TableView.
 * Omogućena je deaktivacija odabranog korisnika iz tablice.
 */
public class AllUsersController {

    @FXML
    private TableColumn<User, Role> roleTableColumn;
    @FXML
    private TableColumn<User, Integer> idTableColumn;
    @FXML
    private TableColumn<User, Boolean> activeTableColumn;
    @FXML
    private TableColumn<User, String> dateTableColumn;
    @FXML
    private TableView<User> usersTableView;
    @FXML
    TableColumn<User, String> usernameTableColumn;
    @FXML
    private TextField searchTextField;
    private UserDao userDao = new UserDaoImpl();

    /**
     * Postavlja CellValueFactory na kolone TableView-a i postavlja listu korisnika iz baze podataka u TableView.
     */
    public void initialize() {
        idTableColumn.setCellValueFactory(productIntegerCellDataFeatures -> new ReadOnlyObjectWrapper<>(productIntegerCellDataFeatures.getValue().getId()));
        activeTableColumn.setCellValueFactory(userBooleanCellDataFeatures -> new ReadOnlyObjectWrapper<>(userBooleanCellDataFeatures.getValue().isActive()));
        dateTableColumn.setCellValueFactory(userStringCellDataFeatures -> new ReadOnlyObjectWrapper<>(userStringCellDataFeatures.getValue().getCreatedAt().toLocalDateTime().format(AllProductsController.DT_FORMAT)));
        usernameTableColumn.setCellValueFactory(userStringCellDataFeatures -> new ReadOnlyStringWrapper(userStringCellDataFeatures.getValue().getUsername()));
        roleTableColumn.setCellValueFactory(productRoleCellDataFeatures -> new ReadOnlyObjectWrapper<>(productRoleCellDataFeatures.getValue().getRoles().getFirst()));
        usersTableView.setItems(FXCollections.observableArrayList(userDao.getUsersFromDataBase()));
        usersTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

    }

    /**
     * Poziva metodu za deaktivaciju korisnika u bazi podataka. Poziva se klikom na gumb "Deaktiviraj".
     * Ako je deaktivacija u bazi podataka uspješna, otovori se dijaloški prozor sa odgovarajućom porukom.
     */
    public void deactivateUser() {
        if (usersTableView.getSelectionModel() == null) {
            return;
        }
        if (confirmDeleting(usersTableView.getSelectionModel().getSelectedItem())) {
            if (userDao.deactivateUser(usersTableView.getSelectionModel().getSelectedItem().getId())) {
                success();
            }
        }

    }

    /**
     * Prikazuje dijaloški prozor koji se prikazuje nakon uspješnog ažuriranja korisnika u bazi podataka.
     */

    private void success() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Korisnik uspješno deaktiviran!");
        alert.setTitle("Uspješno");
        alert.showAndWait();
        if (searchTextField.getText().isEmpty()) {
            usersTableView.setItems(FXCollections.observableArrayList(userDao.getUsersFromDataBase()));
        } else {
            searchByUserName();
        }


    }

    public void searchByUserName() {
        usersTableView.setItems(FXCollections.observableArrayList(userDao.getUsersByUserName(searchTextField.getText())));
    }

    /**
     * Prikazuje dijaloški prozor u kojem korisnik potvrđuje deaktivaciju korisnika.
     *
     * @param user korisnik koji se treba deaktivirati
     * @return {@code true} ako korisnik potvrdi, {@code false} ako odustane od deaktivacije
     */

    private boolean confirmDeleting(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Želite li deaktivirati korisnika: " + user.getUsername());
        alert.setHeaderText("Potvrdite deaktivaciju korisnika: " + user.getUsername());
        return AllProductsController.yesOrCancelButton(alert);
    }


}
