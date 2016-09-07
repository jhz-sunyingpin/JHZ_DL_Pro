/*****************************************************************************
*  @file     sunmerge.h                                                      *
*  @brief    ǰ��ͼ���뱳��ͼ���ں���ؽӿ�                                  *
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
* @brief ��ɫͼ�����ص�
*/
struct ColorPoint
{
	Point2d m_Point;//����
	Vec3b m_Color;//��ɫ
};

/**
* @brief ���ͼ�����ص�
*/
struct ColorPoint_Mark
{
	Point2d m_Point;//����
	uchar m_Color;//�Ҷ�ֵ
};

/**
* @brief ͼ���ں���ؽӿ���
*        ������ֵ�������š���ת��˫���Բ�ֵ����ֵ�˲����ںϵȽӿ�
*/
class sunMerge
{
public:
	/**
	* @brief ���캯��
	*/
	sunMerge();
	/**
	* @brief ��������
	*/
	~sunMerge();
	/**
	* @brief ���ɶ�ά��˹��
	* @param dim     �ߴ�
	* @param sigma   ��˹�ֲ���������
	* @return ��ά��˹��
	*/
	cv::Mat gaussian_kernal(int dim, int sigma);
	/**
	* @brief ��ֵ��
	* @param markImage �Ҷ�ͼ��
	* @return Ĭ�ϻҶ�>128 ��ֵΪ1������Ϊ0
	*/
	cv::Mat binaryMat(Mat markImage);
	/**
	* @brief �߽�Ӱ�췶Χ���
	* @param binMat  �Ҷ�ͼ�񣨶�ֵ��
	* @param nRadius ����߽�Ӱ�췶Χ�뾶
	* @param nMode   �߽�Ӱ�췶Χ��״
	*                0 - ������  1 - ʮ����
	* @return �߽���ͼ�񣨶�ֵ������ֵΪ1��ʾ�߽��ϣ�0��ʾ�Ǳ߽�
	*/
	cv::Mat boundryMark(Mat markImage, int nRadius, int nMode);
	/**
	* @brief ǰ��ͼ���뱳��ͼ���ں�
	* @param oriImage       ����ͼ����ͨ����
	* @param mergeImage     ǰ��ͼ����ͨ����
	* @param markImage      ǰ�����ͼ�񣨵�ͨ����
	* @param nWidth         ͼ����
	* @param nHeight        ͼ��߶�
	* @return �ں�ͼ��ǰ��ͼ���Եδ����
	* @note ���ͼ��Ϊ��ͨ����ͼ���С����ͬ
	*/
	cv::Mat ConvertMat(Mat oriImage, Mat mergeImage, Mat markImage, int nWidth, int nHeight);

	/**
	* @brief ���ں�ͼ��ֻ��ǰ��ͼ���Ե������ֵ�˲�
	* @param oriMat      �ں�ͼ��
	* @param binMat      ǰ���Ķ�ֵͼ��
	* @param nRadius     �˲��뾶
	* @return �˲�����ں�ͼ��
	* @note ���ͼ��Ϊ��ͨ��
	*/
	cv::Mat medianFiltRGB(Mat oriMat, Mat binMat, int nRadius);
	/**
	* @brief ǰ��ͼ���뱳��ͼ���ں�
	* @param backImage      ����ͼ����ͨ����
	* @param foreImage      ǰ��ͼ����ͨ����
	* @param markImage      ǰ�����ͼ�񣨵�ͨ����
	* @param nPointX        �ں�����λ�ú�����
	* @param nPointY        �ں�����λ��������
	* @return �ں�ͼ��ǰ��ͼ���Եδ����
	* @note ���ͼ��Ϊ��ͨ����ǰ��ͼ���뱳��ͼ���С��һ����ͬ��ָ���ں�λ�ö�Ӧǰ��ͼ�������
	*/
	cv::Mat MergeMat(Mat backImage, Mat foreImage, Mat markImage, int nPointX, int nPointY);
	/**
	* @brief �ں�ͼ����ٴ�����ֵ�˲���
	* @param mergImage      �ں�ͼ����ͨ����
	* @param markImage      ǰ�����ͼ�񣨵�ͨ����
	* @param nRadius        �˲��뾶
	* @param nPointX        �ں�����λ�ú�����
	* @param nPointY        �ں�����λ��������
	* @return ǰ��ͼ���Ե�������ں�ͼ��(��ֵ�˲���
	* @note ���ͼ��Ϊ��ͨ��
	*/
	cv::Mat MergeMat(Mat mergImage, Mat markImage, int nRadius, int nPointX, int nPointY);

