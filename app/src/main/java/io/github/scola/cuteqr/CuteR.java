package io.github.scola.cuteqr;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.QRCode;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by shaozheng on 2016/8/12.
 */
public class CuteR {
    private static final String TAG = "CuteR";

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    private static int[] patternCenters;
    private static int scaleQR;
    private static final int MAX_INPUT_GIF_SIZE = 480;
    private static final int SCALE_NORMAL_QR = 10;

    private static final float FULL_LOGO_QR = 507.1f;
    private static final float LOGO_BACKGROUND = 140.7f;
    private static final float LOGO_SIZE = 126.7f;

    private static final int MAX_LOGO_SIZE = 1080;

    public static Bitmap Product(String txt, Bitmap input, boolean colorful, int color){
        Log.d(TAG, "Product start input input.getWidth(): " + input.getWidth() + " input.getHeight(): " + input.getHeight());
        Bitmap QRImage = null;
        try {
            QRImage = encodeAsBitmap(txt);
        } catch (WriterException e) {
            Log.e(TAG, "encodeAsBitmap: " + e);
        }

        if (colorful && color != Color.BLACK) {
            QRImage = replaceColor(QRImage, color);
        }

        int inputSize = Math.max(input.getWidth(), input.getHeight());
        int scale = (int)Math.ceil(1.0 * inputSize / QRImage.getWidth());
        if (scale % 3 != 0) {
            scale += (3 - scale % 3);
        }

        scaleQR = scale;
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

        Bitmap blackWhite = resizedImage;
        if (colorful == false) {
            blackWhite = convertBlackWhiteFull(blackWhite);
        }

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

    public static Bitmap convertBlackWhiteFull(Bitmap blackWhite) {
        blackWhite = createContrast(blackWhite, 50, 30);
        blackWhite = ConvertToBlackAndWhite(blackWhite);
        blackWhite = convertGreyImgByFloyd2(blackWhite);
        return blackWhite;
    }

    public static Bitmap ProductEmbed(String txt, Bitmap input, boolean colorful, int color, int x, int y, Bitmap originBitmap){
        int originalSize = input.getWidth();
        Bitmap qrBitmap = Product(txt, input, colorful, color);
        double newScale = 1.0 * originalSize * scaleQR / (qrBitmap.getWidth() - 2 * 4 * scaleQR);
        int targetSize = qrBitmap.getWidth() * originalSize / (qrBitmap.getWidth() - 2 * 4 * scaleQR);
        qrBitmap = resizeQuiteZone(qrBitmap, newScale); //it does not match QR spec to cut the qr quiet zone
        qrBitmap = Bitmap.createScaledBitmap(qrBitmap, targetSize, targetSize, false);
        originBitmap = originBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(originBitmap);
        canvas.drawBitmap(qrBitmap, x - (int) (4 * newScale), y - (int) (4 * newScale), null);
        return originBitmap;
    }

    private static Bitmap resizeQuiteZone(Bitmap qrBitmap, double scale) {
        int size = qrBitmap.getWidth();
        int boundary = (int) (3.5 * scale);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i < boundary || i > size - (boundary + 1) || j < boundary || j > size - (boundary + 1)) {
                    qrBitmap.setPixel(i, j, Color.TRANSPARENT);
                }
            }
        }
        return qrBitmap;
    }

    public static Bitmap ProductNormal(String txt, boolean colorful, int color) {
        Bitmap QRImage = null;
        try {
            QRImage = encodeAsBitmap(txt);
        } catch (WriterException e) {
            Log.e(TAG, "encodeAsBitmap: " + e);
            return null;
        }

        if (colorful && color != Color.BLACK) {
            QRImage = replaceColor(QRImage, color);
        }
        return Bitmap.createScaledBitmap(QRImage, QRImage.getWidth() * SCALE_NORMAL_QR, QRImage.getHeight() * SCALE_NORMAL_QR, false);
    }

    public static Bitmap ProductForResult(String txt) {
        Bitmap QRImage = null;
        try {
            QRImage = encodeAsBitmap(txt, ErrorCorrectionLevel.L);
        } catch (WriterException e) {
            Log.e(TAG, "encodeAsBitmap: " + e);
            return null;
        }
        return Bitmap.createScaledBitmap(QRImage, QRImage.getWidth() * SCALE_NORMAL_QR, QRImage.getHeight() * SCALE_NORMAL_QR, false);
    }

    public static Bitmap ProductLogo(Bitmap logo, String txt, boolean colorful, int color) {
        Bitmap qrImage = ProductNormal(txt, colorful, color);
        int fullSize = qrImage.getWidth() - 4 * 2 * SCALE_NORMAL_QR;
        int finalSize = (int) (logo.getWidth() * FULL_LOGO_QR / LOGO_SIZE);
        finalSize = Math.min(finalSize, MAX_LOGO_SIZE);
        int scale = SCALE_NORMAL_QR;

        if (finalSize > fullSize) {
            scale = SCALE_NORMAL_QR * finalSize/fullSize;
            qrImage = Bitmap.createScaledBitmap(qrImage, qrImage.getWidth()*finalSize/fullSize, qrImage.getHeight()*finalSize/fullSize, false);
            fullSize = finalSize;
        }
        int background = (int) (fullSize * LOGO_BACKGROUND / FULL_LOGO_QR);
        int logoSize = (int) (fullSize * LOGO_SIZE / FULL_LOGO_QR);

        Bitmap white =  Bitmap.createBitmap(background, background, Bitmap.Config.ARGB_8888);
        int boundary = (background - logoSize) / 2;

        Canvas canvas = new Canvas(white);
        canvas.drawColor(Color.WHITE);

//        Bitmap white =  Bitmap.createBitmap(background - boundary * 2, background - boundary * 2, Bitmap.Config.ARGB_8888);
//        Canvas canvasWhite = new Canvas(white);
//        canvasWhite.drawColor(Color.WHITE);
//        white = fillBoundary(white, boundary);
//
//        canvas.drawBitmap(white, boundary, boundary, null);

        Bitmap scaleLogo = Bitmap.createScaledBitmap(logo, logoSize, logoSize, false);
        scaleLogo = fillBoundary(scaleLogo, boundary, Color.WHITE);
        canvas.drawBitmap(scaleLogo, boundary, boundary, null);
        canvas.save();
        white = fillBoundary(white, boundary, Color.TRANSPARENT);

        Canvas canvasQR = new Canvas(qrImage);
        canvasQR.drawBitmap(white, scale * 4 + (fullSize - background)/2, scale * 4 + (fullSize - background)/2, null);
//        canvasQR.drawBitmap(scaleLogo, scale * 4 + (fullSize - logoSize)/2, scale * 4 + (fullSize - logoSize)/2, null);
        return qrImage;
    }

    private static Bitmap fillBoundary(Bitmap white, int boundary, int color) {
        int size = white.getWidth();
        int r = boundary;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i < r && j < r) {
                    if (Math.pow(r - i, 2) + Math.pow(r - j, 2) > Math.pow(r, 2)) {
                        white.setPixel(i, j, color);
                    }
                } else if (i < r && j > size - (r + 1)) {
                    if (Math.pow(r - i, 2) + Math.pow(size - (r + 1) - j, 2) > Math.pow(r, 2)) {
                        white.setPixel(i, j, color);
                    }
                } else if (i > size - (r + 1) && j < r) {
                    if (Math.pow(size - (r + 1) - i, 2) + Math.pow(r - j, 2) > Math.pow(r, 2)) {
                        white.setPixel(i, j, color);
                    }
                } else if (i > size - (r + 1) && j > size - (r + 1)) {
                    if (Math.pow(size - (r + 1) - i, 2) + Math.pow(size - (r + 1) - j, 2) > Math.pow(r, 2)) {
                        white.setPixel(i, j, color);
                    }
                }
            }
        }
        return  white;
    }

    public static Bitmap[] ProductGIF(String txt, Bitmap[] input, boolean colorful, int color) {
        Log.d(TAG, "ProductGIF start");
        int size = input[0].getWidth();
        int i = 0;
        Bitmap[] output = new Bitmap[input.length];
        for (Bitmap inputBitmap : input){
            if (size > MAX_INPUT_GIF_SIZE) {
                inputBitmap = Bitmap.createScaledBitmap(inputBitmap, MAX_INPUT_GIF_SIZE, MAX_INPUT_GIF_SIZE, false);
            }
            output[i] = Product(txt, inputBitmap, colorful, color);
            i++;
        }
//        int maxSize = Math.min(MAX_INPUT_GIF_SIZE, input[0].getWidth());
////        if (scale < 1) {
////            for (int i = 0; i < input.length; i++) {
////                input[i] = getResizedBitmap(input[i], scale, scale);
////            }
////        }
//        Bitmap white =  Bitmap.createBitmap(maxSize, maxSize, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(white);
//        canvas.drawColor(Color.WHITE);
//        Bitmap whiteQR = Product(txt, white, colorful, color);
//
//        Bitmap[] output = new Bitmap[input.length];
//        int i = 0;
//        double scale = colorful ? 3.0/4 : 1;
//        int resizedImageSize = (whiteQR.getWidth() - scaleQR  * 4 * 2);
//
//        for (Bitmap inputBitmap : input) {
//            if (!colorful) {
//                inputBitmap = convertBlackWhiteFull(inputBitmap);
//            }
//            Bitmap resizeImage = Bitmap.createScaledBitmap(inputBitmap, (int)(resizedImageSize*scale), (int)(resizedImageSize*scale), false);
//            Bitmap finalImage =  Bitmap.createBitmap(whiteQR.getWidth(), whiteQR.getHeight(), Bitmap.Config.ARGB_8888);
//            Canvas canvasQR = new Canvas(finalImage);
//            canvasQR.drawColor(Color.WHITE);
//            canvasQR.drawBitmap(resizeImage, scaleQR * 4 + (resizedImageSize - (int)(resizedImageSize*scale))/2, scaleQR * 4 + (resizedImageSize - (int)(resizedImageSize*scale))/2, null);
//            finalImage = replaceQR(whiteQR, finalImage);
//            output[i] = finalImage;
//            i++;
//        }
//        Log.d(TAG, "ProductGIF end");
        return output;
    }

    public static Bitmap replaceColor(Bitmap qrBitmap, int color) {
        int [] allpixels = new int [qrBitmap.getHeight()*qrBitmap.getWidth()];

        qrBitmap.getPixels(allpixels, 0, qrBitmap.getWidth(), 0, 0, qrBitmap.getWidth(), qrBitmap.getHeight());

        for(int i = 0; i < allpixels.length; i++)
        {
            if(allpixels[i] == Color.BLACK)
            {
                allpixels[i] = color;
            }
        }

        qrBitmap.setPixels(allpixels, 0, qrBitmap.getWidth(), 0, 0, qrBitmap.getWidth(), qrBitmap.getHeight());
        return qrBitmap;
    }

    public static Bitmap replaceQR(Bitmap qrBitmap, Bitmap image) {
        int width = qrBitmap.getWidth();         //获取位图的宽
        int height = qrBitmap.getHeight();       //获取位图的高

        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组

        qrBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] gray=new int[height*width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (pixels[width * i + j] != Color.WHITE) {
                    image.setPixel(i, j, pixels[width * i + j]);
                }
            }
        }
        return image;
    }

    public static Bitmap encodeAsBitmap(String txt) throws WriterException {
        return encodeAsBitmap(txt, ErrorCorrectionLevel.H);
    }

    public static Bitmap encodeAsBitmap(String txt, ErrorCorrectionLevel level) throws WriterException {
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
            qrCode = Encoder.encode(contentsToEncode, level, hints);
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

    public static Bitmap addQRQuietZone(Bitmap qrBitmap) {
        int size = Math.min(qrBitmap.getWidth(), qrBitmap.getHeight());
        int boundary = size / 5;

        Bitmap white =  Bitmap.createBitmap(qrBitmap.getWidth() + boundary*2, qrBitmap.getWidth() + boundary*2, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(white);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(qrBitmap, boundary, boundary, null);

        return white;
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

    public static Result decodeQRImage(Bitmap bitmap) {
        Bitmap blackWhite = ConvertToBlackAndWhite(bitmap);
        int width = blackWhite.getWidth(), height = blackWhite.getHeight();
        int[] pixels = new int[width * height];
        blackWhite.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
        MultiFormatReader reader = new MultiFormatReader();
        try {
            Result result = reader.decode(bBitmap);
            return result;
        } catch (NotFoundException e) {
            Log.e(TAG, "direct decode exception", e);
            HashMap<DecodeHintType, Object> map = new HashMap<>();
            map.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
            try {
                Result result = reader.decode(bBitmap, map);
                return result;
            } catch (NotFoundException ex) {
                Log.e(TAG, "DecodeHintType.PURE_BARCODE exception", ex);
                return null;
            }
        }
    }

}
