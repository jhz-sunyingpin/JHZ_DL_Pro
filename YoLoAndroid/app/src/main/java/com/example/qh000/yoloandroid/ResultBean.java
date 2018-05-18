package com.example.qh000.yoloandroid;

import java.util.List;

/**
 * 上传图片的返回结果
 * Created by jqr111 on 2016/8/15.
 */
public class ResultBean {
    public List<OneObjectResult> objectResults;
    public String sendUrl;
    public String date;
    public String path;

    public ResultBean() {
    }

    public ResultBean(List<OneObjectResult> objectResults, String sendUrl, String date, String path) {
        this.objectResults = objectResults;
        this.sendUrl = sendUrl;
        this.date = date;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<OneObjectResult> getObjectResults() {
        return objectResults;
    }

    public void setObjectResults( List<OneObjectResult> objectResults) {
        this.objectResults = objectResults;
    }

    public String getSendUrl() {
        return sendUrl;
    }

    public void setSendUrl(String sendUrl) {
        this.sendUrl = sendUrl;
    }
}
