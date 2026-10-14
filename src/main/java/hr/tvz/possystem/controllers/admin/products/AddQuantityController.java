package hr.tvz.possystem.controllers.admin.products;

import hr.tvz.possystem.database.ProductDao;
import hr.tvz.possystem.database.ProductDaoImpl;
import hr.tvz.possystem.model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * * Upravljа modalnim dijaloškim prozorom za dodavanje zaliha proizvoda u bazu podataka.
 * * U slučaju krivog unosa prikazuje se dijaloški prozor s odgovarajućom porukom.
 */
public class AddQuantityController {
    @FXML
    private Label productNameLabel;
    @FXML
    private TextField quantityTexField;
    private Product selectedProduct;
    private ProductDao productDao = new ProductDaoImpl();




    public void setSelectedProduct(Product selectedProduct) {
        this.selectedProduct = selectedProduct;
        productNameLabel.setText(selectedProduct.getName());
    }

    /**
     * Ažurira količinu u skladištu za odabrani proizvod u bazi podataka. Uzima vrijednost unesenu u
     * tekstualno polje i ukoliko je unesena brojčana vrijednost veća od 0, poziva metodu {@code addQuantityToProduct} iz ProductDao.
     * Prikazuje alert prozor ukoliko nije unesena ispravna vrijednost.
     *
     * @see ProductDaoImpl
     */

    public void saveChanges() {
        try {
            int value = Integer.parseInt(quantityTexField.getText());
            if (value < 1) {
                showError("Količina mora biti veća od nule.");
                return;
            }

            boolean saved = productDao.addQuantityToProduct(selectedProduct.getId(), selectedProduct.getStockQuantity() + value);
            if (saved) {
                success();
            }

        } catch (NumberFormatException e) {
            showError("Unesite ispravnu brojčanu količinu.");
        }

    }

    /**
     * Prikazuje alert prozor s odgovarajućom porukom.
     *
     * @param message poruka koja se prikazuje kao naslov alert prozora
     */

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(message);
        alert.showAndWait();
    }

    /**
     * Prikazuje alert prozor koji označava uspješno spremanje promjena zaliha u bazi podataka. Zatvaranjem alert prozora
     * zatvra se cijeli iskočni prozor.
     */

    private void success() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Nove zalihe spremljene su u bazu podataka!");
        alert.showAndWait();
        quantityTexField.getScene().getWindow().hide();
    }

    /**
     * Zatvara cijeli iskočni prozor. Poziva se klikom na gumb "Odustani".
     */

    public void cancel() {
        quantityTexField.getScene().getWindow().hide();

    }
}
