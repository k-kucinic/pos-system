package hr.tvz.possystem.controllers;

import hr.tvz.possystem.PosSystemApplication;
import hr.tvz.possystem.constants.ViewType;
import hr.tvz.possystem.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

/**
 * Upravlja navigacijskom trakom u blagajničkom i administratorskom dijelu aplikacije.
 */
public class NavbarController {
    public Label adminLabel;
    @FXML
    private Label newScreenLabel;
    @FXML
    private Label usernameLabel;
    private AdminController adminController;
    @FXML
    private AnchorPane anchorPane;

    /**
     * Inicijalizira navigacijsku traku ovisno o ViewType-u. Ukoliko je učitan ViewType za admina navbar se ne učitava.
     */
    public void initialize() {

        if (PosSystemApplication.getViewType() != ViewType.ADMIN && PosSystemApplication.getViewType() != ViewType.ADMIN_REFUNDS) {

            if (anchorPane != null) {
                anchorPane.setVisible(true);
                anchorPane.setDisable(false);
            }

            if (PosSystemApplication.getViewType() == ViewType.CASHIER) {
                newScreenLabel.setText("Povrati");

            } else if (PosSystemApplication.getViewType() == ViewType.REFUND) {
                newScreenLabel.setText("Blagajna");
            }
            if (Optional.of(PosSystemApplication.getAppUser()).isPresent()) {
                User user = PosSystemApplication.getAppUser();
                usernameLabel.setText("Prijavljen: " + user.getUsername());
            } else {
                usernameLabel.setText("Prijavite se");
            }
        } else {

            if (Optional.of(PosSystemApplication.getAppUser()).isPresent()) {
                User user = PosSystemApplication.getAppUser();
                usernameLabel.setText("Prijavljen: " + user.getUsername());
            } else {
                usernameLabel.setText("Prijavite se");
            }
            if (anchorPane != null) {
                anchorPane.setVisible(false);
                anchorPane.setDisable(true);

            }

        }
        adminLabel.setVisible(PosSystemApplication.multiRoles);


    }

    /**
     * Postavlja novo korisničko sučelje (FXML scenu) uz zadržavanje prethodnih dimenzija i položaja prozora.
     * Koristi se pri prelasku između različitih zaslona aplikacije kako bi se očuvalo trenutno stanje prozora.
     *
     * @param scene  nova scena koja će se prikazati
     * @param wasMax označava je li prethodni prozor bio maksimiziran
     * @param width  širina prethodnog prozora
     * @param height visina prethodnog prozora
     * @param x      vodoravna pozicija prozora na ekranu
     * @param y      okomita pozicija prozora na ekranu
     */
    protected static void setNewScreenResize(Scene scene, boolean wasMax, double width, double height, double x, double y) {
        Stage stage = PosSystemApplication.getMainStage();
        if (wasMax) {
            stage.setScene(scene);
            stage.setMaximized(false);
            stage.setMaximized(true);
        } else {
            stage.setScene(scene);
            stage.setX(x);
            stage.setY(y);
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.show();

        }

    }

    /**
     * Odjavljuje trenutno prijavljenog korisnika iz aplikacije i otvara sučelje za prijavu.
     * Briše korisnika iz aktivne sesije i učitava početni FXML za prijavu.
     */

    public void logOut() {
        PosSystemApplication.setAppUser(null);
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hr/tvz/possystem/login-view.fxml"));
            navigateToNewScreen(loader);

        } catch (IOException e) {
            logger.error(e.getMessage());
        }


    }

    /**
     * Učitava novu scenu iz zadanog FXMLLoadera i prikazuje je u glavnom prozoru aplikacije.
     * Prije postavljanja scene bilježe se dimenzije i položaj trenutnog prozora kako bi nova scena imala iste dimenzije.
     *
     * @param loader {@code FXMLLoader} koji sadrži FXML datoteku nove scene
     * @throws IOException ako dođe do pogreške prilikom učitavanja FXML-a
     */

    protected static void navigateToNewScreen(FXMLLoader loader) throws IOException {
        Scene scene = new Scene(loader.load());
        Stage stage = PosSystemApplication.getMainStage();
        boolean wasMax = stage.isMaximized();
        double width = stage.getWidth();
        double height = stage.getHeight();
        double x = stage.getX();
        double y = stage.getY();
        setNewScreenResize(scene, wasMax, width, height, x, y);
    }

    /**
     * Otvara sučelje za blagajnu ili povrate, ovisno o trenutnom ViewType-u.
     * Poziva se klikom na gumb "Povrati" ili "Blagajna"
     */
    public void openNewScreen() {
        String path = "";
        if (PosSystemApplication.getViewType() == ViewType.CASHIER) {
            path = "/hr/tvz/possystem/cashier/refund-view.fxml";
            PosSystemApplication.setViewType(ViewType.REFUND);

        } else {
            PosSystemApplication.setViewType(ViewType.CASHIER);
            path = "/hr/tvz/possystem/cashier/cashier-view.fxml";

        }
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            navigateToNewScreen(loader);

        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }

    /**
     * Postavlja glavni administratorski kontroler kako bi se pojednostavila navigacija u navigacijskoj traci u
     * administratorskom dijelu aplikacije.
     *
     * @param adminController
     */
    public void setAdminController(AdminController adminController) {
        this.adminController = adminController;
    }

    /**
     * Otvara bočni izbornik za upravljanje proizvodima u administratorskom dijelu aplikacije.
     */
    public void openProductSidebar() {
        if (adminController != null) {
            PosSystemApplication.setViewType(ViewType.ADMIN_PRODUCT);
            adminController.openProducts();
        }
    }

    /**
     * Otvara bočni izbornik za upravljanje korisnicima u administratorskom dijelu aplikacije.
     */

    public void openUserSidebar() {
        if (adminController != null) {
            PosSystemApplication.setViewType(ViewType.ADMIN_USERS);
            adminController.openUsers();
        }
    }

    /**
     * Otvara bočni izbornik za upravljanje računima u administratorskom dijelu aplikacije.
     */
    public void openSales() {
        if (adminController != null) {
            PosSystemApplication.setViewType(ViewType.ADMIN_SALES);
            adminController.openSalesMainScreen();
        }
    }

    /**
     * Otvara bočni izbornik za upravljanje povratima u administratorskom dijelu aplikacije.
     */
    public void openRefundSidebar() {
        if (adminController != null) {
            PosSystemApplication.setViewType(ViewType.ADMIN_REFUNDS);
            adminController.openSidebar("/hr/tvz/possystem/admin/refund/refund-sidebar-view.fxml");
        }
    }

    public void openAdminScreen() {
        String path = "/hr/tvz/possystem/cashier/cashier-view.fxml";
        PosSystemApplication.setViewType(ViewType.CASHIER);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            NavbarController.navigateToNewScreen(loader);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }

    public void openCashierScreen() {
        String path = "/hr/tvz/possystem/admin/admin-view.fxml";
        PosSystemApplication.setViewType(ViewType.ADMIN);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            NavbarController.navigateToNewScreen(loader);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }
}
