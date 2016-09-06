package io.github.scola.cuteqr;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.EnumMap;
import java.util.Map;


/**
 * Created by shaozheng on 2016/8/12.
 */
public class CuteR {
    private static final String TAG = "CuteR";

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    private static int[] patternCenters;
    public static Bitmap Product(String txt, Bitmap input){
        Log.d(TAG, "Product start input input.getWidth(): " + input.getWidth() + " input.getHeight(): " + input.getHeight());
        Bitmap QRImage = null;
        try {
            QRImage = encodeAsBitmap(txt);
        } catch (WriterException e) {
            Log.e(TAG, "encodeAsBitmap: " + e);
        }

        int inputSize = Math.max(input.getWidth(), input.getHeight());
        int scale = (int)Math.ceil(1.0 * inputSize / QRImage.getWidth());
        if (scale % 3 != 0) {
            scale += (3 - scale % 3);
        }

        Bitmap scaledQRImage = Bitmap.createScaledBitmap(QRImage, QRImage.getWidth() * scale, QRImage.getHeight() * scale, false);

        int imageSize = 0;
        Bitmap resizedImage = null;
        if (input.getWidth() < input.getHeight()) {
            resizedImage = Bitmap.createScaledBitmap(input, scaledQRImage.getWidth() - scale  * 4 * 2, (int)((scaledQRImage.getHeight() - scale  * 4 * 2) * (1.0 * input.getHeight() / input.getWidth())), false);
            imageSize = resizedImage.getWidth();
        } else {
            resizedImage = Bitmap.createScaledBitmap(input, (int)((scaledQRImage.getWidth() - scale  * 4 * 2) * (1.0 * input.getWidth() / input.getHeight())), scaledQRImage.getHeight() - scale  * 4 * 2, false);
            imageSize = resizedImage.getHeight();
        }
//
//        if (patternCenters == null || patternCenters.length == 0) {
//            Log.e(TAG, "patternCenters == null || patternCenters.length == 0");
//            return null;
//        }

        int[][] pattern = new int[scaledQRImage.getWidth() - scale  * 4 * 2][scaledQRImage.getWidth() - scale  * 4 * 2];

        for (int i = 0; i < patternCenters.length; i++) {
            for (int j = 0; j < patternCenters.length; j++) {
                if (patternCenters[i] == 6 && patternCenters[j] == patternCenters[patternCenters.length - 1] ||
                        (patternCenters[j] == 6 && patternCenters[i] == patternCenters[patternCenters.length - 1]) ||
                        (patternCenters[i] == 6 && patternCenters[j] == 6)) {
                    continue;
                } else {
                    int initx = scale * (patternCenters[i] - 2);
                    int inity = scale * (patternCenters[j] - 2);
                    for (int x = initx; x < initx + scale * 5; x++) {
                        for (int y = inity; y < inity + scale * 5; y++) {
                            pattern[x][y] = 1;
                        }
                    }
                }
            }
        }

        Bitmap blackWhite = createContrast(resizedImage, 50, 30);
        blackWhite = ConvertToBlackAndWhite(blackWhite);
        blackWhite = convertGreyImgByFloyd2(blackWhite);
        for (int i = 0; i < imageSize; i++) {
            for (int j = 0; j < imageSize; j++) {
                if ((i * 3 / scale) % 3 == 1 && (j * 3 / scale) % 3 == 1) {
                    continue;
                }
                if (i < scale  * 4 * 2 && (j < scale  * 4 * 2 || j > imageSize -(scale  * 4 * 2 + 1))) {
                    continue;
                }
                if (i > imageSize - (scale  * 4 * 2 + 1) && j < scale  * 4 * 2) {
                    continue;
                }

                if (pattern[i][j] == 1) {
                    continue;
                }

                scaledQRImage.setPixel(i + scale  * 4, j + scale  * 4, blackWhite.getPixel(i, j));
            }
        }
        Log.d(TAG, "Product end input scaledQRImage.getWidth(): " + scaledQRImage.getWidth() + " scaledQRImage.getHeight(): " + scaledQRImage.getHeight());
        return scaledQRImage;
    }

    public static Bitmap encodeAsBitmap(String txt) throws WriterException {
        String contentsToEncode = txt;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType,Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }

