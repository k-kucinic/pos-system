package hr.tvz.possystem.model;

import java.math.BigDecimal;

/**
 * Predstavlja stavku računa koja sadrži podatke o proizvodu, njegovoj količini, prodaji.
 */
public class SaleItem {
    /**
     * Pomoćna klasa za kreiranje instanci {@link SaleItem} objekta korištenjem Builder pattern-a.
     */
    public static class SaleItemBuilder {
        private Integer id;
        private Sale sale;
        private Product product;
        private int quantity;
        private BigDecimal unitPrice;
        private boolean isRefunded;
        private BigDecimal totalPrice;
        private Integer originalQuantity;
        private Integer idBeforeRefund;

        /**
         * Postavlja proizvod za ovu stavku računa.
         *
         * @param product proizvod koji se dodaje u stavku računa
         * @return trenutna instanca graditelja
         */
        public SaleItemBuilder withProduct(Product product) {
            this.product = product;
            return this;
        }

        /**
         * Postavlja količinu proizvoda u stavci računa.
         *
         * @param quantity količina proizvoda
         * @return trenutna instanca graditelja
         */
        public SaleItemBuilder withQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        /**
         * Postavlja jediničnu cijenu proizvoda.
         *
         * @param unitPrice jedinična cijena proizvoda
         * @return trenutna instanca graditelja
         */
        public SaleItemBuilder withUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        /**
         * Postavlja status povrata za ovu stavku.
         *
         * @param isRefunded {@code true} ako je stavka vraćena, {@code false} inače
         * @return trenutna instanca graditelja
         */
        public SaleItemBuilder withRefunded(boolean isRefunded) {
            this.isRefunded = isRefunded;
            return this;
        }

        /**
         * Postavlja ukupnu cijenu za ovu stavku računa.
         *
         * @param totalPrice ukupna cijena stavke
         * @return trenutna instanca graditelja
         */
        public SaleItemBuilder withTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }

        /**
         * Kreira novu instancu stavke računa na temelju postavljenih vrijednosti.
         *
         * @return novi objekt tipa {@link SaleItem}
         */
        public SaleItem build() {
            return new SaleItem(id, sale, product, quantity, unitPrice, isRefunded, totalPrice, originalQuantity, idBeforeRefund);
        }

    }


    private Integer id;
    private Sale sale;
    private Product product;
    private int quantity;
    private BigDecimal unitPrice;
    private boolean isRefunded;

    private BigDecimal totalPrice;
    private Integer originalQuantity;
    private Integer idBeforeRefund;

    public SaleItem(Integer id, Sale sale, Product product, int quantity, BigDecimal unitPrice, boolean isRefunded, BigDecimal totalPrice, Integer originalQuantity, Integer idBeforeRefund) {
        this.id = id;
        this.sale = sale;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.isRefunded = isRefunded;
        this.totalPrice = totalPrice;
        this.originalQuantity = originalQuantity;
        this.idBeforeRefund=idBeforeRefund;
    }

    public SaleItem() {

    }


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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public boolean isRefunded() {
        return isRefunded;
    }

    public void setRefunded(boolean refunded) {
        isRefunded = refunded;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Integer getOriginalQuantity() {
        return originalQuantity;
    }

    public void setOriginalQuantity(Integer originalQuantity) {
        this.originalQuantity = originalQuantity;
    }

    public SaleItem cloneWithQuantity(Integer refundQuantity) {
        SaleItem copy = new SaleItem.SaleItemBuilder().build();

        copy.setProduct(this.getProduct());
        copy.setQuantity(refundQuantity);
        copy.setUnitPrice(this.getUnitPrice());
        copy.setSale(this.getSale());
        copy.setOriginalQuantity(refundQuantity);
        copy.setIdBeforeRefund(this.getId());
        copy.setRefunded(true);
        return copy;

    }

    public Integer getIdBeforeRefund() {
        return idBeforeRefund;
    }

    public void setIdBeforeRefund(Integer idBeforeRefund) {
        this.idBeforeRefund = idBeforeRefund;
    }
}
