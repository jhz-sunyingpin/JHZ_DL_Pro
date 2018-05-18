package com.example.qh000.yoloandroid;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

/**
 * 查看照片的弹框
 * Created by jqr111 on 2016/8/16.
 */
public class PicDialog extends Dialog {

    private Context context;

    private String path;

    private ImageView ivBig;

    public PicDialog(Context context) {
        super(context);
    }

    public PicDialog(Context context, int theme, String Path) {
        super(context, R.style.NewSettingDialog);
        this.context = context;
        this.path = Path;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null);
        ivBig = (ImageView) view.findViewById(R.id.ivBig);
        setContentView(view);
        if (TextUtils.isEmpty(path))
            return;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            ivBig.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
