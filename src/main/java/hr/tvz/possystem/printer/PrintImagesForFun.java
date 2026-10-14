package hr.tvz.possystem.printer;

import javax.imageio.ImageIO;
import javax.print.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;

public class PrintImagesForFun {

    public static void printImageEscPos(File imageFile, int maxWidthPx) throws Exception {
        String printerName = "POS-58(copy of 1)";
        PrintService printer = Arrays.stream(PrintServiceLookup.lookupPrintServices(null, null))
                .filter(ps -> ps.getName().equalsIgnoreCase(printerName))
                .findFirst()
                .orElse(null);

        if (printer == null) return;

        BufferedImage src = ImageIO.read(imageFile);
        if (src == null) throw new IllegalArgumentException("Ne mogu učitati sliku: " + imageFile);

        BufferedImage scaled = scaleToWidth(src, maxWidthPx);
        // Pretvori u 1-bit (crno-bijelo) s thresholdom
        boolean[][] bw = toBlackWhite(scaled, 160); // 0-255, niže = tamnije (više crnog)

        byte[] payload = buildEscPosRaster(bw);

        DocPrintJob job = printer.createPrintJob();
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        Doc doc = new SimpleDoc(payload, flavor, null);
        job.print(doc, null);
    }

    private static BufferedImage scaleToWidth(BufferedImage src, int maxWidthPx) {
        int w = src.getWidth();
        int h = src.getHeight();

        if (w <= maxWidthPx) {
            // svejedno prebaci u RGB da je konzistentno
            BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = out.createGraphics();
            g.drawImage(src, 0, 0, null);
            g.dispose();
            return out;
        }

        double scale = (double) maxWidthPx / (double) w;
        int newW = maxWidthPx;
        int newH = Math.max(1, (int) Math.round(h * scale));

        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, newW, newH, null);
        g.dispose();
        return out;
    }

    /**
     * Threshold crno-bijelo: true = crni piksel, false = bijeli.
     */
    private static boolean[][] toBlackWhite(BufferedImage img, int threshold) {
        int w = img.getWidth();
        int h = img.getHeight();
        boolean[][] bw = new boolean[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;


                int lum = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);

                bw[y][x] = lum < threshold;
            }
        }
        return bw;
    }

    /**
     * ESC/POS raster (GS v 0) paket:
     * 1D 76 30 00 xL xH yL yH [data]
     * data: po retku, 1 bit po pikselu, MSB first.
     */
    private static byte[] buildEscPosRaster(boolean[][] bw) throws Exception {
        int height = bw.length;
        int width = bw[0].length;

        int widthBytes = (width + 7) / 8;

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // INIT
        out.write(new byte[]{0x1B, 0x40}); // ESC @

        // opcionalno: centriranje
        out.write(new byte[]{0x1B, 0x61, 0x01}); // ESC a 1 (center)

        // raster command header
        out.write(0x1D); // GS
        out.write(0x76); // 'v'
        out.write(0x30); // '0'
        out.write(0x00); // m = 0

        // xL xH (width in bytes), yL yH (height in dots)
        out.write(widthBytes & 0xFF);
        out.write((widthBytes >> 8) & 0xFF);
        out.write(height & 0xFF);
        out.write((height >> 8) & 0xFF);

        // image data
        for (int y = 0; y < height; y++) {
            for (int xb = 0; xb < widthBytes; xb++) {
                int b = 0;
                for (int bit = 0; bit < 8; bit++) {
                    int x = xb * 8 + bit;
                    boolean black = (x < width) && bw[y][x];
                    if (black) {
                        b |= (1 << (7 - bit)); // MSB first
                    }
                }
                out.write(b);
            }
        }

        // line feeds da izvuče papir malo
        out.write(0x0A);
        out.write(0x0A);
        out.write(0x0A);



        return out.toByteArray();
    }

}
