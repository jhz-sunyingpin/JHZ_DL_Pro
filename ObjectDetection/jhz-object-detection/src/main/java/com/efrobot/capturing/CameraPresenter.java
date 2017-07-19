package com.efrobot.capturing;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;

import com.efrobot.library.mvp.utils.L;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import net.sf.json.JSON;
//import net.sf.json.JSONArray;
//import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.edu.zafu.coreprogress.listener.impl.UIProgressListener;

/**
 * Created by wym on 2015/10/28.邮箱：wuyamin@ren001.com
 */
public class CameraPresenter extends SpeechPresenter<ICameraView> {
    /**
     * 保存成功
     */
    private final int MESSAGE_SVAE_SUCCESS = 22;
    /**
     * 保存失败
     */
    private final int MESSAGE_SVAE_FAILURE = 23;
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

//    /**
//     * adapter适配器
//     */
//    private ResultAdapter adapter;

    /**
     * 上传照片的结果数据
     */
    public ResultBean resultBean = new ResultBean();
    /**
     * 人形识别
     */
    private static int JHZ_PERSON = 0;
    /**
     * 玩具识别
     */
    private static int JHZ_TOY = 1;
    /**
     * 网络图片
     */
    private static int JHZ_IMAGENET = 2;
    /**
     * 当前识别类型
     */
    public int currentType = 0;

    public CameraPresenter(final ICameraView mView) {
        super(mView);
        this.mView = mView;
    }


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initHandler();
        //list = new ArrayList<>();
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
            case MESSAGE_SVAE_SUCCESS:
                /**
                 * 拍照上传成功
                 */
                long endTime = System.currentTimeMillis();
                L.e("===>>>>", "end:::" + (endTime - beginTime));
                String data = msg.getData().getString("result");
                //"["SUCCESS":"LALALA","objects":objectResults]"list<OneObjectResult>的json字符串
                String path = msg.getData().getString("path");
                String time = msg.getData().getString("time");
                try {
                    if (TextUtils.isEmpty(data)) {
                        showToast("上传失败");
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
                                resultBean.setDate(time);
                                resultBean.setPath(path);
                                resultBean.setSendUrl("");

                                setMyDraw();

                                //mDraw.setResultBean(resultBean);
                                //mDraw.drawCanvas();
                            }
                        }
                        //list.add(bean);

                    } else {
                        showToast("上传失败");

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case MESSAGE_SVAE_FAILURE:
                /**
                 * 拍照失败
                 */
                if (!isStop) {
                    showToast("图片上传失败,请检查网络");
//                    getHandler().sendEmptyMessage(0);
                }

                break;
            case 44:
                mView.takePicture();
        }
    }

    /**
     * 设置数据
     */
    private void setMyDraw() {
        if (resultBean == null) {
            resultBean = new ResultBean();
        }
        mView.setResultBean(resultBean);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mView.closeCamera();
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


    /**
     * 停止拍照
     */
    public void stop() {
        isStop = true;
    }

//    ResultAdapter.onPicClick callBack = new ResultAdapter.onPicClick() {
//        @Override
//        public void clickPic(String path) {
//            PicDialog dialog = new PicDialog(getContext(), R.style.NewSettingDialog, path);
//            dialog.show();
//        }
//    };

}
