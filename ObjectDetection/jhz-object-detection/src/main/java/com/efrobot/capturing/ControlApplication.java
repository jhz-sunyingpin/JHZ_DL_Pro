package com.efrobot.capturing;


import android.app.Application;
import android.content.Context;

import com.efrobot.library.mvp.utils.L;
import com.efrobot.library.net.NetClient;
import com.efrobot.library.net.NetMessage;
import com.efrobot.library.net.SendRequestListener;

/**
 * Created by jqr111 on 2016/8/15.
 */
public class ControlApplication extends Application implements SendRequestListener<NetMessage> {

    /**
     * 访问网络的类
     */
    private NetClient client;

    @Override
    public void onCreate() {
        super.onCreate();
        client = NetClient.getInstance(getApplicationContext());
        client.setSendRequestListener(this);
    }

    /**
     * 获取Application对象
     *
     * @param context 上下文
     * @return Application对象
     */
    public static ControlApplication from(Context context) {
        return (ControlApplication) context.getApplicationContext();
    }

    /**
     * 获取访问网络对象
     *
     * @return 访问网络对象
     */
    public NetClient getNetClient() {
        return client;
    }

    @Override
    public void onSending(NetMessage message, long total, long current) {

    }

    @Override
    public void onSuccess(NetMessage message, String result) {
    }

    @Override
    public void onFail(NetMessage message, int errorCode, String errorMessage) {
    }
}
