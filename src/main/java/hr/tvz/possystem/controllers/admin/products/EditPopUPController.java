package hr.tvz.possystem.controllers.admin.products;

import hr.tvz.possystem.PosSystemApplication;
import hr.tvz.possystem.constants.ViewType;
import hr.tvz.possystem.database.ProductDao;
import hr.tvz.possystem.database.ProductDaoImpl;
import hr.tvz.possystem.model.Product;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.math.BigDecimal;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

/**
 * Upravlja modalnim dijaloškim prozorom za uređivanje proizvoda u bazi podataka.
 * U slučaju pogrešnog unosa prikazuje dijaloški prozor s odgovarajućom porukom.
 */
public class EditPopUPController {
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField priceTextField;
    @FXML
    private TextField discountTextField;
    private Product editingProduct;
    private ProductDao productDao = new ProductDaoImpl();

    /**
     * Postavlja proizvod koji se uređuje.
     *
     * @param product proizvod koji se uređuje
     */
    public void setEditingProduct(Product product) {
        this.editingProduct = product;

    }

    /**
     * Popunjava tekstualna polja vrijednostima odabranog proizovda.
     */
    public void fillTheTextFields() {
        if (editingProduct != null) {
            nameTextField.setText(editingProduct.getName());
            priceTextField.setText(editingProduct.getPrice().toString());
            discountTextField.setText(editingProduct.getDiscount().toString());
        }
    }

    /**
     * Sprema promjene proizvoda nakon provjere unesenih podataka.
     * Provjerava jesu li izmijenjeni naziv, cijena ili popust, i ažurira ih ako su vrijenosti ispravno unesene.
     * Ako su podaci neispravni (npr. negativan popust ili krivi format broja), prikazuje poruku o grešci u dijaloškom prozoru.
     * Promjene se spremaju u bazu podataka samo ako postoji barem jedna valjana izmjena.
     * @see ProductDaoImpl
     */
    public void save() {
        StringBuilder message = new StringBuilder();
        int numberOfChanges = 0;

        if (!nameTextField.getText().isBlank() && editingProduct.getName().compareTo(nameTextField.getText()) != 0) {
            editingProduct.setName(nameTextField.getText());
            numberOfChanges++;
        }

        BigDecimal price = BigDecimal.ZERO;
        try {
            price = new BigDecimal(priceTextField.getText());
        } catch (
                NumberFormatException e) {
            message.append("Neispravan unos cijene!\n");
            logger.error("Neispravan unos cijene: " + e.getMessage());

        }
        if (price.compareTo(BigDecimal.ZERO) != 0 && price.compareTo(editingProduct.getPrice()) != 0) {
            editingProduct.setPrice(price);
            numberOfChanges++;

        }
        BigDecimal discount = BigDecimal.valueOf(-1);
        try {
            discount = new BigDecimal(discountTextField.getText());
            if (discount.compareTo(BigDecimal.ZERO) < 0) {
                message.append("Popust ne smije biti negativan.");
            }
            if (discount.compareTo(editingProduct.getPrice()) > 0) {
                message.append("Popust ne smije biti veći od cijene!");
            }
        } catch (
                NumberFormatException e) {
            message.append("Neispravan unos popusta (" + discountTextField.getText() + ")\n");
            logger.error("Neispravan unos popusta: " + e.getMessage());

        }
        if (discount.compareTo(BigDecimal.valueOf(-1)) != 0 && discount.compareTo(editingProduct.getDiscount()) != 0 && discount.compareTo(BigDecimal.ZERO) >= 0) {
            editingProduct.setDiscount(discount);
            numberOfChanges++;
        }
        if (message.length() > 1) {
            showErrors(message);
        } else {
            if (numberOfChanges > 0) {
                productDao.editProduct(editingProduct);
                success();
            }
        }

    }
    /**
     * Prikazuje obavijest o uspješnom dodavanju proizvoda. Zatvara trenutno otvoreno
     * korisničko sučelje.
     */
    private void success() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Proizvod uspješno ažuriran");
        alert.showAndWait();
        priceTextField.getScene().getWindow().hide();

    }

    /**
     * Prikazuje dijaloški prozor koji prikazuje greške prilikom ažuriranja proizvoda.
     *
     * @param message poruka koja se prikazuje u dijaloškom prozoru.
     */
    private void showErrors(StringBuilder message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška prilikom uređivanja proizvoda");
        alert.setHeaderText("Neispravan unos: ");
        alert.setContentText(message.toString());
        alert.showAndWait();
    }

    /**
     * Zatvara cijeli dijaloški prozor.
     * Poziva se klikom na gumb "Odustani"
     */
    public void cancel() {
        discountTextField.getScene().getWindow().hide();

    }
}
