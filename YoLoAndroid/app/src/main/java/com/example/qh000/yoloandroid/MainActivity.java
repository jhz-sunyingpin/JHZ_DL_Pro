package com.example.qh000.yoloandroid;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity implements View.OnClickListener{

    TextView view_status;
    ImageView view_dstimg;
//    private static final int DETECT_FINISH = 22;
//    private static final int COPY_FALSE = -1;
    private static final int WRITE_EXTERNAL_STORAGE = 2;
    Bitmap dstimg;

//    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("darknet-lib");
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        //检查权限
//        if(ActivityCompat
//                .checkSelfPermission(MainActivity.this,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
//        { //调用这个方法只会在API>=23的时候才会起作用，否则一律返回false
//            // 第一次请求权限时，用户拒绝了，调用后返回true
//            // 第二次请求权限时，用户拒绝且选择了“不在提醒”，调用后返回false。
//            // 设备的策略禁止当前应用获取这个权限的授权时调用后返回false 。
//            if(ActivityCompat.shouldShowRequestPermissionRationale(
//                    MainActivity.this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE))
//            { //此时我们都弹出提示
//                ActivityCompat.requestPermissions(
//                        MainActivity.this,
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        WRITE_EXTERNAL_STORAGE);
//            }
//            else
//            {
//                //这里是用户各种拒绝后我们也弹出提示
//                ActivityCompat.requestPermissions(
//                        MainActivity.this,
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        WRITE_EXTERNAL_STORAGE);
//            }
//        }
//        else
//        {
//            //正常情况，表示权限是已经被授予的
//
//        }

        view_status = (TextView) findViewById(R.id.textView1);
        //解压设置的网络结构和模型到指定路径
        view_status.setText("Exacting model, please wait");
        //如果相关文件都已存在，则不需解压
        //  /sdcard/yolo/data/coco.names
        //  /sdcard/yolo/cfg/coco.data
        //  /sdcard/yolo/cfg/yolov2-tiny.cfg
        //  /sdcard/yolo/weights/yolov2-tiny.weights
        //  /sdcard/yolo/data/labels
        boolean bExtract = false;
        String strPath = "/sdcard/yolo/data/coco.names";
        File file = new File(strPath);
        if (!file.exists())
        {
            bExtract = true;
        }
        strPath = "/sdcard/yolo/cfg/coco.data";
        file = new File(strPath);
        if (!file.exists())
        {
            bExtract = true;
        }
        strPath = "/sdcard/yolo/cfg/yolov2-tiny.cfg";
        file = new File(strPath);
        if (!file.exists())
        {
            bExtract = true;
        }
        strPath = "/sdcard/yolo/weights/yolov2-tiny.weights";
        file = new File(strPath);
        if (!file.exists())
        {
            bExtract = true;
        }
        strPath = "/sdcard/yolo/data/labels";
        file = new File(strPath);
        if (!file.exists())
        {
            bExtract = true;
        }

        if (bExtract)
        {
            copyFilesFassets(this, "cfg", "/sdcard/yolo/cfg");
            copyFilesFassets(this, "data", "/sdcard/yolo/data");
            copyFilesFassets(this, "weights", "/sdcard/yolo/weights");
        }

        view_status.setText("Exact model finish");

        // Example of a call to a native method
//        TextView tv = (TextView) findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.button1:
                intent = new Intent(this, CameraActivity.class);
                startActivity(intent);
                break;
        }
        MainActivity.this.finish();
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();

    /**
     *  从assets目录中复制整个文件夹内容
     *  @param  context  Context 使用CopyFiles类的Activity
     *  @param  oldPath  String  原文件路径  如：/aa
     *  @param  newPath  String  复制后路径  如：xx:/bb/cc
     */
    public void copyFilesFassets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {//如果是目录
                File file = new File(newPath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesFassets(context,oldPath + "/" + fileName,newPath+"/"+fileName);
                }
            } else {//如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount=0;
                while((byteCount=is.read(buffer))!=-1) {//循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                }
                fos.flush();//刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //如果捕捉到错误则通知UI线程
//            mHandler.sendEmptyMessage(COPY_FALSE);
        }
    }
}
