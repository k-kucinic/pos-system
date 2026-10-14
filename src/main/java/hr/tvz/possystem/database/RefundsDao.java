package hr.tvz.possystem.database;

import hr.tvz.possystem.model.Refund;
import hr.tvz.possystem.model.SaleItem;

import java.time.LocalDate;
import java.util.List;
/**
 * Definira metode za pristup i upravljanje podacima o povratima u bazi podataka.
 */
public interface RefundsDao {
    /**
     * Obrađuje povrat artikala i bilježi ga u bazu podataka.
     *
     * Unosi novi zapis o povratu u tablicu {@code refunds}, ažurira originalni račun kao vraćen, vraća zalihe za
     * odabrane artikle i stvara novi račun za artikle koji nisu vraćeni. Sve operacije izvršavaju se unutar iste
     * transakcije.
     *
     * @param refund povrat koji se unosi u bazu
     * @param saleItems lista artikala koji su ostali nakon povrata
     * @param refundedItems lista artikala koji se vraćaju
     */
    void insertNewRefundIntoDatabase(Refund refund, List<SaleItem> saleItems, List<SaleItem> refundedItems);

    /**
     * Dohvaća iz baze podataka sve povrate iz tablice {@code refunds}
     * @return listu svih porata iz baze podataka
     */
    List<Refund> getAllRefundsFromDatabase();

    /**
     * Dohvaća sve povrate iz baze podataka koji su kreirani unutar zadanog vremenskog raspona.
     *
     * @param dateFrom početni datum raspona
     * @param dateTo krajnji datum raspona
     * @return listu povrata iz baze podataka koji su kreirani između {@code dateFrom} i {@code datoTo}
     */
    List<Refund> getRefundsByDate(LocalDate dateFrom, LocalDate dateTo);


}
