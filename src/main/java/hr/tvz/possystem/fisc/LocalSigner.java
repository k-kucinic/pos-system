package hr.tvz.possystem.fisc;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

public final class LocalSigner {

    private static final String ALG = "SHA256withRSA";

    public static byte[] sign(byte[] input, PrivateKey key) {
        try {
            Signature signature = Signature.getInstance(ALG);
            signature.initSign(key);
            signature.update(input);
            return signature.sign();
        } catch (Exception e) {
            logger.error(e.getClass().getName() + "" + e.getMessage());
            return new byte[0];
        }
    }

    public static boolean verify(byte[] input, byte[] signature, PublicKey key) {
        try {
            Signature s = Signature.getInstance("SHA256withRSA");
            s.initVerify(key);
            s.update(input);
            return s.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }


}
