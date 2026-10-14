package hr.tvz.possystem.controllers.admin.products;

import hr.tvz.possystem.PosSystemApplication;
import hr.tvz.possystem.constants.ViewType;
import hr.tvz.possystem.database.*;
import hr.tvz.possystem.model.Category;
import hr.tvz.possystem.model.Product;
import hr.tvz.possystem.model.TaxRate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

/**
 * Upravlja sučeljem za kreiranje i unos novog proizvoda u bazu podataka.
 */
public class NewProductController {
    @FXML
    private ComboBox<TaxRate> taxRateComboBox;
    @FXML
    private ComboBox<Category> categoryComboBox;
    @FXML
    private TextField barcodeTextfield;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField priceTextField;
    @FXML
    private TextField discountTextField;
    @FXML
    private TextField stockQuantityTextField;
    List<String> categoryNamesFromDatabase = new ArrayList<>();
    private ProductDao productDao = new ProductDaoImpl();
    private CategoryDao categoryDao = new CategoryDaoImpl();
    private TaxRateDao taxDao = new TaxRateDaoImpl();

    /**
     * Inicijalizira podatke u ComboBox-u za kategorije i stope poreza sa podacima iz baze podataka.
     * Pokreće se automatski pri otvaranju prozora.
     */
    public void initialize() {

        PosSystemApplication.setViewType(ViewType.ADMIN_PRODUCT);


        List<Category> allCategories = categoryDao.getAllCategoriesFromDatabase();
        this.categoryNamesFromDatabase = allCategories.stream().map(Category::getName).collect(Collectors.toList());


        if (categoryComboBox != null && taxRateComboBox != null) {

            ObservableList<Category> comboBoxItems = FXCollections.observableArrayList(allCategories);
            comboBoxItems.add(new Category(-1, "+ Nova kategorija", null, null));
            categoryComboBox.setItems(comboBoxItems);

            taxRateComboBox.setItems(FXCollections.observableArrayList(taxDao.getTaxRatesFromDatabase()));
        }


        TextFormatter<String> barcodeFormatter = new TextFormatter<>(change -> change.getControlNewText().length() > 14 ? null : change);
        barcodeTextfield.setTextFormatter(barcodeFormatter);


        categoryComboBox.setEditable(true);
        categoryComboBox.setOnAction(event -> {
            Object selected = categoryComboBox.getValue();

            if (selected instanceof Category selectedCategory && selectedCategory.getId() == -1) {

                categoryComboBox.getSelectionModel().clearSelection();
                categoryComboBox.getEditor().clear();
                categoryComboBox.getEditor().requestFocus();
            }
        });
    }

    /**
     * Sprema novi proizvod u bazu podataka na temelju podataka unesenih u formu.
     * Omogućuje odabir postojeće ili unos nove kategorije.
     * U slučaju neispravno unesenih podataka prikazuje se dijaloški prozor sa odgovarajućom porukom.
     *
     * @see ProductDaoImpl
     */
    public void saveProductToDatabase() {
        StringBuilder message = new StringBuilder();
        boolean isValid = taxRateComboBox.getValue() != null && categoryComboBox.getValue() != null && !nameTextField.getText().isEmpty() && !barcodeTextfield.getText().isEmpty() && !priceTextField.getText().isEmpty() && !stockQuantityTextField.getText().isEmpty();

        if (!isValid) {
            emptyFieldsAlert();
            return;
        }

        TaxRate taxRate = taxRateComboBox.getValue();
        Optional<Category> category = Optional.empty();
        Object categoryObject = categoryComboBox.getValue();
        if (categoryObject instanceof String categoryString) {
            boolean categoryExists = categoryNamesFromDatabase.stream().anyMatch(name -> name.equalsIgnoreCase(categoryString.trim()));

            if (!categoryExists && !categoryString.isBlank() && !categoryString.equals("+ Nova kategorija")) {
                String categoryName = categoryString.trim().toLowerCase();
                categoryName = categoryName.substring(0, 1).toUpperCase() + categoryName.substring(1);
                Timestamp timestamp = Timestamp.from(Instant.now());
                Optional<Integer> optionalId = categoryDao.insertNewCategoryToDatabase(new Category(null, categoryName, timestamp, timestamp));

                if (optionalId.isPresent()) {
                    Category newCategory = new Category(optionalId.get(), categoryName, timestamp, timestamp);
                    category = Optional.of(newCategory);
                } else {
                    message.append("Greška pri unosu kategorije!\n");
                }
            } else if (categoryExists) {

                if (categoryDao.getCategoryByName(categoryString).isPresent()) {
                    category = categoryDao.getCategoryByName(categoryString);
                }
            }
        } else if (categoryObject instanceof Category) {
            category = Optional.of(categoryComboBox.getValue());
        }
        if (category.isEmpty()) {
            message.append("Neispravan unos kategorije");

        }


        String name = nameTextField.getText();
        String barcode = barcodeTextfield.getText();

        BigDecimal price = BigDecimal.ZERO;
        try {
            price = new BigDecimal(priceTextField.getText());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                message.append("Cijena mora biti veća od 0!");
            }
        } catch (NumberFormatException e) {
            message.append("Neispravan unos cijene!\n");
            logger.error("Neispravan unos cijene: " + e.getMessage());

        }


