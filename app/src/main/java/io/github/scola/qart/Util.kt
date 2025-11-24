package io.github.scola.qart;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by shaozheng on 2016/9/1.
 */
public class Util {
    public static void saveBitmap(Bitmap bitmap, String path) {
        try {
            FileOutputStream stream = new FileOutputStream(path); // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int calculateColorGrayValue(int color) {
        final double GS_RED = 0.299;
        final double GS_GREEN = 0.587;
        final double GS_BLUE = 0.114;
        return (int)(Color.red(color) *  GS_RED + Color.green(color) * GS_GREEN + Color.blue(color) * GS_BLUE);
    }

    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static void saveConfig(File dst, String config) throws IOException {
        OutputStream out = new FileOutputStream(dst);
        Writer writer = new OutputStreamWriter(out);
        writer.write(config);
        writer.close();
        out.close();
    }
}