package hr.tvz.possystem.controllers;

import hr.tvz.possystem.model.SaleItem;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

/**
 * Upravlja dijaloškim prozorom koji služi za odabir kolčine za povrat. Poziva se u korisničkom sučelju za povrat
 * proizvoda.
 */
public class RefundQuantityController {

    @FXML
    public Button returnAllButton;
    private SaleItem selectedItem;
    private int selectedQuantity = -1;
    private Stage dialogStage;
    private int maxQuantity;


    @FXML
    private Spinner<Integer> quantityToReturn;
    @FXML
    private Button returnButton;
    @FXML
    private Button cancelButton;


    /**
     * Postavlja maksimalnu količinu koju korisnik može odabrati za povrat u Spinner komponenti. Metoda se poziva prije
     * učitavanja sučelja kako bi se ograničio odabir na količinu dostupnu na računu.
     *
     * @param max maksimalna dozvoljena količina za povrat
     */

    public void setMaxQuantity(int max) {
        this.maxQuantity = max;
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, max, 1);
        quantityToReturn.setValueFactory(valueFactory);
    }

    /**
     * Inicijalizira vrijednost Spinner komponente prilikom učitavanja sučelja.
     * Ako je artikl odabran, Spinner se ažurira dostupnom količinom.
     */

    public void initialize() {
        if (selectedItem != null) {
            updateSpinner();
        }

    }

    /**
     * Ažurira Spinner komponentu tako da omogućuje odabir količine od 1 do ukupne količine odabranog artikla.
     */

    private void updateSpinner() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, selectedItem.getQuantity(), 1);
        quantityToReturn.setValueFactory(valueFactory);
    }

    public int getSelectedQuantity() {
        return this.selectedQuantity;
    }

    /**
     * Označava povrat cjelokupne količine odabranog artikla i zatvara dijaloški prozor.
     */

    public void returnAll() {
        selectedQuantity = this.maxQuantity;
        ((Stage) returnAllButton.getScene().getWindow()).close();
    }

    /**
     * Zatvara dijaloški prozor bez povratka artikla. Poziva se klikom na gumb "Odustani".
     */
    public void close() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    /**
     * Sprema korisnički odabranu količinu za povrat iz Spinner komponente i zatvara dijaloški prozor.
     */

    public void returnQuantity() {
        selectedQuantity = quantityToReturn.getValue();
        ((Stage) returnButton.getScene().getWindow()).close();
    }

    /**
     * Postavlja pozornicu (Stage) dijaloškog prozora.
     * @param dialogStage pozornica na kojoj se prikazuje dijalog
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}
