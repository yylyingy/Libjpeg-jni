package com.yangyl.libjpeg;

/**
 * Created by Yangyl on 2016/11/15.
 */

public class JpegDecCompress {
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native byte [] decodeJpeg(byte[] jpeg,int length);
}
