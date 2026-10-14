package hr.tvz.possystem.controllers;

import hr.tvz.possystem.PosSystemApplication;
import hr.tvz.possystem.constants.ViewType;
import hr.tvz.possystem.controllers.admin.SidebarController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

/**
 * Upravlja glavnim prozorom administratorskog dijela aplikacije.
 * Omogućuje postavljanje sadržaja u gornji, lijevi i središnji dio BorderPane layouta.
 */
public class AdminController {
    @FXML
    private BorderPane borderPane;

    /**
     * Postavlja navigacijsku traku (navbar) u gornji dio BorderPanea.
     * Učitava FXML, dohvaća pripadajući kontroler i prosljeđuje mu referencu na AdminController.
     */

    public void initialize() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/hr/tvz/possystem/admin/navbar.fxml"));
            Node navbar = loader.load();
            NavbarController nav = loader.getController();
            nav.setAdminController(this);
            FXMLLoader welcomeLoader = new FXMLLoader(
                    getClass().getResource("/hr/tvz/possystem/admin/welcome-admin.fxml"));
            Node welocomeNode = welcomeLoader.load();
            borderPane.setTop(navbar);
            borderPane.setCenter(welocomeNode);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }

    /**
     * Postavlja ViewType aplikacije na ADMIN_PRODUCT i poziva metodu za otvranje bočnog izbornika.
     */

    public void openProducts() {
        PosSystemApplication.setViewType(ViewType.ADMIN_PRODUCT);
        openSidebar("/hr/tvz/possystem/admin/product/product-sidebar-view.fxml");


    }
    /**
     * Postavlja ViewType aplikacije na ADMIN_USERS i poziva metodu za otvranje bočnog izbornika.
     */

    public void openUsers() {
        PosSystemApplication.setViewType(ViewType.ADMIN_USERS);
        openSidebar("/hr/tvz/possystem/admin/users/user-sidebar-view.fxml");
    }

    /**
     * Postavlja bočni izbornik u lijevi dio BorderPanea.
     * Poziva se iz različitih dijelova aplikacije ovisno o tome koji bočni izbornik se želi otvoriti.
     * @param path putanja do FXML datoteke izbornika koji se treba otvoriti
     */
    public void openSidebar(String path) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            VBox sidebar = loader.load();
            if (sidebar != null) {
                borderPane.setLeft(sidebar);
                if (loader.getController() != null && loader.getController() instanceof SidebarController) {
                    SidebarController productSidebarController = loader.getController();
                    productSidebarController.setAdminController(this);
                    if (borderPane.getCenter() != null) {
                        borderPane.setCenter(null);
                    }
                }

            }

        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }
    /**
     * Postavlja glavno korisničko sučelje u središnji dio BorderPanea.
     * Poziva se iz različitih dijelova aplikacije, ovisno o tome koje
     * sučelje treba otvoriti.
     *
     * @param path putanja do FXML datoteke sučelja koje treba otvoriti
     */

    public void openMainScreen(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent form = loader.load();

            borderPane.setCenter(form);

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
    /**
     * Postavlja korisničko sučelje za prikaz i upravljanje prodajama u središnji dio BorderPanea.
     * Lijevi dio BorderPanea se uklanja jer modul prodaje nema bočni izbornik.
     */
    public void openSalesMainScreen() {
        openMainScreen("/hr/tvz/possystem/admin/sales/all-sales-view.fxml");
        borderPane.setLeft(null);
    }

}
