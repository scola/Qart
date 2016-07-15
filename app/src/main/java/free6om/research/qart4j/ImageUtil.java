package free6om.research.qart4j;

import com.google.zxing.common.BitMatrix;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by free6om on 7/27/15.
 */
public class ImageUtil {
    public static int[][] makeTarget(BufferedImage image, int x, int y, int width, int height) {
        int[][] target = new int[height][width];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                int argb = image.getRGB(j+x, i+y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                if (a == 0) {
                    target[i][j] = -1;
                } else {
                    target[i][j] = ((299 * r + 587 * g + 114 * b) + 500) / 1000;
                }
            }
        }

        return target;

    }

    public static BufferedImage loadImage(String filename, int width, int height) throws IOException, ImageReadException {
        BufferedImage image = Imaging.getBufferedImage(new File(filename));

        BufferedImage finalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = finalImage.createGraphics();
        graphics.drawImage(image, 0, 0, width, height, null);
        graphics.dispose();

        return finalImage;
    }

    public static BitMatrix makeBitMatrix(QRCode code, int quietZone, int size) {
        Pixel[][] pixels = code.getPixels();
        byte[] bytes = code.getBytes();

        int inputWidth = pixels[0].length;
        int inputHeight = pixels.length;
        int qrWidth = inputWidth + (quietZone * 2);
        int qrHeight = inputHeight + (quietZone * 2);
        int outputWidth = size;
        int outputHeight = size;

        int multiple = size / qrWidth;
        // Padding includes both the quiet zone and the extra white pixels to accommodate the requested
        // dimensions. For example, if input is 25x25 the QR will be 33x33 including the quiet zone.
        // If the requested size is 200x160, the multiple will be 4, for a QR of 132x132. These will
        // handle all the padding from 100x100 (the actual QR) up to 200x160.
        int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
        int topPadding = (outputHeight - (inputHeight * multiple)) / 2;

        BitMatrix output = new BitMatrix(outputWidth, outputHeight);

        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            // Write the contents of this row of the barcode
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                Pixel pixel = new Pixel(pixels[inputY][inputX]);
                Pixel.PixelRole role = pixel.getPixelRole();
                if(role == Pixel.PixelRole.DATA || role == Pixel.PixelRole.CHECK) {
                    int offset = pixel.getOffset();
                    int value = (bytes[offset/8]>>(7-offset&7))&0x1;
                    if(pixel.shouldInvert()) {
                        pixel.xorPixel(value);
                    } else {
                        pixel.setPixel(value);
                    }
                }

                if((pixel.getPixel()&Pixel.BLACK.getPixel()) != 0) {
                    output.setRegion(outputX, outputY, multiple, multiple);
                }
            }
        }

        return output;
    }
}
