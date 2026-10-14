package hr.tvz.possystem.database;

import hr.tvz.possystem.model.TaxRate;

import java.util.List;
/**
 * Definira metode za pristup i upravljanje podacima o poreznim stopama u bazi podataka.
 */
public interface TaxRateDao {
    /**
     * Dohvaća sve zapise iz tablice {@code tax_rates} u bazi podataka.
     *
     * @return lista objekata tipa {@link TaxRate}, ako nema zapisa vraća praznu listu
     */
    List<TaxRate> getTaxRatesFromDatabase();
}
