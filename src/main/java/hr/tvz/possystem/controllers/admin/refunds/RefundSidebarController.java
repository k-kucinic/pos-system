package hr.tvz.possystem.controllers.admin.refunds;

import hr.tvz.possystem.PosSystemApplication;
import hr.tvz.possystem.constants.ViewType;
import hr.tvz.possystem.controllers.admin.SidebarController;
import javafx.event.ActionEvent;

/**
 * Upravlja bočnim izbornikom za povrate.
 * Omogućuje navigaciju između sučelja za prikaz svih povrata i stvaranje novog povrata.
 */
public class RefundSidebarController extends SidebarController {
    /**
     * Otvara korisničko sučelje za prikaz svih povrata.
     */
    public void openRefundHistory() {
        if (this.adminController != null) {
            adminController.openMainScreen("/hr/tvz/possystem/admin/refund/refund-history-view.fxml");
        }
    }

    /**
     * Otvara korisničko sučelje za prikaz svih povrata.
     * Postavlja ViewType na AMIN_REFUNDS jer se isto sučelje koristi i u blagajničkom dijelu aplikacije.
     *
     * @see ViewType
     */

    public void openNewRefundWindow() {
        if (this.adminController != null) {
            PosSystemApplication.setViewType(ViewType.ADMIN_REFUNDS);
            adminController.openMainScreen("/hr/tvz/possystem/cashier/refund-view.fxml");
        }
    }
}
