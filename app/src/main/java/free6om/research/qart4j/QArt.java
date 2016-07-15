package free6om.research.qart4j;

import com.google.zxing.common.BitMatrix;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

/**
 * Hello world!
 */
public class QArt {
    private static final Logger LOGGER = LoggerFactory.getLogger("test");

    public static void main(String[] args) {
        OptionParser parser = new OptionParser() {
            {
                acceptsAll(Arrays.asList("help", "?"), "show this help.");
                acceptsAll(Arrays.asList("l", "log4j")).withRequiredArg()
                        .ofType(String.class)
                        .describedAs("log config file path.")
                        .defaultsTo("./src/main/config/log4j.properties");
                //input
                acceptsAll(Arrays.asList("i", "input")).withRequiredArg()
                        .ofType(String.class)
                        .describedAs("input image file")
                        .defaultsTo("input.png");
                acceptsAll(Arrays.asList("u", "url")).withRequiredArg()
                        .ofType(String.class)
                        .describedAs("URL to encode")
                        .defaultsTo("http://free6om.me");
                //output QR code
                acceptsAll(Arrays.asList("v", "version")).withRequiredArg()
                        .ofType(Integer.class)
                        .describedAs("QR version: 1 - 40")
                        .defaultsTo(6);
                acceptsAll(Arrays.asList("m", "mask")).withRequiredArg()
                        .ofType(Integer.class)
                        .describedAs("QR mask: 0 - 7")
                        .defaultsTo(2);
                acceptsAll(Arrays.asList("q", "quiet")).withRequiredArg()
                        .ofType(Integer.class)
                        .describedAs("QR quiet zone")
                        .defaultsTo(2);
                acceptsAll(Arrays.asList("r", "rotation")).withRequiredArg()
                        .ofType(Integer.class)
                        .describedAs("rotation of the image in clockwise: 0 - 3")
                        .defaultsTo(0);
                acceptsAll(Arrays.asList("z", "size")).withRequiredArg()
                        .ofType(Integer.class)
                        .describedAs("output QR code size, 0 means don't scale")
                        .defaultsTo(0);
                acceptsAll(Arrays.asList("cb", "colorBlack")).withOptionalArg()
                        .ofType(String.class)
                        .describedAs("ARGB of the black pixel of the QR code, 0x00000000 - 0xFFFFFFFF")
                        .defaultsTo("FF000000");
                acceptsAll(Arrays.asList("cw", "colorWhite")).withOptionalArg()
                        .ofType(String.class)
                        .describedAs("ARGB of the white pixel of the QR code, 0x00000000 - 0xFFFFFFFF")
                        .defaultsTo("FFFFFFFF");
                //how to generate QR code
                acceptsAll(Arrays.asList("randControl")).withRequiredArg()
                        .ofType(Boolean.class)
                        .describedAs("rand control or not")
                        .defaultsTo(Boolean.FALSE);
                acceptsAll(Arrays.asList("seed")).withRequiredArg()
                        .ofType(Long.class)
                        .describedAs("random seed, -1 use System.currentMillis()")
                        .defaultsTo(-1L);
                acceptsAll(Arrays.asList("d", "dither")).withRequiredArg()
                        .ofType(Boolean.class)
                        .describedAs("dither image or not")
                        .defaultsTo(Boolean.FALSE);
                acceptsAll(Arrays.asList("onlyData")).withRequiredArg()
                        .ofType(Boolean.class)
                        .describedAs("only use data bits to emulate input image")
                        .defaultsTo(Boolean.FALSE);
                acceptsAll(Arrays.asList("c", "saveControl")).withRequiredArg()
                        .ofType(Boolean.class)
                        .describedAs("show pixel we have control")
                        .defaultsTo(Boolean.FALSE);
                //output image
                acceptsAll(Arrays.asList("mt", "marginTop")).withOptionalArg()
                        .ofType(Integer.class)
                        .describedAs("margin top")
                        .defaultsTo(0);
                acceptsAll(Arrays.asList("mb", "marginBottom")).withOptionalArg()
                        .ofType(Integer.class)
                        .describedAs("margin bottom");
                acceptsAll(Arrays.asList("ml", "marginLeft")).withOptionalArg()
                        .ofType(Integer.class)
                        .describedAs("margin left")
                        .defaultsTo(0);
                acceptsAll(Arrays.asList("mr", "marginRight")).withOptionalArg()
                        .ofType(Integer.class)
                        .describedAs("margin right");
                acceptsAll(Arrays.asList("w", "width")).withRequiredArg()
                        .ofType(Integer.class)
                        .describedAs("output image width, 0 means same as input image width")
                        .defaultsTo(180);
                acceptsAll(Arrays.asList("h", "height")).withRequiredArg()
                        .ofType(Integer.class)
                        .describedAs("output image height, 0 means same as input image height")
                        .defaultsTo(180);
                acceptsAll(Arrays.asList("f", "format")).withRequiredArg()
                        .ofType(String.class)
                        .describedAs("output image format")
                        .defaultsTo("PNG");
                acceptsAll(Arrays.asList("o", "output")).withRequiredArg()
                        .ofType(String.class)
                        .describedAs("output image file")
                        .defaultsTo("output.png");

            }
        };
        OptionSet options = parser.parse(args);
        if (options.hasArgument("help") || options.has("?") || args.length == 0) {
            try {
                parser.printHelpOn(System.out);
            } catch (IOException e) {
            }
            return;
        }

        String log4j = (String) options.valueOf("l");

        //input
        String filename = (String) options.valueOf("i");
        String url = (String) options.valueOf("u");

        //QR code
        int version = (Integer) options.valueOf("v");
        int mask = (Integer) options.valueOf("m");
        int quietZone = (Integer) options.valueOf("q");
        int rotation = (Integer) options.valueOf("r");
        int size = (Integer) options.valueOf("z");
        int colorBlack = (int) Long.parseLong((String) options.valueOf("cb"), 16);
        int colorWhite = (int) Long.parseLong((String) options.valueOf("cw"), 16);

        //how to generate QR code
        boolean randControl = (Boolean) options.valueOf("randControl");
        long seed = (Long) options.valueOf("seed");
        if (seed == -1) {
            seed = System.currentTimeMillis();
        }
        boolean dither = (Boolean) options.valueOf("d");
        boolean onlyDataBits = (Boolean) options.valueOf("onlyData");
        boolean saveControl = (Boolean) options.valueOf("saveControl");

        //output image
        int width = (Integer) options.valueOf("w");
        int height = (Integer) options.valueOf("h");

        Integer marginTop = options.has("mt") ? (Integer) options.valueOf("mt") : null;
        Integer marginBottom = options.has("mb") ? (Integer) options.valueOf("mb") : null;
        Integer marginLeft = options.has("ml") ? (Integer) options.valueOf("ml") : null;
        Integer marginRight = options.has("mr") ? (Integer) options.valueOf("mr") : null;

        String outputFormat = (String) options.valueOf("f");
        String output = (String) options.valueOf("o");

        configLog(log4j);

        //todo validate input params, make sure all of them are valid

        try {
            BufferedImage input = ImageUtil.loadImage(filename, width, height);

            int qrSizeWithoutQuiet = 17 + 4*version;
            int qrSize = qrSizeWithoutQuiet + quietZone * 2;
            if(size < qrSize) { //don't scale
                size = qrSize;
            }
            int scale = size / qrSize;
            int targetQrSizeWithoutQuiet = qrSizeWithoutQuiet * scale;

            Rectangle inputImageRect = new Rectangle(new Point(0, 0), width, height);
            int startX = 0, startY = 0;
            if(marginLeft != null) {
                startX = marginLeft;
            } else if(marginRight != null) {
                startX = width - marginRight - size;
            }
            if(marginTop != null) {
                startY = marginTop;
            } else if(marginBottom != null) {
                startY = height - marginBottom - size;
            }

            Rectangle qrRect = new Rectangle(new Point(startX, startY), size, size);
            Rectangle qrWithoutQuietRect = new Rectangle(new Point(startX + (size-targetQrSizeWithoutQuiet)/2, startY + (size-targetQrSizeWithoutQuiet)/2), targetQrSizeWithoutQuiet, targetQrSizeWithoutQuiet);

            int[][] target = null;
            int dx = 0, dy = 0;
            BufferedImage targetImage = null;
            Rectangle targetRect = inputImageRect.intersect(qrWithoutQuietRect);
            if(targetRect == null) {
                LOGGER.warn("no intersect zone");
                target = new int[0][0];
            } else {
                targetImage = input.getSubimage(targetRect.start.x, targetRect.start.y, targetRect.width, targetRect.height);
                int scaledWidth = targetRect.width/scale;
                int scaledHeight = targetRect.height/scale;
                BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics graphics = scaledImage.createGraphics();
                graphics.drawImage(targetImage, 0, 0, scaledWidth, scaledHeight, null);
                graphics.dispose();

                target = ImageUtil.makeTarget(scaledImage, 0, 0, scaledWidth, scaledHeight);
                dx = (qrWithoutQuietRect.start.x - targetRect.start.x)/scale;
                dy = (qrWithoutQuietRect.start.y - targetRect.start.y)/scale;
            }


            Image image = new Image(target, dx, dy, url, version, mask, rotation, randControl, seed, dither, onlyDataBits, saveControl);

            QRCode qrCode = image.encode();
            BitMatrix bitMatrix = ImageUtil.makeBitMatrix(qrCode, quietZone, size);

            MatrixToImageConfig config = new MatrixToImageConfig(colorBlack, colorWhite);
            BufferedImage finalQrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, config);

            Rectangle finalRect = qrRect.union(inputImageRect);
            BufferedImage finalImage = new BufferedImage(finalRect.width, finalRect.height, BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = finalImage.createGraphics();
            graphics.drawImage(input,
                    inputImageRect.start.x - finalRect.start.x, inputImageRect.start.y - finalRect.start.y,
                    inputImageRect.width, inputImageRect.height, null);
            graphics.drawImage(finalQrImage,
                    qrRect.start.x - finalRect.start.x, qrRect.start.y - finalRect.start.y,
                    qrRect.width, qrRect.height, null);
            graphics.dispose();

            ImageIO.write(finalImage, outputFormat, new File(output));
        } catch (Exception e) {
            LOGGER.error("encode error", e);
        }

    }

    private static void configLog(String configFile) {
        if(new File(configFile).exists()) {
            PropertyConfigurator.configure(configFile);
            return;
        }

        Properties properties = new Properties();

        properties.setProperty("log4j.rootLogger", "DEBUG, CA");
        properties.setProperty("log4j.appender.CA", "org.apache.log4j.ConsoleAppender");
        properties.setProperty("log4j.appender.CA.layout", "org.apache.log4j.PatternLayout");
        properties.setProperty("log4j.appender.CA.layout.ConversionPattern", "%d{yyyy-MM-dd HH:mm:ss.SSS} %-4r [%t] %-5p %c %x - %m%n");
        PropertyConfigurator.configure(properties);
    }
}
