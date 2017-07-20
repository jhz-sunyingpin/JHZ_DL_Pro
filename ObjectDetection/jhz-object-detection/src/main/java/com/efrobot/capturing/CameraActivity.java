package com.efrobot.capturing;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.efrobot.library.RobotManager;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by wym on 2015/10/28.邮箱：wuyamin@ren001.com
 */
public class CameraActivity extends PresenterActivity<CameraPresenter> implements ICameraView {
    @Override
    protected int getContentViewResource() {
        return R.layout.camera_layout;
    }


    @Override
    public CameraPresenter createPresenter() {
        return new CameraPresenter(this);
    }

    @Override
    public Context getContext() {
        return this;
    }


    private SurfaceView mCameraShow;
    /**
     * 绘检测结果（矩形框+类别）
     */
    private MySurfaceView mDraw;

    private SurfaceHolder mHolder;
    /**
     * 相机控制
     */
    private CameraCallback mCallback;
    /**
     * 保存进度条
     */
    ProgressDialog mProgressDialog;
    int mCameraId = 0;
    /**
     * 识别类型选择
     */
    private RadioGroup rgType;

    @Override
    protected void onViewInit() {
        super.onViewInit();
        mPresenter.initHandler();
        mCameraShow = (SurfaceView) findViewById(R.id.cameraShow);//视频显示
        mDraw = (com.efrobot.capturing.MySurfaceView)findViewById(R.id.myDraw);//检测结果显示

        initCameraAtActivity();
        /**
         * 点击屏幕后进行自动聚焦
         */
        mCameraShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (mCallback != null)
                    mCallback.autoFocus();
            }
        });

        /**
         * 开始
         */
        findViewById(R.id.begin_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mPresenter.isStop = false;
                mCallback.setStart(true);
                mCallback.currentTime = 0;
                mCallback.count = 0;

                findViewById(R.id.begin_camera).setEnabled(false);
                findViewById(R.id.stop_camera).setEnabled(true);
                mDraw.setVisibility(View.VISIBLE);

//                //测试绘制矩形框
//                ResultBean resultBean = new ResultBean();
//                List<OneObjectResult> objectResultsList = new ArrayList<OneObjectResult>();
//                OneObjectResult oneObjectResult1 = new OneObjectResult("bed",0.9,100,100,400,479);
//                OneObjectResult oneObjectResult2 = new OneObjectResult("desk",0.8,200,300,300,400);
//                objectResultsList.add(oneObjectResult1);
//                objectResultsList.add(oneObjectResult2);
//                resultBean.setObjectResults(objectResultsList);
//                mDraw.setResultBean(resultBean);
//                mDraw.drawCanvas();
            }
        });

        /**
         * 停止
         */
        findViewById(R.id.stop_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                mCallback.setStart(false);
                mPresenter.stop();
                findViewById(R.id.begin_camera).setEnabled(true);
                findViewById(R.id.stop_camera).setEnabled(false);

                mDraw.clearDraw();
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
                    RobotManager.getInstance(getContext()).getWheelInstance().moveLeft(200);
                }
                //抬起操作
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    RobotManager.getInstance(getContext()).getWheelInstance().stop();
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
                    RobotManager.getInstance(getContext()).getWheelInstance().moveRight(200);
                }
                //抬起操作
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    RobotManager.getInstance(getContext()).getWheelInstance().stop();
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
                    RobotManager.getInstance(getContext()).getWheelInstance().moveFront(200);
                }
                //抬起操作
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    RobotManager.getInstance(getContext()).getWheelInstance().stop();
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
                    RobotManager.getInstance(getContext()).getWheelInstance().moveBack(200);
                }
                //抬起操作
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    RobotManager.getInstance(getContext()).getWheelInstance().stop();
                }
                return  false;
            }
        });

        /**
         * 头部左转
         */
        findViewById(R.id.head_left_turn_btn).setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent){
                //按下操作
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    RobotManager.getInstance(getContext()).getHeadInstance().moveLeft(50);
                }
                //抬起操作
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    RobotManager.getInstance(getContext()).getHeadInstance().stop();
                }
                return  false;
            }
        });

        /**
         * 头部右转
         */
        findViewById(R.id.head_right_turn_btn).setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent){
                //按下操作
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    RobotManager.getInstance(getContext()).getHeadInstance().moveRight(50);
                }
                //抬起操作
                if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    RobotManager.getInstance(getContext()).getHeadInstance().stop();
                }
                return  false;
            }
        });
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        mCallback.switchCamera(mCameraShow, mCameraId, mPresenter.getHandler());
    }

    /**
     * 初始化相机数据
     */
    private void initCameraAtActivity() {

        mHolder = mCameraShow.getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mCallback = new CameraCallback(this);
        // 照相机镜头数目
        mHolder.addCallback(mCallback);
        mCameraId = 0;

        try {
            /**
             * 开始前的准备
             */
            mCallback.startPreview(mPresenter.getHandler());
        } catch (Exception e) {
            e.printStackTrace();
            mPresenter.getHandler().sendEmptyMessage(5);
        }
        mCallback.setOnFinish(new Onfinish() {

            @Override
            public void onFinish() {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
//        initCameraAtActivity();
    }

    private void takeCamera() {
        mPresenter.takeCamea();
    }

    @Override
    public void takePicture() {
        mCallback.startTime = System.currentTimeMillis();
        mCallback.takePicture(mPresenter.getHandler());
    }

//    /**
//     * 设置数据
//     *
//     * @param adapter
//     */
//    @Override
//    public void setAdapter(ResultAdapter adapter) {
//        if (adapter != null)
//            lvContent.setAdapter(adapter);
//    }
//
//    @Override
//    public void setShowFirst(int position) {
//        lvContent.setSelection(position);
//    }

    @Override
    public void setResultBean(ResultBean resultBean)
    {
        if (resultBean != null) {
            mDraw.setResultBean(resultBean);
            mDraw.drawCanvas();
        }
    }

    @Override
    public void setmSwitched(boolean switched)
    {
        mDraw.setmSwitched(switched);
    }

    @Override
    public void closeCamera() {
        try {
            mCallback.closeCamera();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setFullScreen();
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
            myIntent = new Intent(CameraActivity.this, HomeActivity.class);
            startActivity(myIntent);
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }

//    @Override
//    public void onCheckedChanged(RadioGroup group, int checkedId) {
//        switch (checkedId) {
//            case R.id.rbPerson:
//                mPresenter.currentType = 0;
//                break;
//            case R.id.rbToy:
//                mPresenter.currentType = 1;
//                break;
//            case R.id.rbImageNet:
//                mPresenter.currentType = 2;
//                break;
//        }
//        mCallback.setModel(mPresenter.currentType);
//    }
}
