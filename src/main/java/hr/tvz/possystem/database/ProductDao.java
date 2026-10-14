package hr.tvz.possystem.database;

import hr.tvz.possystem.exceptions.InsufficientStockException;
import hr.tvz.possystem.model.Product;
import hr.tvz.possystem.model.TaxRate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;

/**
 * Definira metode za pristup i upravljanje podacima o proizvodima u bazi podataka.
 */
public interface ProductDao {
    /**
     * Služi za dohvat svih proizvoda iz baze podataka.
     *
     * @return listu svih proizvoda iz baze podataka.
     */
    List<Product> getAllProductsFromDatabase();

    /**
     * Služi pronalaženju liste proizvoda iz baze podataka na temelju barkoda proizvoda.
     *
     * @param partOfBarcode dio barkoda prozivoda koji se pretražuje
     * @return listu proizovda koja u svom barkodu sadrži dio barkoda koji se pretražuje
     */
    List<Product> productNamesFromBarcode(String partOfBarcode);

    /**
     * Služi pronalaženju jednog proizvoda iz bazte podataka na temelju njegovog barkoda.
     *
     * @param barcode barkod proizvoda koji se pretražuje
     * @return proizvod kojemu je barkod isti onomu koji se pretražuje, ako ga nema vraća se prazna vrijednost
     */
    Optional<Product> productFromBarcode(String barcode);

    /**
     * Služi dohvaćanju proizvoda iz baze podataka na temelju njegovog id-a.
     *
     * @param id broj koji predstavlja identifikacijski broj potencijalnog proizvoda
     * @return proizvod sa id-om ukoliko postoji, ako ne onda se vraća Optional.Empty()
     */
    Optional<Product> getProductById(Integer id);

    /**
     * Smanjuje zalihe proizvoda u bazi podataka. Koristi se pri kreiranju nove prodaje.
     *
     * @param quantity   količina za umanjenje zaliha
     * @param product    proizvod kojem se smanjuje količina
     * @param connection veza prema bazi podataka koja se koristi za izvršavanje upita
     * @throws SQLException               ako dođe do pogreške pri komunikaciji s bazom podataka
     * @throws InsufficientStockException ako je količina proizvoda u bazi manja od vrijednosti parametra {@code quantity}
     */
    void lowerStockQuantity(Integer quantity, Product product, Connection connection) throws SQLException, InsufficientStockException;

    /**
     * Povećava zalihe proizvoda u bazi podataka. Koristi se pri povratu artikla.
     *
     * @param quantity   količina za povećanje zaliha
     * @param product    proizvod kojem se vraćaju zalihe
     * @param connection veza prema bazi podataka koja se koristi za izvršavanje upita
     * @throws SQLException ako dođe do pogreške pri komunikaciji s bazom podataka
     */
    void restoreStockQuantityAfterRefund(Integer quantity, Product product, Connection connection) throws SQLException;

    /**
     * Zapisuje novi proizvod u bazu podataka.
     *
     * @param product proizvod koji se unosi u bazu podataka
     * @return
     * 1 ako je unos uspješan,
     * 0 ako dođe do SQLIntegrityConstraintViolationException zbog postojećeg barkoda ili
     * 2 ako dođe do druge pogreške
     *
     */
    Integer addProductToDatabase(Product product);

    /**
     * Deaktivira proizvod u bazi podataka postavljanjem vrijednosti stupca {@code is_active} na 0.
     *
     * @param id identifikacijska oznaka proizvoda koji se deaktivira
     */
    void setProductAsInactive(Integer id);

    /**
     * Ažurira podatke postojećeg proizvoda u bazi podataka.
     *
     * @param product proizvod s novim vrijednostima koje zamjenjuju postojeće podatke u bazi
     */
    void editProduct(Product product);

    /**
     * Dodaje zadanu količinu zaliha postojećem proizvodu u bazi podataka.
     *
     * @param id            identifikacijska oznaka proizvoda kojem se dodaju zalihe
     * @param stockQuantity količina zaliha koja se dodaje
     * @return {@code true} ako je nova količina uspješno spremljena, {@code false} ako dođe do pogreške pri spremanju
     */
    boolean addQuantityToProduct(Integer id, Integer stockQuantity);

    /**
     * Provjerava dostupnost tražene količine zaliha za određeni proizvod.
     *
     * @param productId identifikacijska oznaka proizvoda koji se provjerava
     * @param quantity  potrebna količina proizvoda
     * @return {@code true} ako je količina zaliha proizvoda jednaka ili veća od {@code quantity}, inače {@code false}
     */
    boolean checkQuantity(Integer productId, Integer quantity);
}
