package org.tensorflow.demo.Model;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by liach on 10/15/2016.
 */

public class FileOperation {

    private final String TAG = "FileOperation";

    public static String saveToInternalStorage(Context ctx, Bitmap bitmapImage, String filename){
        ContextWrapper cw = new ContextWrapper(ctx.getApplicationContext());
        // path to /data/data/<app_name>/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,filename+".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //return directory.getAbsolutePath();
        return mypath.getAbsolutePath();
    }

    /*
    *
    * @param path: absolute path for the image file
    * 
    * @param corp_size: assign picture corp size
    * */
    public static Bitmap loadImageFromStorage(String path, int corp_size){
        try {
            File f=new File(path);
            Bitmap b =  BitmapFactory.decodeStream(new FileInputStream(f));
            return b.createScaledBitmap(b, corp_size, corp_size, true);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /*
    *
    * @param path: absolute path for the image file
    * */
    public static Bitmap loadImageFromStorage(String path)
    {
        return loadImageFromStorage(path, 85);
    }
}
