package hr.tvz.possystem.fisc;

import java.math.BigDecimal;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

public final class ZkiPlaceholder {
    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit((b & 0xF), 16));
        }
        return sb.toString();
    }

    /**
     * Deterministički ZKI (32 hexa),  služi samo za test tokova bez privatnog ključa.
     * Ulaz kombinira tipična polja koja ulaze u pravi ZKI; hashiramo SHA-256 i uzmemo prvih 32 hexa.
     */
    public static String generatePlaceholder(String oib, String datVrijemeIso,
                                             String brOznRac, String oznPosPr, String oznNapUr,
                                             BigDecimal iznosUkupno) {
        String seed = String.join("|",
                oib == null ? "" : oib.trim(),
                datVrijemeIso == null ? "" : datVrijemeIso.trim(),
                brOznRac == null ? "" : brOznRac.trim(),
                oznPosPr == null ? "" : oznPosPr.trim(),
                oznNapUr == null ? "" : oznNapUr.trim(),
                iznosUkupno == null ? "" : iznosUkupno.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
        );
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(seed.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return toHex(hash).substring(0, 32);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "e4d909c290d0fb1ca068ffaddf22cbd0";
        }
    }
}
