package com.efrobot.capturing;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;


import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.net.FileMessage;
import com.efrobot.library.net.SendRequestListener;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class CameraCallback implements SurfaceHolder.Callback {
    private Context mContext;
    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;

    private static final int FOCUS_BEEP_VOLUME = 100;
    public ToneGenerator mFocusToneGenerator;

    private Parameters mParameters;
    private int screenWidth;
    private int screenHeight;

    private int mCameraId;

    private String TAG = "camera";
    /**
     * 摄像头个数
     */
    public int mNumberOfCameras;

    /**
     * 相机信息
     */
    private CameraInfo[] mInfo;
    protected int MESSAGE_SVAE_SUCCESS = 22;
    protected int MESSAGE_SVAE_FAILURE = 23;
    private boolean videoQualityHigh;
    private CamcorderProfile mProfile;
    private boolean mStart = false;
//    /**
//     * 只拍照一次
//     */
//    private boolean mbOnce = false;

    private Onfinish mOnFinish;
    private Mytask current;

    /**
     * 当前识别模式
     */
    private int model = 0;//物体检测


    public CameraCallback(Context context) {
        this.mContext = context;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = Math.min(dm.widthPixels, dm.heightPixels);
        screenHeight = Math.max(dm.widthPixels, dm.heightPixels);
        initializeFocusTone();
        mCameraId = 0;
        mNumberOfCameras = Camera.getNumberOfCameras();
        mInfo = new CameraInfo[mNumberOfCameras];
        for (int i = 0; i < mNumberOfCameras; i++) {
            mInfo[i] = new CameraInfo();
            Camera.getCameraInfo(i, mInfo[i]);
        }
        videoQualityHigh = true;
        mProfile = CamcorderProfile.get(mCameraId, videoQualityHigh ? CamcorderProfile.QUALITY_HIGH : CamcorderProfile.QUALITY_LOW);

    }

    public CameraInfo[] getCameraInfo() {
        return mInfo;
    }

    // Use a singleton.
    private static CameraCallback callback;

    public static synchronized CameraCallback instance() {
        if (callback == null) {
            callback = new CameraCallback();
        }
        return callback;
    }

    private CameraCallback() {
        mNumberOfCameras = Camera.getNumberOfCameras();
        mInfo = new CameraInfo[mNumberOfCameras];
        for (int i = 0; i < mNumberOfCameras; i++) {
            mInfo[i] = new CameraInfo();
            Camera.getCameraInfo(i, mInfo[i]);
        }
    }

    private void initializeFocusTone() {
        // Initialize focus tone generator.
        try {
            mFocusToneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM,
                    FOCUS_BEEP_VOLUME);
        } catch (Throwable ex) {
            Log.w("", "Exception caught while creating tone generator: ", ex);
            mFocusToneGenerator = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        if (holder.getSurface() == null) {
            Log.d(TAG, "holder.getSurface() == null");
            return;
        }
        // We need to save the holder for later use, even when the mCameraDevice
        // is null. This could happen if onResume() is invoked after this
        // function.
        mSurfaceHolder = holder;

        // The mCameraDevice will be null if it fails to connect to the camera
        // hardware. In this case we will show a dialog and then finish the
        // activity, so it's OK to ignore it.
        if (mCamera == null)
            return;

        if (holder.isCreating()) {
            // Set preview display if the surface is being created and preview
            // was already started. That means preview display was set to null
            // and we need to set it now.
            setPreviewDisplay(holder);
        } else {
            // 1. Restart the preview if the size of surface was changed. The
            // framework may not support changing preview display on the fly.
            // 2. Start the preview now if surface was destroyed and preview
            // stopped.
            restartPreview(handler);
        }


    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e("tag", " surfaceDestroyed");
        closeCamera();
    }

    private void setPreviewDisplay(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("setPreviewDisplay failed", ex);
        }
    }