        BigDecimal discount = BigDecimal.ZERO;
        if (!discountTextField.getText().isEmpty()) {
            try {
                discount = new BigDecimal(discountTextField.getText());
                if (discount.compareTo(price) > 0 && price.compareTo(BigDecimal.ZERO) != 0) {
                    message.append("Popust ne smije biti veći od cijene!\n");
                }
            } catch (NumberFormatException e) {
                message.append("Neispravan unos popusta!\n");
                logger.error("Neispravan unos popusta: " + e.getMessage());

            }
        }

        int stockQuantity = 0;
        try {
            stockQuantity = Integer.parseInt(stockQuantityTextField.getText());
        } catch (NumberFormatException e) {
            logger.error("Neispravan unos količine: " + e.getMessage());
            message.append("Neispravan unos količine!\n ");
        }
        if (!checkBarcode(barcode)) {
            message.append("Bar-kod neispravno unesen!");
        }
        if (message.length() < 3 && stockQuantity != 0 && price.compareTo(BigDecimal.ZERO) != 0 && category.isPresent()) {
            Product newProduct = new Product.ProductBuilder().withCreatedAt(Timestamp.from(Instant.now())).withBarcode(barcode).withPrice(price).withDiscount(discount).withName(name).withTaxRate(taxRate).withCategory(category.get()).withIsActive(true).withStockQuantity(stockQuantity).withCreatedBy(PosSystemApplication.getAppUser()).withUpdatedAt(Timestamp.from(Instant.now())).build();
            Integer saveTheProductInteger = productDao.addProductToDatabase(newProduct);

            if (saveTheProductInteger == 1) {
                clearAll();
                success();
            } else if (saveTheProductInteger == 0) {
                mistakesAlert(new StringBuilder("Proizvod s unesenim bar kodom već postoji!"));
            }

        } else {
            mistakesAlert(message);
        }


    }

    /**
     * Briše sve unose iz tekstualnih polja i uklanja odabir u ComboBoxima. Poziva se nakon uspješnog spremanja
     * proizvoda u bazu podataka ili klikom na gumb "Odustani".
     */

    public void clearAll() {
        taxRateComboBox.setValue(null);
        categoryComboBox.setValue(null);
        barcodeTextfield.setText("");
        nameTextField.setText("");
        priceTextField.setText("");
        discountTextField.setText("");
        stockQuantityTextField.setText("");

    }

    /**
     * Prikazuje dijaloški prozor koji se prikazuje nakon uspješnog unosa proizvoda u bazu podataka.
     */
    private void success() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Proizvod uspješno spremljen!");
        alert.setContentText("Proizvod uspješno spremljen!");
        alert.showAndWait();

    }

    /**
     * Prikazuje dijaloški prozor koji se prikazuje ukoliko su sva polja za unos novog proizvoda prazna.
     */
    private void emptyFieldsAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Krivi unos!");
        alert.setHeaderText("Unesite sva obavezna polja!");
        alert.showAndWait();
    }

    /**
     * Prikazuje dijaloški prozor s popisom grešaka koje su se dogodile prilikom unosa proizvoda.
     *
     * @param message poruka koja se prikazuje u dijaloškom prozoru
     */
    private void mistakesAlert(StringBuilder message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Neispravan unos!");
        alert.setHeaderText("Neispravan unos!");
        alert.setContentText(message.toString());
        alert.showAndWait();
    }

    /**
     * Provjerava sadrži li uneseni barkod isključivo znamenke.
     *
     * @param barcode uneseni barkod u tekstualnom polju za unos barkoda
     * @return {@code true} ako uneseni barkod sadrži samo znamenke, {@code false} ako ne
     */

    private boolean checkBarcode(String barcode) {
        for (char c : barcode.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }

        }
        return true;
    }


}
