package hr.tvz.possystem.authentication;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Predstavlja entitet koji sadrži statičke metode koje se pozivaju pri kreiranju i prijavi korisnika u aplikaciju.
 * Omogućava provjeru ispravnosti lozinki i haširanje lozinki pomoću BCrypt algoritma.
 */
public class PasswordHashing {

    /**
     * Pretvara unesenu lozinku u haširanu lozinku koja se zapisuje u bazu podataka.
     * Koristi se pri kreiranju novog korisnika.
     * Koristi BCrypt algoritam za provjeru ispravnosti lozinke.
     *
     * @param passwordNormal lozinka prije haširanja
     * @return haširanu lozinku spremnu za upisivanje u bazu
     */
    public static String getHashedPassword(String passwordNormal) {
        return BCrypt.hashpw(passwordNormal, BCrypt.gensalt());
    }

    /**
     * Provjerava odgovara li unesena lozinka kriptiranoj lozinci iz baze podataka.
     * Koristi BCrypt algoritam i njegovu metodu checkpw.
     *
     * @param passwordNormal unesena lozinka od strane korisnika
     * @param hashedPassword haširana lozinka iz baze
     * @return {@code true} ako lozinka odgovara, {@code false} ako lozinka ne odgovara
     * @see BCrypt
     */

    public static boolean checkIfPasswordIsCorrect(String passwordNormal, String hashedPassword) {
        return BCrypt.checkpw(passwordNormal, hashedPassword);
    }
}
