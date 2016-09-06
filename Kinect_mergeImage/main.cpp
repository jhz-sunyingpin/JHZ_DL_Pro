/*****************************************************************************
*  @file     main.cpp                                                        *
*  @brief    前景图像与背景图像融合，前景对应的标记图像辅助                  *
*            随机调整前景图像的位置、大小、角度                              *
*            相同大小和角度的前景图像与背景图像在多个位置融合                *
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
#include <time.h>
#include <iostream>
//! 最大放大倍数*10
int m_nZoomRatioMax = 20;
//! 最大旋转角度
int m_nThetaMax = 359;
//! 初始缩放系数
int m_nZoomSlider = 10;
//! 初始旋转系数
int m_nRotateSlider = 0;
//! 缩放系数
double m_dZoomRatio = 1.0;
//! 旋转角度
double m_dTheta = 0.0;
//! 前景图像在背景图像中的位置
int nPosX, nPosY;
//! 原始背景图像
Mat m_MatBackImageOri;
//! 原始前景图像
Mat m_MatForeImageOri;
//! 原始前景标记图像
Mat m_MatMarkImageOri;
//! 处理后的背景图像
Mat m_MatBackImage;
//! 处理后的前景图像
Mat m_MatForeImage;
//! 处理后的标记图像
Mat m_MatMarkImage;
//! 融合图像
Mat m_MatMergeImage;
//! 图像初始宽度
int nForeBaseWidth = 640;
//! 图像初始高度
int nForeBaseHeigth = 480;
//! 融合相关接口类
sunMerge Merge;
//! 当前位置是否改变
//! 执行鼠标双击操作后，位置改变，将赋值为true；否则为false
bool bChange = false;

/**
* @brief 图像缩放
*        将对前景图像和标记图像进行旋转和缩放操作
*/
void on_trackbarZoom()
{
	m_dZoomRatio = (double)m_nZoomSlider / 10;
	//先完成当前参数的旋转，再执行缩放
	Mat matTemp1 = m_MatForeImageOri.clone();
	Mat matTemp2 = m_MatMarkImageOri.clone();
	Merge.rotateMat(matTemp1, matTemp2, m_MatForeImage, m_MatMarkImage, m_dTheta);
	//缩放
	m_MatForeImage = Merge.zoomMat(m_MatForeImage, nForeBaseWidth, nForeBaseHeigth, m_dZoomRatio);
	m_MatMarkImage = Merge.zoomMat(m_MatMarkImage, nForeBaseWidth, nForeBaseHeigth, m_dZoomRatio);
}

/**
* @brief 图像旋转
*        将对前景图像和标记图像进行缩放和旋转操作
*/
void on_trackbarRotate()
{
	m_dTheta = (double)(m_nRotateSlider)* 3.1415926 / 180;
	//先完成当前参数的缩放，再执行旋转
	m_MatForeImage = Merge.zoomMat(m_MatForeImageOri, nForeBaseWidth, nForeBaseHeigth, m_dZoomRatio);
	m_MatMarkImage = Merge.zoomMat(m_MatMarkImageOri, nForeBaseWidth, nForeBaseHeigth, m_dZoomRatio);
	//旋转
	Mat matTemp1 = m_MatForeImage.clone();
	Mat matTemp2 = m_MatMarkImage.clone();
	Merge.rotateMat(matTemp1, matTemp2, m_MatForeImage, m_MatMarkImage, m_dTheta);

}

/**
* @brief 鼠标事件 - 未使用
*        双击确定图片融合位置
* @param event       鼠标事件
* @param x           像素横坐标
* @param y           像素纵坐标
* @note 并标记当前位置是否改变；
*       当前对应的像素点坐标将与前景图像的中心坐标一致
*
*/
static void onMouse(int event, int x, int y, int, void*)
{
	if (event != CV_EVENT_LBUTTONDBLCLK)
	{
		bChange = false;
		return;
	}
	bChange = true;
	nPosY = x;
	nPosX = y;
}
/**
* @brief 去文件名后缀
* @param file       带后缀文件名
* @return 不带后缀的文件名
*
*/
char *remove_suffix(char *file) 
{
	char *last_dot = strrchr(file, '.');
	if (last_dot != NULL && strrchr(file, '\\') < last_dot)
		*last_dot = '\0';
	return file;
}

int nCommonWidth = 640;
int nCommonHeight = 480;

