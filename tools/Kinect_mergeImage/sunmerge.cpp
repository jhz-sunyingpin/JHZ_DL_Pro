/*****************************************************************************
*  @file     sunmerge.cpp                                                      *
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
#include "sunmerge.h"
/**
* @brief ���캯��
*/
sunMerge::sunMerge()
{
	center = Point2d(320, 240);
}
/**
* @brief ��������
*/
sunMerge::~sunMerge()
{
}
/**
* @brief �����Ԫ�غ�
* @param src     ����
* @return ��
*/
float sunMerge::mat_sum(Mat src)
{
	float sum = 0.0;
	for (int i = 0; i < src.cols; i++)
	for (int j = 0; j < src.rows; j++)
	{
		sum += src.at<float>(i, j);
	}
	return sum;
}

/**
* @brief ��ǰ���ص㣨��ͨ�����ĸ�˹�˲�����
* @param src     ���������ͨ����
* @param kernal  ��˹�ˣ���src��Сһ�£�
* @param a       ��һͨ������ֵ
* @param b       �ڶ�ͨ������ֵ
* @param c       ����ͨ������ֵ
*/
void sunMerge::Kernal_val(Mat& src, Mat kernal, float* a, float* b, float* c)
{
	vector<Mat> ch_img(3);
	split(src, ch_img);
	ch_img[0] = ch_img[0].mul(kernal);
	ch_img[1] = ch_img[1].mul(kernal);
	ch_img[2] = ch_img[2].mul(kernal);

	*a = mat_sum(ch_img[0]);
	*b = mat_sum(ch_img[1]);
	*c = mat_sum(ch_img[2]);
}
/**
* @brief ���ɶ�ά��˹��
* @param dim     �ߴ�
* @param sigma   ��˹�ֲ���������
* @return ��ά��˹��
*/
cv::Mat sunMerge::gaussian_kernal(int dim, int sigma)
{
	int c = dim / 2;
	Mat K(dim, dim, CV_32FC1);
	//���ɶ�ά��˹��  
	float s2 = 2.0 * sigma * sigma;
	for (int i = (-c); i <= c; i++)
	{
		int m = i + c;
		for (int j = (-c); j <= c; j++)
		{
			int n = j + c;
			float v = exp(-(1.0*i*i + 1.0*j*j) / s2);
			K.ptr<float>(m)[n] = v;
		}
	}
	Scalar all = sum(K);
	Mat gaussK;
	K.convertTo(gaussK, CV_32FC1, (1 / all[0]));
	all = sum(gaussK);
	return gaussK;
}
/**
* @brief ȡͼ������ĳ���ص�Ϊ���ĵ�С��ͼ��
* @param i              �������ص������
* @param j              �������ص�������
* @param kernal_size    �߶�
* @param img            ԭʼͼ��
* @param res            ���ȡ����ͼ��
*/
void sunMerge::GetMatOfSize(int i, int j, int kernal_size, Mat img, Mat& res)
{
	res = Mat::zeros(Size(kernal_size, kernal_size), CV_8UC3);
	int nRadius = kernal_size / 2;
	int nWidth = img.cols;
	int nHeight = img.rows;
	for (int m = 0; m < kernal_size; m++)
	{
		int nI = i + m - nRadius;
		if (nI < 0 || nI >= nHeight)
		{
			continue;
		}
		for (int n = 0; n < kernal_size; n++)
		{
			int nJ = j + n - nRadius;
			if (nJ < 0 || nJ >= nWidth)
			{
				continue;
			}
			res.at<Vec3b>(m, n) = img.at<Vec3b>(nI, nJ);
		}
	}
	//res = img(Range(i - kernal_size / 2, i + kernal_size / 2 + 1), Range(j - kernal_size / 2, j + kernal_size / 2 + 1));
}

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
cv::Mat sunMerge::ConvertMat(Mat oriImage, Mat mergeImage, Mat markImage, int nWidth, int nHeight)
{
	cv::Mat img(nHeight, nWidth, CV_8UC3);
	for (int i = 0; i < nHeight; i++)
	{
		for (int j = 0; j < nWidth; j++)
		{
			Vec3b  color0 = oriImage.at<Vec3b>(i, j);
			Vec3b  color1 = mergeImage.at<Vec3b>(i, j);
			if (markImage.at<uchar>(i, j) > 128)
			{//ǰ��
				img.at<Vec3b>(i, j) = color1;
			}
			else
			{
				img.at<Vec3b>(i, j) = color0;
			}
		}
	}
	return img;
}

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
cv::Mat sunMerge::MergeMat(Mat backImage, Mat foreImage, Mat markImage, int nPointX, int nPointY)
{
	int nBackWidth, nBackHeight, nForeWidth, nForeHeight;
	nBackWidth = backImage.cols;
	nBackHeight = backImage.rows;
	nForeWidth = foreImage.cols;
	nForeHeight = foreImage.rows;

	cv::Mat img(nBackHeight, nBackWidth, CV_8UC3);
	int nForeYRadius = nForeWidth / 2;
	int nForeXRadius = nForeHeight / 2;
	//ǰ��ͼ��0��0�����ڱ���ͼ���λ��
	int nForeX = nPointX - nForeXRadius;
	int nForeY = nPointY - nForeYRadius;
	for (int i = 0; i < nBackHeight; i++)
	{
		int nX = i - nForeX;
		for (int j = 0; j < nBackWidth; j++)
		{
			Vec3b  color0 = backImage.at<Vec3b>(i, j);
			//��ǰ������ǰ��ͼ���λ��	
			int nY = j - nForeY;
			if (nX >= 0 && nX < nForeHeight && nY >= 0 && nY < nForeWidth)
			{
				//cout << nX << "\t" << nY << endl;
				Vec3b  color1 = foreImage.at<Vec3b>(nX, nY);
				if (markImage.at<uchar>(nX, nY) > 128)
				{
					img.at<Vec3b>(i, j) = color1;
				}
				else
				{
					img.at<Vec3b>(i, j) = color0;
				}
			}
			else
			{
				img.at<Vec3b>(i, j) = color0;
			}
		}
	}
	return img;
}

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
cv::Mat sunMerge::MergeMat(Mat mergImage, Mat markImage, int nRadius, int nPointX, int nPointY)
{
	int nBackWidth, nBackHeight, nForeWidth, nForeHeight;
	nBackWidth = mergImage.cols;
	nBackHeight = mergImage.rows;
	nForeWidth = markImage.cols;
	nForeHeight = markImage.rows;

	cv::Mat img = mergImage.clone();
	cv::Mat binMat = binaryMat(markImage);
	cv::Mat boundaryMat = boundryMark(binMat, nRadius, 1);
	int nLen = 2 * nRadius + 1;
	nLen *= nLen;
	uchar *tempB = new uchar[nLen];
	uchar *tempG = new uchar[nLen];
	uchar *tempR = new uchar[nLen];

	int nForeYRadius = nForeWidth / 2;
	int nForeXRadius = nForeHeight / 2;
	//ǰ��ͼ��0��0�����ڱ���ͼ���λ��
	int nForeX = nPointX - nForeXRadius;
	int nForeY = nPointY - nForeYRadius;
	for (int i = 0; i < nBackHeight; i++)
	{
		//��ǰ������ǰ��ͼ���λ��	
		int nX = i - nForeX;
		int nI1 = (i - nRadius >= 0) ? i - nRadius : 0;
		int nI2 = (i + nRadius < nBackHeight) ? i + nRadius : nBackHeight - 1;
		for (int j = 0; j < nBackWidth; j++)
		{
			//��ǰ������ǰ��ͼ���λ��	
			int nY = j - nForeY;
			if (nX >= 0 && nX < nForeHeight && nY >= 0 && nY < nForeWidth)
			{
				if (boundaryMat.at<uchar>(nX, nY) == 0)
				{
					continue;
				}
				int nJ1 = (j - nRadius >= 0) ? j - nRadius : 0;
				int nJ2 = (j + nRadius < nBackWidth) ? j + nRadius : nBackWidth - 1;
				int nCount = 0;
				for (int m = nI1; m <= nI2; m++)
				{
					for (int n = nJ1; n <= nJ2; n++)
					{
						Vec3b color = mergImage.at<Vec3b>(m, n);
						tempB[nCount] = color(0);
						tempG[nCount] = color(1);
						tempR[nCount] = color(2);
						nCount++;
					}
				}
				if (nCount > nRadius)
				{
					int nMedian = nCount / 2;
					shellSort(tempB, nCount);
					shellSort(tempG, nCount);
					shellSort(tempR, nCount);
					img.at<Vec3b>(i, j) = Vec3b(tempB[nMedian], tempG[nMedian], tempR[nMedian]);
				}
			}
		}
	}
	if (tempB)
	{
		delete[] tempB;
		tempB = NULL;
	}
	if (tempG)
	{
		delete[] tempG;
		tempG = NULL;
	}
	if (tempR)
	{
		delete[] tempR;
		tempR = NULL;
	}
	return img;
}
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
cv::Mat sunMerge::MergeMat(Mat mergImage, Mat markImage, Mat kernal, int nRadius, int nPointX, int nPointY)
{
	int nBackWidth, nBackHeight, nForeWidth, nForeHeight;
	nBackWidth = mergImage.cols;
	nBackHeight = mergImage.rows;
	nForeWidth = markImage.cols;
	nForeHeight = markImage.rows;

	cv::Mat img = mergImage.clone();
	cv::Mat binMat = binaryMat(markImage);
	cv::Mat boundaryMat = boundryMark(binMat, nRadius, 1);
	int nLen = 2 * nRadius + 1;
	//nLen *= nLen;

	cv::Mat tmpMat;
	float a = 0.0, b = 0.0, c = 0.0;

	int nForeYRadius = nForeWidth / 2;
	int nForeXRadius = nForeHeight / 2;
	//ǰ��ͼ��0��0�����ڱ���ͼ���λ��
	int nForeX = nPointX - nForeXRadius;
	int nForeY = nPointY - nForeYRadius;
	for (int i = 0; i < nBackHeight; i++)
	{
		//��ǰ������ǰ��ͼ���λ��	
		int nX = i - nForeX;
		for (int j = 0; j < nBackWidth; j++)
		{
			//��ǰ������ǰ��ͼ���λ��	
			int nY = j - nForeY;
			if (nX >= 0 && nX < nForeHeight && nY >= 0 && nY < nForeWidth)
			{
				if (boundaryMat.at<uchar>(nX, nY) == 0)
				{
					continue;
				}
				//GetMatOfSize(i, j, nLen, mergImage, tmpMat);//�����˲������ص㲻�μӺ����Ĵ���
				GetMatOfSize(i, j, nLen, img, tmpMat);//�����˲������ص�μӺ����Ĵ���
				tmpMat.convertTo(tmpMat, CV_32FC3, 1.0 / 255.0);
				Kernal_val(tmpMat, kernal, &a, &b, &c);
				img.at<Vec3b>(i, j) = Vec3b((uchar)(a * 255), (uchar)(b * 255), (uchar)(c * 255));
			}
		}
	}
	return img;
}
/**
* @brief ��ֵ��
* @param markImage �Ҷ�ͼ��
* @return Ĭ�ϻҶ�>128 ��ֵΪ255������Ϊ0
*/
cv::Mat sunMerge::binaryMat(Mat markImage)
{
	int nWidth = markImage.cols;
	int nHeight = markImage.rows;
	cv::Mat temp = Mat::zeros(Size(nWidth, nHeight), CV_8UC1);
	for (int i = 0; i < nHeight; i++)
	{
		for (int j = 0; j < nWidth; j++)
		{
			if (markImage.at<uchar>(i, j) > 128)
			{
				temp.at<uchar>(i, j) = 255;
			}
		}
	}
	return temp;
}

