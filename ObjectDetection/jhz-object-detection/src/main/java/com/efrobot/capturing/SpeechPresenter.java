package com.efrobot.capturing;

import android.os.Bundle;

import com.efrobot.library.mvp.presenter.BasePresenter;
import com.efrobot.library.mvp.utils.RobotToastUtil;
import com.efrobot.library.mvp.view.UiView;

/**
 * Presenter 的基类
 */
public class SpeechPresenter<T extends UiView> extends BasePresenter<T> {

    public SpeechPresenter(T mView) {
        super(mView);
    }


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    @Override
    public void onPause() {
        super.onPause();

    }

    /**
     * 显示Toast
     */
    public void showToast(String content) {
        RobotToastUtil.getInstance(getContext()).showToast(content);
    }

}
