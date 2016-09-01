package io.github.scola.qart;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
}
