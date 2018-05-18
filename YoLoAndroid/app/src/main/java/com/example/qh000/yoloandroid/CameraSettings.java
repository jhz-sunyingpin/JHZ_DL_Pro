/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.qh000.yoloandroid;

import android.app.Activity;
import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;


import com.efrobot.library.mvp.utils.L;

import java.util.List;


/**
 * 照片的设置
 * Provides utilities and keys for Camera settings.
 */
public class CameraSettings {

    private static final String TAG = "CameraSettings";
    private static int screenwidth;
    private static int screenHeight;

    private final Context mContext;
    private final Parameters mParameters;
    private final CameraInfo[] mCameraInfo;

    public CameraSettings(Activity activity, Parameters parameters,
                          CameraInfo[] cameraInfo) {
        mContext = activity;
        mParameters = parameters;
        mCameraInfo = cameraInfo;
    }

    public static void initialCameraPictureSize(Context context,
                                                Parameters mParameters) {
        List<Size> supported = mParameters.getSupportedPictureSizes();
        if (supported == null)
            return;
        setCameraPictureSize(context, supported, mParameters);

    }

    private static final int NOT_FOUND = -1;

    public static boolean setCameraPictureSize(Context context, List<Size> supported, Parameters parameters) {
//        DisplayParamsUtil di = new DisplayParamsUtil(context);
//        int width = di.screenWidth;
//        int height = di.screenHeight;
//        L.e("====>>>", "屏幕" + width + "x" + height);

        Camera.Size size = supported.get(0);
//        parameters.setPictureSize(size.width, size.height);
        parameters.setPictureSize(640, 480);
        parameters.setPreviewSize(640, 480);
        return true;


//        for (Size size : supported) {
//            L.e("====>>>", "设置" + size.width + "x" + size.height);
//            if ((double) size.width == height && size.height == width) {
//                L.e("====>>>", "设置的照片大小是" + size.width + "x" + size.height);
//                parameters.setPictureSize(size.width, size.height);
//                return true;
//            }
//        }
        //照片大小修改
//        for (Size size : supported) {
//            L.e("====>>>", "设置" + size.width + "x" + size.height);
//            if ((double) size.width / size.height == (double) height / width) {
//                L.e("====>>>", "设置的照片大小是" + size.width + "x" + size.height);
//                parameters.setPictureSize(size.width, size.height);
//                return true;
//            }
//        }
//        return false;
    }

    /**
     * 判断是否有闪光灯
     */
    public static boolean checkCamerFlash(Context context) {
        PackageManager pM = context.getPackageManager();
        FeatureInfo[] mInfo = pM.getSystemAvailableFeatures();
        for (FeatureInfo f : mInfo) {
            if (PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) {
                return true;
            }
        }
        return false;
    }

//    public static void initialCameraPreviewSize(Context mContext,
//                                                Parameters mParameters) {
//        // TODO Auto-generated method stub
//
//        List<Size> supported = mParameters.getSupportedPreviewSizes();
//        if (supported == null)
//            return;
//
//        for (String candidate : mContext.getResources().getStringArray(
//                R.array.pref_camera_PreviewSize_entryvalues)) {
//            if (setCameraPreviewSize(candidate, supported, mParameters,
//                    mContext)) {
//                return;
//            }
//        }
//    }

    private static boolean setCameraPreviewSize(String candidate,
                                                List<Size> supported, Parameters mParameters, Context mContext) {
        // TODO Auto-generated method stub

        DisplayParamsUtil displayParamsUtil = new DisplayParamsUtil(mContext);

        screenwidth = displayParamsUtil.screenWidth;
        screenHeight = displayParamsUtil.screenHeight;
        int index = candidate.indexOf('x');
        if (index == NOT_FOUND)
            return false;
        int width = Integer.parseInt(candidate.substring(0, index));
        int height = Integer.parseInt(candidate.substring(index + 1));
        for (Size size : supported) {
            // 判断支不支持
            if (size.width == width && size.height == height) {
                if (size.width <= screenHeight && size.height <= screenwidth) {
                    mParameters.setPreviewSize(width, height);
                    return true;
                }

            }
        }
        return false;
    }


    private static Size getOptimalPreviewSize(List<Size> sizes,
                                              double targetRatio, Context mContext) {
        final double ASPECT_TOLERANCE = 0.05;
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(ratio - targetRatio) < minDiff) {
                optimalSize = size;
                break;
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            Log.v(TAG, "No preview size match the aspect ratio");
            minDiff = Double.MAX_VALUE;

            for (Size size : sizes) {
                double ratio = (double) size.width / size.height;
                if (Math.abs(ratio - targetRatio) < minDiff) {
                    optimalSize = size;
                }
            }
        }
        return optimalSize;
    }


}
