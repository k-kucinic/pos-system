package hr.tvz.possystem.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Predstavlja entitet kategorije proizvoda, koja je definirana id-om, nazivom, vremenom kreiranja i vremenom promjene.
 */
public class Category {
    private Integer id;
    private String name;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    /**
     * Služi za inicijalizaciju cijelog objekta kategorije.
     * @param id jedinstveni identifikator kategorije
     * @param name naziv kategorije
     * @param createdAt trenutak kreiranja kategorije
     * @param updatedAt trenutak ažuriranja kategorije
     */
    public Category(Integer id, String name, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Služi za inicijalizaciju kategorije sa podacima o id-u i nazivu.
     * @param id jedinstveni identifikator kategorije
     * @param name naziv kategorije
     */
    public Category(Integer id, String name) {
        this.id = id;
        this.name = name;

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

    /**
     * {@return a string representation of the object}

     */
    @Override
    public String toString() {
        return this.name;
    }
}
