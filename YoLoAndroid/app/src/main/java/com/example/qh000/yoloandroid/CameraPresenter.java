package com.example.qh000.yoloandroid;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.os.Environment;
import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;

import com.efrobot.library.mvp.utils.L;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import net.sf.json.JSON;
//import net.sf.json.JSONArray;
//import net.sf.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import cn.edu.zafu.coreprogress.listener.impl.UIProgressListener;

/**
 * Created by wym on 2015/10/28.邮箱：wuyamin@ren001.com
 */
public class CameraPresenter extends SpeechPresenter<ICameraView> {
    /**
     * 保存成功
     */
    private final int DETECT_FINISH = 22;
    /**
     * 保存失败
     */
    private final int DETECT_FAILURE = 23;
    /**
     * 拍照
     */
    private final int TAKE_CAMERA = 0;
    /**
     * 开始时间
     */
    private long beginTime;
    /**
     * 是否停止
     */
    boolean isStop = false;

    private ICameraView mView;

    /**
     * adapter适配器
     */
    private ResultAdapter adapter;
//    public ArrayList<String> timeResumeList = new ArrayList<>();
//
//    /**
//     * 上传照片的结果数据
//     */
//    public ResultBean resultBean = new ResultBean();
//
    /**
     * 上传照片的结果数据
     */
    public List<ResultBean> list;


//    private File newFile;
//    private String fileName = "ODTime.txt";
//    private  FileOutputStream out = null;
//    private BufferedWriter writer = null;
//    private String filePath;

    public CameraPresenter(final ICameraView mView) {
        super(mView);
        this.mView = mView;
    }


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initHandler();
        list = new ArrayList<>();

//        File sdFile = Environment.getExternalStorageDirectory();
//        String newFolderPath = sdFile.getPath() + "/ODTime";
//        File dir = new File(newFolderPath);
//        if (!dir.exists())
//            dir.mkdir();
////        filePath = newFolderPath + "/" + fileName;
//        newFile = new File(newFolderPath,fileName);
//        if (newFile.exists()){
//            newFile.delete();
//        }
    }


    @Override
    protected void handleMessage(final Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case TAKE_CAMERA:
                /**
                 * 拍照
                 */
                mView.takePicture();
                beginTime = System.currentTimeMillis();
                L.e("===>>>>", "STARTTIME:::" + beginTime);
//                getHandler().sendEmptyMessageDelayed(0, 100);
                break;
            case 5:
                /**
                 * 关闭相机
                 */
                mView.closeCamera();

                break;
            case DETECT_FINISH:
                /**
                 * 检测成功
                 */

//                Bitmap dstimg = BitmapFactory.decodeFile("/sdcard/yolo/out.png");
                String path = msg.getData().getString("path");
                String time = msg.getData().getString("time");

                ResultBean bean = new ResultBean(null, "", time, path);
                list.add(bean);
                initAdapter();
                break;
            case DETECT_FAILURE:
                /**
                 * 拍照失败
                 */
                if (!isStop) {
                    showToast("检测失败");
                }

                break;
            case 44:
                mView.takePicture();
        }
    }

    private void initAdapter() {
        if (adapter == null) {
            adapter = new ResultAdapter(getContext(), list, callBack);
            mView.setAdapter(adapter);
        } else {
            adapter.setResources(list);
            mView.setShowFirst(list.size() - 1);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mView.closeCamera();

//        try {
//            out = new FileOutputStream(newFile);
//            String timeWrite = "";
//            for (int i = 0; i < timeResumeList.size(); i++)
//            {
//                timeWrite = timeWrite + "\n";
//                timeWrite = timeWrite + timeResumeList.get(i);
//            }
//            out.write(timeWrite.getBytes());
//            out.flush();
//            out.close();
//        }catch (FileNotFoundException e){
//            e.printStackTrace();
//        }catch (IOException e){
//            e.printStackTrace();
//        }
    }

    @Override
    public boolean onBackPressed() {
        return super.onBackPressed();
    }

    /**
     * 拍照
     */
    public void takeCamea() {
        getHandler().sendEmptyMessage(TAKE_CAMERA);
    }

//    private void initData() {
//        String filePath = "/sdcard/Test/";
//        String fileName = "log.txt";
//
//        writeTxtToFile("txt content", filePath, fileName);
//    }
//
//    // 将字符串写入到文本文件中
//    public void writeTxtToFile(String strcontent, String filePath, String fileName) {
//        //生成文件夹之后，再生成文件，不然会出错
//        makeFilePath(filePath, fileName);
//
//        String strFilePath = filePath+fileName;
//        // 每次写入时，都换行写
//        String strContent = strcontent + "\r\n";
//        try {
//            File file = new File(strFilePath);
//            if (!file.exists()) {
//                Log.d("TestFile", "Create the file:" + strFilePath);
//                file.getParentFile().mkdirs();
//                file.createNewFile();
//            }
//            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
//            raf.seek(file.length());
//            raf.write(strContent.getBytes());
//            raf.close();
//        } catch (Exception e) {
//            Log.e("TestFile", "Error on write File:" + e);
//        }
//    }
//
//    // 生成文件
//    public File makeFilePath(String filePath, String fileName) {
//        File file = null;
//        makeRootDirectory(filePath);
//        try {
//            file = new File(filePath + fileName);
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return file;
//    }
//
//    // 生成文件夹
//    public static void makeRootDirectory(String filePath) {
//        File file = null;
//        try {
//            file = new File(filePath);
//            if (!file.exists()) {
//                file.mkdir();
//            }
//        } catch (Exception e) {
//            Log.i("error:", e + "");
//        }
//    }


        /**
         * 停止拍照
         */
    public void stop() {
        isStop = true;
    }

    ResultAdapter.onPicClick callBack = new ResultAdapter.onPicClick() {
        @Override
        public void clickPic(String path) {
            PicDialog dialog = new PicDialog(getContext(), R.style.NewSettingDialog, path);
            dialog.show();
        }
    };

}
