package com.example.qh000.yoloandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * 图片上传结果适配
 * Created by jqr111 on 2016/8/16.
 */
public class ResultAdapter extends BaseAdapter {

    private Context context;
    private List<ResultBean> list;

    private onPicClick callBack;

    public ResultAdapter(Context context, List<ResultBean> list, onPicClick callBack) {
        this.context = context;
        this.list = list;
        this.callBack = callBack;
    }

    public void setResources(List<ResultBean> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list != null && !list.isEmpty() ? list.size() : 0;
    }

    @Override
    public ResultBean getItem(int position) {
        return list != null && !list.isEmpty() ? list.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, null);
            viewHolder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
            viewHolder.ivPic = (ImageView) convertView.findViewById(R.id.ivPic);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ResultBean bean = getItem(position);
        if (bean != null) {
            viewHolder.tvDate.setText(bean.getDate());
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(bean.getPath());
                viewHolder.ivPic.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }

            viewHolder.ivPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callBack != null) {
                        callBack.clickPic(bean.getPath());
                    }
                }
            });
        }
        return convertView;
    }

    class ViewHolder {
        TextView tvDate;
        ImageView ivPic;
    }

    public interface onPicClick {
        void clickPic(String path);


    }
}
