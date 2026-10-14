package hr.tvz.possystem.controllers;

import hr.tvz.possystem.PosSystemApplication;
import hr.tvz.possystem.constants.ViewType;
import hr.tvz.possystem.database.UserDao;
import hr.tvz.possystem.database.UserDaoImpl;
import hr.tvz.possystem.authentication.PasswordDialog;
import hr.tvz.possystem.authentication.PasswordHashing;
import hr.tvz.possystem.model.Role;
import hr.tvz.possystem.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

/**
 * Upravlja sučeljem za prijavu korisnika u aplikaciju.
 */
public class LoginController {


    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordField;
    private UserDao userDao = new UserDaoImpl();

    /**
     * Prijavljuje korisnika u bazu podataka. Poziva se klikom na gumb {@code Prijava}. Provjerava ispravnost
     * unesenost korisničkog imena i lozinke. Ako su ispravni korisnik je preusmjeren na sučelje za blagajnika ili
     * admina. Ukoliko podaci za prijavu nisu ispravni, korisnik dobiva odgovarajuću poruku.
     */
    public void login() {

        String username = usernameTextField.getText();
        String passwordNormal = passwordField.getText();
        if (username.isBlank() && passwordNormal.isBlank()) {
            wrongInformationAlert(("Korisničko ime i lozinka ne smiju biti prazni!"));

        } else if (username.isBlank()) {
            wrongInformationAlert(("Unesite korisničko ime!"));
        } else if (passwordNormal.isBlank()) {
            wrongInformationAlert(("Unesite lozinku!"));
        } else {
            Optional<User> finalUser = userDao.getUserByUsername(usernameTextField.getText());

            if (finalUser.isEmpty()) {
                wrongInformationAlert("Krivo korisničko ime");
            } else {
                if (PasswordHashing.checkIfPasswordIsCorrect(passwordNormal, finalUser.get().getPasswordHash())) {
                    PosSystemApplication.setAppUser(finalUser.get());
                    Optional<Role> userRole = finalUser.get().getRoles().stream()
                            .filter(role -> role.getName().equals("admin") || role.getName().equals("cashier"))
                            .findFirst();
                    PosSystemApplication.setMultiRoles(finalUser.get().getRoles().size() > 1);
                    if (finalUser.get().isActive()) {
                        userRole.ifPresent(role -> loadNewScreen(role.getName()));
                    } else {
                        wrongInformationAlert("Vaš profil nije aktiviran!\nNe možete se prijaviti!");

                    }

                } else {
                    wrongInformationAlert("Kriva lozinka");
                }
            }


        }

    }

    /**
     * Prikazuje se prilikom krivog unosa informacija.
     *
     * @param message poruka koja opisuje pogrešku
     */
    private void wrongInformationAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Pokušajte ponovno");
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    /**
     * Otvara sučelje za unos novog korisnika. Prije preusmjeravanja poziva se metoda za unos administratorske
     * lozinke.
     */
    public void addNewUser() {
        if (!authorizeAdmin()) return;
        try {
            FXMLLoader loader = new FXMLLoader(PosSystemApplication.class.getResource("add-new-user-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage registerScreen = new Stage();
            registerScreen.setTitle("Registrirajte novog korisnika");
            registerScreen.setScene(scene);
            registerScreen.setResizable(true);
            registerScreen.setMaximized(true);
            registerScreen.initModality(Modality.APPLICATION_MODAL);
            try {
                Image icon = new Image(new FileInputStream("src/main/resources/images/icon.png"));
                registerScreen.getIcons().add(icon);
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage());
            }
            registerScreen.showAndWait();
        } catch (IOException e) {
            logger.error(e.getMessage());
            wrongInformationAlert(e.getMessage());
        }
    }

    /**
     * Preusmjerava korisnika na odgovarajuće sučelje po ulozi.
     *
     * @param r naziv uloge korisnika
     */
    public void loadNewScreen(String r) {
        String path;
        if (r.compareTo("admin") == 0) {
            path = "/hr/tvz/possystem/admin/admin-view.fxml";
            PosSystemApplication.setViewType(ViewType.ADMIN);
        } else {
            path = "/hr/tvz/possystem/cashier/cashier-view.fxml";
            PosSystemApplication.setViewType(ViewType.CASHIER);
        }
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            NavbarController.navigateToNewScreen(loader);

        } catch (IOException e) {
            String message = "Pokušajte se ponovno prijaviti";
            wrongInformationAlert(message);
            logger.error(e.getMessage());
        }


    }

    /**
     * Prikazuje dijalog za unos administratorske lozinke i provjerava točnost lozinke.
     * {@link PasswordDialog}
     *
     * @return {@code true} ako je lozinka točna, {@code false} ako nije
     */
    public static boolean authorizeAdmin() {
        PasswordDialog dialog = new PasswordDialog("Unesite administratorsku lozinku:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String enteredPassword = result.get();
            String storedHash = UserDaoImpl.getAdminPasswordHash();

            if (storedHash != null && PasswordHashing.checkIfPasswordIsCorrect(enteredPassword, storedHash)) {

                return true;
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Neuspješna provjera");
                alert.setHeaderText(null);
                alert.setContentText("Pogrešna lozinka. Pristup odbijen.");
                alert.showAndWait();
            }
        }
        return false;
    }

    public static boolean authorizeCashier() {
        if (Optional.of(PosSystemApplication.getAppUser()).isPresent()) {
            PasswordDialog dialog = new PasswordDialog("Unesite svoju lozinku za blagajnu:");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String enteredPassword = result.get();
                String storedHash = PosSystemApplication.getAppUser().getPasswordHash();

                if (storedHash != null && PasswordHashing.checkIfPasswordIsCorrect(enteredPassword, storedHash)) {
                    return true;
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Neuspješna provjera");
                    alert.setHeaderText(null);
                    alert.setContentText("Pogrešna lozinka. Pristup odbijen.");
                    alert.showAndWait();
                }
            }
            return false;
        }
        return false;
    }


}
