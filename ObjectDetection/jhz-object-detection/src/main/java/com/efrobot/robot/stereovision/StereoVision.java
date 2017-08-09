package com.efrobot.robot.stereovision;

public class StereoVision {

	static {
		System.loadLibrary("ffmpegutils");
		System.loadLibrary("visionCameraCalibration");
		System.loadLibrary("visionObjectLocation");
//		System.loadLibrary("visionCalcCalibrationPos1");
//		System.loadLibrary("visionCalcCalibrationPos");
//		System.loadLibrary("visionDetectDistance");
	}

	/************************************************************************/
    /* 功能：获取自动曝光时的曝光值和增益值
    /* 输出：int 数组
	/*		 0 曝光值
	/*       1 增益值
	/*       若0 和1 的值全为0 则表示设定失败
	/*       NUll 摄像头已关闭
	/************************************************************************/
	public native int[] getAECAutoL();

	public native int[] getAECAutoR();

	/************************************************************************/
    /* 功能：先设定摄像头手动曝光，再设定手动曝光时的曝光值
    /* 输入：aveExposure  两个摄像头自动曝光时曝光值的平均值
	/*       aveGain  两个摄像头自动曝光时增益值的平均值
	/* 输出：-2 摄像头已关闭
	/*		 -1 摄像头设定手动曝光失败
	/*       0  摄像头参数设定失败
	/*       1  摄像头参数设定成功
	/************************************************************************/
	public native int setAECAutoL(int aveExposure, int aveGain);

	public native int setAECAutoR(int aveExposure, int aveGain);

	/************************************************************************/
    /* 功能：查询摄像头是否在打开
    /* 输出： 1 表示摄像头已经打开
	/*		 其他 表示摄像头没有打开
	/************************************************************************/
	public native int qInuseL();

	public native int qInuseR();

	/************************************************************************/
    /* 功能：打开摄像头获得摄像头句柄
    /* 输入：devid 摄像头已知id 号
	/* 输出：获得的摄像头句柄，若小于0则表示打开失败
	/************************************************************************/
	public native int openL(int devid);

	public native int openR(int devid);

	/************************************************************************/
    /* 功能：初始化摄像头
    /* 输入：width 图像的宽
	/*       height 图像的高
	/*       numbuf 缓存池中的图像张数
	/* 输出：-2 摄像头没有打开
	/*       -1 摄像头初始化失败
	/*        1 摄像头初始化成功
	/************************************************************************/
	public native int initL(int width, int height, int numbuf);

	public native int initR(int width, int height, int numbuf);

	/************************************************************************/
    /* 功能：初始化摄像头图像流
    /* 输出：-2 摄像头没有打开
	/*       -1 摄像头图像流初始化失败
	/*        1 摄像头初始化成功
	/************************************************************************/
	public native int streamonL();

	public native int streamonR();

	/************************************************************************/
    /* 功能：设定摄像头曝光、白平衡类型
    /* 输入： id 设定的类型： 0  V4L2自动曝光自动白平衡  1  V4L2手动曝光自动白平衡  2  V4L2自动曝光手动白平衡  3  V4L2手动曝光手动白平衡  4 V4L2手动白平衡直接设定手动曝光
	/*        value_EXPOSURE  要设定的曝光值  在id=1、3 有效
	/*        value_BALANCE   要设定的白平衡值  在id= 2、3、4 有效
	/* 输出：-2 摄像头没有打开
	/*       -1 摄像头参数设定失败
	/*        1 摄像头设定成功
	/************************************************************************/
	public native int setCamParaL(int id, int value_EXPOSURE, int value_BALANCE);

	public native int setCamParaR(int id, int value_EXPOSURE, int value_BALANCE);

	/************************************************************************/
    /* 功能：获取一帧图像数据
	/* 输入： videodata 存放格式为YUV图像的数组
	/* 输出：-2 摄像头没有打开
	/*       -1 获取数据失败
	/*        正数 当前使用缓存池中的序号
	/************************************************************************/
	public native int dqbufL(byte[] videodata);

	public native int dqbufR(byte[] videodata);

	/************************************************************************/
	/* 功能：将YUV 格式转换为RBG565
	/* 输入： yuvdata 存放格式为YUV图像的数组
	/*        rgbdata 存放格式为RGB图像的数组
	/*        dwidth  RGB图像的宽
	/*        dheight RGB图像的高
	/* 输出：-2 摄像头没有打开
	/*        1 格式转换成功
	/************************************************************************/
	public native int yuvtorgbL(byte[] yuvdata, byte[] rgbdata, int dwidth, int dheight);

