#include <jni.h>
#include "darknet.h"

#include <android/bitmap.h>
#include <android/asset_manager_jni.h>
#include <android/asset_manager.h>

#define true JNI_TRUE
#define false JNI_FALSE

static network *pNet;
static image **pAlphabet;
static char **pNames;

//define global param for thread

//char *voc_names[] = {"aeroplane", "bicycle", "bird", "boat", "bottle", "bus", "car", "cat", "chair", "cow", "diningtable", "dog", "horse", "motorbike", "person", "pottedplant", "sheep", "sofa", "train", "tvmonitor"};

//rewrite test demo for android
double test_detector(char *datacfg, char *cfgfile, char *weightfile, char *filename, float thresh, float hier_thresh, char *outfile, int fullscreen)
{
    LOGD("data=%s",datacfg);
    LOGD("cfg=%s",cfgfile);
    LOGD("wei=%s",weightfile);
    LOGD("img=%s",filename);

    //list *options = read_data_cfg(datacfg);
    char *name_list = "/sdcard/yolo/data/coco.names";//option_find_str(options, "names", "data/names.list");
    char **names = get_labels(name_list);

    image **alphabet = load_alphabet();
    network *net = load_network(cfgfile, weightfile, 0);
    set_batch_network(net, 1);
    srand(2222222);
    double time;
    char buff[256];
    char *input = buff;
    float nms=.45;
    while(1){
        if(filename){
            strncpy(input, filename, 256);
        } else {
            printf("Enter Image Path: ");
            fflush(stdout);
            input = fgets(input, 256, stdin);
            if(!input) return 0;
            strtok(input, "\n");
        }
        image im = load_image_color(input,0,0);
        image sized = letterbox_image(im, net->w, net->h);
        //save_image(sized, "/sdcard/yolo/sized_im");
        //image sized = resize_image(im, net->w, net->h);
        //image sized2 = resize_max(im, net->w);
        //image sized = crop_image(sized2, -((net->w - sized2.w)/2), -((net->h - sized2.h)/2), net->w, net->h);
        //resize_network(net, sized.w, sized.h);
        layer l = net->layers[net->n-1];

        float *X = sized.data;
        time=what_time_is_it_now();
        network_predict(net, X);
        time = what_time_is_it_now()-time;
        LOGD("%s: Predicted in %f seconds.\n", input, time);
        int nboxes = 0;
        detection *dets = get_network_boxes(net, im.w, im.h, thresh, hier_thresh, 0, 1, &nboxes);
        //if (nms) do_nms_obj(boxes, probs, l.w*l.h*l.n, l.classes, nms);
        if (nms) do_nms_sort(dets, nboxes, l.classes, nms);
        draw_detections(im, dets, nboxes, thresh, names, alphabet, l.classes);
        if(outfile){
            save_image(im, outfile);
        }
        else{
            save_image(im, "predictions");
#ifdef OPENCV
            cvNamedWindow("predictions", CV_WINDOW_NORMAL);
            if(fullscreen){
                cvSetWindowProperty("predictions", CV_WND_PROP_FULLSCREEN, CV_WINDOW_FULLSCREEN);
            }
            show_image(im, "predictions");
            cvWaitKey(0);
            cvDestroyAllWindows();
#endif
        }

        free_image(im);
        free_image(sized);
        free_network(net);
        if (filename)
            break;
    }
    return time;
}


//Todo:
// use init and detect to process camera
void
JNICALL
Java_com_example_qh000_yoloandroid_DarknetFunc_inityolo(JNIEnv *env, jobject obj, jstring cfgfile, jstring weightfile)
{
    const char *cfgfile_str = (*env)->GetStringUTFChars(env, cfgfile, 0);
    const char *weightfile_str = (*env)->GetStringUTFChars(env, weightfile, 0);

    //test_detector(cfgfile_str, weightfile_str, "/sdcard/name", 0.5f);

    (*env)->ReleaseStringUTFChars(env, cfgfile, cfgfile_str);
    (*env)->ReleaseStringUTFChars(env, weightfile, weightfile_str);
    return;
}

//test demo
// process imgfile to /sdcard/yolo/out
jdouble
JNICALL
Java_com_example_qh000_yoloandroid_DarknetFunc_testyolo(JNIEnv *env, jobject obj, jstring imgfile)
{
    double time;
    const char *imgfile_str = (*env)->GetStringUTFChars(env, imgfile, 0);

    char *datacfg_str = "/sdcard/yolo/cfg/coco.data";
    char *cfgfile_str = "/sdcard/yolo/cfg/yolov2-tiny.cfg";
    char *weightfile_str = "/sdcard/yolo/weights/yolov2-tiny.weights";
//    char *imgfile_str = "/sdcard/yolo/data/dog.jpg";
    char *outimgfile_str = imgfile_str;

    time = test_detector(datacfg_str, cfgfile_str,
                  weightfile_str, imgfile_str,
                  0.5f, 0.5f, outimgfile_str, 0);

    (*env)->ReleaseStringUTFChars(env, imgfile, imgfile_str);
    return time;
}


