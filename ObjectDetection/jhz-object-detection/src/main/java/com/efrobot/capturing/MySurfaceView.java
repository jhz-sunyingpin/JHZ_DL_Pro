package com.efrobot.capturing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.List;
import java.util.Map;

/*定义一个画矩形框的类*/
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback{

    protected SurfaceHolder sh;
    private int mWidth;
    private int mHeight;
    private int mWidthBase = 640;//上传服务器的图片尺寸 -- 横屏
    private int mHeightBase = 480;
    private float mRatioWidth;
    private float mRatioHeight;
    private int mTextSize = 30;//自适应字体大小
    private boolean mSwitched = false;//长宽切换
    private ResultBean resultBean;

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        sh = getHolder();
        sh.addCallback(this);
        sh.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int w, int h) {
        // TODO Auto-generated method stub
        mWidth = w;//手机上为竖屏的宽
        mHeight = h;
        mRatioWidth = (float)mWidth / mWidthBase;
        mRatioHeight = (float)mHeight / mHeightBase;
//        mRatioWidth = (float)mHeight / mWidthBase;
//        mRatioHeight = (float)mWidth / mHeightBase;
        float RATIO = Math.min(mRatioWidth, mRatioHeight);
        mTextSize = Math.round(25 * RATIO);
    }

    public void surfaceCreated(SurfaceHolder arg0) {
        // TODO Auto-generated method stub

    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
        // TODO Auto-generated method stub

    }
    public void setResultBean(ResultBean resultBean)
    {
        this.resultBean = resultBean;
    }
    public void setmSwitched(boolean switched)
    {
        this.mSwitched = switched;
    }
    public void clearDraw()
    {
        Canvas canvas = sh.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//设置背景透明
        sh.unlockCanvasAndPost(canvas);
    }

    public static String decimalFormat2(double value) {

        return String.format("%.2f", value).toString();
    }

    //颜色定义
    public int objectColor(String strObject)
    {
        int  color = Color.WHITE;
        switch(strObject){
            case "sofa":
                color = Color.RED;
                break;
            case "teatable":
                color = Color.rgb(255,69,0);
                break;
            case "tvcabinet":
                color = Color.CYAN;
                break;
            case "tv":
                color = Color.GREEN;
                break;
            case "desk":
                color = Color.rgb(255,0,255);
                break;
            case "bookcase":
                color = Color.rgb(3,168,158);
                break;
            case "bed":
                color = Color.YELLOW;
                break;
            case "bedsidecabinet":
                color = Color.rgb(30,144,255);
                break;
            case "diningtable":
                color = Color.rgb(255,99,71);
                break;
            case "wardrobe":
                color = Color.BLUE;
                break;

        }
        return color;
    }

    public void drawCanvas()
    {
        Canvas canvas = sh.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//设置背景透明
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setTextSize(mTextSize);
        p.setStrokeWidth(4);
        //canvas.drawPoint(100.0f, 100.0f, p);
        //canvas.drawLine(0,110, 500, 110, p);
        //canvas.drawCircle(110, 110, 10.0f, p);
        List<OneObjectResult> objectResultList = resultBean.getObjectResults();
        for (int i = 0; i < objectResultList.size(); i++)
        {
            OneObjectResult objectResult = objectResultList.get(i);
            String strClassName = objectResult.getClass_name();
            double dProb = objectResult.getProbability();
            String strProb = decimalFormat2(dProb);
            int color = objectColor(strClassName);
            int nMinX = objectResult.getX_min();
            int nMaxX = objectResult.getX_max();
            int nMinY = objectResult.getY_min();
            int nMaxY = objectResult.getY_max();

            String strText = strClassName + " " + strProb;
            Rect rect = new Rect();
            p.getTextBounds(strText,0,strText.length(),rect);

            if (mSwitched)
            {
                int nMinX_new = mWidthBase - nMinY;
                int nMinY_new = nMinX;
                int nMaxX_new = mWidthBase - nMaxY;
                int nMaxY_new = nMaxX;
                nMinX = Math.round(nMinX_new * mRatioWidth);
                nMaxX = Math.round(nMaxX_new * mRatioWidth);
                nMinY = Math.round(nMinY_new * mRatioHeight);
                nMaxY = Math.round(nMaxY_new * mRatioHeight);

                p.setColor(color);
                p.setStyle(Style.STROKE);
                canvas.drawRect(nMinY,nMinX,nMaxY,nMaxX,p);
                p.setColor(Color.WHITE);
                p.setStyle(Style.FILL);
                int halfHeigth = rect.height()/2;
                int nTemp = 0;
                if (nMinX - 2*halfHeigth > 0)
                    nTemp = nMinX - 2*halfHeigth;
                canvas.drawRect(nMinY,nTemp,nMinY + rect.width(),nMinX + halfHeigth,p);
                p.setColor(Color.BLACK);
                p.setStyle(Style.FILL);
                canvas.drawText(strClassName + " " + dProb,nMinY,nMinX,p);
            }
            else {
                nMinX = Math.round(nMinX * mRatioWidth);
                nMaxX = Math.round(nMaxX * mRatioWidth);
                nMinY = Math.round(nMinY * mRatioHeight);
                nMaxY = Math.round(nMaxY * mRatioHeight);

                p.setColor(color);
                p.setStyle(Style.STROKE);
                canvas.drawRect(nMinX,nMinY,nMaxX,nMaxY,p);
                p.setColor(Color.WHITE);
                p.setStyle(Style.FILL);
                int halfHeigth = rect.height()/2;
                int nTemp = 0;
                if (nMinY - 2*halfHeigth > 0)
                    nTemp = nMinY - 2*halfHeigth;
                canvas.drawRect(nMinX,nTemp,nMinX + rect.width(),nMinY + halfHeigth,p);
                p.setColor(Color.BLACK);
                p.setStyle(Style.FILL);
                canvas.drawText(strClassName + " " + strProb,nMinX,nMinY,p);

            }
        }

        sh.unlockCanvasAndPost(canvas);

    }

}