/**
* @brief �߽�Ӱ�췶Χ���
* @param binMat  �Ҷ�ͼ�񣨶�ֵ��
* @param nRadius ����߽�Ӱ�췶Χ�뾶
* @param nMode   �߽�Ӱ�췶Χ��״
*                0 - ������  1 - ʮ����
* @return �߽���ͼ�񣨶�ֵ������ֵΪ1��ʾ�߽��ϣ�0��ʾ�Ǳ߽�
*/
cv::Mat sunMerge::boundryMark(Mat binMat, int nRadius, int nMode)
{
	int nWidth = binMat.cols;
	int nHeight = binMat.rows;
	int nLen = 2 * nRadius + 1;

	cv::Mat temp = Mat::zeros(Size(nWidth, nHeight), CV_8UC1);
	if (nMode == 0)
	{
		int nTotal = nLen * nLen;
		int nSumTemp = 0;
		for (int i = 0; i < nHeight; i++)
		{
			int nI1 = (i - nRadius >= 0) ? i - nRadius : 0;
			int nI2 = (i + nRadius < nHeight) ? i + nRadius : nHeight - 1;
			int nLen1 = nI2 - nI1 + 1;
			for (int j = 0; j < nWidth; j++)
			{
				int nJ1 = (j - nRadius >= 0) ? j - nRadius : 0;
				int nJ2 = (j + nRadius < nWidth) ? j + nRadius : nWidth - 1;
				int nLen2 = nJ2 - nJ1 + 1;
				nSumTemp = 0;
				for (int m = nI1; m <= nI2; m++)
				{
					for (int n = nJ1; n <= nJ2; n++)
					{
						int nValue = (int)(binMat.at<uchar>(m, n));
						nSumTemp += nValue;
					}
				}
				if (nSumTemp > 0 && nSumTemp < nLen1 * nLen2 * 255)//�ڱ߽�Ӱ�췶Χ��
				{
					temp.at<uchar>(i, j) = 255;
				}
			}
		}
	}
	else if (nMode == 1)
	{
		int nTotal = 2 * nLen - 1;
		int nSumTemp = 0;
		for (int i = 0; i < nHeight; i++)
		{
			int nI1 = (i - nRadius >= 0) ? i - nRadius : 0;
			int nI2 = (i + nRadius < nHeight) ? i + nRadius : nHeight - 1;
			int nLen1 = nI2 - nI1 + 1;
			for (int j = 0; j < nWidth; j++)
			{
				int nJ1 = (j - nRadius >= 0) ? j - nRadius : 0;
				int nJ2 = (j + nRadius < nWidth) ? j + nRadius : nWidth - 1;
				int nLen2 = nJ2 - nJ1 + 1;
				nSumTemp = 0;
				for (int m = nI1; m <= nI2; m++)
				{
					int nValue = (int)(binMat.at<uchar>(m, j));
					nSumTemp += nValue;
				}
				for (int n = nJ1; n <= nJ2; n++)
				{
					int nValue = (int)(binMat.at<uchar>(i, n));
					nSumTemp += nValue;
				}
				if (nSumTemp > 0 && nSumTemp < (nLen1 + nLen2) * 255)//�ڱ߽�Ӱ�췶Χ��
				{
					temp.at<uchar>(i, j) = 255;
				}
			}
		}
	}
	else
	{
	}

	return temp;
}


