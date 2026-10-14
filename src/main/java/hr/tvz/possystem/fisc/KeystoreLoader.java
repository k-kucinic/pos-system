package hr.tvz.possystem.fisc;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

public final class KeystoreLoader {
    public record KeyMaterial(PrivateKey privateKey, X509Certificate certificate) {
    }

    /**
     * Učitaj iz .p12 s diska
     */
    public static KeyMaterial loadPkcs12(String path, String storePass, String alias, String keyPass) {
        try (InputStream is = new FileInputStream(path)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(is, storePass.toCharArray());

            var key = keyStore.getKey(alias, keyPass.toCharArray());
            if (!(key instanceof PrivateKey pk)) {
                throw new IllegalStateException("Alias '" + alias + "' nema PrivateKey");
            }
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
            if (cert == null) throw new IllegalStateException("Nema certifikata za alias '" + alias + "'");

            return new KeyMaterial(pk, cert);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }


    private KeystoreLoader() {
    }
}