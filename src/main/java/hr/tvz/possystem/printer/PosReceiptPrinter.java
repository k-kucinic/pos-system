package hr.tvz.possystem.printer;

import hr.tvz.possystem.controllers.admin.refunds.RefundHistoryController;
import hr.tvz.possystem.database.SaleDaoImpl;
import hr.tvz.possystem.model.Sale;
import hr.tvz.possystem.model.SaleItem;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static hr.tvz.possystem.database.DatabaseUtil.logger;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * Pomoćna klasa za ispis računa na POS termalni pisač širine 58 mm.
 * Sadrži statičke metode za generiranje teksta računa i ispis barkoda prema ESC/POS standardu.
 */
public class PosReceiptPrinter {
    public static boolean isPrinterConnected(String printerName) {
        try {
            PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
            for (PrintService service : services) {
                if (service.getName().equalsIgnoreCase(printerName)) {
                    DocPrintJob job = service.createPrintJob();
                    byte[] testBytes = {0x1B, '@'};
                    Doc doc = new SimpleDoc(testBytes, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
                    job.print(doc, new HashPrintRequestAttributeSet());
                    return true;
                }

            }
        } catch (Exception e) {
            System.out.println("Printer not connected: " + e.getMessage());
        }
        return false;
    }
    /**
     * Prikazuje dijalog za unos administratorske lozinke i provjerava točnost lozinke.
     * {@link PasswordDialog}
     *
     * @return {@code true} ako je lozinka točna, {@code false} ako nije
     */



    /**
     * Ispisuje račun na termalnom POS pisaču. Pisač radi sa papirom širine 58 mm.
     * Na računu su prikazani svi artikli, količine, cijene, ukupni iznos, popusti i
     * način plaćanja. Jedinstveni identifikator računa ispisuje se u obliku barkoda.
     *
     * @param sale prodaja čiji se podaci ispisuju na računu
     * @throws Exception ako dođe do greške prilikom ispisa na pisač
     */
    public static void printTheReceipt(Sale sale) throws Exception {
        String printerName = "POS-58(copy of 1)";
        PrintService printer = Arrays.stream(PrintServiceLookup.lookupPrintServices(null, null))
                .filter(ps -> ps.getName().equalsIgnoreCase(printerName))
                .findFirst()
                .orElse(null);
        if (printer == null) return;

        Charset cp437 = Charset.forName("CP437");
        Charset latin2 = Charset.forName("ISO-8859-2");

        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        String barcodeData = String.format("%010d", sale.getId());

        BigDecimal tax25 = sale.calculateTax(new BigDecimal(25));
        BigDecimal tax10 = sale.calculateTax(new BigDecimal(13));
        BigDecimal tax5 = sale.calculateTax(new BigDecimal(5));
        BigDecimal tax = tax5.add(tax10).add(tax25);
        BigDecimal netAmount = sale.getTotalAmount().subtract(tax);


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(0x1B);
        outputStream.write('@');

        outputStream.write("TRGOVINA\n".getBytes(cp437));
        outputStream.write(("Blagajnik: " + sale.getUser().getUsername() + "\n").getBytes(cp437));
        outputStream.write(("Račun br.: " + barcodeData + "\n").getBytes(latin2));
        outputStream.write("-----------------------------\n".getBytes(cp437));
        String labelsRow = String.format("%-12s %4s %-6s %7s\n",
                "Artikl",
                "Kol",
                "Cijena",
                "Iznos");
        outputStream.write(labelsRow.getBytes(latin2));

        for (SaleItem si : sale.getSaleItems()) {
            String name = si.getProduct().getName();
            if (name.length() > 12) name = name.substring(0, 11);

            BigDecimal unit = si.getUnitPrice();
            BigDecimal total = unit.multiply(BigDecimal.valueOf(si.getQuantity()));
            BigDecimal discount = si.getProduct().getDiscount();

            String row = String.format("%-12s %2dx %-7s %7s\n",
                    name,
                    si.getQuantity(),
                    decimalFormat.format(unit),
                    decimalFormat.format(total));
            outputStream.write(row.getBytes(latin2));

            if (discount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountTotal = discount.multiply(BigDecimal.valueOf(si.getQuantity()));
                int lineWidth = 32;
                String discountText = "-" + decimalFormat.format(discountTotal);
                String discountRow = String.format("%" + lineWidth + "s", discountText);
                outputStream.write(discountRow.getBytes(latin2));
                outputStream.write("\n".getBytes(latin2));

            }


        }


        outputStream.write("-----------------------------\n".getBytes(cp437));
        outputStream.write(String.format("Osnovica:        %6s EUR\n", decimalFormat.format(netAmount)).getBytes(cp437));
        if (tax5.compareTo(BigDecimal.ZERO) != 0) {
            String line = String.format("PDV 5%%:          %6s EUR\n", decimalFormat.format(tax5));
            outputStream.write(line.getBytes(cp437));
        }
        if (tax10.compareTo(BigDecimal.ZERO) != 0) {
            String line = String.format("PDV 13%%:         %6s EUR\n", decimalFormat.format(tax10));
            outputStream.write(line.getBytes(cp437));
        }
        if (tax25.compareTo(BigDecimal.ZERO) != 0) {
            String line = String.format("PDV 25%%:         %6s EUR\n", decimalFormat.format(tax25));
            outputStream.write(line.getBytes(cp437));
        }


        outputStream.write(new byte[]{0x1B, 0x45, 0x01});


        outputStream.write(String.format("Ukupno:          %6s EUR\n",
                decimalFormat.format(sale.getTotalAmount())).getBytes(cp437));

        outputStream.write(new byte[]{0x1B, 0x45, 0x00});


        outputStream.write("-----------------------------\n".getBytes(cp437));
        if (sale.getPaymentMethod().contains("cash")) {
            sale.setPaymentMethod("novčanice");
        }
        outputStream.write(("Način plaćanja: " + sale.getPaymentMethod() + "\n").getBytes(latin2));
        outputStream.write("-----------------------------\n".getBytes(latin2));
        barcodeOutput(barcodeData, outputStream);
        outputStream.write(0x00);
        outputStream.write(new byte[]{0x0A});
        outputStream.write(new byte[]{0x1B, '@'});
        outputStream.write(new byte[]{0x1B, 0x61, 0x01});
        outputStream.write("\n".getBytes(cp437));
        if (sale.getNotes() != null) {
            outputStream.write(sale.getNotes().getBytes(latin2));
        }
        String jir = SaleDaoImpl.getJirBySaleId(sale.getId());
        if (jir != null) {
            outputStream.write("jir:\n".getBytes(latin2));
            outputStream.write(jir.getBytes(latin2));
            outputStream.write("\n".getBytes(latin2));
            String iznosForQr = decimalFormat.format(sale.getTotalAmount()).replace(',', '.');
            String qrData = "JIR=" + jir + "|Datum:" + sale.getCreatedAt().format(RefundHistoryController.DATE_FORMATTER) + "|Vrijeme:" + sale.getCreatedAt().format(RefundHistoryController.TIME_FORMATTER) + "|Iznos=" + iznosForQr;
            outputStream.write(new byte[]{0x1B, '@'});
            outputStream.write(new byte[]{0x1C, 0x2E});
            outputStream.write(new byte[]{0x1B, 0x21, 0x00});
            outputStream.write(new byte[]{0x1B, 0x61, 0x01});


            try {
                printQrWithEscPos(qrData, outputStream);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

        outputStream.write("Hvala na kupnji!\n".getBytes(cp437));
        outputStream.write(("Datum: " + sale.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm")) + "\n\n").getBytes(cp437));
        outputStream.write("\n\n\n\n".getBytes());
        byte[] data = outputStream.toByteArray();
        DocPrintJob job = printer.createPrintJob();
        job.print(new SimpleDoc(data, DocFlavor.BYTE_ARRAY.AUTOSENSE, null), null);
        outputStream.close();
    }

    /**
     * Dodaje barkod u ispis računa koristeći ESC/POS naredbe.
     *
     * @param barcodeData  jedinstveni identifikator računa računa formatiran na 10 znamenki
     * @param outputStream tok podataka u koji se šalju naredbe za ispis
     * @throws IOException ako dođe do pogreške prilikom pisanja u izlazni tok
     */
    private static void barcodeOutput(String barcodeData, ByteArrayOutputStream
                                      outputStream) throws IOException {
        outputStream.write(new byte[]{0x1B, 0x40});
        outputStream.write(new byte[]{0x1B, 0x61, 0x01});
        outputStream.write(new byte[]{0x1D, 0x48, 0x02});
        outputStream.write(new byte[]{0x1D, 0x68, 0x50});
        outputStream.write(new byte[]{0x1D, 0x77, 0x02});
        outputStream.write(new byte[]{0x1D, 0x6B, 0x49});
        outputStream.write((byte) (barcodeData.length() + 2));
        outputStream.write('{');
        outputStream.write('B');
        outputStream.write(barcodeData.getBytes(StandardCharsets.US_ASCII));
        outputStream.write(new byte[]{0x1B, 0x61, 0x00});
    }


    /**
     * Ispisuje QR kod na POS termalnom pisaču koristeći ESC/POS naredbe.
     * Zpisuje sadržaj QR koda u pisač preko predanog izlaznog toka.
     *
     * @param data tekstualni sadržaj koji se kodira u QR kod
     * @param outputStream   izlazni tok povezan s POS pisačem
     * @throws IOException ako dođe do pogreške pri pisanju u izlazni tok
     */
    private static void printQrWithEscPos(String data, OutputStream outputStream) throws IOException {
        outputStream.write(new byte[]{0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00});
        outputStream.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x04});
        outputStream.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x31});

        byte[] payload = data.getBytes(ISO_8859_1);
        int len = payload.length + 3;
        byte pL = (byte) (len & 0xFF);
        byte pH = (byte) ((len >> 8) & 0xFF);
        outputStream.write(new byte[]{0x1D, 0x28, 0x6B, pL, pH, 0x31, 0x50, 0x30});
        outputStream.write(payload);
        outputStream.write(new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30});

        outputStream.flush();
    }


}