//    public int getNumberOfCameras() {
//        try {
//            Method method = Camera.getMethod("getNumberOfCameras", null);
//            if (method != null) {
//                Object object = method.invoke(mCamera, null);
//                if (object != null) {
//                    return (Integer) object;
//                }
//            }s
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return 0;
//    }


    public static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        // See android.hardware.Camera.setCameraDisplayOrientation for
        // documentation.
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int degrees = getDisplayRotation(activity);
        int result;
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
           result = (info.orientation + degrees) % 360;
           result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public Camera open(int i) {
        try {
            Method method = Camera.class.getMethod("open", int.class);
            if (method != null) {
                Object object = method.invoke(mCamera, i);
                if (object != null) {
                    return (Camera) object;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Camera getCamera() {
        return mCamera;
    }

    public static final String SPEECH_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/efrobot/camera";

    private Handler handler;

    long startTime = 0;
    long endTime = 0;

    // 拍照-----没用到？
    public void takePicture(final Handler handler) {
        if (mCamera == null)
            return;
        mCamera.takePicture(null, null, new PictureCallback() {

            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {

                L.e("====>>>", "Time::" + System.currentTimeMillis() + "=====" + (System.currentTimeMillis() - startTime));

                restartPreview(handler);
                save(data, handler);
                handler.sendEmptyMessage(MESSAGE_SVAE_FAILURE);
            }
        });

    }

    int count = 0;
    int neamLength = 6;
    long currentTime = 0;

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
     * 拍照后的图片保存
     *
     * @param data
     * @param handler
     */
    private void save(final byte[] data, final Handler handler) {
        this.handler = handler;
        long stay = System.currentTimeMillis();
        L.e("====>>>", "save_begin::" + stay);
        FileOutputStream fos = null;
        try {
            File directory;

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                directory = new File(SPEECH_PATH + "/pictures");
            } else {
                if (handler != null)
                    handler.sendEmptyMessage(MESSAGE_SVAE_FAILURE);
                return;
            }

            if (!directory.exists()) {
                directory.mkdirs();
            }


            int len = String.valueOf(count).length();
            String name = "";
            if (neamLength > len) {
                for (int i = 0; i < (neamLength - len); i++) {
                    name += "0";
                }
                name += count;
            } else {
                name = String.valueOf(count);
            }


            File file = new File(directory, name + ".png");
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            bitmap = convertGreyImg(bitmap);
            fos = new FileOutputStream(file);
            boolean compress = bitmap.compress(CompressFormat.PNG, 100, fos);
            Message msg = new Message();

            if (compress) {
                count++;
                msg.what = MESSAGE_SVAE_SUCCESS;
                msg.obj = file.getPath();


                if (currentTime == 0) {
                    currentTime = System.currentTimeMillis();
                    File fileText = new File(SPEECH_PATH + "/cameName.txt");


                    if (fileText.exists())
                        fileText.delete();
                    if (directory.exists()) {

                        final File[] arr = directory.listFiles();
                        for (File me : arr) {
                            me.delete();
                        }
                    }

                    method1(SPEECH_PATH + "/cameName.txt", getShowTime(0));
                } else {
                    method1(SPEECH_PATH + "/cameName.txt", getShowTime(System.currentTimeMillis() - currentTime));
                }

            }
            L.e("====>>>", "save_end::" + System.currentTimeMillis() + "===" + (System.currentTimeMillis() - stay));
            L.e("===>>>", name + " 保存是否成功:" + compress + "  file.exists:" + file.exists());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
     * 将彩色图转换为灰度图
     *
     * @param img 位图
     * @return 返回转换好的位图
     */
    public Bitmap convertGreyImg(Bitmap img) {
        int width = img.getWidth();         //获取位图的宽
        int height = img.getHeight();       //获取位图的高

        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    /**
     * 多镜头切换
     *
     * @param surfaceView
     * @param mCameraId
     */
    public void switchCamera(SurfaceView surfaceView, int mCameraId, Handler handler) {
        if (handler != null)
            this.handler = handler;
        closeCamera();

        this.mCameraId = mCameraId;
        try {
            startPreview(handler);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public boolean isSupportedZoom() {
        if (mCamera != null) {
            Parameters parameters = mCamera.getParameters();
            return parameters.isZoomSupported();
        }
        return false;
    }

    public int getMaxZoom() {
        if (mCamera == null) {
            mCamera = Camera.open(mCameraId);
        }
        mParameters = mCamera.getParameters();
        return mParameters.getMaxZoom();
    }

    // 设置Zoom
    public void setZoom(int value) {
        Log.i("tag", "value:" + value);
        mParameters.setZoom(value);
        mCamera.setParameters(mParameters);
        mCamera.startPreview();
    }

    public boolean isSupportedFlashMode() {
        if (mCamera == null) {
            mCamera = Camera.open(mCameraId);
        }
        Parameters parameters = mCamera.getParameters();
        List<String> modes = parameters.getSupportedFlashModes();
        if (modes != null && modes.size() != 0) {
            boolean autoSupported = modes.contains(Parameters.FLASH_MODE_AUTO);
            boolean onSupported = modes.contains(Parameters.FLASH_MODE_ON);
            boolean offSupported = modes.contains(Parameters.FLASH_MODE_OFF);
            return autoSupported && onSupported && offSupported;
        }
        return false;
    }

    /**
     * 自动对焦
     */
    public void autoFocus() {

        String mFocusMode = mParameters.getFocusMode();

        if (mFocusMode.equals(Parameters.FOCUS_MODE_AUTO)
                || mFocusMode.equals(Parameters.FOCUS_MODE_MACRO)) {
            if (mCamera != null) {
                mCamera.autoFocus(new Camera.AutoFocusCallback() {

                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                    }
                });
            }
        }
    }

    /**
     * 设置相机参数
     *
     * @param mParameters
     */
    private void setCameraParameters(Parameters mParameters) {
        // 设置多少帧
        updateCameraParametersInitialize();
        // 设置holder
        setPreviewDisplay(mSurfaceHolder);
        Size size = mParameters.getPreviewSize();
        // 设置照相机属性
        updateCameraParametersPreference();
        //
        if (mCamera != null)
            mCamera.setParameters(mParameters);
    }

    /**
     * 设置多少帧
     */
    private void updateCameraParametersInitialize() {
        // Reset preview frame rate to the maximum because it may be lowered by
        // video camera application.
        List<Integer> frameRates = mParameters.getSupportedPreviewFrameRates();
        if (frameRates != null) {
            Integer max = Collections.min(frameRates);
            mParameters.setPreviewFrameRate(max);
            L.e("==========>>>>>", frameRates.toString() + "==" + max + "");
        }

    }

    /**
     * 设置照相机属性
     */
    private void updateCameraParametersPreference() {

        mParameters.setPreviewSize(640, 480);
        mParameters.setPictureSize(640, 480);
        CameraSettings.initialCameraPictureSize(mContext, mParameters);


        Size size = mParameters.getPictureSize();
        mParameters.setPictureSize(640, 480);
        List<Size> sizes = mParameters.getSupportedPreviewSizes();
        Size optimalSize = null;
        for (Size siz : sizes) {
//            L.e("====>>>", "预览" + siz.width + "x" + siz.height);

            if ((double) size.width / size.height == (double) size.width / size.height) {
//                L.e("====>>>", "设置的预览大小是" + siz.width + "x" + siz.height);
                optimalSize = siz;
                break;
            }


        }


        if (optimalSize == null)
            optimalSize = getOptimalPreviewSize(sizes, (double) size.width / size.height);

        if (optimalSize != null) {
            Size original = mParameters.getPreviewSize();
            if (!original.equals(optimalSize)) {
//                mParameters.setPreviewSize(optimalSize.width, optimalSize.height);
                mParameters.setPreviewSize(640, 480);
                mCamera.setParameters(mParameters);
                mParameters = mCamera.getParameters();
            }
        }
        mParameters.setPreviewSize(640, 480);
        mParameters.setPictureSize(640, 480);
        // Since change scene mode may change supported values,
        // Set scene mode first,
        String mSceneMode = "auto";
        if (isSupported(mSceneMode, mParameters.getSupportedSceneModes())) {
            if (!mParameters.getSceneMode().equals(mSceneMode)) {
                mParameters.setSceneMode(mSceneMode);
                mCamera.setParameters(mParameters);

                // Setting scene mode will change the settings of flash mode,
                // white balance, and focus mode. Here we read back the
                // parameters, so we can know those settings.
                mParameters = mCamera.getParameters();
            }
        } else {
            mSceneMode = mParameters.getSceneMode();
            if (mSceneMode == null) {
                mSceneMode = Parameters.SCENE_MODE_AUTO;
            }
        }

        // Set JPEG quality.
        String jpegQuality = "superfine";
        mParameters.setJpegQuality(JpegEncodingQualityMappings
                .getQualityNumber(jpegQuality));

        // For the following settings, we need to check if the settings are
        // still supported by latest driver, if not, ignore the settings.

        // Set color effect parameter.
        String colorEffect = "none";
        if (isSupported(colorEffect, mParameters.getSupportedColorEffects())) {
            mParameters.setColorEffect(colorEffect);
        }

        // Set exposure compensation
        String exposure = "0";
        try {
            int value = Integer.parseInt(exposure);
            int max = mParameters.getMaxExposureCompensation();
            int min = mParameters.getMinExposureCompensation();
            if (value >= min && value <= max) {
                mParameters.setExposureCompensation(value);
            } else {
                Log.w(TAG, "invalid exposure range: " + exposure);
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "invalid exposure: " + exposure);
        }

        String mFocusMode;
        if (Parameters.SCENE_MODE_AUTO.equals(mSceneMode)) {
            // Set flash mode.
            String flashMode = "off";
            List<String> supportedFlash = mParameters.getSupportedFlashModes();
            if (isSupported(flashMode, supportedFlash)) {
                mParameters.setFlashMode(flashMode);
            } else {
                flashMode = mParameters.getFlashMode();
                if (flashMode == null) {
                    flashMode = "no_flash";
                }
            }

            // Set white balance parameter.
            String whiteBalance = "auto";
            if (isSupported(whiteBalance,
                    mParameters.getSupportedWhiteBalance())) {
                mParameters.setWhiteBalance(whiteBalance);
            } else {
                whiteBalance = mParameters.getWhiteBalance();
                if (whiteBalance == null) {
                    whiteBalance = Parameters.WHITE_BALANCE_AUTO;
                }
            }

            // Set focus mode.
            mFocusMode = "auto";
            if (isSupported(mFocusMode, mParameters.getSupportedFocusModes())) {
                mParameters.setFocusMode(mFocusMode);
            } else {
                mFocusMode = mParameters.getFocusMode();
                if (mFocusMode == null) {
                    mFocusMode = Parameters.FOCUS_MODE_AUTO;
                }
            }
        } else {
            mFocusMode = mParameters.getFocusMode();
        }
    }

    private static boolean isSupported(String value, List<String> supported) {
        return supported == null ? false : supported.indexOf(value) >= 0;
    }

    private Size getOptimalPreviewSize(List<Size> sizes, double targetRatio) {
        final double ASPECT_TOLERANCE = 0.05;
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        // Because of bugs of overlay and layout, we sometimes will try to
        // layout the viewfinder in the portrait orientation and thus get the
        // wrong size of mSurfaceView. When we change the preview size, the
        // new overlay will be created before the old one closed, which causes
        // an exception. For now, just get the screen size

        int targetHeight = Math.min(screenHeight, screenWidth);

        if (targetHeight <= 0) {
            WindowManager windowManager = (WindowManager) mContext
                    .getSystemService(Context.WINDOW_SERVICE);
            targetHeight = windowManager.getDefaultDisplay().getHeight();
        }

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            Log.v(TAG, "No preview size match the aspect ratio");
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void restartPreview(Handler handler) {
        // TODO Auto-generated method stub

        try {
            startPreview(handler);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    boolean isNeed = true;

//    Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(final Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case 0:
//                    isNeed = true;
//                    break;
//            }
//        }
//    };

    long begin;
    /**
     * 当前是否需要保存
     */
    private boolean currentSave = false;

    public void startPreview(Handler handler) throws Exception {
        this.handler = handler;
        ensureCameraDevice();

        setCameraParameters(mParameters);

        try {
            Log.v(TAG, "startPreview");
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(final byte[] data, final Camera camera) {

                    if (mStart && currentSave) {
                        L.e("===>>>", "开始的时间是" + (begin == 0 ? begin : (System.currentTimeMillis() - begin)));

                        begin = System.currentTimeMillis();
                        int w = camera.getParameters().getPreviewSize().width;
                        int h = camera.getParameters().getPreviewSize().height;
                        save(w, h, data);
                        endTime = System.currentTimeMillis();
                        currentSave = false;
                        L.e("====>>>>>", (endTime - begin) + ":::" + begin);
                    }

                }
            });
            mCamera.startPreview();
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("startPreview failed", ex);
        }
    }

    /**
     * 转换格式之后保存
     *
     * @param w
     * @param h
     * @param data
     */
    public void save(int w, int h, byte[] data) {
        //预览编码为YUV420SP的视频流，需转换为RGB编码
        int[] RGBData = new int[w * h];
        byte[] mYUVData = new byte[data.length];
        System.arraycopy(data, 0, mYUVData, 0, data.length);
        decodeYUV420SP(RGBData, mYUVData, w, h);

        //图片保存到sdcard
        Bitmap bitmap = Bitmap.createBitmap(RGBData, w, h, Bitmap.Config.RGB_565);
        try {
            save(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
            closeCamera();
        }
    }

    /**
     * 预览编码为YUV420SP的视频流，转换为RGB编码
     *
     * @param rgb
     * @param yuv420sp
     * @param width
     * @param height
     */
    static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

    public void setOnFinish(final Onfinish onFinish) {
        mOnFinish = onFinish;
    }

    public void setModel(int model) {
        this.model = model;
    }


    class Mytask extends AsyncTask<Integer, Integer, Bitmap> {


        private final String timeName;
        private Bitmap bitmap;

        public Mytask(final Bitmap bitmap, final String timeName) {
            this.bitmap = bitmap;
            this.timeName = timeName;
        }

        @Override
        protected Bitmap doInBackground(final Integer... params) {

            return null;
        }

        @Override
        protected void onPostExecute(final Bitmap bitmap) {
            super.onPostExecute(bitmap);
            list.remove(this);
            current = null;
        }
    }


    /**
     * 图片转灰度
     *
     * @param bmSrc
     * @return
     */
    public static Bitmap bitmap2Gray(Bitmap bmSrc) {
        int width, height;
        height = bmSrc.getHeight();
        width = bmSrc.getWidth();
        Bitmap bmpGray = null;
        bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGray);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmSrc, 0, 0, paint);

        return bmpGray;
    }


    ArrayList<Mytask> list = new ArrayList<>();

    /**
     * 保存
     *
     * @param bitmap
     */
    private void save(Bitmap bitmap) {
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
//            int len = String.valueOf(count).length();
//            String name = "";
//            if (neamLength > len) {
//                for (int i = 0; i < (neamLength - len); i++) {
//                    name += "0";
//                }
//                name += count;
//            } else {
//                name = String.valueOf(count);
//            }

            String name = getDate();

            File file = new File(directory, name + ".png");


//            bitmap = bitmap2Gray(bitmap);

            fos = new FileOutputStream(file);
            boolean compress = bitmap.compress(CompressFormat.PNG, 100, fos);
            if (compress) {
                method1(SPEECH_PATH + "/cameName.txt", timeName);
                count++;
            }

            /**
             * 发送到服务器
             */
            sendFile(SPEECH_PATH + "/pictures/" + name + ".png", name);
            L.e("===>>>", name + " 保存是否成功:" + compress + "  file.exists:" + file.exists());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            closeCamera();
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

    private String JHZDOOR = "JHZ_DOOR";//门检测
    private String JHZOBJECT = "JHZ_OBJECT";//物体检测

    /**
     * 发送文件
     *
     * @param path 文件路径
     */
    private void sendFile(final String path, final String time) {
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
            ControlApplication.from(mContext).getNetClient().sendNetMessage(message, new SendRequestListener<FileMessage>() {
                @Override
                public void onFail(FileMessage message, int errorCode, String errorMessage) {
                    L.e("===>>", "onFail   errorMessage=" + errorMessage);
                    currentSave = true;
                    handler.sendEmptyMessage(MESSAGE_SVAE_FAILURE);
                }

                @Override
                public void onSending(FileMessage message, long total, long current) {

                }

                @Override
                public void onSuccess(FileMessage message, String result) {
                    L.e("===>>", "onSuccess=" + result);
                    currentSave = true;
                    if (!TextUtils.isEmpty(result) && mStart) {
                        Message msg = new Message();
                        msg.what = MESSAGE_SVAE_SUCCESS;
                        Bundle bundle = new Bundle();
                        bundle.putString("result", result.toString());
                        bundle.putString("path", path);
                        bundle.putString("time", time);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
//                        //测试画框--------------------------------------------------------------------------->>
//                        String strTest = "{\"SUCCESS\":\"LALALA\",\"objects\":[{\"x_min\":100,\"class_name\":\"bed\",\"probability\":0.9,\"y_min\":100,\"y_max\":479,\"x_max\":400}," +
//                                "{\"x_min\":200,\"class_name\":\"desk\",\"probability\":0.8,\"y_min\":300,\"y_max\":400,\"x_max\":300}]}";
//                        Message msg = new Message();
//                        msg.what = MESSAGE_SVAE_SUCCESS;
//                        Bundle bundle = new Bundle();
//                        bundle.putString("result", strTest);
//                        bundle.putString("path", path);
//                        bundle.putString("time", time);
//                        msg.setData(bundle);
//                        handler.sendMessage(msg);
//                        //---------------------------------------------------------------------------------------<<
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void ensureCameraDevice() throws Exception {
        try {
            if (mCamera == null) {
                mCamera = Camera.open(mCameraId);
                setCameraDisplayOrientation((Activity) mContext, mCameraId, mCamera);
                mParameters = mCamera.getParameters();
                mParameters.set("orientation", "portrait");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭相机
     */
    public void closeCamera() {
        try {
            if (mCamera != null) {
                // CameraHolder.instance().release();

                mCamera.setPreviewCallback(null); //！！这个必须在前，不然退出出错
                mCamera.setZoomChangeListener(null);
                mCamera.stopPreview();

                mCamera.release();

                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始拍照
     *
     * @param start
     */
    public void setStart(final boolean start) {
        mStart = start;
        currentSave = start;

        if (!mStart) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (!list.isEmpty()) {
                            if (current == null) {
                                current = list.get(0);
                                current.execute(0);
                            }
                        }
                        if (mOnFinish != null)
                            mOnFinish.onFinish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

//    public void setMbOnce(final boolean bOnce) {
//        mbOnce = bOnce;
//    }

    /**
     * 时间
     */
    public String getDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        long time = System.currentTimeMillis();
        Date curDate = new Date(time);//获取当前时间
        String datetime = formatter.format(curDate);
        return datetime;
    }

}

/*
 * Provide a mapping for Jpeg encoding quality levels from String representation
 * to numeric representation.
 */
class JpegEncodingQualityMappings {
    private static final String TAG = "JpegEncodingQualityMappings";
    private static final int DEFAULT_QUALITY = 85;
    private static HashMap<String, Integer> mHashMap = new HashMap<String, Integer>();

    static {
        mHashMap.put("normal", CameraProfile.QUALITY_LOW);
        mHashMap.put("fine", CameraProfile.QUALITY_MEDIUM);
        mHashMap.put("superfine", CameraProfile.QUALITY_HIGH);
    }

    public static int getQualityNumber(String jpegQuality) {
        Integer quality = mHashMap.get(jpegQuality);
        if (quality == null) {
            return DEFAULT_QUALITY;
        }
        return CameraProfile
                .getJpegEncodingQualityParameter(quality.intValue());
    }


}
