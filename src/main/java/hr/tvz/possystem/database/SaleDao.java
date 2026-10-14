package hr.tvz.possystem.database;

import hr.tvz.possystem.model.Sale;
import hr.tvz.possystem.model.SaleItem;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
/**
 * Definira metode za pristup i upravljanje podacima o računima u bazi podataka.
 */
public interface SaleDao {
    /**
     * Sprema novi račun za preostale artikle u bazu podataka. Ukoliko je pisač spojen, ispisuje se novi račun sa
     * bilješkom o povratu.
     *
     * @param remainingProducts preostali artikli nakon povrata
     * @param total ukupna cijena svih artikala
     * @param notes bilješke o povratu
     * @param connection veza s bazom podataka proslijeđena iz metode za kreiranje novog povrata
     */
    void createSaleAfterRefund(List<SaleItem> remainingProducts, BigDecimal total, String notes, Connection connection);

    /**
     * Kreira novi račun u bazi podataka i smanjuje zalihe za sve kupljene artikle.
     *
     * @param saleItems lista kupljenih artikala koji se evidentiraju na računu
     * @param total ukupni iznos računa
     * @param notes bilješka koja se pridružuje računu radi evidencije prodaje
     */
    void createSale(ObservableList<SaleItem> saleItems, BigDecimal total, String notes);


    /**
     * Dohvaća račun iz baze podataka na temelju identifikacijske oznake.
     *
     * @param saleId identifikacijska oznaka računa
     * @return račun ako postoji, inače prazan {@code Optional}
     */
    Optional<Sale> getSaleById(Integer saleId);

    /**
     * Dohvaća artikle povezane s određenim računom.
     *
     * @param sale račun za koji se dohvaćaju artikli
     * @return lista artikala povezanih s računom
     */
    List<SaleItem> getSaleItemsBySaleId(Sale sale);

    /**
     * Dohvaća sve račune iz baze podataka.
     *
     * @return lista svih računa spremljenih u bazi
     */
    List<Sale> getAllSalesFromDatabase();

    /**
     * Dohvaća sve račune iz baze koji su kreirani unutar zadanog vremenskog raspona.
     *
     * @param dateFrom početni datum
     * @param dateTo krajnji datum
     * @return lista računa koji su kreirani između navedenih datuma
     */
    List<Sale> getSalesByDate(LocalDate dateFrom, LocalDate dateTo);


    /**
     * Dohvaća artikle iz baze koji su vraćeni na temelju identifikacijskog broja prodaje.
     *
     * @param sale identifikacijska oznaka prodaje
     * @return listu artikala koji su vraćeni
     */
    List<SaleItem> getSaleItemsByRefund(Sale sale);
}
