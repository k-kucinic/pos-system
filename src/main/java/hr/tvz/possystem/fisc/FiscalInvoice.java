package hr.tvz.possystem.fisc;

import hr.tvz.possystem.model.Sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Properties;

import static hr.tvz.possystem.database.DatabaseUtil.logger;

public record FiscalInvoice(String issuerOib,
                            String deviceLabel,
                            String invoiceNumber,
                            LocalDateTime issueDateTime,
                            BigDecimal totalAmount,
                            String paymentMethod) {
    private static final String DATABASE_FILE = "conf/fiscalization.properties";
    static final Properties PROPERTIES = new Properties();

    static {
        try (var in = new java.io.FileReader(DATABASE_FILE)) {
            PROPERTIES.load(in);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
    public static FiscalInvoice fromSale(Sale sale) {
        return new FiscalInvoice(
                PROPERTIES.getProperty("fiscal.issuerOib"),
                PROPERTIES.getProperty("fiscal.deviceLabel"),
                String.valueOf(sale.getId()),
                sale.getCreatedAt(),
                sale.getTotalAmount(),
                sale.getPaymentMethod()
        );
    }



}
