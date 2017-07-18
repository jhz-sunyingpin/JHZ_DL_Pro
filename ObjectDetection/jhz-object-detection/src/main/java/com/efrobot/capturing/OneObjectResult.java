package com.efrobot.capturing;

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

    private String class_name;
    private double probability;
    private int x_min;
    private int y_min;
    private int x_max;
    private int y_max;

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
}