jboolean
JNICALL
Java_com_example_qh000_yoloandroid_DarknetFunc_yoloinit(JNIEnv *env, jobject obj)
{
    char *datacfg_str = "/sdcard/yolo/cfg/coco.data";
    char *cfgfile_str = "/sdcard/yolo/cfg/yolov2-tiny.cfg";
    char *weightfile_str = "/sdcard/yolo/weights/yolov2-tiny.weights";
    LOGD("data=%s",datacfg_str);
    LOGD("cfg=%s",cfgfile_str);
    LOGD("wei=%s",weightfile_str);
    //list *options = read_data_cfg(datacfg);
    char *name_list = "/sdcard/yolo/data/coco.names";//option_find_str(options, "names", "data/names.list");
    pNames = get_labels(name_list);

    pAlphabet = load_alphabet();
    pNet = load_network(cfgfile_str, weightfile_str, 0);
    set_batch_network(pNet, 1);

    LOGD("====load model succeed=====");

    return true;
}

jboolean
JNICALL
Java_com_example_qh000_yoloandroid_DarknetFunc_yolorealese(JNIEnv *env, jobject obj)
{
    if(pNet)
    {
        free_network(pNet);
    }
    return true;
}


jdouble
JNICALL
Java_com_example_qh000_yoloandroid_DarknetFunc_yolodetectimg(JNIEnv *env, jobject obj, jstring imgfile)
{
    double time;
    const char *imgfile_str = (*env)->GetStringUTFChars(env, imgfile, 0);
    char *outimgfile_str = imgfile_str;

    srand(2222222);
    char buff[256];
    char *input = buff;
    float nms=.45;
    while(1){
        if(imgfile_str){
            strncpy(input, imgfile_str, 256);
        } else {
            printf("Enter Image Path: ");
            fflush(stdout);
            input = fgets(input, 256, stdin);
            if(!input) return 0;
            strtok(input, "\n");
        }
        image im = load_image_color(input,0,0);
        image sized = letterbox_image(im, pNet->w, pNet->h);
        layer l = pNet->layers[pNet->n-1];

        float *X = sized.data;
        time=what_time_is_it_now();
        network_predict(pNet, X);
        time = what_time_is_it_now()-time;
        LOGD("%s: Predicted in %f seconds.\n", input, time);
        int nboxes = 0;
        detection *dets = get_network_boxes(pNet, im.w, im.h, 0.5f, 0.5f, 0, 1, &nboxes);
        //if (nms) do_nms_obj(boxes, probs, l.w*l.h*l.n, l.classes, nms);
        if (nms) do_nms_sort(dets, nboxes, l.classes, nms);
        draw_detections(im, dets, nboxes, 0.5f, pNames, pAlphabet, l.classes);
        if(outimgfile_str){
            save_image(im, outimgfile_str);
        }
        else{
            save_image(im, "predictions");
#ifdef OPENCV
            cvNamedWindow("predictions", CV_WINDOW_NORMAL);
            if(fullscreen){
                cvSetWindowProperty("predictions", CV_WND_PROP_FULLSCREEN, CV_WINDOW_FULLSCREEN);
            }
            show_image(im, "predictions");
            cvWaitKey(0);
            cvDestroyAllWindows();
#endif
        }

        free_image(im);
        free_image(sized);
        if (imgfile_str)
            break;
    }

    (*env)->ReleaseStringUTFChars(env, imgfile, imgfile_str);
    return time;
}

//Todo:
// finish detectimg with camera
jboolean
JNICALL
Java_com_example_qh000_yoloandroid_DarknetFunc_detectimg(JNIEnv *env, jobject obj, jobject dst, jobject src)
{
    AndroidBitmapInfo srcInfo, dstInfo;
    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_getInfo(env, src, &srcInfo)
        || ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_getInfo(env, dst, &dstInfo)) {
        LOGE("get bitmap info failed");
        return false;
    }

    void *srcBuf, *dstBuf;
    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_lockPixels(env, src, &srcBuf)) {
        LOGE("lock src bitmap failed");
        return false;
    }

    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_lockPixels(env, dst, &dstBuf)) {
        LOGE("lock dst bitmap failed");
        return false;
    }


    AndroidBitmap_unlockPixels(env, src);
    AndroidBitmap_unlockPixels(env, dst);
    return 1;
}