        BitMatrix result;
        QRCode qrCode;
        try {
            qrCode = Encoder.encode(contentsToEncode, ErrorCorrectionLevel.H, hints);
            patternCenters = qrCode.getVersion().getAlignmentPatternCenters();
            // result = new MultiFormatWriter().encode(contentsToEncode, BarcodeFormat.QR_CODE, dimension, dimension, hints);
            result = renderResult(qrCode, 4);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

    // Note that the input matrix uses 0 == white, 1 == black, while the output matrix uses
    // 0 == black, 255 == white (i.e. an 8 bit greyscale bitmap).
    private static BitMatrix renderResult(QRCode code, int quietZone) {
        ByteMatrix input = code.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int qrWidth = inputWidth + (quietZone * 2);
        int qrHeight = inputHeight + (quietZone * 2);
        int outputWidth = Math.max(0, qrWidth);
        int outputHeight = Math.max(0, qrHeight);
        int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
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
                if (input.get(inputX, inputY) == 1) {
                    output.setRegion(outputX, outputY, multiple, multiple);
                }
            }
        }
        return output;
    }

    public static Bitmap ConvertToBlackAndWhite(Bitmap sampleBitmap){
        ColorMatrix bwMatrix =new ColorMatrix();
        bwMatrix.setSaturation(0);
        final ColorMatrixColorFilter colorFilter= new ColorMatrixColorFilter(bwMatrix);
        Bitmap rBitmap = sampleBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Paint paint=new Paint();
        paint.setColorFilter(colorFilter);
        Canvas myCanvas =new Canvas(rBitmap);
        myCanvas.drawBitmap(rBitmap, 0, 0, paint);
        return rBitmap;
    }

    public static Bitmap createContrast(Bitmap src, double value, int brightness) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int)(((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0) + brightness;
                if(R < 0) { R = 0; }
                else if(R > 255) { R = 255; }

                G = Color.green(pixel);
                G = (int)(((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0) + brightness;
                if(G < 0) { G = 0; }
                else if(G > 255) { G = 255; }

                B = Color.blue(pixel);
                B = (int)(((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0) + brightness;
                if(B < 0) { B = 0; }
                else if(B > 255) { B = 255; }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    public static Bitmap convertGreyImgByFloyd(Bitmap img) {


        int width = img.getWidth();         //获取位图的宽
        int height = img.getHeight();       //获取位图的高


        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组


        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] gray=new int[height*width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                int red = ((grey  & 0x00FF0000 ) >> 16);
                gray[width*i+j]=red;
            }
        }


        int e=0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int g=gray[width*i+j];
                if (g>=128) {
                    pixels[width*i+j]=0xffffffff;
                    e=g-255;


                }else {
                    pixels[width*i+j]=0xff000000;
                    e=g-0;
                }
                if (j<width-1&&i<height-1) {
//右边像素处理
                    gray[width*i+j+1]+=3*e/8;
//下
                    gray[width*(i+1)+j]+=3*e/8;
//右下
                    gray[width*(i+1)+j+1]+=e/4;
                }else if (j==width-1&&i<height-1) {//靠右或靠下边的像素的情况
//下方像素处理
                    gray[width*(i+1)+j]+=3*e/8;
                }else if (j<width-1&&i==height-1) {
//右边像素处理
                    gray[width*(i)+j+1]+=e/4;
                }
            }


        }


        Bitmap mBitmap=Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return mBitmap;
    }

    public static Bitmap convertGreyImgByFloyd2(Bitmap img) {

        int width = img.getWidth();         //获取位图的宽
        int height = img.getHeight();       //获取位图的高

        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] gray=new int[height*width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                gray[width*i+j] = grey & 0xFF;
            }
        }

        int e = 0;
        int divide = 16;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int g = gray[width * i + j];
                int newPixel = (g >> 7) * 255;
                e = g - newPixel;
                pixels[width * i + j] = newPixel > 0 ? WHITE : BLACK;
                if (j + 1 < width) {
                    gray[width * i + j + 1] += e * 7 / divide;
                }

                if (j - 1 >= 0 && i + 1 < height) {
                    gray[width * (i + 1) + j - 1] += e * 3 / divide;
                }

                if (i + 1 < height) {
                    gray[width * (i + 1) + j] += e * 5 / divide;
                }

                if (j + 1 < width && i + 1 < height) {
                    gray[width * (i + 1) + j + 1] += e * 1 / divide;
                }
            }
        }
        Bitmap mBitmap=Bitmap.createBitmap(width, height, img.getConfig());
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return mBitmap;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix,
                true);
    }

    public static Bitmap getResizedBitmap(Bitmap source, float scaleX, float scaleY) {
        if (source == null) {
            return null;
        }
        Bitmap bitmap = null;
        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY);
        bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        return bitmap;
    }

}