/**
* @brief 主函数
* 读取背景图像、前景图像、前景标记图像
* 随机调整前景图像大小、角度
* 同一背景，同一前景在四个位置融合分别输出
* 当前图片处理确认结束后，自动处理下一张图片，直到所有图片处理保存结束
*
*/
void main()
{
	const char* rootPathIn = "D:\\Databases\\000JHZ\\office\\0906\\";//背景图像
	const char* rootPathOut = "D:\\Databases\\000JHZ\\mergeTest\\merged0906\\";//输出贴图
	//const char* rootPathOut0 = "D:\\Databases\\000JHZ\\mergeTest\\back0901\\";//重新保存的背景图像，为去掉异常不能打开的图片
	const char* foreImagePath = "D:\\Databases\\000JHZ\\mergeTest\\foreimages\\";//前景图像
	const char* foreMarkPath = "D:\\Databases\\000JHZ\\mergeTest\\foremarks\\";//前景标记图像

	if (_access(rootPathIn,0)==-1)
	{
		cout << rootPathIn << "不存在" << endl;
		return;
	}
	if (_access(rootPathOut, 0)==-1)
	{
		cout << rootPathOut << "不存在" << endl;
		return;
	}
	//if (_access(rootPathOut0, 0) == -1)
	//{
	//	cout << rootPathOut0 << "不存在" << endl;
	//	return;
	//}
	if (_access(foreImagePath, 0) == -1)
	{
		cout << foreImagePath << "不存在" << endl;
		return;
	}
	if (_access(foreMarkPath, 0) == -1)
	{
		cout << foreMarkPath << "不存在" << endl;
		return;
	}

	//保存所有前景图像的文件名
	string pStrForeFileName[250];
	string pStrMarkFileName[250];
	
	string strForeFilePath = string(foreImagePath) + "hou_fore*";
	_finddata_t foreFileDir;
	long lfForeFileDir;

	//统计前景图像数
	int nTotalForeImageNum = 0;
	if ((lfForeFileDir = _findfirst(strForeFilePath.c_str(), &foreFileDir)) == -1)
	{
		cout << foreImagePath << "中没有找到文件" << endl;
		return;
	}
	else
	{
		cout << "前景图像共：" ;
		do
		{
			pStrForeFileName[nTotalForeImageNum] = string(foreFileDir.name);
			nTotalForeImageNum++;
		} while (_findnext(lfForeFileDir, &foreFileDir) == 0);
		cout << nTotalForeImageNum<< "张" << endl;
	}
	_findclose(lfForeFileDir);

	//统计标记图像数
	string strMarkFilePath = string(foreMarkPath) + "hou_mark*";
	_finddata_t markFileDir;
	long lfMarkFileDir;
	int nTotalMarkImageNum = 0;
	if ((lfMarkFileDir = _findfirst(strMarkFilePath.c_str(), &markFileDir)) == -1)
	{
		cout << foreMarkPath << "中没有找到文件" << endl;
		return;
	}
	else
	{
		cout << "标记图像共：";
		do
		{
			pStrMarkFileName[nTotalMarkImageNum] = string(markFileDir.name);
			nTotalMarkImageNum++;
		} while (_findnext(lfMarkFileDir, &markFileDir) == 0);
		cout << nTotalMarkImageNum << "张" << endl;
	}
	_findclose(lfMarkFileDir);

	if (nTotalMarkImageNum != nTotalForeImageNum)
	{
		cout << "前景图像与标记图像不一致！" << endl;
		return;
	}


	//读取背景图像
	string strBackFilePath = string(rootPathIn) + "*.jpg";
	_finddata_t backFileDir;
	long lfBackFileDir;
	//统计背景图像数
	int nTotalBackImageNum = 0;
	if ((lfBackFileDir = _findfirst(strBackFilePath.c_str(), &backFileDir)) == -1)
	{
		cout << rootPathIn << "中没有找到文件" << endl;
		return;
	}
	else
	{
		cout << "背景图像共：";
		do
		{
			nTotalBackImageNum++;
		} while (_findnext(lfBackFileDir, &backFileDir) == 0);
		cout << nTotalBackImageNum << "张" << endl;
	}
	_findclose(lfBackFileDir);

	//-----------------------------------------------------------------------------------------------
	//开始图像融合
	srand((unsigned)time(NULL));

	//生成高斯核
	int kernal_size = 3;
	Mat kernal(kernal_size, kernal_size, CV_32FC1, 1);
	kernal = Merge.gaussian_kernal(kernal_size, 11.0);

	//记录读取错误的图像名称
	FILE *fp = fopen("D:\\Databases\\000JHZ\\mergeTest\\errorlog.txt", "w");
	//for (int i = 0; i < 9; i++)
	//{
	//	fprintf(fp, "%s\t读取错误\n", pStrForeFileName[i].c_str());
	//}
	//fclose(fp);

	/*//--------------------------------------------------------------------------------------------------------->>
	//-----------方式一：一张背景分别贴多张前景输出多张-------------------------------------------------------->>
	//--------------------------------------------------------------------------------------------------------->>
	//重新读前景图像
	for (int nIndex = 0; nIndex < nTotalForeImageNum; nIndex++)
	{
		//每个前景图像与背景图像遍历融合
		//当前前景图像完整路径
		string strForeFilePathTemp = string(foreImagePath) + pStrForeFileName[nIndex];
		m_MatForeImageOri = imread(strForeFilePathTemp, 1);//三通道
		if (!m_MatForeImageOri.data)
		{
			cout << strForeFilePathTemp << "不存在" << endl;
			continue;
		}

		nForeBaseWidth = m_MatForeImageOri.cols;
		nForeBaseHeigth = m_MatForeImageOri.rows;

		string strMarkFilePathTemp = string(foreMarkPath) + pStrMarkFileName[nIndex];
		m_MatMarkImageOri = imread(strMarkFilePathTemp, 0);//单通道
		if (!m_MatMarkImageOri.data)
		{
			cout << strMarkFilePathTemp << "不存在" << endl;
			continue;
		}
		if (m_MatMarkImageOri.cols != nForeBaseWidth || m_MatMarkImageOri.rows != nForeBaseHeigth)
		{
			cout << "当前前景图像与标记图像尺寸不一致！" << endl;
			continue;
		}
		char* charTemp = const_cast<char*>(pStrForeFileName[nIndex].c_str());
		char *currForeFileName = remove_suffix(charTemp);

		//操作背景图像
		string strFilePath = string(rootPathIn) + "*.jpg";

		_finddata_t fileDir;
		long lfDir;

		if ((lfDir = _findfirst(strFilePath.c_str(), &fileDir)) == -1)
		{
			cout << rootPathIn << "中没有找到文件" << endl;
			break;
		}
		else
		{
			int nCount = 1;
			do
			{
				cout << "背景图像：" << fileDir.name << "\t" << \
					nCount << "/" << nTotalBackImageNum << "\t" << \
					nIndex + 1 << "/" << nTotalForeImageNum << endl;
				//先缩放-后旋转
				//随机缩放[5,10]/10，旋转[0,359]
				m_nZoomSlider = (rand() % (10 - 5 + 1)) + 5;
				m_nRotateSlider = (rand() % (359 - 0 + 1)) + 0;

				on_trackbarZoom();
				on_trackbarRotate();
				//完全路径
				string strFileIn = string(rootPathIn) + string(fileDir.name);

				m_MatBackImage = imread(strFileIn,1);//三通道BGR
				if (!m_MatBackImage.data)
				{
					if (nIndex == 0)
					{
						fprintf(fp, "%s\t读取错误\n", strFileIn.c_str());
					}
					cout << strFileIn << "读取错误！" << endl;
					continue;
				}
				//调整大小
				Mat tempMat;
				resize(m_MatBackImage, tempMat, Size(nCommonWidth, nCommonHeight));

				//读取当前背景文件名
				char *currFileName = remove_suffix((char *)fileDir.name);

				//随机产生融合位置（中心）
				for (int nDir = 0; nDir < 1; nDir++)
			    {
					nPosY = (rand() % (tempMat.cols * 7 / 8 - tempMat.cols / 8 + 1)) + tempMat.cols / 8;
					nPosX = (rand() % (tempMat.rows * 7 / 8 - tempMat.rows / 8 + 1)) + tempMat.rows / 8;

					m_MatMergeImage = Merge.MergeMat(tempMat, m_MatForeImage, m_MatMarkImage, nPosX, nPosY);
					//m_MatMergeImage = Merge.MergeMat(m_MatMergeImage, m_MatMarkImage, 1, nPosX, nPosY);
					m_MatMergeImage = Merge.MergeMat(m_MatMergeImage, m_MatMarkImage, kernal, 1, nPosX, nPosY);

					//imshow("Merge", m_MatMergeImage);

					//waitKey(0);

					//保存
					char s[12];
					itoa(nDir, s, 10);
					string strFileOut = string(rootPathOut) + string(currFileName) + "_" + string(currForeFileName) + "_" + string(s) + ".jpg";
					if (!imwrite(strFileOut, m_MatMergeImage))
					{
						cout << strFileOut << "保存错误！" << endl;
						continue;
					}
			    }
				nCount++;
			} while (_findnext(lfDir, &fileDir) == 0);
		}
		_findclose(lfDir);
	}
	//---------------------------------------------------------------------------------------------------------<<*/

	//--------------------------------------------------------------------------------------------------------->>
	//-----------方式二：一张背景只与一张前景， 输出一张------------------------------------------------------->>
	//--------------------------------------------------------------------------------------------------------->>
    //当前前景图像完整路径
    //读取所有前景图像
    cv::Mat foreImageMats[250];
	cv::Mat markImageMats[250];

	for (int nIndex = 0; nIndex < nTotalForeImageNum; nIndex++)
	{
		string strForeFilePathTemp = string(foreImagePath) + pStrForeFileName[nIndex];
		foreImageMats[nIndex] = imread(strForeFilePathTemp, 1);//三通道
		if (!foreImageMats[nIndex].data)
		{
			cout << strForeFilePathTemp << "不存在" << endl;
			return;
		}

		nForeBaseWidth = foreImageMats[nIndex].cols;
		nForeBaseHeigth = foreImageMats[nIndex].rows;

		string strMarkFilePathTemp = string(foreMarkPath) + pStrMarkFileName[nIndex];
		markImageMats[nIndex] = imread(strMarkFilePathTemp, 0);//单通道
		if (!markImageMats[nIndex].data)
		{
			cout << strMarkFilePathTemp << "不存在" << endl;
			return;
		}
		if (markImageMats[nIndex].cols != nForeBaseWidth || markImageMats[nIndex].rows != nForeBaseHeigth)
		{
			cout << "当前前景图像与标记图像尺寸不一致！" << endl;
			return;
		}
	}

	//操作背景图像
	string strFilePath = string(rootPathIn) + "*.jpg";

	_finddata_t fileDir;
	long lfDir;

	if ((lfDir = _findfirst(strFilePath.c_str(), &fileDir)) == -1)
	{
		cout << rootPathIn << "中没有找到文件" << endl;
		return;
	}
	else
	{
		int nCount = 1;
		do
		{
			int nIndex = (rand() % (nTotalForeImageNum - 1 - 0 + 1)) + 0;

			char* charTemp = const_cast<char*>(pStrForeFileName[nIndex].c_str());
			char *currForeFileName = remove_suffix(charTemp);

			m_MatForeImageOri = foreImageMats[nIndex];
			m_MatMarkImageOri = markImageMats[nIndex];

			cout << "背景图像：" << fileDir.name << "\t" << \
				nCount << "/" << nTotalBackImageNum << "\t" << \
				"前景图像序号："<< nIndex << endl;
			//先缩放-后旋转
			//随机缩放[8,15]/10，旋转[0,359]
			m_nZoomSlider = (rand() % (15 - 8 + 1)) + 8;
			m_nRotateSlider = (rand() % (359 - 0 + 1)) + 0;

			on_trackbarZoom();
			on_trackbarRotate();
			//完全路径
			string strFileIn = string(rootPathIn) + string(fileDir.name);

			m_MatBackImage = imread(strFileIn, 1);//三通道BGR
			if (!m_MatBackImage.data)
			{
				if (nIndex == 0)
				{
					fprintf(fp, "%s\t读取错误\n", strFileIn.c_str());
				}
				cout << strFileIn << "读取错误！" << endl;
				continue;
			}
			////如果读取成功则重新保存背景图像
			//string strBackFileOut = string(rootPathOut0) + string(fileDir.name);
			//if (!imwrite(strBackFileOut, m_MatBackImage))
			//{
			//	cout << strBackFileOut << "保存错误！" << endl;
			//	continue;
			//}
			//调整大小
			Mat tempMat;
			resize(m_MatBackImage, tempMat, Size(nCommonWidth, nCommonHeight));

			//读取当前背景文件名
			char *currFileName = remove_suffix((char *)fileDir.name);

			//随机产生融合位置（中心）
			for (int nDir = 0; nDir < 1; nDir++)
			{
				nPosY = (rand() % (tempMat.cols * 7 / 8 - tempMat.cols / 8 + 1)) + tempMat.cols / 8;
				nPosX = (rand() % (tempMat.rows * 7 / 8 - tempMat.rows / 8 + 1)) + tempMat.rows / 8;

				m_MatMergeImage = Merge.MergeMat(tempMat, m_MatForeImage, m_MatMarkImage, nPosX, nPosY);
				//m_MatMergeImage = Merge.MergeMat(m_MatMergeImage, m_MatMarkImage, 1, nPosX, nPosY);
				m_MatMergeImage = Merge.MergeMat(m_MatMergeImage, m_MatMarkImage, kernal, 1, nPosX, nPosY);

				//imshow("Merge", m_MatMergeImage);

				//waitKey(0);

				//保存
				char s[12];
				itoa(nDir, s, 10);
				string strFileOut = string(rootPathOut) + string(currFileName) + "_" + string(currForeFileName) + "_" + string(s) + ".jpg";
				if (!imwrite(strFileOut, m_MatMergeImage))
				{
					cout << strFileOut << "保存错误！" << endl;
					continue;
				}
				cout << strFileOut << endl;
			}
			nCount++;
		} while (_findnext(lfDir, &fileDir) == 0);
	}
	_findclose(lfDir);

	fclose(fp);
	//waitKey(0);
}