void sunMerge::shellSort(uchar* pBuffer, int len)
{
	int step;
	int i, j;
	int temp;
	for (step = len / 2; step > 0; step /= 2) //�������Ʋ���,���ݼ���1
	{
		// i�ӵ�step��ʼ���У�ӦΪ��������ĵ�һ��Ԫ��
		// �����Ȳ������ӵڶ�����ʼ����
		for (i = step; i < len; i++)
		{
			temp = pBuffer[i];
			for (j = i - step; (j >= 0 && temp < pBuffer[j]); j -= step)
			{
				pBuffer[j + step] = pBuffer[j];
			}
			pBuffer[j + step] = temp; //����һ��λ������
		}
	}
}

/**
* @brief ���ں�ͼ��ֻ��ǰ��ͼ���Ե������ֵ�˲�
* @param oriMat      �ں�ͼ��
* @param binMat      ǰ���Ķ�ֵͼ��
* @param nRadius     �˲��뾶
* @return �˲�����ں�ͼ��
* @note ���ͼ��Ϊ��ͨ��
*/
cv::Mat sunMerge::medianFiltRGB(Mat oriMat, Mat boundaryMat, int nRadius)
{
	int nWidth = oriMat.cols;
	int nHeight = oriMat.rows;
	Mat ouputMat = oriMat.clone();
	int nLen = 2 * nRadius + 1;
	nLen *= nLen;
	uchar *tempB = new uchar[nLen];
	uchar *tempG = new uchar[nLen];
	uchar *tempR = new uchar[nLen];
	for (int i = 0; i < nHeight; i++)
	{
		int nI1 = (i - nRadius >= 0) ? i - nRadius : 0;
		int nI2 = (i + nRadius < nHeight) ? i + nRadius : nHeight - 1;
		for (int j = 0; j < nWidth; j++)
		{
			int nJ1 = (j - nRadius >= 0) ? j - nRadius : 0;
			int nJ2 = (j + nRadius < nWidth) ? j + nRadius : nWidth - 1;

			if (boundaryMat.at<uchar>(i, j) == 0)
			{//���ڱ߽緶Χ
				continue;
			}

			int nCount = 0;
			for (int m = nI1; m <= nI2; m++)
			{
				for (int n = nJ1; n <= nJ2; n++)
				{
					Vec3b color = oriMat.at<Vec3b>(m, n);
					tempB[nCount] = color(0);
					tempG[nCount] = color(1);
					tempR[nCount] = color(2);
					nCount++;
				}
			}
			//int nCount = nearlyPointsVec.size();
			if (nCount > nRadius)
			{
				int nMedian = nCount / 2;
				shellSort(tempB, nCount);
				shellSort(tempG, nCount);
				shellSort(tempR, nCount);
				ouputMat.at<Vec3b>(i, j) = Vec3b(tempB[nMedian], tempG[nMedian], tempR[nMedian]);
			}
		}
	}
	if (tempB)
	{
		delete[] tempB;
		tempB = NULL;
	}
	if (tempG)
	{
		delete[] tempG;
		tempG = NULL;
	}
	if (tempR)
	{
		delete[] tempR;
		tempR = NULL;
	}
	return ouputMat;
}

