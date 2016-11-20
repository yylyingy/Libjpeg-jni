package com.yangyl.testlibjpeg;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.yangyl.libjpeg.JpegDecCompress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
    Bitmap mBitmap = BitmapFactory.decodeFile("");
    byte [] jpeg;
    ImageView iv;
    JpegDecCompress libjpeg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        libjpeg = new JpegDecCompress();
        if (Build.VERSION.SDK_INT >= 23){
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },1);
            }
        }
        iv = (ImageView) findViewById(R.id.iv);
        File file = new File(Environment.getExternalStorageDirectory() + "/360/test.jpg");
        try {
            FileInputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte [] buff = new byte[1024];
            int i;
            while ((i = inputStream.read(buff)) != -1){
                byteArrayOutputStream.write(buff,0,i);
            }
            jpeg = byteArrayOutputStream.toByteArray();
            Log.d("Main",jpeg.length + "");
//            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream()
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(libjpeg.stringFromJNI());
//        new Thread() {
//            @Override
//            public void run() {
//                for( int i = 0;true;i++){
//                    byte[] rgb = decodeJpeg(jpeg, jpeg.length);
//                    byte [] a = new byte[1024 * 10240];
//                    a[0] = 0;
//                    Bitmap bitmap = createMyBitmap(rgb, 1080, 1920);
//                }
//            }
//        }.start();
        byte[] rgb = libjpeg.decodeJpeg(jpeg, jpeg.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(rgb);
        byteBuffer.flip();
        Bitmap bitmap = createMyBitmap(rgb, 1080, 1920);
//        Bitmap bitmap1 = Bitmap.createBitmap(1080,1920, Bitmap.Config.ARGB_8888);
//        bitmap1.copyPixelsFromBuffer(byteBuffer);
        iv.setImageBitmap(bitmap);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public Bitmap createMyBitmap(byte[] data, int width, int height){
        int []colors = convertByteToColor(data);
        if (colors == null){
            return null;
        }

        Bitmap bmp = null;

        try {
            bmp = Bitmap.createBitmap(colors, 0, width, width, height,
                    Bitmap.Config.ARGB_8888);
        } catch (Exception e) {
            // TODO: handle exception

            return null;
        }

        return bmp;
    }
    /*
     * 将RGB数组转化为像素数组
     */
    private static int[] convertByteToColor(byte[] data){
        int size = data.length;
        if (size == 0){
            return null;
        }


        // 理论上data的长度应该是3的倍数，这里做个兼容
        int arg = 0;
        if (size % 3 != 0){
            arg = 1;
        }

        int []color = new int[size / 3 + arg];
        int red, green, blue;


        if (arg == 0){                                  //  正好是3的倍数
            for(int i = 0; i < color.length; ++i){

                color[i] = (data[i * 3] << 16 & 0x00FF0000) |
                        (data[i * 3 + 1] << 8 & 0x0000FF00 ) |
                        (data[i * 3 + 2] & 0x000000FF ) |
                        0xFF000000;
            }
        }else{                                      // 不是3的倍数
            for(int i = 0; i < color.length - 1; ++i){
                color[i] = (data[i * 3] << 16 & 0x00FF0000) |
                        (data[i * 3 + 1] << 8 & 0x0000FF00 ) |
                        (data[i * 3 + 2] & 0x000000FF ) |
                        0xFF000000;
            }

            color[color.length - 1] = 0xFF000000;                   // 最后一个像素用黑色填充
        }

        return color;
    }

}
