package com.example.qh000.yoloandroid;

/**
 * Created by LJ000PC on 2017/6/21.
 */

public class OneObjectResult {

    /**
     * class_name : bed
     * probability : 0.9
     * x_min : 100
     * y_min : 100
     * x_max : 200
     * y_max : 200
     */

    private String class_name;//类名
    private double probability;//置信概率
    private int x_min;//矩形框左上角x坐标
    private int y_min;//矩形框左上角y坐标
    private int x_max;//矩形框右下角x坐标
    private int y_max;//矩形框右下角y坐标
    private float worldCor_x = 0;//世界坐标
    private float worldCor_y = 0;
    private float worldCor_z = 0;

    public OneObjectResult(String class_name, double probability, int x_min, int y_min, int x_max, int y_max) {
        this.class_name = class_name;
        this.probability = probability;
        this.x_min = x_min;
        this.y_min = y_min;
        this.x_max = x_max;
        this.y_max = y_max;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public int getX_min() {
        return x_min;
    }

    public void setX_min(int x_min) {
        this.x_min = x_min;
    }

    public int getY_min() {
        return y_min;
    }

    public void setY_min(int y_min) {
        this.y_min = y_min;
    }

    public int getX_max() {
        return x_max;
    }

    public void setX_max(int x_max) {
        this.x_max = x_max;
    }

    public int getY_max() {
        return y_max;
    }

    public void setY_max(int y_max) {
        this.y_max = y_max;
    }

    public float getWorldCor_x() { return worldCor_x; }

    public void setWorldCor_x(float worldCor_x) { this.worldCor_x = worldCor_x; }

    public float getWorldCor_y() { return worldCor_y; }

    public void setWorldCor_y(float worldCor_y) { this.worldCor_y = worldCor_y; }

    public float getWorldCor_z() { return worldCor_z; }

    public void setWorldCor_z(float worldCor_z) { this.worldCor_z = worldCor_z; }
}
