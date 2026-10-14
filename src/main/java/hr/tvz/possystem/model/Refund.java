package hr.tvz.possystem.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
/**
 * Predstavlja entitet povrata u sustavu koji je definiran prodajom na koju se odnosi,
 * vraćenim iznosom, korisnikom koji je obradio povrat,
 * razlogom povrata i vremenom kreiranja.
 */
public class Refund {
    private Integer id;
    private Sale sale;
    private BigDecimal refundAmount;
    private User processedBy;
    private String reason;
    private Timestamp createdAt;

    /**
     * Inicijalizira objekt povrata definiran sa svim dostupnim poljima.
     * @param id jedinstveni identifikacijski broj povrata
     * @param sale prodaja nad kojom se vrši povrat
     * @param refundAmount vraćeni iznos
     * @param processedBy blagajnik koji je obradio povrat
     * @param reason razlog povrata
     * @param createdAt datum i vrijeme kreiranja povrata
     */
    public Refund(Integer id, Sale sale, BigDecimal refundAmount, User processedBy, String reason, Timestamp createdAt) {
        this.id = id;
        this.sale = sale;
        this.refundAmount = refundAmount;
        this.processedBy = processedBy;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    /**
     * Inicijalizira objekt povrata bez inicijalizacije polja.
     */
    public Refund(){}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public User getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(User processedBy) {
        this.processedBy = processedBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
