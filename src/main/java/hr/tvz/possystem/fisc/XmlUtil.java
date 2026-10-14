package hr.tvz.possystem.fisc;

import org.w3c.dom.Document;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public final class XmlUtil {
    /** Pretvara DOM Document u UTF‑8 String bez dodavanja nepotrebnih razmaka. */
    public static String serialize(Document doc) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            t.setOutputProperty(OutputKeys.INDENT, "no");
            StringWriter sw = new StringWriter();
            t.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException("XML serialize failed: " + e.getMessage(), e);
        }
    }

    private XmlUtil() {}
    public static String buildLocalSignatureSeed(FiscalInvoice fi, String oznPosPr, String oznNapUr) {
        return String.join("|",
                fi.issuerOib(),
                fi.issueDateTime().format(SoapXmlBuilder.F),
                fi.invoiceNumber(),
                oznPosPr,
                oznNapUr,
                fi.totalAmount().setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
        );
    }

}