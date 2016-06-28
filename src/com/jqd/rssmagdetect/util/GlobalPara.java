package com.jqd.rssmagdetect.util;


/**
 * @author jiangqideng@163.com
 * @date 2016-6-28 ����7:21:29
 * @description ���������ı�������ʵ��������ûʲô���ûʲô�ã��Ժ������ʵ�ʿ��Ƶ�����᷽���
 */
public class GlobalPara {
	public long timeOfStartScan=0;
	public long timeSinceStart=0;
	public int position_index=1;
	
	private volatile static GlobalPara globalPara = null;
	public static GlobalPara getInstance() {
		if (globalPara == null) {
			synchronized (GlobalPara.class) {
				if (globalPara == null) {
					globalPara = new GlobalPara();
				}
			}
		}
		return globalPara;
	}
}
