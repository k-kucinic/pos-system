package hr.tvz.possystem.fisc;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.UUID;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

public final class SoapXmlBuilder {
    public static final java.time.format.DateTimeFormatter F = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final String DATABASE_FILE = "conf/fiscalization.properties";
    static final Properties PROPERTIES = new Properties();

    static {
        try (var in = new java.io.FileReader(DATABASE_FILE)) {
            PROPERTIES.load(in);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private static String mapPayment(String paymentMethod) {
        if (paymentMethod == null) return "O";
        return switch (paymentMethod.toLowerCase()) {
            case "cash" -> "G";
            case "card" -> "K";
            case "bank_transfer" -> "T";
            default -> "O";
        };
    }

    final static DateTimeFormatter F_SPEC = DateTimeFormatter.ofPattern("dd.MM.yyyy'T'HH:mm:ss");

    public static String buildInvoiceRequest(FiscalInvoice inv, String maybeZki) {
        final String idPoruke = UUID.randomUUID().toString();
        final String nowHeader  = LocalDateTime.now().format(F_SPEC);
        final String datVrijeme = inv.issueDateTime().format(F_SPEC);

        final String reqId = "RacunZahtjev-" + idPoruke;

        return """
      <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:tns="http://www.apis-it.hr/fin/2012/types/f73">
        <soapenv:Header/>
        <soapenv:Body>
          <tns:RacunZahtjev Id="%s">
            <tns:Zaglavlje>
              <tns:IdPoruke>%s</tns:IdPoruke>
              <tns:DatumVrijeme>%s</tns:DatumVrijeme>
            </tns:Zaglavlje>
            <tns:Racun>
              <tns:Oib>%s</tns:Oib>
              <tns:USustPdv>%s</tns:USustPdv>
              <tns:DatVrijeme>%s</tns:DatVrijeme>
              <tns:OznSlijed>%s</tns:OznSlijed>
              <tns:BrRac>
                <tns:BrOznRac>%s</tns:BrOznRac>
                <tns:OznPosPr>%s</tns:OznPosPr>
                <tns:OznNapUr>%s</tns:OznNapUr>
              </tns:BrRac>
              <tns:IznosUkupno>%s</tns:IznosUkupno>
              <tns:NacinPlac>%s</tns:NacinPlac>
              <tns:OibOper>%s</tns:OibOper>
              %s
              <tns:NakDost>false</tns:NakDost>
            </tns:Racun>
          </tns:RacunZahtjev>
        </soapenv:Body>
      </soapenv:Envelope>
    """.formatted(
                reqId,
                idPoruke, nowHeader,
                inv.issuerOib(),
                Boolean.toString(Boolean.parseBoolean(PROPERTIES.getProperty("fiscal.inVat","true"))),
                datVrijeme,
                PROPERTIES.getProperty("fiscal.sequence").toUpperCase(),
                inv.invoiceNumber(),
                System.getProperty("fiscal.posLabel", PROPERTIES.getProperty("fiscal.posLabel","POS01")),
                System.getProperty("fiscal.deviceNumber", PROPERTIES.getProperty("fiscal.deviceNumber","1")),
                inv.totalAmount().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                mapPayment(inv.paymentMethod()),
                java.util.Objects.requireNonNullElse(PROPERTIES.getProperty("fiscal.operatorOib"), inv.issuerOib()),
                (maybeZki == null || maybeZki.isBlank() ? "" : "<tns:ZastKod>"+ maybeZki +"</tns:ZastKod>")
        );
    }

}
