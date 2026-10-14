package hr.tvz.possystem.database;

import hr.tvz.possystem.model.Category;

import java.util.List;
import java.util.Optional;
/**
 * Definira metode za pristup i upravljanje podacima o kategorijama u bazi podataka.
 */
public interface CategoryDao {
    /**
     * Dohvaća sve kategorije iz tablice "categories" u bazi podataka. Ako dođe do pogreške, vrati praznu listu.
     *
     * @return listu kategorija iz baze podataka
     */
    List<Category> getAllCategoriesFromDatabase();

    /**
     * Zapisuje novu kategoriju u bazu podataka.
     *
     * @param category nova kategorija za unos u bazu podataka
     * @return identifikacijski broj nove kategorije ako je unos uspješan;
     * prazan {@link Optional} ako unos nije izvršen
     */
    Optional<Integer> insertNewCategoryToDatabase(Category category);

    /**
     * Dohvaća kategoriju iz baze podataka prema nazivu.
     *
     * @param categoryString naziv kategorije koji se pretražuje
     * @return {@link Category} objekt ako postoji zapis s navedenim nazivom, prazan {@link Optional} ako ne postoji
     */
    Optional<Category> getCategoryByName(String categoryString);
}
