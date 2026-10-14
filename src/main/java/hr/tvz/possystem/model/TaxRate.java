package hr.tvz.possystem.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;
/**
 * Predstavlja poreznu stopu koja se može primijeniti na proizvod.
 * Sadrži podatke o identifikacijskoj oznaci, nazivu, postotku i datumu kreiranja.
 */
public class TaxRate {
    private Integer id;
    private String name;
    private BigDecimal rate;
    private Timestamp createdAt;

    /**
     * Kreira novu instancu porezne stope sa svim zadanim podacima.
     *
     * @param id identifikacijska oznaka porezne stope
     * @param name naziv porezne stope
     * @param rate postotak porezne stope
     * @param createdAt datum i vrijeme kreiranja
     */
    public TaxRate(Integer id, String name, BigDecimal rate, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.rate = rate;
        this.createdAt = createdAt;
    }

    /**
     * Kreira novu instancu porezne stope bez podatka o datumu kreiranja.
     *
     * @param id identifikacijska oznaka porezne stope
     * @param name naziv porezne stope
     * @param rate postotak porezne stope
     */
    public TaxRate(Integer id, String name, BigDecimal rate) {
        this.id = id;
        this.name = name;
        this.rate = rate;
    }

    /**
     * Kreira praznu instancu porezne stope.
     */
    public TaxRate() {
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }


    @Override
    public String toString() {
        return this.name;
    }
}
