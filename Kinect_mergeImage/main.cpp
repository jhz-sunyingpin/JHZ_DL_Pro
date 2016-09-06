/*****************************************************************************
*  @file     main.cpp                                                        *
*  @brief    ǰ��ͼ���뱳��ͼ���ںϣ�ǰ����Ӧ�ı��ͼ����                  *
*            �������ǰ��ͼ���λ�á���С���Ƕ�                              *
*            ��ͬ��С�ͽǶȵ�ǰ��ͼ���뱳��ͼ���ڶ��λ���ں�                *
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
//! ���Ŵ���*10
int m_nZoomRatioMax = 20;
//! �����ת�Ƕ�
int m_nThetaMax = 359;
//! ��ʼ����ϵ��
int m_nZoomSlider = 10;
//! ��ʼ��תϵ��
int m_nRotateSlider = 0;
//! ����ϵ��
double m_dZoomRatio = 1.0;
//! ��ת�Ƕ�
double m_dTheta = 0.0;
//! ǰ��ͼ���ڱ���ͼ���е�λ��
int nPosX, nPosY;
//! ԭʼ����ͼ��
Mat m_MatBackImageOri;
//! ԭʼǰ��ͼ��
Mat m_MatForeImageOri;
//! ԭʼǰ�����ͼ��
Mat m_MatMarkImageOri;
//! �����ı���ͼ��
Mat m_MatBackImage;
//! ������ǰ��ͼ��
Mat m_MatForeImage;
//! �����ı��ͼ��
Mat m_MatMarkImage;
//! �ں�ͼ��
Mat m_MatMergeImage;
//! ͼ���ʼ���
int nForeBaseWidth = 640;
//! ͼ���ʼ�߶�
int nForeBaseHeigth = 480;
//! �ں���ؽӿ���
sunMerge Merge;
//! ��ǰλ���Ƿ�ı�
//! ִ�����˫��������λ�øı䣬����ֵΪtrue������Ϊfalse
bool bChange = false;

/**
* @brief ͼ������
*        ����ǰ��ͼ��ͱ��ͼ�������ת�����Ų���
*/
void on_trackbarZoom()
{
	m_dZoomRatio = (double)m_nZoomSlider / 10;
	//����ɵ�ǰ��������ת����ִ������
	Mat matTemp1 = m_MatForeImageOri.clone();
	Mat matTemp2 = m_MatMarkImageOri.clone();
	Merge.rotateMat(matTemp1, matTemp2, m_MatForeImage, m_MatMarkImage, m_dTheta);
	//����
	m_MatForeImage = Merge.zoomMat(m_MatForeImage, nForeBaseWidth, nForeBaseHeigth, m_dZoomRatio);
	m_MatMarkImage = Merge.zoomMat(m_MatMarkImage, nForeBaseWidth, nForeBaseHeigth, m_dZoomRatio);
}

/**
* @brief ͼ����ת
*        ����ǰ��ͼ��ͱ��ͼ��������ź���ת����
*/
void on_trackbarRotate()
{
	m_dTheta = (double)(m_nRotateSlider)* 3.1415926 / 180;
	//����ɵ�ǰ���������ţ���ִ����ת
	m_MatForeImage = Merge.zoomMat(m_MatForeImageOri, nForeBaseWidth, nForeBaseHeigth, m_dZoomRatio);
	m_MatMarkImage = Merge.zoomMat(m_MatMarkImageOri, nForeBaseWidth, nForeBaseHeigth, m_dZoomRatio);
	//��ת
	Mat matTemp1 = m_MatForeImage.clone();
	Mat matTemp2 = m_MatMarkImage.clone();
	Merge.rotateMat(matTemp1, matTemp2, m_MatForeImage, m_MatMarkImage, m_dTheta);

}

