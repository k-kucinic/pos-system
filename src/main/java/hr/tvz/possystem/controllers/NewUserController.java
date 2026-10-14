package hr.tvz.possystem.controllers;

import hr.tvz.possystem.PosSystemApplication;
import hr.tvz.possystem.constants.ViewType;
import hr.tvz.possystem.database.UserDao;
import hr.tvz.possystem.database.UserDaoImpl;
import hr.tvz.possystem.authentication.PasswordHashing;
import hr.tvz.possystem.model.Role;
import hr.tvz.possystem.model.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Upravlja korisničkim sučeljem za dodavanje novog korisnika u bazu podataka.
 */
public class NewUserController {
    @FXML
    public PasswordField repeatPasswordField;
    @FXML
    public ComboBox<Role> roleSeleectionCombobox;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordField;
    private final StringBuilder message = new StringBuilder();
    private UserDao userDao = new UserDaoImpl();

    /**
     * Inicijalizira podatke o ulogama iz baze podataka u ComboBox.
     */
    public void initialize() {
        roleSeleectionCombobox.setItems(FXCollections.observableArrayList(userDao.getRolesFromDatabase()));
    }

    public void register() {
        Optional<String> userName = Optional.empty();
        Optional<String> password = Optional.empty();
        Optional<String> passwordRepeated = Optional.empty();
        Optional<Role> role = Optional.empty();
        Optional<User> user = Optional.empty();
        if (!usernameTextField.getText().isBlank()) {
            if (usernameTextField.getText().length() < 3) {
                message.append("Korisničko ime mora imati barem 3 znaka!\n");
            } else {
                if (userDao.isUsernameTaken(usernameTextField.getText())) {
                    message.append("Korisničko ime već postoji!\n");
                } else {
                    userName = Optional.of(usernameTextField.getText());
                }
            }
        } else {
            message.append("Korisničko ime ne smije biti prazno!\n");
        }
        if (!passwordField.getText().isBlank()) {
            if (passwordField.getText().length() < 5)
                message.append("Lozinka mora imati barem 6 znakova!\n");
            else password = Optional.of(passwordField.getText());
        } else message.append("Lozinka ne smije biti prazna!\n");
        if (!repeatPasswordField.getText().isBlank()) {
            passwordRepeated = Optional.of(repeatPasswordField.getText());
        } else message.append("Morate ponoviti zaporku!\n");
        if (Optional.ofNullable(roleSeleectionCombobox.getValue()).isPresent()) {
            role = Optional.of(roleSeleectionCombobox.getValue());

        } else message.append("Odaberite ulogu korisnika\n");
        if (userName.isPresent() && passwordRepeated.isPresent() && password.isPresent() && role.isPresent()) {
            List<Role> roles = new ArrayList<>();
            roles.add(role.get());
            if (passwordRepeated.get().compareTo(password.get()) == 0) {
                user = Optional.of(new User.UserBuilder().withUsername(userName.get()).withCreatedAt(Timestamp.from(Instant.now())).withActive(true).withPasswordHash(PasswordHashing.getHashedPassword(password.get())).withRoles(roles).build());
            } else {
                message.append("Lozinke se ne podudaraju!\n");

            }
        }


        if (message.length() > 2) {
            errors(message);
        } else if (user.isPresent()) {
            userDao.addUserToDatabase(user.get());
            success();
        }

    }

    /**
     * Prikazuje dijaloški prozor s popisom pogrešaka koje su se dogodile pri unosu novog korisnika.
     *
     * @param message poruka koja sadrži sve zabilježene pogreške
     */

    private void errors(StringBuilder message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Pokušajte ponovno");
        alert.setHeaderText(message.toString());
        this.message.setLength(0);
        alert.showAndWait();

    }

    /**
     * Prikazuje obavijest o uspješnom dodavanju korisnika. Ako administrator nije prijavljen, zatvara se trenutno
     * korisničko sučelje.
     */

    private void success() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Korisnik uspješno spremljen!");
        alert.showAndWait();
        if (PosSystemApplication.getViewType() != ViewType.ADMIN_USERS) {
            repeatPasswordField.getScene().getWindow().hide();
        }

    }

}
