package com.example.qh000.yoloandroid;


import android.widget.ArrayAdapter;

/**
 * Created by xfk on 2015/5/18.
 * 方法集合
 * void setTitle(String title);
 */
public interface ICameraView extends ISpeachView {


    void closeCamera();

    void takePicture();

//    void setAdapter(ArrayAdapter<String> adapter);
    void setAdapter(ResultAdapter adapter);

    void setShowFirst(int position);

    void setResultBean(ResultBean resultBean);

    void setmSwitched(boolean switched);

}