	public native int yuvtorgbR(byte[] yuvdata, byte[] rgbdata, int dwidth, int dheight);

	/************************************************************************/
	/* 功能：put in frame buffer to queue
	/* 输出：-2 摄像头没有打开
	/*       -1 存放失败
	/*        1 存放成功
	/************************************************************************/
	public native int qbufL(int index);

	public native int qbufR(int index);

	/**
	 * 功能：成功打开时释放摄像头设备
	 * 输出：-1 释放失败
	 * 1 存放成功
	 *
	 * @return
	 */
	public native int releaseL();

	public native int releaseR();

	/**
	 * 功能：初始化失败时释放摄像头设备
	 * 输出：-1 释放失败
	 * 1 存放成功
	 *
	 * @return
	 */
	public native int uninitL();

	public native int uninitR();

	public native float[] GetDoor(byte[] buf1, byte[] buf2, int dwidth, int dheight, float[] Configuration, int picnum);


	/**
	 * jstrSavePathL   jstrSavePathR  左右两个图像所在路径
	 * dwidth   dheight  图像的宽和高 640 480
	 * nx  ny角点的数量  int nx = 8;int ny = 4;
	 * 返回数组内容：第一位表示识别是否成功，若成功则为1
	 * 第二位表示左图中所得数据所占数量
	 * 第三位表示右图中所得数据所占数量
	 * 以后每个点占两位，先x 后y
	 */
	public native float[] GetWholeImgData(String jstrSavePathL, String jstrSavePathR, int dwidth, int dheight, int nx, int ny);

	/**
	 * 输入：jstring jstrSavePath 保存的文件名
	 * 输入：jbyteArray jbImg保存的RGB565的数组
	 * 返回：成功：1
	 * 失败：0  传入数据为空
	 * 2  转化失败
	 */
	public native int SaveOneImg(String jstrSavePath, byte[] jbImg, int dwidth, int dheight);

	/**
	 * 返回值：四位folat
	 * 数组  0：识别是否成功
	 * 1 成功
	 * 0 左原图像为空
	 * 2 左原图像获取角点失败
	 * 3 左图像角点个数不对
	 * 4 左原图像为空
	 * 5 左原图像获取角点失败
	 * 6 左图像角点个数不对
	 * 7 左右摄像头反了
	 * 8 未知错误
	 * 数组  1：fRobotCalibAngle  标定板中心与小胖方向的夹角 左负右正
	 * 数组  2：fRobotCalibX    标定板中心与在小胖坐标系下的X坐标 左负右正
	 * 数组  3：fRobotCalibZ    标定板中心与在小胖坐标系下的Z坐标 左负右正
	 */
	public native float[] CalculateRobotCalibAngle(byte[] buf1, byte[] buf2, int dwidth, int dheight);

	/**
	 * 返回值：四位folat
	 * 数组  0：识别是否成功
	 * 1 成功
	 * 0 左原图像为空
	 * 2 左原图像获取角点失败
	 * 3 左图像角点个数不对
	 * 4 左原图像为空
	 * 5 左原图像获取角点失败
	 * 6 左图像角点个数不对
	 * 7 左右摄像头反了
	 * 8 未知错误
	 * 数组  1：fFirstAngle  第一次转的角度 左负右正    out
	 * 数组  2：fMoveDis    移动距离   out
	 * 数组  3：fSecondAngle    第二次转的角度 左负右正
	 */
	public native float[] CalculateRobotCalibPos(byte[] buf1, byte[] buf2, int dwidth, int dheight);

	/**
	 * 输出：  2 输入数据为空
	 * 1  有障碍点
	 * 0  没有障碍点
	 * 输入 ：buf1  左图像RGB565
	 * 输入 ：buf2  右图像RGB565
	 * 输入 ：dwidth  图像的宽
	 * 输入 ：dheight  图像的高
	 * 输入 ：Configuration  双目配置文件
	 * 输入：picNum   保存图像编号，路径为根文件夹
	 */
	public native int DetectDistance(byte[] buf1, byte[] buf2, int dwidth, int dheight, float[] Configuration, int picNum);

