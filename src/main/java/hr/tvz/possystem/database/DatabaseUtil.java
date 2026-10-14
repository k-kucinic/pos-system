package hr.tvz.possystem.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Služi za stvaranje konekcije sa bazom podataka
 */
public class DatabaseUtil {

    public static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    private static final String DATABASE_FILE = "conf/database.properties";
    private static final Properties PROPERTIES = new Properties();

    static {
        try (var in = new java.io.FileReader(DATABASE_FILE)) {
            PROPERTIES.load(in);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Uspostavlja vezu s bazom podataka sa parametrima definiranim u datoteci database.properties.
     * Koristi se u svim metodama u aplikaciji koje uspostavljaju vezu s bazom podataka.
     * @return otvorena {@link Connection}  spremna za rad s bazom podataka
     * @throws SQLException ako se ne može uspostaviti veza s bazom
     * @throws IOException ako dođe do pogreške pri učitavanju konfiguracijske datoteke
     */

    public static Connection connectToDatabase() throws SQLException, IOException {
        String databaseUrl = PROPERTIES.getProperty("databaseUrl");
        String username = PROPERTIES.getProperty("username");
        String password = PROPERTIES.getProperty("password");
        return DriverManager.getConnection(databaseUrl, username, password);
    }



}