/**
* @brief ����¼� - δʹ��
*        ˫��ȷ��ͼƬ�ں�λ��
* @param event       ����¼�
* @param x           ���غ�����
* @param y           ����������
* @note ����ǵ�ǰλ���Ƿ�ı䣻
*       ��ǰ��Ӧ�����ص����꽫��ǰ��ͼ�����������һ��
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
* @brief ȥ�ļ�����׺
* @param file       ����׺�ļ���
* @return ������׺���ļ���
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
* @brief ������
* ��ȡ����ͼ��ǰ��ͼ��ǰ�����ͼ��
* �������ǰ��ͼ���С���Ƕ�
* ͬһ������ͬһǰ�����ĸ�λ���ںϷֱ����
* ��ǰͼƬ����ȷ�Ͻ������Զ�������һ��ͼƬ��ֱ������ͼƬ���������
*
*/
void main()
{
	const char* rootPathIn = "D:\\Databases\\000JHZ\\office\\0906\\";//����ͼ��
	const char* rootPathOut = "D:\\Databases\\000JHZ\\mergeTest\\merged0906\\";//�����ͼ
	//const char* rootPathOut0 = "D:\\Databases\\000JHZ\\mergeTest\\back0901\\";//���±���ı���ͼ��Ϊȥ���쳣���ܴ򿪵�ͼƬ
	const char* foreImagePath = "D:\\Databases\\000JHZ\\mergeTest\\foreimages\\";//ǰ��ͼ��
	const char* foreMarkPath = "D:\\Databases\\000JHZ\\mergeTest\\foremarks\\";//ǰ�����ͼ��

	if (_access(rootPathIn,0)==-1)
	{
		cout << rootPathIn << "������" << endl;
		return;
	}
	if (_access(rootPathOut, 0)==-1)
	{
		cout << rootPathOut << "������" << endl;
		return;
	}
	//if (_access(rootPathOut0, 0) == -1)
	//{
	//	cout << rootPathOut0 << "������" << endl;
	//	return;
	//}
	if (_access(foreImagePath, 0) == -1)
	{
		cout << foreImagePath << "������" << endl;
		return;
	}
	if (_access(foreMarkPath, 0) == -1)
	{
		cout << foreMarkPath << "������" << endl;
		return;
	}

	//��������ǰ��ͼ����ļ���
	string pStrForeFileName[250];
	string pStrMarkFileName[250];
	
	string strForeFilePath = string(foreImagePath) + "hou_fore*";
	_finddata_t foreFileDir;
	long lfForeFileDir;

	//ͳ��ǰ��ͼ����
	int nTotalForeImageNum = 0;
	if ((lfForeFileDir = _findfirst(strForeFilePath.c_str(), &foreFileDir)) == -1)
	{
		cout << foreImagePath << "��û���ҵ��ļ�" << endl;
		return;
	}
	else
	{
		cout << "ǰ��ͼ�񹲣�" ;
		do
		{
			pStrForeFileName[nTotalForeImageNum] = string(foreFileDir.name);
			nTotalForeImageNum++;
		} while (_findnext(lfForeFileDir, &foreFileDir) == 0);
		cout << nTotalForeImageNum<< "��" << endl;
	}
	_findclose(lfForeFileDir);

	//ͳ�Ʊ��ͼ����
	string strMarkFilePath = string(foreMarkPath) + "hou_mark*";
	_finddata_t markFileDir;
	long lfMarkFileDir;
	int nTotalMarkImageNum = 0;
	if ((lfMarkFileDir = _findfirst(strMarkFilePath.c_str(), &markFileDir)) == -1)
	{
		cout << foreMarkPath << "��û���ҵ��ļ�" << endl;
		return;
	}
	else
	{
		cout << "���ͼ�񹲣�";
		do
		{
			pStrMarkFileName[nTotalMarkImageNum] = string(markFileDir.name);
			nTotalMarkImageNum++;
		} while (_findnext(lfMarkFileDir, &markFileDir) == 0);
		cout << nTotalMarkImageNum << "��" << endl;
	}
	_findclose(lfMarkFileDir);

	if (nTotalMarkImageNum != nTotalForeImageNum)
	{
		cout << "ǰ��ͼ������ͼ��һ�£�" << endl;
		return;
	}


	//��ȡ����ͼ��
	string strBackFilePath = string(rootPathIn) + "*.jpg";
	_finddata_t backFileDir;
	long lfBackFileDir;
	//ͳ�Ʊ���ͼ����
	int nTotalBackImageNum = 0;
	if ((lfBackFileDir = _findfirst(strBackFilePath.c_str(), &backFileDir)) == -1)
	{
		cout << rootPathIn << "��û���ҵ��ļ�" << endl;
		return;
	}
	else
	{
		cout << "����ͼ�񹲣�";
		do
		{
			nTotalBackImageNum++;
		} while (_findnext(lfBackFileDir, &backFileDir) == 0);
		cout << nTotalBackImageNum << "��" << endl;
	}
	_findclose(lfBackFileDir);

	//-----------------------------------------------------------------------------------------------
	//��ʼͼ���ں�
	srand((unsigned)time(NULL));

	//���ɸ�˹��
	int kernal_size = 3;
	Mat kernal(kernal_size, kernal_size, CV_32FC1, 1);
	kernal = Merge.gaussian_kernal(kernal_size, 11.0);

	//��¼��ȡ�����ͼ������
	FILE *fp = fopen("D:\\Databases\\000JHZ\\mergeTest\\errorlog.txt", "w");
	//for (int i = 0; i < 9; i++)
	//{
	//	fprintf(fp, "%s\t��ȡ����\n", pStrForeFileName[i].c_str());
	//}
	//fclose(fp);

	/*//--------------------------------------------------------------------------------------------------------->>
	//-----------��ʽһ��һ�ű����ֱ�������ǰ���������-------------------------------------------------------->>
	//--------------------------------------------------------------------------------------------------------->>
	//���¶�ǰ��ͼ��
	for (int nIndex = 0; nIndex < nTotalForeImageNum; nIndex++)
	{
		//ÿ��ǰ��ͼ���뱳��ͼ������ں�
		//��ǰǰ��ͼ������·��
		string strForeFilePathTemp = string(foreImagePath) + pStrForeFileName[nIndex];
		m_MatForeImageOri = imread(strForeFilePathTemp, 1);//��ͨ��
		if (!m_MatForeImageOri.data)
		{
			cout << strForeFilePathTemp << "������" << endl;
			continue;
		}

		nForeBaseWidth = m_MatForeImageOri.cols;
		nForeBaseHeigth = m_MatForeImageOri.rows;

		string strMarkFilePathTemp = string(foreMarkPath) + pStrMarkFileName[nIndex];
		m_MatMarkImageOri = imread(strMarkFilePathTemp, 0);//��ͨ��
		if (!m_MatMarkImageOri.data)
		{
			cout << strMarkFilePathTemp << "������" << endl;
			continue;
		}
		if (m_MatMarkImageOri.cols != nForeBaseWidth || m_MatMarkImageOri.rows != nForeBaseHeigth)
		{
			cout << "��ǰǰ��ͼ������ͼ��ߴ粻һ�£�" << endl;
			continue;
		}
		char* charTemp = const_cast<char*>(pStrForeFileName[nIndex].c_str());
		char *currForeFileName = remove_suffix(charTemp);

		//��������ͼ��
		string strFilePath = string(rootPathIn) + "*.jpg";

		_finddata_t fileDir;
		long lfDir;

		if ((lfDir = _findfirst(strFilePath.c_str(), &fileDir)) == -1)
		{
			cout << rootPathIn << "��û���ҵ��ļ�" << endl;
			break;
		}
		else
		{
			int nCount = 1;
			do
			{
				cout << "����ͼ��" << fileDir.name << "\t" << \
					nCount << "/" << nTotalBackImageNum << "\t" << \
					nIndex + 1 << "/" << nTotalForeImageNum << endl;
				//������-����ת
				//�������[5,10]/10����ת[0,359]
				m_nZoomSlider = (rand() % (10 - 5 + 1)) + 5;
				m_nRotateSlider = (rand() % (359 - 0 + 1)) + 0;

				on_trackbarZoom();
				on_trackbarRotate();
				//��ȫ·��
				string strFileIn = string(rootPathIn) + string(fileDir.name);

				m_MatBackImage = imread(strFileIn,1);//��ͨ��BGR
				if (!m_MatBackImage.data)
				{
					if (nIndex == 0)
					{
						fprintf(fp, "%s\t��ȡ����\n", strFileIn.c_str());
					}
					cout << strFileIn << "��ȡ����" << endl;
					continue;
				}
				//������С
				Mat tempMat;
				resize(m_MatBackImage, tempMat, Size(nCommonWidth, nCommonHeight));

				//��ȡ��ǰ�����ļ���
				char *currFileName = remove_suffix((char *)fileDir.name);

				//��������ں�λ�ã����ģ�
				for (int nDir = 0; nDir < 1; nDir++)
			    {
					nPosY = (rand() % (tempMat.cols * 7 / 8 - tempMat.cols / 8 + 1)) + tempMat.cols / 8;
					nPosX = (rand() % (tempMat.rows * 7 / 8 - tempMat.rows / 8 + 1)) + tempMat.rows / 8;

					m_MatMergeImage = Merge.MergeMat(tempMat, m_MatForeImage, m_MatMarkImage, nPosX, nPosY);
					//m_MatMergeImage = Merge.MergeMat(m_MatMergeImage, m_MatMarkImage, 1, nPosX, nPosY);
					m_MatMergeImage = Merge.MergeMat(m_MatMergeImage, m_MatMarkImage, kernal, 1, nPosX, nPosY);

					//imshow("Merge", m_MatMergeImage);

					//waitKey(0);

					//����
					char s[12];
					itoa(nDir, s, 10);
					string strFileOut = string(rootPathOut) + string(currFileName) + "_" + string(currForeFileName) + "_" + string(s) + ".jpg";
					if (!imwrite(strFileOut, m_MatMergeImage))
					{
						cout << strFileOut << "�������" << endl;
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
	//-----------��ʽ����һ�ű���ֻ��һ��ǰ���� ���һ��------------------------------------------------------->>
	//--------------------------------------------------------------------------------------------------------->>
    //��ǰǰ��ͼ������·��
    //��ȡ����ǰ��ͼ��
    cv::Mat foreImageMats[250];
	cv::Mat markImageMats[250];

	for (int nIndex = 0; nIndex < nTotalForeImageNum; nIndex++)
	{
		string strForeFilePathTemp = string(foreImagePath) + pStrForeFileName[nIndex];
		foreImageMats[nIndex] = imread(strForeFilePathTemp, 1);//��ͨ��
		if (!foreImageMats[nIndex].data)
		{
			cout << strForeFilePathTemp << "������" << endl;
			return;
		}

		nForeBaseWidth = foreImageMats[nIndex].cols;
		nForeBaseHeigth = foreImageMats[nIndex].rows;

		string strMarkFilePathTemp = string(foreMarkPath) + pStrMarkFileName[nIndex];
		markImageMats[nIndex] = imread(strMarkFilePathTemp, 0);//��ͨ��
		if (!markImageMats[nIndex].data)
		{
			cout << strMarkFilePathTemp << "������" << endl;
			return;
		}
		if (markImageMats[nIndex].cols != nForeBaseWidth || markImageMats[nIndex].rows != nForeBaseHeigth)
		{
			cout << "��ǰǰ��ͼ������ͼ��ߴ粻һ�£�" << endl;
			return;
		}
	}

	//��������ͼ��
	string strFilePath = string(rootPathIn) + "*.jpg";

	_finddata_t fileDir;
	long lfDir;

	if ((lfDir = _findfirst(strFilePath.c_str(), &fileDir)) == -1)
	{
		cout << rootPathIn << "��û���ҵ��ļ�" << endl;
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

			cout << "����ͼ��" << fileDir.name << "\t" << \
				nCount << "/" << nTotalBackImageNum << "\t" << \
				"ǰ��ͼ����ţ�"<< nIndex << endl;
			//������-����ת
			//�������[8,15]/10����ת[0,359]
			m_nZoomSlider = (rand() % (15 - 8 + 1)) + 8;
			m_nRotateSlider = (rand() % (359 - 0 + 1)) + 0;

			on_trackbarZoom();
			on_trackbarRotate();
			//��ȫ·��
			string strFileIn = string(rootPathIn) + string(fileDir.name);

			m_MatBackImage = imread(strFileIn, 1);//��ͨ��BGR
			if (!m_MatBackImage.data)
			{
				if (nIndex == 0)
				{
					fprintf(fp, "%s\t��ȡ����\n", strFileIn.c_str());
				}
				cout << strFileIn << "��ȡ����" << endl;
				continue;
			}
			////�����ȡ�ɹ������±��汳��ͼ��
			//string strBackFileOut = string(rootPathOut0) + string(fileDir.name);
			//if (!imwrite(strBackFileOut, m_MatBackImage))
			//{
			//	cout << strBackFileOut << "�������" << endl;
			//	continue;
			//}
			//������С
			Mat tempMat;
			resize(m_MatBackImage, tempMat, Size(nCommonWidth, nCommonHeight));

			//��ȡ��ǰ�����ļ���
			char *currFileName = remove_suffix((char *)fileDir.name);

			//��������ں�λ�ã����ģ�
			for (int nDir = 0; nDir < 1; nDir++)
			{
				nPosY = (rand() % (tempMat.cols * 7 / 8 - tempMat.cols / 8 + 1)) + tempMat.cols / 8;
				nPosX = (rand() % (tempMat.rows * 7 / 8 - tempMat.rows / 8 + 1)) + tempMat.rows / 8;

				m_MatMergeImage = Merge.MergeMat(tempMat, m_MatForeImage, m_MatMarkImage, nPosX, nPosY);
				//m_MatMergeImage = Merge.MergeMat(m_MatMergeImage, m_MatMarkImage, 1, nPosX, nPosY);
				m_MatMergeImage = Merge.MergeMat(m_MatMergeImage, m_MatMarkImage, kernal, 1, nPosX, nPosY);

				//imshow("Merge", m_MatMergeImage);

				//waitKey(0);

				//����
				char s[12];
				itoa(nDir, s, 10);
				string strFileOut = string(rootPathOut) + string(currFileName) + "_" + string(currForeFileName) + "_" + string(s) + ".jpg";
				if (!imwrite(strFileOut, m_MatMergeImage))
				{
					cout << strFileOut << "�������" << endl;
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