	/**
	 * 作用：获得小胖转角和标定板的转角，使标定板最大面积的位于双目视野中心
	 * 输入： byte[] buf1 当前左摄像头拍摄的图像
	 * byte[] buf2 当前右摄像头拍摄的图像
	 * int dwidth  图像的宽度
	 * int dheight 图像的高度
	 * string jstrSavePath 之前拍摄三组图片的路径，图片默认名为l_1.bmp r_1.bmp l_2.bmp r_2.bmp l_3.bmp r_3.bmp
	 * int nBoardX 角点数量  nBoardX=6
	 * int nBoardY 角点数量  nBoardY=7
	 * float squareSize 角点间实际的尺寸  float squareSize  = 40.f;
	 * 返回值：
	 * NULL 或是三位folat
	 * NULL  传入的数据为空
	 * 数组  0：识别是否成功
	 * 1 成功
	 * 0 左原图像为空
	 * 2 左原图像获取角点失败
	 * 3 左图像角点个数不对
	 * 4 左原图像为空
	 * 5 左原图像获取角点失败
	 * 6 左图像角点个数不对
	 * 7 左右摄像头反了
	 * 8 未知错误
	 * 9 方向点错误
	 * 10 之前左摄像头保存的图像不存在，此时数组1代表是第几张
	 * 11   之前右摄像头保存的图像不存在，此时数组1 代表是第几张
	 * 12 图像角点提取错误
	 * 13 图像角点提取数量错误
	 * 14 左摄像头标定失败
	 * 15 右摄像头标定失败
	 * 数组  1：fRobotCalibAngle  标定板中心与小胖方向的夹角 左负右正    out
	 * 数组  2：fBoardCalibAngle  标定板应转的角度 左负右正    out
	 */
	public native float[] CalculateRobotCalibTwoAngle(byte[] buf1, byte[] buf2, int dwidth, int dheight, String jstrSavePath, int nBoardX, int nBoardY, float squareSize);
	/************************************************************************/
	/* 函数名：ObjectLocation(byte[] jbtImgDataL,byte[] jbtImgDataR,int dwidth,int dheight,
				float[] jfGlobalData,float[] jfObjectInfo,int nObjectSize,
				float[] jfCamPara,int picNum)
	 * 文件名：visionObjectLocation.so

	 * 时  间：20170801
	 * 作  者：薛林
	 * 功  能：对双目识别的物体进行定位
	 * 输  入：
	 *         jbtImgDataL：    左摄像头采集的RGB565图像数据
	 *         jbtImgDataR：    右摄像头采集的RGB565图像数据
	 *         dwidth：                 图像的宽
	 *         dheight：              图像的高
	 *         jfGlobalData: 3位数组，表示机器人当前在世界坐标系下的坐标（x,y,theta）
	 *         jfObjectInfo: 物体识别信息，每个物体信息存储顺序依次为：物品类别ID，物品概率，物品在左图像外接矩形左上点x、y，右下点x、y
	 *         nObjectSize：   识别物体的个数
	 *         jfCamPara：        摄像头参数
	 *		   picnum 是否保存图像和数据，若picnum>0,则在根目录下的Data文件夹内保存图像和数据，保存名字的序号与picnum相同，即picnum+L.bmp、picnum+R.bmp、picnum+ResultL.png、picnum+ResultR.png
	 *		                          同时保存过程数据ObjectInfo.txt内为物体检测的数据：对应图片ID，检测物体的数量，以后顺序保存物体识别信息，每个物体信息存储顺序依次为：物品类别ID，物品概率，物品在左图像外接矩形左上点x、y，右下点x、y
	 *		          ObjectWorldInfo.txt 内为物体定位的空间坐标: 对应图片ID,检测物体的数量，定位物体的数量，以后5位一组：0（物品类别ID）、1（物品概率）、2（物品中心在世界坐标系下的x坐标）、3（物品中心在世界坐标系下的y坐标）、4（物品中心在世界坐标系下的z坐标）
	 * 输  出：无
	 * 返回值：float[]仅返回有实际空间位置的物体检测
	 *         Null 输入数据为空
	 *		        数组长度为1，值为0：摄像头参数错误
	 *		        数组长度为1，值为非0:算法识别错误
	 *         数组长度为5的倍数，每5位表示：
	 *         		0（物品类别ID）、1（物品概率）、2（物品中心在世界坐标系下的x坐标）、3（物品中心在世界坐标系下的y坐标）、4（物品中心在世界坐标系下的z坐标）
	/************************************************************************/
	public native float[] ObjectLocation(byte[] jbtImgDataL,byte[] jbtImgDataR,int dwidth,int dheight,float[] jfGlobalData,float[] jfObjectInfo,int nObjectSize,float[] jfCamPara,int picNum);

}
