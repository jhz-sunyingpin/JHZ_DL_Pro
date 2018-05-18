package com.example.qh000.yoloandroid;

import android.graphics.Bitmap;

/**
 * Created by LJ000PC on 2018/4/14.
 */

public class DarknetFunc {
    static {
        System.loadLibrary("darknetlib");
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void inityolo(String cfgfile, String weightfile);
    public native double testyolo(String imgfile);
    public native boolean detectimg(Bitmap dst, Bitmap src);
    public native boolean yoloinit();
    public native boolean yolorealese();
    public native double yolodetectimg(String imgfile);
}
