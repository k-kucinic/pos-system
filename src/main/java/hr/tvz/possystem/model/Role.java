package hr.tvz.possystem.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Predstavlja ulogu korisnika u sustavu. Sadrži identifikacijsku oznaku i naziv uloge.
 */
public class Role {
    private Integer id;
    private String name;


    /**
     * Kreira novu instancu uloge s navedenim identifikatorom i nazivom.
     *
     * @param id identifikacijska oznaka uloge
     * @param name naziv uloge
     */
    public Role(Integer id, String name) {
        this.id = id;
        this.name = name;
    }


    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    /**
     * {@inheritDoc}
     */
    public String toString() {
        if(this.name==null){
            return "";
        }

        if(this.name.compareTo("cashier")==0){
            return "blagajnik";
        } else{
            return this.name;
        }
    }
}
