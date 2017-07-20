package com.efrobot.capturing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.mvp.utils.RobotToastUtil;
import com.efrobot.library.net.FileMessage;
import com.efrobot.library.net.SendRequestListener;
import com.efrobot.robot.stereovision.StereoVision;

import com.efrobot.library.RobotManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class BinocularVisionActivity extends Activity implements View.OnClickListener {

    private Context theContext = this;
    private static final String TAG = "BinocularVisionActivity";

    private static int width = 640;
    private static int height = 480;
    private static int dwidth = 640;
    private static int dheight = 480;
    private int nx = 8;
    private int ny = 4;

    private static final int NEXT_TEST = 0;
    private static final int SHOW_IMAGE = 1;
    private static final int UPDATE_UI = 2;
    private static final int UPDATE_UI_L = 3;
    private static final int UPDATE_UI_R = 4;
    private static final int SEND_IMAGE_FAILURE = 5;
    private static final int DETECTION_END = 6;

    private final static int TEST_PASS = 0;
    private final static int TEST_FAIL = -1;
    private final static int TEST_NOT = 2;
    private final static int SINGLE_MODE = 0;
    private final static int CONTINUE_MODE = 1;
    private static final int BINOCULAR_VISION_ACTIVITY_REQUEST_CODE = 1000;



    // 摄像头缓冲帧数
    private int cameraBufferNumber = 4;

    private byte[] leftCameraYUVData;
    private byte[] rightCameraYUVData;
    private byte[] leftCameraRGBData;
    private byte[] rightCameraRGBData;
    private Bitmap leftCameraBitmap;
    private Bitmap rightCameraBitmap;
    private ByteBuffer leftCameraBuffer;
    private ByteBuffer rightCameraBuffer;
    private StereoVision leftCamera;
    private StereoVision rightCamera;

    //private byte[] cameraYUVData;
    private byte[] cameraYUVData_Save;
    //private byte[] cameraRGBData;
    private byte[] cameraRGBData_Save;
    //private Bitmap cameraBitmap;
    private Bitmap cameraBitmap_Save;
    //private ByteBuffer cameraBuffer;
    private ByteBuffer cameraBuffer_Save;
    //private StereoVision currentCamera;

    //private LinearLayout mButtons;
    //private RelativeLayout mLeft1, mRight1;
    //private ImageView mLeft2, mRight2, mLeft3, mRight3;

    private ImageView mCameraShow_BV;
    /**
     * 绘检测结果（矩形框+类别）
     */
    private MySurfaceView mDraw_BV;

    private Button mSaveBtn1, mSaveBtn2, mSaveBtn3;
    private TextView mSaveTips;
    public static final String ROOT_PATH = Environment.getExternalStorageDirectory() + "/efrobot/";
    public static final String ROOT_PARAMS_PATH = ROOT_PATH + "params/";

    private boolean isLeftStop = true;
    private boolean isRightStop = true;
    private boolean isBothStop = true;

    private int leftCameraIndex = 1;
    private int rightCameraIndex = 0;

    private BaseHandler mHandler;

    private CameraPresenter cameraPresenter;

    private int nCurrentCamIndex = leftCameraIndex;

    private boolean bODThread_End = false;
    private boolean bImageSave = false;
    private boolean bImageSendStart = false;
    //private boolean bCallBackRes = true;
    long currentTime = 0;
    int count = 0;
    private int model = 0;//物体检测
    public static final String SPEECH_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/efrobot/camera";
    private String strODResult = "";
    private String strSaveTime = "";
    private String strSavePath = "";

    //private ObjectDetectionThread objectDetectionThread = null;

    /**
     * 上传照片的结果数据
     */
    public ResultBean resultBean = new ResultBean();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fullScreen();
        setContentView(R.layout.binocularcam_layout);

        mHandler = new BaseHandler(this);

        mCameraShow_BV = (ImageView) findViewById(R.id.bCameraShow);//视频显示
        mDraw_BV = (com.efrobot.capturing.MySurfaceView)findViewById(R.id.bMyDraw);//检测结果显示
        mSaveTips = (TextView) findViewById(R.id.b_textView);

        leftCameraYUVData = new byte[width * height * 2];
        leftCameraRGBData = new byte[dwidth * dheight * 2];
        leftCameraBitmap = Bitmap.createBitmap(dwidth, dheight, Bitmap.Config.RGB_565);
        leftCameraBuffer = ByteBuffer.wrap(leftCameraRGBData);

        rightCameraYUVData = new byte[width * height * 2];
        rightCameraRGBData = new byte[dwidth * dheight * 2];
        rightCameraBitmap = Bitmap.createBitmap(dwidth, dheight, Bitmap.Config.RGB_565);
        rightCameraBuffer = ByteBuffer.wrap(rightCameraRGBData);

        cameraYUVData_Save = new byte[width * height * 2];
        cameraRGBData_Save = new byte[dwidth * dheight * 2];
        cameraBitmap_Save = Bitmap.createBitmap(dwidth, dheight, Bitmap.Config.RGB_565);
        cameraBuffer_Save = ByteBuffer.wrap(cameraRGBData_Save);

        String version = SystemProperties.get("ro.product.display", "unknow");
        Log.d(TAG, "version: " + version);
        if (!version.equals("unknow")) {
            String versionNumber = version.substring(6, 8);
            if(versionNumber.equals("00")) {
                Log.d(TAG, "美睿");
            }else if(versionNumber.equals("01")) {
                Log.d(TAG, "宇芯");
                leftCameraIndex = 0;
                rightCameraIndex =1;
            }
        }
        nCurrentCamIndex = leftCameraIndex;
        openCamera();
        //leftOpenCamera();
        //rightOpenCamera();
        ObjectDetectionThread objectDetectionThread = new ObjectDetectionThread();
        objectDetectionThread.start();

        /**
         * 开始
         */
        findViewById(R.id.begin_b_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                currentTime = 0;
                bODThread_End = false;
                bImageSendStart = true;
                bImageSave = true;

                findViewById(R.id.begin_b_camera).setEnabled(false);
                findViewById(R.id.stop_b_camera).setEnabled(true);
                mDraw_BV.setVisibility(View.VISIBLE);

                //Log.d(TAG, "这里是----开始按钮");
            }
        });

        /**
         * 停止
         */
        findViewById(R.id.stop_b_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //bODThread_End = true;
                bImageSendStart = false;

                findViewById(R.id.begin_b_camera).setEnabled(true);
                findViewById(R.id.stop_b_camera).setEnabled(false);

                //Log.d(TAG, "这里是----停止按钮");
                //mDraw_BV.clearDraw();
                mHandler.sendEmptyMessage(DETECTION_END);
            }
        });

        /**
         * 切换镜头
         */
        findViewById(R.id.switch_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //bODThread_End = true;
//                releaseCamera();
                if (nCurrentCamIndex == leftCameraIndex){
                    nCurrentCamIndex = rightCameraIndex;
                    mSaveTips.setText("双目-右摄像头");
//                    isLeftStop = true;
//                    isRightStop = false;
//                    rightOpenCamera();
                }
                else{
                    nCurrentCamIndex = leftCameraIndex;
                    mSaveTips.setText("双目-左摄像头");
//                    isLeftStop = false;
//                    isRightStop = true;
//                    leftOpenCamera();
                }
                //openCamera();
                bImageSendStart = false;

                findViewById(R.id.begin_b_camera).setEnabled(true);
                findViewById(R.id.stop_b_camera).setEnabled(true);

                //Log.d(TAG, "这里是----切换镜头");
                //mDraw_BV.clearDraw();
                mHandler.sendEmptyMessage(DETECTION_END);
            }
        });

        /**
         * 左转
         */
        findViewById(R.id.left_turn_btn).setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent){
                //按下操作
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    RobotManager.getInstance(theContext).getWheelInstance().moveLeft(200);
                }
                //抬起操作
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    RobotManager.getInstance(theContext).getWheelInstance().stop();
                }
                return  false;
            }
        });

        /**
         * 右转
         */
        findViewById(R.id.right_turn_btn).setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent){
                //按下操作
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    RobotManager.getInstance(theContext).getWheelInstance().moveRight(200);
                }
                //抬起操作
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    RobotManager.getInstance(theContext).getWheelInstance().stop();
                }
                return  false;
            }
        });

        /**
         * 前进
         */
        findViewById(R.id.forward_btn).setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent){
                //按下操作
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    RobotManager.getInstance(theContext).getWheelInstance().moveFront(200);
                }
                //抬起操作
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    RobotManager.getInstance(theContext).getWheelInstance().stop();
                }
                return  false;
            }
        });

        /**
         * 后退
         */
        findViewById(R.id.backward_btn).setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent){
                //按下操作
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    RobotManager.getInstance(theContext).getWheelInstance().moveBack(200);
                }
                //抬起操作
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    RobotManager.getInstance(theContext).getWheelInstance().stop();
                }
                return  false;
            }
        });
    }

    /**
     * 双摄像头初始化
     */
    private void openCamera(){
        int ret = -1;

        isBothStop = true;

        leftCamera = new StereoVision();
        if (leftCamera.qInuseL() != 1) {
            ret = leftCamera.openL(leftCameraIndex);
        }
        if (ret < 0) {
            Toast.makeText(this, "Open left camera failed", Toast.LENGTH_LONG).show();
            finish();
        } else {
            ret = leftCamera.initL(width, height, cameraBufferNumber);
            if (ret < 0) {
                Toast.makeText(this, "Init left camera failed", Toast.LENGTH_LONG).show();
                finish();
            } else {
                ret = leftCamera.streamonL();
                if (ret < 0) {
                    Toast.makeText(this, "Stream on left camera failed", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }

        ret = -1;

        //isRightStop = true;
        rightCamera = new StereoVision();
        if (rightCamera.qInuseR() != 1) {
            ret = rightCamera.openR(rightCameraIndex);
        }
        if (ret < 0) {
            Toast.makeText(this, "Open right Camera failed", Toast.LENGTH_LONG).show();
            finish();
        } else {
            ret = rightCamera.initR(width, height, cameraBufferNumber);
            if (ret < 0) {
                Toast.makeText(this, "Init right Camera failed", Toast.LENGTH_LONG).show();
                finish();
            } else {
                ret = rightCamera.streamonR();
                if (ret < 0) {
                    Toast.makeText(this, "Stream on right Camera failed", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }

        rightCamera.setCamParaR(0, 0, 0);
        leftCamera.setCamParaL(0, 0, 0);
        CameraBothReadThread cameraBothReadThread = new CameraBothReadThread();
        cameraBothReadThread.start();
    }

    /**
     * 左摄像头初始化
     */
    private void leftOpenCamera(){
        int ret = -1;

        isLeftStop = true;

        leftCamera = new StereoVision();

        if (leftCamera.qInuseL() != 1) {
            ret = leftCamera.openL(leftCameraIndex);
        }

        if (ret < 0) {
            Toast.makeText(this, "Open left camera failed", Toast.LENGTH_LONG).show();
        } else {
            ret = leftCamera.initL(width, height, cameraBufferNumber);

            if (ret < 0) {
                Toast.makeText(this, "Init left camera failed", Toast.LENGTH_LONG).show();
            } else {
                ret = leftCamera.streamonL();
                if (ret < 0) {
                    Toast.makeText(this, "Stream on left camera failed", Toast.LENGTH_LONG).show();
                }
            }
        }

        CameraReadThreadL cameraReadThread = new CameraReadThreadL();
        cameraReadThread.start();

    }

    /**
     * 右摄像头初始化
     */
    private void rightOpenCamera() {
        int ret = -1;

        isRightStop = true;

        rightCamera = new StereoVision();

        if (rightCamera.qInuseR() != 1) {
            ret = rightCamera.openR(rightCameraIndex);
        }

        if (ret < 0) {
            Toast.makeText(this, "Open right Camera failed", Toast.LENGTH_LONG).show();
        } else {
            ret = rightCamera.initR(width, height, cameraBufferNumber);
            if (ret < 0) {
                Toast.makeText(this, "Init right Camera failed", Toast.LENGTH_LONG).show();
            } else {
                ret = rightCamera.streamonR();
                if (ret < 0) {
                    Toast.makeText(this, "Stream on right Camera failed", Toast.LENGTH_LONG).show();
                }
            }
        }

        CameraReadThreadR cameraReadThread = new CameraReadThreadR();
        cameraReadThread.start();

        //isRightStop = false;
    }

    private void releaseCamera() {
        isLeftStop = true;
        isRightStop = true;
        isBothStop = true;

        if (leftCamera != null)
            leftCamera.releaseL();
        if (rightCamera != null)
            rightCamera.releaseR();

        leftCamera = null;
        rightCamera = null;
    }

    /**
     * 同时操作双摄像头的进程
     */
    class CameraBothReadThread extends Thread {
        @Override
        public void run() {
            isBothStop = false;

            while (true) {
                if (isBothStop) {
                    isBothStop = false;
                    break;
                }

                int lCameraIndex = leftCamera.dqbufL(leftCameraYUVData);
                if (lCameraIndex < 0) {
                    break;
                }
                int rCameraIndex = rightCamera.dqbufR(rightCameraYUVData);
                if (rCameraIndex < 0) {
                    break;
                }
                leftCamera.yuvtorgbL(leftCameraYUVData, leftCameraRGBData, dwidth, dheight);
                rightCamera.yuvtorgbR(rightCameraYUVData, rightCameraRGBData, dwidth, dheight);
                //leftCameraBitmap.copyPixelsFromBuffer(leftCameraBuffer);
                //rightCameraBitmap.copyPixelsFromBuffer(rightCameraBuffer);
                //当前显示的图像
                if (nCurrentCamIndex == rightCameraIndex)
                {
                    mHandler.sendEmptyMessage(UPDATE_UI_R);
                }
                else
                {
                    mHandler.sendEmptyMessage(UPDATE_UI_L);
                }

//                //启动图像检测的线程
//                if (objectDetectionThread == null)
//                {
//                    objectDetectionThread = new ObjectDetectionThread();
//                    objectDetectionThread.start();
//                }

//                if (isBothStop) {
//                    leftCamera.releaseL();
//                    rightCamera.releaseR();
//                    break;
//                }

                leftCamera.qbufL(lCameraIndex);
                rightCamera.qbufR(rCameraIndex);
            }
        }
    }


    /**
     * 左摄像头显示视频进程
     */
    class CameraReadThreadL extends Thread {
        @Override
        public void run() {
            isLeftStop = false;

            while (true) {
                if (isLeftStop) {
                    isLeftStop = false;
                    break;
                }
                if (nCurrentCamIndex == rightCameraIndex)
                {
                    break;
                }

                int leftCameraIndex = leftCamera.dqbufL(leftCameraYUVData);
                if (leftCameraIndex < 0) {
                    break;
                }

                leftCamera.yuvtorgbL(leftCameraYUVData, leftCameraRGBData, dwidth, dheight);

                mHandler.sendEmptyMessage(UPDATE_UI_L);

                leftCamera.qbufL(leftCameraIndex);
            }
        }
    }

    /**
     * 右摄像头显示视频进程
     */
    class CameraReadThreadR extends Thread {
        @Override
        public void run() {
            isRightStop = false;

            while (true) {
                if (isRightStop) {
                    isRightStop = false;
                    break;
                }
                if (nCurrentCamIndex == leftCameraIndex)
                {
                    break;
                }

                int rightCameraIndex = rightCamera.dqbufR(rightCameraYUVData);
                if (rightCameraIndex < 0) {
                    break;
                }

                rightCamera.yuvtorgbR(rightCameraYUVData, rightCameraRGBData, dwidth, dheight);

                mHandler.sendEmptyMessage(UPDATE_UI_R);

                rightCamera.qbufR(rightCameraIndex);
            }
        }
    }

//    private void openCamera(){
//        int ret = -1;
//
//        isStop = true;
//
//        bODThread_End = false;
//
//        currentCamera = new StereoVision();
//
//        if (nCurrentCamIndex == 0)
//        {
//            if (currentCamera.qInuseL() != 1) {
//                ret = currentCamera.openL(nCurrentCamIndex);
//            }
//
//            if (ret < 0) {
//                Toast.makeText(this, "Open left camera failed", Toast.LENGTH_LONG).show();
//            } else {
//                ret = currentCamera.initL(width, height, cameraBufferNumber);
//
//                if (ret < 0) {
//                    Toast.makeText(this, "Init left camera failed", Toast.LENGTH_LONG).show();
//                } else {
//                    ret = currentCamera.streamonL();
//                    if (ret < 0) {
//                        Toast.makeText(this, "Stream on left camera failed", Toast.LENGTH_LONG).show();
//                    }
//                }
//            }
//        }
//        else
//        {
//            if (currentCamera.qInuseR() != 1) {
//                ret = currentCamera.openR(nCurrentCamIndex);
//            }
//
//            if (ret < 0) {
//                Toast.makeText(this, "Open left camera failed", Toast.LENGTH_LONG).show();
//            } else {
//                ret = currentCamera.initR(width, height, cameraBufferNumber);
//
//                if (ret < 0) {
//                    Toast.makeText(this, "Init left camera failed", Toast.LENGTH_LONG).show();
//                } else {
//                    ret = currentCamera.streamonR();
//                    if (ret < 0) {
//                        Toast.makeText(this, "Stream on left camera failed", Toast.LENGTH_LONG).show();
//                    }
//                }
//            }
//        }
//
//        CameraReadThread cameraReadThread = new CameraReadThread();
//        cameraReadThread.start();
//        ObjectDetectionThread objectDetectionThread = new ObjectDetectionThread();
//        objectDetectionThread.start();
//    }
//
////    private void rightOpenCamera() {
////        int ret = -1;
////
////        isStop = true;
////
////        rightCamera = new StereoVision();
////
////        if (rightCamera.qInuseR() != 1) {
////            ret = rightCamera.openR(rightCameraIndex);
////        }
////
////        if (ret < 0) {
////            Toast.makeText(this, "Open right Camera failed", Toast.LENGTH_LONG).show();
////        } else {
////            ret = rightCamera.initR(width, height, cameraBufferNumber);
////            if (ret < 0) {
////                Toast.makeText(this, "Init right Camera failed", Toast.LENGTH_LONG).show();
////            } else {
////                ret = rightCamera.streamonR();
////                if (ret < 0) {
////                    Toast.makeText(this, "Stream on right Camera failed", Toast.LENGTH_LONG).show();
////                }
////            }
////        }
////
////        CameraReadThreadR cameraReadThread = new CameraReadThreadR();
////        cameraReadThread.start();
////
////        isStop = false;
////    }
//
//    private void releaseCamera() {
//        isStop = true;
//        bODThread_End = true;
//
//        if (currentCamera != null)
//        {
//            if (nCurrentCamIndex == 0)
//            {
//                currentCamera.releaseL();
//            }
//            else
//            {
//                currentCamera.releaseR();
//            }
//        }
//
//        currentCamera = null;
//
//    }
//
//    class CameraReadThread extends Thread {
//        @Override
//        public void run() {
//            isStop = false;
//
//            while (true) {
//                if (isStop) {
//                    isStop = false;
//                    break;
//                }
//
//                if (nCurrentCamIndex == 0)
//                {
//                    int cameraIndex = currentCamera.dqbufL(cameraYUVData);
//                    if (cameraIndex < 0) {
//                        break;
//                    }
//
//                    currentCamera.yuvtorgbL(cameraYUVData, cameraRGBData, dwidth, dheight);
//
//                    mHandler.sendEmptyMessage(UPDATE_UI_L);
//
//                    currentCamera.qbufL(nCurrentCamIndex);
//                }
//                else
//                {
//                    int cameraIndex = currentCamera.dqbufR(cameraYUVData);
//                    if (cameraIndex < 0) {
//                        break;
//                    }
//
//                    currentCamera.yuvtorgbR(cameraYUVData, cameraRGBData, dwidth, dheight);
//
//                    mHandler.sendEmptyMessage(UPDATE_UI_R);
//
//                    currentCamera.qbufR(nCurrentCamIndex);
//                }
//
//            }
//        }
//    }

    /**
     * 新线程：图片上传到服务器，接收服务器处理后的数据，并绘图
     */
    class ObjectDetectionThread extends Thread {
        @Override
        public void run() {
            bODThread_End = false;
            while (true) {
                if (bODThread_End) {
                    bODThread_End = false;
                    //bImageSave = false;
                    break;
                }
//                if (isBothStop) {
//                    isBothStop = false;
//                    //bImageSave = false;
//                    break;
//                }
                if (nCurrentCamIndex == leftCameraIndex)
                {
//                    int cameraIndex = leftCamera.dqbufL(cameraYUVData_Save);
//                    if (cameraIndex < 0) {
//                        break;
//                    }

                    if (bImageSendStart && bImageSave) {
//                       System.arraycopy(leftCameraYUVData,0,cameraYUVData_Save,0,leftCameraYUVData.length);
//                        System.arraycopy(leftCameraRGBData,0,cameraRGBData_Save,0,leftCameraRGBData.length);
//                        leftCamera.yuvtorgbL(cameraYUVData_Save, cameraRGBData_Save, dwidth, dheight);
//                        cameraBitmap_Save.copyPixelsFromBuffer(cameraBuffer_Save);
//                        save4SendFile(width,height,cameraYUVData_Save);
                        //Log.d(TAG, "这里是----图像检测");
                        bImageSave = false;//等待当前图片处理完毕再继续下一张图片
                        String name = getDate();
                        Bitmap leftCameraBitmapTemp = leftCameraBitmap.copy(Bitmap.Config.RGB_565,true);
                        Bitmap rightCameraBitmapTemp = rightCameraBitmap.copy(Bitmap.Config.RGB_565,true);
                        //bCallBackRes = true;
                        save(leftCameraBitmapTemp,name,true);
                        //bCallBackRes = false;
                        save(rightCameraBitmapTemp,name,false);

                        //mHandler.sendEmptyMessage(SHOW_IMAGE);
                        //leftCamera.qbufL(nCurrentCamIndex);
                    }
                }
                else
                {
//                    int cameraIndex = rightCamera.dqbufR(cameraYUVData_Save);
//                    if (cameraIndex < 0) {
//                        break;
//                    }

                    if (bImageSendStart && bImageSave) {
//                        System.arraycopy(rightCameraYUVData,0,cameraYUVData_Save,0,leftCameraYUVData.length);
//                        rightCamera.yuvtorgbR(cameraYUVData_Save, cameraRGBData_Save, dwidth, dheight);
//                        cameraBitmap_Save.copyPixelsFromBuffer(cameraBuffer_Save);
                        //save4SendFile(width,height,cameraYUVData_Save);
                        bImageSave = false;//等待当前图片处理完毕再继续下一张图片
                        String name = getDate();
                        Bitmap leftCameraBitmapTemp = leftCameraBitmap.copy(Bitmap.Config.RGB_565,true);
                        Bitmap rightCameraBitmapTemp = rightCameraBitmap.copy(Bitmap.Config.RGB_565,true);
                        //bCallBackRes = true;
                        save(rightCameraBitmapTemp,name,true);
                        //bCallBackRes = false;
                        save(leftCameraBitmapTemp,name,false);
                        //mHandler.sendEmptyMessage(SHOW_IMAGE);
                       // rightCamera.qbufR(nCurrentCamIndex);
                    }
                }

            }
        }
    }


    /**
     * 保存
     *
     * @param bitmap
     */
    private void save(Bitmap bitmap,String name,boolean bCallBackRes) {
        final String timeName;
        if (currentTime == 0) {

            File directory;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                directory = new File(SPEECH_PATH + "/pictures");
                if (directory.exists()) {

                    final File[] fileList = directory.listFiles();
                    for (File me : fileList) {
                        me.delete();
                    }
                }

            }

            File fileText = new File(SPEECH_PATH + "/cameName.txt");
            if (fileText.exists())
                fileText.delete();
            timeName = getShowTime(0);
            currentTime = System.currentTimeMillis();
        } else {
            timeName = getShowTime(System.currentTimeMillis() - currentTime);
        }
        FileOutputStream fos = null;
        try {
            File directory;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                directory = new File(SPEECH_PATH + "/pictures");
            } else {
                return;
            }
            if (!directory.exists()) {
                directory.mkdirs();
            }

            //String name = getDate();

            /**
             * 发送到服务器,区分左右摄像头的图片
             */
            String strImageFileName;
            if (nCurrentCamIndex == leftCameraIndex)
            {
                if (bCallBackRes)
                {
                    strImageFileName = name + "_left";
                }
                else
                {
                    strImageFileName = name + "_right";
                }
            }
            else
            {
                if (bCallBackRes)
                {
                    strImageFileName = name + "_right";
                }
                else
                {
                    strImageFileName = name + "_left";
                }
            }

            File file = new File(directory, strImageFileName + ".png");

            fos = new FileOutputStream(file);
            boolean compress = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            if (compress) {
                method1(SPEECH_PATH + "/cameName.txt", timeName);
                count++;
            }
            sendFile(SPEECH_PATH + "/pictures/" + strImageFileName + ".png", name, bCallBackRes);
            L.e("===>>>", name + " 保存是否成功:" + compress + "  file.exists:" + file.exists());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //closeCamera();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


    }

    private String getShowTime(final long l) {
        if (l == 0) {
            return "0.000000e+00\r";
        }

        BigDecimal cd3 = new BigDecimal(l * 0.001, MathContext.DECIMAL32);// 构造BigDecimal时指定有效精度
        BigDecimal cd2 = new BigDecimal(cd3.floatValue(), new MathContext(7, RoundingMode.HALF_EVEN));// 构造BigDecimal时指定有效精度

        final String valie = String.valueOf(cd2);

        String current1 = "";
        int current2;
        String mindle = "";

        if (cd2.doubleValue() > 1) {
            int len;
            if (valie.contains(".")) {
                len = valie.indexOf(".");
            } else {
                len = valie.length();
            }

            final BigDecimal dn = cd2.movePointLeft(len - 1);
            current1 = dn.toEngineeringString();
            mindle = "e+";
            current2 = (len - 1);
        } else {
            final String[] arr = valie.split("|");
            int count = 0;
            for (String me : arr) {
                if (!TextUtils.isEmpty(me)) {
                    if (me.equals("0")) {
                        count++;
                    } else {
                        if (!me.equals(".")) {
                            break;
                        }
                    }
                }
            }
            final BigDecimal dn = cd2.movePointRight(count);
            current1 = dn.toEngineeringString();
            mindle = "e-";
            current2 = count;
        }

        if (current1.length() < 8) {
            int len = current1.length();
            for (int i = 0; i < 8 - len; i++) {

                if (current1.contains(".")) {
                    current1 += "0";
                } else {
                    if (i == 0) {
                        current1 += ".";
                    } else {
                        current1 += "0";
                    }
                }

            }
        }

        String end;
        if (current2 < 10) {
            end = "0" + current2;
        } else {
            end = current2 + "";
        }
        return current1 + mindle + end + "\r";
    }

    /**
     * 时间,精确到毫秒
     */
    public String getDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        long time = System.currentTimeMillis();
        Date curDate = new Date(time);//获取当前时间
        String datetime = formatter.format(curDate);
        return datetime;
    }

    /**
     * 追加文件：使用FileOutputStream，在构造FileOutputStream时，把第二个参数设为true
     */
    public static void method1(String file, String conent) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file, true)));
            out.write(conent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送文件
     *
     * @param path 文件路径
     */
    private void sendFile(final String path, final String time, final boolean bCallBackRes) {
        try {

            final HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("file", path);

            final FileMessage message = new FileMessage();
            message.setMediaTypeMarkdown("multipart/form-data; charset=utf-8");
            message.addFile(path);///storage/emulated/0/efrobot/camera/pictures/2017-05-18 02:31:48.png
            message.getBody().put("modelType", model);//当前应用默认支持物体检测
            message.setUrl(AndroidConstants.UPLOAD_URL);//http://101.200.194.252:8080/v1/word/form/pic
            message.setRequestMethod(FileMessage.REQUEST_METHOD_POST);
            message.setSendMethod(FileMessage.SEND_METHOD_INTERNET);
            message.setConnectTimeout(10 * 60);

            L.e("Log=====", "发送文件URL--->" + message.getUrl());
            ControlApplication.from(theContext).getNetClient().sendNetMessage(message, new SendRequestListener<FileMessage>() {
                @Override
                public void onFail(FileMessage message, int errorCode, String errorMessage) {
                    L.e("===>>", "onFail   errorMessage=" + errorMessage);
                    mHandler.sendEmptyMessage(SEND_IMAGE_FAILURE);
                    bImageSave = true;
                }

                @Override
                public void onSending(FileMessage message, long total, long current) {

                }

                @Override
                public void onSuccess(FileMessage message, String result) {
                    L.e("===>>", "onSuccess=" + result);
                    bImageSave = true;
                    if (!TextUtils.isEmpty(result) && !bODThread_End && bCallBackRes) {
                        strODResult = result.toString();
                        strSavePath = path;
                        strSaveTime = time;
                        mHandler.sendEmptyMessage(SHOW_IMAGE);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    class CameraReadThreadR extends Thread {
//        @Override
//        public void run() {
//            isStop = false;
//
//            while (true) {
//                if (isStop) {
//                    isStop = false;
//                    break;
//                }
//
//
//                int rightCameraIndex = rightCamera.dqbufR(rightCameraYUVData);
//                if (rightCameraIndex < 0) {
//                    break;
//                }
//
//                rightCamera.yuvtorgbR(rightCameraYUVData, rightCameraRGBData, dwidth, dheight);
//
//                mHandler.sendEmptyMessage(UPDATE_UI_R);
//
//                rightCamera.qbufR(rightCameraIndex);
//            }
//        }
//    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case UPDATE_UI_L:
                leftCameraBitmap.copyPixelsFromBuffer(leftCameraBuffer);
                rightCameraBitmap.copyPixelsFromBuffer(rightCameraBuffer);
                mCameraShow_BV.setImageBitmap(leftCameraBitmap);
                leftCameraBuffer.clear();
                rightCameraBuffer.clear();
                break;
            case UPDATE_UI_R:
                leftCameraBitmap.copyPixelsFromBuffer(leftCameraBuffer);
                rightCameraBitmap.copyPixelsFromBuffer(rightCameraBuffer);
                mCameraShow_BV.setImageBitmap(rightCameraBitmap);
                leftCameraBuffer.clear();
                rightCameraBuffer.clear();
                break;
            case SHOW_IMAGE:
                if (!bImageSendStart)//如果当前已停止检测，则应立即清除结果显示
                {
                    //Log.d(TAG, "这里是----图像检测停止");
                    mDraw_BV.clearDraw();
                    break;
                }
                /**
                 * 拍照上传成功
                 */
                long endTime = System.currentTimeMillis();
                L.e("===>>>>", "end:::" + (endTime));
                String data = strODResult;
                try {
                    if (TextUtils.isEmpty(data)) {
                        RobotToastUtil.getInstance(theContext).showToast("上传失败");
                        return;
                    }
                    JSONObject jsonObject = new JSONObject(data);
                    if (jsonObject.has("resultCode") && "SUCCESS".equals(jsonObject.optString("resultCode"))) {
                        String strObjects = null;
                        //tomcat 客户端定义
                        if (jsonObject.has("objects")) {
                            JSONArray jsonArray = jsonObject.optJSONArray("objects");
                            if (jsonArray != null) {
                                //解析为OneObjectResult类
                                List<OneObjectResult> objectResults = new ArrayList<OneObjectResult>();
                                for (int i = 0; i < jsonArray.length(); i++)
                                {
                                    JSONObject myObject = jsonArray.getJSONObject(i);
                                    String classname = myObject.getString("strClass");
                                    double prob = myObject.getDouble("dProb");
                                    int xMin = myObject.getInt("nXmin");
                                    int xMax = myObject.getInt("nXmax");
                                    int yMin = myObject.getInt("nYmin");
                                    int yMax = myObject.getInt("nYmax");
                                    OneObjectResult object1 = new OneObjectResult(classname,prob,xMin,yMin,xMax,yMax);
                                    objectResults.add(object1);
                                }
                                //resultBean = new ResultBean(objectResults, "",time, path);
                                resultBean.setObjectResults(objectResults);
                                resultBean.setDate(strSaveTime);
                                resultBean.setPath(strSavePath);
                                resultBean.setSendUrl("");

                                if (resultBean != null) {
                                    mDraw_BV.setResultBean(resultBean);
                                    mDraw_BV.drawCanvas();
                                }
                            }
                        }

                    } else {
                        RobotToastUtil.getInstance(theContext).showToast("上传失败");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case SEND_IMAGE_FAILURE:
                RobotToastUtil.getInstance(theContext).showToast("图片上传失败,请检查网络");
                break;
            case DETECTION_END:
                //Log.d(TAG, "这里是----图像检测停止");
                mDraw_BV.clearDraw();
                break;
            case NEXT_TEST:
//                isLeftStop = true;
//                isRightStop = true;
                isBothStop = true;
                bODThread_End = true;

                if (leftCamera != null)
                    leftCamera.releaseL();
                if (rightCamera != null)
                    rightCamera.releaseR();

                leftCamera = null;
                rightCamera = null;

                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode: " + requestCode
                + ", resultCode: " + resultCode + ", data: " + data);

        if (BINOCULAR_VISION_ACTIVITY_REQUEST_CODE == requestCode) {
            Log.d(TAG, "Binocular Vision TEST");

            if (0 == resultCode) {
                Log.d(TAG, "RESULT_OK");
//                openCamera();
//                passBtn.setEnabled(true);
            } else {
//                passBtn.setEnabled(false);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        releaseCamera();
        bODThread_End = true;

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    public void fullScreen() {
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            uiFlags |= 0x00001000;
        } else {
            uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }
        getWindow().getDecorView().setSystemUiVisibility(uiFlags);
    }

    private static class BaseHandler extends Handler {
        private final WeakReference<BinocularVisionActivity> mObjects;

        public BaseHandler(BinocularVisionActivity mCoreService) {
            mObjects = new WeakReference<BinocularVisionActivity>(mCoreService);
        }

        @Override
        public void handleMessage(Message msg) {
            BinocularVisionActivity mObject = mObjects.get();
            if (mObject != null)
                mObject.handleMessage(msg);
        }
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            Intent myIntent = new Intent();
            myIntent = new Intent(BinocularVisionActivity.this, HomeActivity.class);
            startActivity(myIntent);
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