	/**
	* @brief �ں�ͼ����ٴ�����˹�˲���
	* @param mergImage      �ں�ͼ����ͨ����
	* @param markImage      ǰ�����ͼ�񣨵�ͨ����
	* @param kernal         ��˹�˲��ˣ���ͨ����
	* @param nRadius        �˲��뾶�����˹�˳߶ȶ�Ӧ��
	* @param nPointX        �ں�����λ�ú�����
	* @param nPointY        �ں�����λ��������
	* @return ǰ��ͼ���Ե�������ں�ͼ��(��˹�˲���
	* @note   ���ͼ��Ϊ��ͨ��
	*/
	cv::Mat MergeMat(Mat mergImage, Mat markImage, Mat kernal, int nRadius, int nPointX, int nPointY);
	/**
	* @brief ͼ������
	* @param mergImage      ԭʼͼ��
	* @param nWidth         ͼ��������
	* @param nHeight        ͼ������߶�
	* @param dZoomRatio     ���ű���
	* @return ���ź��ͼ��
	*/
	cv::Mat zoomMat(Mat oriMat, int nWidth, int nHeight, double dZoomRatio);
	/**
	* @brief ��ɫͼ����ͨ�������ص��˫���Բ�ֵ
	* @param leftUp         ����ֵ���ص���������ص�
	* @param rightUp        ����ֵ���ص���������ص�
	* @param leftDown       ����ֵ���ص���������ص�
	* @param rightDown      ����ֵ���ص���������ص�
	* @retval point         ����ֵ���ص�
	*/
	void doubleLinerInterpolate(ColorPoint leftUp, ColorPoint rightUp, ColorPoint leftDown, ColorPoint rightDown, ColorPoint &point);
	/**
	* @brief �Ҷ�ͼ�񣨵�ͨ�������ص��˫���Բ�ֵ
	* @param leftUp         ����ֵ���ص���������ص�
	* @param rightUp        ����ֵ���ص���������ص�
	* @param leftDown       ����ֵ���ص���������ص�
	* @param rightDown      ����ֵ���ص���������ص�
	* @retval point         ����ֵ���ص�
	*/
	void doubleLinerInterpolate(ColorPoint_Mark leftUp, ColorPoint_Mark rightUp, ColorPoint_Mark leftDown, ColorPoint_Mark rightDown, ColorPoint_Mark &point);
	/**
	* @brief ǰ��ͼ������ͼ����ת
	* @param oriForeMat     ԭʼǰ��ͼ��
	* @param oriMarkMat     ԭʼ���ͼ��
	* @param foreMat        ��ת���ǰ��ͼ��
	* @param markMat        ��ת��ı��ͼ��
	* @param dTheta         ��ת�Ƕ�
	* @note ���ͼ���С���ı�
	*/
	void rotateMat(Mat oriForeMat, Mat oriMarkMat, Mat &foreMat, Mat &markMat, double dTheta);


private:
	/**
	* @brief ������ת-��������任
	* @param point     ԭ����ϵ�е������
	* @param dTheta    ��ת�Ƕ�
	* @return ������ϵ�е������
	* @note   δʹ��
	*/
	Point2d coordinates(Point2d point, double dTheta);
	/**
	* @brief ������ת-�������任
	* @param point     ������ϵ�е������
	* @param dTheta    ��ת�Ƕ�
	* @return ԭ����ϵ�е������
	* @note   δʹ��
	*/
	Point2d coordinatesInv(Point2d point, double dTheta);
	/**
	* @brief ϣ������
	* @param pBuffer     ����
	* @param len         ����
	*/
	void shellSort(uchar* pBuffer, int len);
	/**
	* @brief �����Ԫ�غ�
	* @param src     ����
	* @return ��
	*/
	float mat_sum(Mat src);
	/**
	* @brief ��ǰ���ص㣨��ͨ�����ĸ�˹�˲�����
	* @param src     ���������ͨ����
	* @param kernal  ��˹�ˣ���src��Сһ�£�
	* @param a       ��һͨ������ֵ
	* @param b       �ڶ�ͨ������ֵ
	* @param c       ����ͨ������ֵ
	*/
	void Kernal_val(Mat& src, Mat kernal, float* a, float* b, float* c);
	/**
	* @brief ȡͼ������ĳ���ص�Ϊ���ĵ�С��ͼ��
	* @param i              �������ص������
	* @param j              �������ص�������
	* @param kernal_size    �߶�
	* @param img            ԭʼͼ��
	* @param res            ���ȡ����ͼ��
	*/
	void GetMatOfSize(int i, int j, int kernal_size, Mat img, Mat& res);
	//! ��ת����
	//@note δʹ��
	Point2d center;
};


#endif