/**
* @brief ͼ������
* @param mergImage      ԭʼͼ��
* @param nWidth         ͼ��������
* @param nHeight        ͼ������߶�
* @param dZoomRatio     ���ű���
* @return ���ź��ͼ��
*/
cv::Mat sunMerge::zoomMat(Mat oriMat, int nWidth, int nHeight, double dZoomRatio)
{
	Mat tempMat;
	nWidth = (int)(nWidth * dZoomRatio);
	nHeight = (int)(nHeight * dZoomRatio);
	resize(oriMat, tempMat, Size(nWidth, nHeight));
	return tempMat;
}

/**
* @brief ������ת-��������任
* @param point     ԭ����ϵ�е������
* @param dTheta    ��ת�Ƕ�
* @return ������ϵ�е������
* @note   δʹ��
*/
Point2d sunMerge::coordinates(Point2d point, double dTheta)
{
	Point2d pointTemp;
	pointTemp.x = (point.x - center.x) * cos(dTheta) + (point.y - center.y) * sin(dTheta) + center.x;
	pointTemp.y = -(point.x - center.x) * sin(dTheta) + (point.y - center.y) * cos(dTheta) + center.y;
	return pointTemp;
}
/**
* @brief ������ת-�������任
* @param point     ������ϵ�е������
* @param dTheta    ��ת�Ƕ�
* @return ԭ����ϵ�е������
* @note   δʹ��
*/
Point2d sunMerge::coordinatesInv(Point2d point, double dTheta)
{
	Point2d pointTemp;
	pointTemp.x = (point.x - center.x) * cos(dTheta) - (point.y - center.y) * sin(dTheta) + center.x;
	pointTemp.y = (point.x - center.x) * sin(dTheta) + (point.y - center.y) * cos(dTheta) + center.y;
	return pointTemp;
}

