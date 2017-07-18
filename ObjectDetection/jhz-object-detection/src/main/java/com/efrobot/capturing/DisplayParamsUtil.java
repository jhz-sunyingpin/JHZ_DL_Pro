package com.efrobot.capturing;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * 屏幕参数工具
 *
 * @author wuyamin
 */
public class DisplayParamsUtil {
    /**
     * 屏幕宽度——px
     */
    public int screenWidth;
    /**
     * 屏幕高度——px
     */
    public int screenHeight;
    /**
     * 屏幕密度——dpi
     */
    public int densityDpi;
    /**
     * 缩放系数——densityDpi/160
     */
    public float scale;
    /**
     * 文字缩放系数
     */
    public float fontScale;
    /**
     * 屏幕朝向
     */
    public int screenOrientation;
    /**
     * 表示屏幕朝向垂直
     */
    public final static int SCREEN_ORIENTATION_VERTICAL = 1;
    /**
     * 表示屏幕朝向水平
     */
    public final static int SCREEN_ORIENTATION_HORIZONTAL = 2;


    public DisplayParamsUtil(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth =dm.widthPixels;
        screenHeight= dm.heightPixels;
        densityDpi = dm.densityDpi;
        scale = dm.density;
        fontScale = dm.scaledDensity;
        screenOrientation = screenHeight > screenWidth ? SCREEN_ORIENTATION_VERTICAL
                : SCREEN_ORIENTATION_HORIZONTAL;
    }

    /**
     * 将pixel转换成dip(dp)
     *
     * @param context    上下文
     * @param pixelValue pixel值
     * @return dip值
     */
    public static int pixelToDip(Context context, float pixelValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (pixelValue / density + 0.5f);
    }

    /**
     * 将dip(dp)转换成pixel
     *
     * @param context  上下文
     * @param dipValue dip值
     * @return pixel值
     */
    public static int dipToPixel(Context context, float dipValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * density + 0.5f);
    }

    /**
     * 将pixel转换成sp
     *
     * @param context    上下文
     * @param pixelValue pixel值
     * @return sp值
     */
    public int pixelToSp(Context context, float pixelValue) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pixelValue / scaledDensity + 0.5f);
    }

    /**
     * 将sp转换成pixel
     *
     * @param context 上下文
     * @param spValue sp值
     * @return pixel值
     */
    public static int spToPixel(Context context, float spValue) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * scaledDensity + 0.5f);
    }

    /**
     * 将pixel转换成dp
     *
     * @param context 上下文
     * @param val     pixel值
     * @return dp值
     */
    public static float pixelToDp(Context context, float val) {
        float density = context.getResources().getDisplayMetrics().density;
        return val * density;
    }

}
