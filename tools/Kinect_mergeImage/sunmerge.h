/*****************************************************************************
*  @file     sunmerge.h                                                      *
*  @brief    前景图像与背景图像融合相关接口                                  *
*  Details.                                                                  *
*                                                                            *
*  @author   Sun                                                             *
*  @email    sunyingpin@126.com                                              *
*  @version  1.0.0.1                                                         *
*  @date     2016/08/19                                                      *
*  @license                                                                  *
*                                                                            *
*----------------------------------------------------------------------------*
*  Remark         : Description                                              *
*----------------------------------------------------------------------------*
*  Change History :                                                          *
*  <Date>     | <Version> | <Author>       | <Description>                   *
*----------------------------------------------------------------------------*
*  2016/08/19 | 1.0.0.1   | Sun            | Create file                     *
*----------------------------------------------------------------------------*
*                                                                            *
*****************************************************************************/
#ifndef SUN_MERGE_H
#define SUN_MERGE_H

#include <io.h>
#include <iostream>
#include <string>
#include <opencv2/imgproc.hpp>
#include <opencv2\highgui.hpp>

#define  CHARNUM 5

using   namespace   std;
using   namespace   cv;

char *my_itoa(int n);

/**
* @brief 彩色图像像素点
*/
struct ColorPoint
{
	Point2d m_Point;//坐标
	Vec3b m_Color;//颜色
};

/**
* @brief 标记图像像素点
*/
struct ColorPoint_Mark
{
	Point2d m_Point;//坐标
	uchar m_Color;//灰度值
};

/**
* @brief 图像融合相关接口类
*        包含二值化、缩放、旋转、双线性插值、中值滤波、融合等接口
*/
class sunMerge
{
public:
	/**
	* @brief 构造函数
	*/
	sunMerge();
	/**
	* @brief 析构函数
	*/
	~sunMerge();
	/**
	* @brief 生成二维高斯核
	* @param dim     尺寸
	* @param sigma   高斯分布函数方差
	* @return 二维高斯核
	*/
	cv::Mat gaussian_kernal(int dim, int sigma);
	/**
	* @brief 二值化
	* @param markImage 灰度图像
	* @return 默认灰度>128 赋值为1，否则为0
	*/
	cv::Mat binaryMat(Mat markImage);
	/**
	* @brief 边界影响范围标记
	* @param binMat  灰度图像（二值）
	* @param nRadius 定义边界影响范围半径
	* @param nMode   边界影响范围形状
	*                0 - 正方形  1 - 十字形
	* @return 边界标记图像（二值），数值为1表示边界上，0表示非边界
	*/
	cv::Mat boundryMark(Mat markImage, int nRadius, int nMode);
	/**
	* @brief 前景图像与背景图像融合
	* @param oriImage       背景图像（三通道）
	* @param mergeImage     前景图像（三通道）
	* @param markImage      前景标记图像（单通道）
	* @param nWidth         图像宽度
	* @param nHeight        图像高度
	* @return 融合图像，前景图像边缘未处理
	* @note 输出图像为三通道，图像大小都相同
	*/
	cv::Mat ConvertMat(Mat oriImage, Mat mergeImage, Mat markImage, int nWidth, int nHeight);

	/**
	* @brief 对融合图像只在前景图像边缘处做中值滤波
	* @param oriMat      融合图像
	* @param binMat      前景的二值图像
	* @param nRadius     滤波半径
	* @return 滤波后的融合图像
	* @note 输出图像为三通道
	*/
	cv::Mat medianFiltRGB(Mat oriMat, Mat binMat, int nRadius);
	/**
	* @brief 前景图像与背景图像融合
	* @param backImage      背景图像（三通道）
	* @param foreImage      前景图像（三通道）
	* @param markImage      前景标记图像（单通道）
	* @param nPointX        融合中心位置横坐标
	* @param nPointY        融合中心位置纵坐标
	* @return 融合图像，前景图像边缘未处理
	* @note 输出图像为三通道，前景图像与背景图像大小不一定相同，指定融合位置对应前景图像的中心
	*/
	cv::Mat MergeMat(Mat backImage, Mat foreImage, Mat markImage, int nPointX, int nPointY);
	/**
	* @brief 融合图像的再处理（中值滤波）
	* @param mergImage      融合图像（三通道）
	* @param markImage      前景标记图像（单通道）
	* @param nRadius        滤波半径
	* @param nPointX        融合中心位置横坐标
	* @param nPointY        融合中心位置纵坐标
	* @return 前景图像边缘处理后的融合图像(中值滤波）
	* @note 输出图像为三通道
	*/
	cv::Mat MergeMat(Mat mergImage, Mat markImage, int nRadius, int nPointX, int nPointY);