/**
* @brief ��ɫͼ����ͨ�������ص��˫���Բ�ֵ
* @param leftUp         ����ֵ���ص���������ص�
* @param rightUp        ����ֵ���ص���������ص�
* @param leftDown       ����ֵ���ص���������ص�
* @param rightDown      ����ֵ���ص���������ص�
* @retval point         ����ֵ���ص�
*/
void sunMerge::doubleLinerInterpolate(ColorPoint leftUp, ColorPoint rightUp, ColorPoint leftDown, ColorPoint rightDown, ColorPoint &point)
{
	double dXlen = rightUp.m_Point.x - leftUp.m_Point.x;
	double dYlen = leftUp.m_Point.y - leftDown.m_Point.y;
	double dZTemp1 = (rightUp.m_Color(0) - leftUp.m_Color(0)) * (point.m_Point.x - leftUp.m_Point.x) / dXlen + leftUp.m_Color(0);
	double dZTemp2 = (rightDown.m_Color(0) - leftDown.m_Color(0)) * (point.m_Point.x - leftDown.m_Point.x) / dXlen + leftDown.m_Color(0);
	point.m_Color(0) = (dZTemp2 - dZTemp1) * (leftUp.m_Point.y - point.m_Point.y) / dYlen + dZTemp1;

	dZTemp1 = (rightUp.m_Color(1) - leftUp.m_Color(1)) * (point.m_Point.x - leftUp.m_Point.x) / dXlen + leftUp.m_Color(1);
	dZTemp2 = (rightDown.m_Color(1) - leftDown.m_Color(1)) * (point.m_Point.x - leftDown.m_Point.x) / dXlen + leftDown.m_Color(1);
	point.m_Color(1) = (dZTemp2 - dZTemp1) * (leftUp.m_Point.y - point.m_Point.y) / dYlen + dZTemp1;

	dZTemp1 = (rightUp.m_Color(2) - leftUp.m_Color(2)) * (point.m_Point.x - leftUp.m_Point.x) / dXlen + leftUp.m_Color(2);
	dZTemp2 = (rightDown.m_Color(2) - leftDown.m_Color(2)) * (point.m_Point.x - leftDown.m_Point.x) / dXlen + leftDown.m_Color(2);
	point.m_Color(2) = (dZTemp2 - dZTemp1) * (leftUp.m_Point.y - point.m_Point.y) / dYlen + dZTemp1;
}
/**
* @brief �Ҷ�ͼ�񣨵�ͨ�������ص��˫���Բ�ֵ
* @param leftUp         ����ֵ���ص���������ص�
* @param rightUp        ����ֵ���ص���������ص�
* @param leftDown       ����ֵ���ص���������ص�
* @param rightDown      ����ֵ���ص���������ص�
* @retval point         ����ֵ���ص�
*/
void sunMerge::doubleLinerInterpolate(ColorPoint_Mark leftUp, ColorPoint_Mark rightUp, ColorPoint_Mark leftDown, ColorPoint_Mark rightDown, ColorPoint_Mark &point)
{
	double dXlen = rightUp.m_Point.x - leftUp.m_Point.x;
	double dYlen = leftUp.m_Point.y - leftDown.m_Point.y;
	double dZTemp1 = (rightUp.m_Color - leftUp.m_Color) * (point.m_Point.x - leftUp.m_Point.x) / dXlen + leftUp.m_Color;
	double dZTemp2 = (rightDown.m_Color - leftDown.m_Color) * (point.m_Point.x - leftDown.m_Point.x) / dXlen + leftDown.m_Color;
	point.m_Color = (dZTemp2 - dZTemp1) * (leftUp.m_Point.y - point.m_Point.y) / dYlen + dZTemp1;
}

