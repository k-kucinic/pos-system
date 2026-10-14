package hr.tvz.possystem.database;

import hr.tvz.possystem.model.Role;
import hr.tvz.possystem.model.User;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
/**
 * Definira metode za pristup i upravljanje podacima o korisnicima u bazi podataka.
 */
public interface UserDao {
    /**
     * Dohvaća sve korisnike iz baze podataka.
     *
     * @return lista svih korisnika spremljenih u bazi
     */
    List<User> getUsersFromDataBase();

    /**
     * Dodaje novog korisnika u bazu podataka.
     *
     * @param user korisnik koji se dodaje
     */
    void addUserToDatabase(User user);

    /**
     * Dohvaća identifikacijsku oznaku korisnika na temelju korisničkog imena.
     *
     * @param username korisničko ime
     * @return {@link Optional} koji sadrži ID korisnika ako postoji; u suprotnom prazan {@code Optional}
     * @throws IOException ako dođe do pogreške pri pristupu bazi podataka
     * @throws SQLException ako dođe do pogreške pri obradi podataka korisnika
     */
    Optional<Integer> getUserIdByUsername(String username) throws SQLException, IOException;

    /**
     * Dohvaća korisnika iz baze podataka prema identifikacijskoj oznaci.
     *
     * @param id identifikacijska oznaka korisnika
     * @return {@link Optional} koji sadrži korisnika ako postoji; u suprotnom prazan {@code Optional}
     * @throws SQLException ako dođe do pogreške pri pristupu bazi podataka
     * @throws IOException ako dođe do pogreške pri obradi podataka korisnika
     */
    Optional<User> getUserById(Integer id) throws SQLException, IOException;

    /**
     * Dohvaća korisnika iz baze podataka prema korisničkom imenu.
     *
     * @param username korisničko ime
     * @return {@link Optional} koji sadrži korisnika ako postoji, u suprotnom prazan {@code Optional}
     */
    Optional<User> getUserByUsername(String username);

    /**
     * Dohvaća sve uloge iz baze podataka.
     *
     * @return lista svih uloga spremljenih u bazi
     */
    List<Role> getRolesFromDatabase();

    /**
     * Provjerava postoji li korisničko ime u bazi podataka.
     *
     * @param username korisničko ime koje se provjerava
     * @return {@code true} ako korisničko ime postoji, inače {@code false}
     */
    boolean isUsernameTaken(String username);

    /**
     * Deaktivira korisnika u bazi podataka postavljanjem statusa na neaktivan.
     *
     * @param id identifikacijska oznaka korisnika
     * @return {@code true} ako je korisnik uspješno deaktiviran; inače {@code false}
     */
    boolean deactivateUser(Integer id);

    /**
     * Služi za pretragu korisnika po korisničkom imenu.
     * @param username dio korisničkog imena
     * @return listu korisnika koji sadržavaju {@code username}
     */
    List<User> getUsersByUserName(String username);


}

