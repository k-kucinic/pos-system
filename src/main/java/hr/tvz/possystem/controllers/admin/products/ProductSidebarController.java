package hr.tvz.possystem.controllers.admin.products;

import hr.tvz.possystem.controllers.AdminController;
import hr.tvz.possystem.controllers.admin.SidebarController;

/**
 * Upravlja bočnim izbornikom aplikacije u dijelu za upravljanje proizodima.
 * Omogućuje navigaciju između sučelja za prikaz svih proizvoda i unos novog proizovda.
 */

public class ProductSidebarController extends SidebarController {

    /**
     * Otvara sučelje za unos novog proizvoda.
     */
    public void openAddNewProduct() {
        if(this.adminController!=null){
            adminController.openMainScreen("/hr/tvz/possystem/admin/product/add-new-product-view.fxml");
        }

    }

    /**
     * Otvara sučelje za prikaz i upravljanje postojećim proizvodima.
     */
    public void openAllProducts(){
        if(this.adminController!=null){
            adminController.openMainScreen("/hr/tvz/possystem/admin/product/all-products-view.fxml");
        }
    }
}
