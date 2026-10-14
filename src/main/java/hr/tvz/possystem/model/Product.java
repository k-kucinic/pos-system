package hr.tvz.possystem.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Predstavja entitet proizvoda. Sadrži informacije
 */

public class Product {
    /**
     * Pomoćna klasa za kreiranje instanci {@link Product} objekta korištenjem Builder pattern-a.
     */
    public static class ProductBuilder {
        private Integer id;
        private String name;
        private String barcode;
        private Category category;
        private TaxRate taxRate;
        private BigDecimal price;
        private Integer stockQuantity;
        private BigDecimal discount;
        private boolean isActive;
        private User createdBy;
        private Timestamp createdAt;
        private Timestamp updatedAt;

        /**
         * Postavlja identifikacijsku oznaku proizvoda.
         *
         * @param id identifikacijska oznaka proizvoda
         * @return trenutna instanca graditelja
         */
        public ProductBuilder withId(Integer id) {
            this.id = id;
            return this;
        }

        /**
         * Postavlja naziv proizvoda.
         *
         * @param name naziv proizvoda
         * @return trenutna instanca graditelja
         */
        public ProductBuilder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Postavlja barkod proizvoda.
         *
         * @param barcode barkod proizvoda
         * @return trenutna instanca graditelja
         */
        public ProductBuilder withBarcode(String barcode) {
            this.barcode = barcode;
            return this;
        }

        /**
         * Postavlja kategoriju proizvoda.
         *
         * @param category kategorija proizvoda
         * @return trenutna instanca graditelja
         */
        public ProductBuilder withCategory(Category category) {
            this.category = category;
            return this;
        }

        /**
         * Postavlja poreznu stopu proizvoda.
         *
         * @param taxRate porezna stopa
         * @return trenutna instanca graditelja
         */
        public ProductBuilder withTaxRate(TaxRate taxRate) {
            this.taxRate = taxRate;
            return this;
        }

        /**
         * Postavlja cijenu proizvoda.
         *
         * @param price cijena proizvoda
         * @return trenutna instanca graditelja
         */
        public ProductBuilder withPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        /**
         * Postavlja količinu zaliha proizvoda.
         *
         * @param stockQuantity količina zaliha
         * @return trenutna instanca graditelja
         */
        public ProductBuilder withStockQuantity(Integer stockQuantity) {
            this.stockQuantity = stockQuantity;
            return this;
        }

        /**
         * Postavlja popust na proizvod.
         *
         * @param discount iznos popusta
         * @return trenutna instanca graditelja
         */
        public ProductBuilder withDiscount(BigDecimal discount) {
            this.discount = discount;
            return this;
        }

        /**
         * Postavlja status aktivacije proizvoda.
         *
         * @param isActive {@code true} ako je proizvod aktivan, {@code false} ako nije
         * @return trenutna instanca graditelja
         */
        public ProductBuilder withIsActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        /**
         * Postavlja korisnika koji je kreirao proizvod.
         *
         * @param createdBy korisnik koji je kreirao proizvod
         * @return trenutna instanca graditelja
         */
        public ProductBuilder withCreatedBy(User createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        /**
         * Postavlja datum i vrijeme kreiranja proizvoda.
         *
         * @param createdAt datum i vrijeme kreiranja
         * @return trenutna instanca graditelja
         */
        public ProductBuilder withCreatedAt(Timestamp createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * Postavlja datum i vrijeme zadnje izmjene proizvoda.
         *
         * @param updatedAt datum i vrijeme zadnje izmjene
         * @return trenutna instanca graditelja
         */
        public ProductBuilder withUpdatedAt(Timestamp updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * Kreira novu instancu proizvoda na temelju postavljenih vrijednosti.
         *
         * @return novi objekt tipa {@link Product}
         */
        public Product build() {
            return new Product(id, name, barcode, category, taxRate, price, stockQuantity, discount, isActive, createdBy, createdAt, updatedAt);
        }

    }
    private Integer id;
    private String name;
    private String barcode;
    private Category category;
    private TaxRate taxRate;
    private BigDecimal price;
    private Integer stockQuantity;
    private BigDecimal discount;
    private boolean isActive;
    private User createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    private Product(Integer id, String name, String barcode, Category category, TaxRate taxRate, BigDecimal price, Integer stockQuantity, BigDecimal discount, boolean isActive, User createdBy, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.name = name;
        this.barcode = barcode;
        this.category = category;
        this.taxRate = taxRate;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.discount = discount;
        this.isActive = isActive;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public Product(){}


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

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public TaxRate getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(TaxRate taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    @Override
    public String toString() {
        return name + " - " + barcode;
    }

}
