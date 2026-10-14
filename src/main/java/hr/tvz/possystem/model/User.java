package hr.tvz.possystem.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;


/**
 * Predstavlja entitet korisnika. Sadrži informacije o identifikacijskoj oznaci, korisničkom imenu, haširanoj lozinci
 * ulozi, aktivnosti i vremenu kreiranja.
 */
public class User {
    /**
     * Pomoćna klasa za kreiranje instanci {@link User} objekta korištenjem Builder pattern-a.
     */
    public static class UserBuilder{
        private Integer id;
        private String username;
        private String passwordHash;
        private List<Role> roles;
        private boolean isActive;
        private Timestamp createdAt;
        /**
         * Postavlja identifikacijsku oznaku korisnika.
         *
         * @param id identifikacijska oznaka korisnika
         * @return trenutna instanca graditelja
         */
        public UserBuilder withId(Integer id) {
            this.id = id;
            return this;
        }

        /**
         * Postavlja korisničko ime.
         *
         * @param username korisničko ime
         * @return trenutna instanca graditelja
         */
        public UserBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        /**
         * Postavlja hash lozinke korisnika.
         *
         * @param passwordHash hash vrijednost lozinke
         * @return trenutna instanca graditelja
         */
        public UserBuilder withPasswordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        /**
         * Postavlja popis uloga korisnika.
         *
         * @param roles popis uloga
         * @return trenutna instanca graditelja
         */
        public UserBuilder withRoles(List<Role> roles) {
            this.roles = roles;
            return this;
        }

        /**
         * Postavlja status aktivacije korisnika.
         *
         * @param isActive {@code true} ako je korisnik aktivan, {@code false} ako nije
         * @return trenutna instanca graditelja
         */
        public UserBuilder withActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        /**
         * Postavlja datum i vrijeme kreiranja korisnika.
         *
         * @param createdAt datum i vrijeme kreiranja
         * @return trenutna instanca graditelja
         */
        public UserBuilder withCreatedAt(Timestamp createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * Kreira novu instancu korisnika na temelju postavljenih vrijednosti.
         *
         * @return novi objekt tipa {@link User}
         */
        public User build() {
            return new User(id, username, passwordHash, roles, isActive, createdAt);
        }

    }
    
    private Integer id;
    
    private String username;
    
    private String passwordHash;
    
    private List<Role> roles;
    private boolean isActive;
    
    private Timestamp createdAt;



    private User(Integer id, String username, String passwordHash, List<Role> roles, boolean isActive, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.roles = roles;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }
    public User(){

    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }


    @Override
    public String toString() {
        return this.username;
    }
}
