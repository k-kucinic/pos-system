package hr.tvz.possystem.controllers.admin.users;

import hr.tvz.possystem.PosSystemApplication;
import hr.tvz.possystem.constants.ViewType;
import hr.tvz.possystem.controllers.admin.SidebarController;
import javafx.event.ActionEvent;
/**
 * Upravlja bočnim izbornikom za upravljanje korisnicima.
 * Omogućuje navigaciju između sučelja za prikaz svih korisnika i stvaranje novog korisnika.
 */
public class UserSidebarController extends SidebarController {
    /**
     * Otvara korisničko sučelje za dodavanje novog korisnika.
     * Postavlja ViewType na ADMIN_USERS.
     */
    public void newUser() {
        PosSystemApplication.setViewType(ViewType.ADMIN_USERS);
        if (adminController != null) {
            adminController.openMainScreen("/hr/tvz/possystem/add-new-user-view.fxml");
        }
    }
    /**
     * Otvara korisničko sučelje za prikaz i upravljanje korisnicima.
     * Postavlja ViewType na ADMIN_USERS.
     */
    public void allUsers() {
        PosSystemApplication.setViewType(ViewType.ADMIN_USERS);
        if (adminController != null) {
            adminController.openMainScreen("/hr/tvz/possystem/admin/users/all-users-view.fxml");
        }
    }
}
