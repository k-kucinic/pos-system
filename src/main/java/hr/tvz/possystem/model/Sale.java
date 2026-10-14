package hr.tvz.possystem.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Predstavlja entitet prodaje. Sadrži podatke o datumu i vremenu prodaje, ukupnom iznosu, načinu plaćanja,
 * korisniku koji je obradio prodaju, statusu povrata, datumu kreiranja i popisu prodanih artikala.
 */
public class Sale {
    private Integer id;
    private LocalDateTime dateTime;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private User user;
    private String notes;
    private boolean isRefunded;
    private LocalDateTime createdAt;
    private List<SaleItem> saleItems;


    /**
     * Kreira novu instancu računa sa svim zadanim podacima.
     *
     * @param id            identifikacijska oznaka računa
     * @param dateTime      datum i vrijeme prodaje
     * @param totalAmount   ukupan iznos računa
     * @param paymentMethod način plaćanja
     * @param user          korisnik koji je obradio prodaju
     * @param isRefunded    označava je li račun refundiran
     * @param createdAt     datum i vrijeme kreiranja računa
     * @param saleItems     popis artikala koji se nalaze na računu
     */
    public Sale(Integer id, LocalDateTime dateTime, BigDecimal totalAmount, String paymentMethod, User user, boolean isRefunded, LocalDateTime createdAt, List<SaleItem> saleItems) {
        this.id = id;
        this.dateTime = dateTime;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.user = user;
        this.isRefunded = isRefunded;
        this.createdAt = createdAt;
        this.saleItems = saleItems;
    }

    /**
     * Kreira praznu instancu računa.
     */
    public Sale() {

    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<SaleItem> getSaleItems() {
        return saleItems;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isRefunded() {
        return isRefunded;
    }

    public void setRefunded(boolean refunded) {
        isRefunded = refunded;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setSaleItems(List<SaleItem> saleItems) {
        this.saleItems = saleItems;
    }



    private BigDecimal calculateTotal(SaleItem saleItem) {
        return saleItem.getUnitPrice().multiply(BigDecimal.valueOf(saleItem.getQuantity()));
    }

    public BigDecimal calculateTax(BigDecimal taxRate) {
        BigDecimal totalTaxForRate = BigDecimal.ZERO;
        for (SaleItem saleItem : saleItems) {
            if (saleItem.getProduct().getTaxRate().getRate().compareTo(taxRate) == 0) {
                BigDecimal itemTotal = calculateTotal(saleItem);
                BigDecimal itemTaxRate = saleItem.getProduct().getTaxRate().getRate().multiply(BigDecimal.valueOf(0.01));
                BigDecimal itemTax = itemTotal.multiply(itemTaxRate);
                totalTaxForRate = totalTaxForRate.add(itemTax);
            }
        }
        return totalTaxForRate;
    }
}
