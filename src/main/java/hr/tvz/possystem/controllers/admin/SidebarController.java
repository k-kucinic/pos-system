package hr.tvz.possystem.controllers.admin;

import hr.tvz.possystem.controllers.AdminController;
/**
 * Predstavlja bazičnu klasu za kontrolere bočnog izbornika u administracijskom sučelju.
 * Sadrži referencu na AdminController kako bi se SideBar mogao postaviti lijevo u BorderPane layoutu
 */

public class SidebarController {
    protected AdminController adminController;

    /**
     * Postavlja glavni administratorski kontroler kako bi se pojednostavila navigacija u bočnim izbornicima.
     * @param adminController
     */
    public void setAdminController(AdminController adminController){
        this.adminController=adminController;
    }
}