/**
* @brief ǰ��ͼ������ͼ����ת
* @param oriForeMat     ԭʼǰ��ͼ��
* @param oriMarkMat     ԭʼ���ͼ��
* @param foreMat        ��ת���ǰ��ͼ��
* @param markMat        ��ת��ı��ͼ��
* @param dTheta         ��ת�Ƕ�
* @note ���ͼ���С���ı�
*/
void sunMerge::rotateMat(Mat oriForeMat, Mat oriMarkMat, Mat &foreMat, Mat &markMat, double dTheta)
{
	int nWidth, nHeight;
	nWidth = oriForeMat.cols;
	nHeight = oriForeMat.rows;

	if (dTheta == 0)
	{
		foreMat = oriForeMat.clone();
		markMat = oriMarkMat.clone();
		return;
	}

	foreMat = Mat::zeros(Size(nWidth, nHeight), CV_8UC3);
	markMat = Mat::zeros(Size(nWidth, nHeight), CV_8UC1);

	double cosAngle = cos(dTheta);
	double sinAngle = sin(dTheta);

	//����ת�������ϵ��Ϊ��׼����ϵ�����㵱ǰͼ�����أ����µ�����ϵ�е����꼰��Ӧ��color
	Vec3b *p;
	uchar *p_mark;
	for (int i = 0; i < nHeight; i++)
	{
		double dCurrentY = i - nHeight / 2;
		p = foreMat.ptr<Vec3b>(i);
		p_mark = markMat.ptr<uchar>(i);
		for (int j = 0; j < nWidth; j++)
		{
			//����任
			//int x = static_cast<int>(j  * cosAngle + i * sinAngle + num1 + 0.5);
			//int y = static_cast<int>(-j * sinAngle + i * cosAngle + num2 + 0.5);
			//double dx = j  * cosAngle + i * sinAngle + num1;
			//double dy = -j * sinAngle + i * cosAngle + num2;
			double dCurrentX = j - nWidth / 2;
			double dx = dCurrentX  * cosAngle - dCurrentY * sinAngle + (nWidth / 2);
			double dy = dCurrentX * sinAngle + dCurrentY * cosAngle + (nHeight / 2);
			//�ٽ��ľ������������
			int nXMinus, nXPlus, nYMinus, nYPlus;
			if (dx >= 0)
			{
				nXMinus = (int)dx;
				nXPlus = (int)(dx + 1);
			}
			else
			{
				nXMinus = (int)(dx - 1);
				nXPlus = (int)dx;
			}
			if (dy >= 0)
			{
				nYMinus = (int)dy;
				nYPlus = (int)(dy + 1);
			}
			else
			{
				nYMinus = (int)(dy - 1);
				nYPlus = (int)dy;
			}

			if (nXMinus >= 0 && nYMinus >= 0 && nXPlus < nWidth && nYPlus < nHeight)//��������㶼��ͼ����
			{//˫���Բ�ֵ - ǰ��ͼ
				ColorPoint leftUp, rightUp, leftDown, rightDown, current;
				leftUp.m_Point = Point2d((double)nXMinus, (double)nYPlus);
				leftUp.m_Color = oriForeMat.ptr<Vec3b>(nYPlus)[nXMinus];

				rightUp.m_Point = Point2d((double)nXPlus, (double)nYPlus);
				rightUp.m_Color = oriForeMat.ptr<Vec3b>(nYPlus)[nXPlus];

				leftDown.m_Point = Point2d((double)nXMinus, (double)nYMinus);
				leftDown.m_Color = oriForeMat.ptr<Vec3b>(nYMinus)[nXMinus];

				rightDown.m_Point = Point2d((double)nXPlus, (double)nYMinus);
				rightDown.m_Color = oriForeMat.ptr<Vec3b>(nYMinus)[nXPlus];

				current.m_Point = Point2d(dx, dy);
				current.m_Color = Vec3b(0, 0, 0);

				doubleLinerInterpolate(leftUp, rightUp, leftDown, rightDown, current);

				p[j] = current.m_Color;

				//˫���Բ�ֵ--���ͼ��

				ColorPoint_Mark leftUp_Mark, rightUp_Mark, leftDown_Mark, rightDown_Mark, current_Mark;
				leftUp_Mark.m_Point = Point2d((double)nXMinus, (double)nYPlus);
				leftUp_Mark.m_Color = oriMarkMat.ptr<uchar>(nYPlus)[nXMinus];

				rightUp_Mark.m_Point = Point2d((double)nXPlus, (double)nYPlus);
				rightUp_Mark.m_Color = oriMarkMat.ptr<uchar>(nYPlus)[nXPlus];

				leftDown_Mark.m_Point = Point2d((double)nXMinus, (double)nYMinus);
				leftDown_Mark.m_Color = oriMarkMat.ptr<uchar>(nYMinus)[nXMinus];

				rightDown_Mark.m_Point = Point2d((double)nXPlus, (double)nYMinus);
				rightDown_Mark.m_Color = oriMarkMat.ptr<uchar>(nYMinus)[nXPlus];

				//if (rightDown_Mark.m_Color > 128)
				//{
				//	cout << "��" << "\t" << i << "\t" << j << "\t" << rightDown_Mark.m_Color << endl;
				//}

				current_Mark.m_Point = Point2d(dx, dy);
				current_Mark.m_Color = 0;

				doubleLinerInterpolate(leftUp_Mark, rightUp_Mark, leftDown_Mark, rightDown_Mark, current_Mark);

				p_mark[j] = current_Mark.m_Color;
			}
			else if (nXPlus < 0 && nYPlus < 0 && nXMinus >= nWidth && nYMinus >= nHeight)//��������㶼��ͼ����
			{
			}
			else//����
			{//���ڽ�ȡֵ
				int nX, nY;
				if (dx > 0)
				{
					nX = (int)(dx + 0.5);
				}
				else
				{
					nX = (int)(dx - 0.5);
				}
				if (dy > 0)
				{
					nY = (int)(dy + 0.5);
				}
				else
				{
					nY = (int)(dy - 0.5);
				}
				//
				if (nX >= 0 && nY >= 0 && nX < oriForeMat.cols && nY < oriForeMat.rows)
				{
					p[j] = oriForeMat.ptr<Vec3b>(nY)[nX];
					p_mark[j] = oriMarkMat.ptr<uchar>(nY)[nX];
				}
			}
		}
	}
}

//
char *my_itoa(int n)
{
	static char str[CHARNUM];
	int i;
	for (i = 0; i < CHARNUM - 1; i++)
	{
		str[i] = '0';
	}
	str[CHARNUM - 1] = '\0';
	i = CHARNUM - 1;
	while (n)
	{
		str[i--] = n % 10 + '0';
		n /= 10;
	}
	return str;
}