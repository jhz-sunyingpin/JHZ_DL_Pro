package com.efrobot.capturing;

import android.os.Bundle;
import android.view.View;

import com.efrobot.library.mvp.presenter.BasePresenter;
import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.mvp.view.BaseActivity;


/**
 *
 * 基础类
 * Created by xfk on 2015/5/18.
 */
public abstract class PresenterActivity<T extends BasePresenter> extends BaseActivity {
    protected T mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setFullScreen();
        mPresenter = createPresenter();

        super.onCreate(savedInstanceState);

        mPresenter.onCreate(savedInstanceState);
    }
    public void setFullScreen() {
        L.e("--->>>", "BodyShowBaseActivity    执行了全屏操作");
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN;// hide status bar


        if (android.os.Build.VERSION.SDK_INT >= 19) {
            uiFlags |= 0x00001000;    //SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide navigation bars - compatibility: building API level is lower thatn 19, use magic number directly for higher API target level
        } else {
            uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }
        getWindow().getDecorView().setSystemUiVisibility(uiFlags);
    }
    public abstract T createPresenter();

    @Override
    protected void onViewCreateBefore() {
        mPresenter.onViewCreateBefore();
    }


    @Override
    protected void onViewCreated() {
        mPresenter.onViewCreated();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.onPause();
    }

    @Override
    protected void onDestroy() {
        mPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(mPresenter.onBackPressed())
            super.onBackPressed();
    }


}