	/**
	* @brief 融合图像的再处理（高斯滤波）
	* @param mergImage      融合图像（三通道）
	* @param markImage      前景标记图像（单通道）
	* @param kernal         高斯滤波核（单通道）
	* @param nRadius        滤波半径（与高斯核尺度对应）
	* @param nPointX        融合中心位置横坐标
	* @param nPointY        融合中心位置纵坐标
	* @return 前景图像边缘处理后的融合图像(高斯滤波）
	* @note   输出图像为三通道
	*/
	cv::Mat MergeMat(Mat mergImage, Mat markImage, Mat kernal, int nRadius, int nPointX, int nPointY);
	/**
	* @brief 图像缩放
	* @param mergImage      原始图像
	* @param nWidth         图像基础宽度
	* @param nHeight        图像基础高度
	* @param dZoomRatio     缩放比例
	* @return 缩放后的图像
	*/
	cv::Mat zoomMat(Mat oriMat, int nWidth, int nHeight, double dZoomRatio);
	/**
	* @brief 彩色图像（三通道）像素点的双线性插值
	* @param leftUp         待插值像素点的左上像素点
	* @param rightUp        待插值像素点的右上像素点
	* @param leftDown       待插值像素点的左下像素点
	* @param rightDown      待插值像素点的右下像素点
	* @retval point         待插值像素点
	*/
	void doubleLinerInterpolate(ColorPoint leftUp, ColorPoint rightUp, ColorPoint leftDown, ColorPoint rightDown, ColorPoint &point);
	/**
	* @brief 灰度图像（单通道）像素点的双线性插值
	* @param leftUp         待插值像素点的左上像素点
	* @param rightUp        待插值像素点的右上像素点
	* @param leftDown       待插值像素点的左下像素点
	* @param rightDown      待插值像素点的右下像素点
	* @retval point         待插值像素点
	*/
	void doubleLinerInterpolate(ColorPoint_Mark leftUp, ColorPoint_Mark rightUp, ColorPoint_Mark leftDown, ColorPoint_Mark rightDown, ColorPoint_Mark &point);
	/**
	* @brief 前景图像与标记图像旋转
	* @param oriForeMat     原始前景图像
	* @param oriMarkMat     原始标记图像
	* @param foreMat        旋转后的前景图像
	* @param markMat        旋转后的标记图像
	* @param dTheta         旋转角度
	* @note 输出图像大小不改变
	*/
	void rotateMat(Mat oriForeMat, Mat oriMarkMat, Mat &foreMat, Mat &markMat, double dTheta);


private:
	/**
	* @brief 坐标旋转-坐标点正变换
	* @param point     原坐标系中的坐标点
	* @param dTheta    旋转角度
	* @return 新坐标系中的坐标点
	* @note   未使用
	*/
	Point2d coordinates(Point2d point, double dTheta);
	/**
	* @brief 坐标旋转-坐标点逆变换
	* @param point     新坐标系中的坐标点
	* @param dTheta    旋转角度
	* @return 原坐标系中的坐标点
	* @note   未使用
	*/
	Point2d coordinatesInv(Point2d point, double dTheta);
	/**
	* @brief 希尔排序
	* @param pBuffer     数组
	* @param len         长度
	*/
	void shellSort(uchar* pBuffer, int len);
	/**
	* @brief 矩阵各元素和
	* @param src     矩阵
	* @return 和
	*/
	float mat_sum(Mat src);
	/**
	* @brief 当前像素点（三通道）的高斯滤波处理
	* @param src     邻域矩阵（三通道）
	* @param kernal  高斯核（与src大小一致）
	* @param a       第一通道返回值
	* @param b       第二通道返回值
	* @param c       第三通道返回值
	*/
	void Kernal_val(Mat& src, Mat kernal, float* a, float* b, float* c);
	/**
	* @brief 取图像中以某像素点为中心的小块图像
	* @param i              中心像素点横坐标
	* @param j              中心像素点纵坐标
	* @param kernal_size    尺度
	* @param img            原始图像
	* @param res            输出取出的图像
	*/
	void GetMatOfSize(int i, int j, int kernal_size, Mat img, Mat& res);
	//! 旋转中心
	//@note 未使用
	Point2d center;
};


#endif