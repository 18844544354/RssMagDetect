package com.jqd.rssmagdetect.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import com.jqd.rssmagdetect.file.FileManager;
import com.jqd.rssmagdetect.ui.MainActivity;
import com.jqd.rssmagdetect.ui.WiFiListAdapter;
import com.jqd.rssmagdetect.util.GlobalPara;

/**
 * @author jiangqideng@163.com
 * @date 2016-6-28 ����7:17:43
 * @description ����wifi��ѭ��ɨ��ͼ�¼wifiģ���ȡ������
 */
public class WiFiDataManager {
	private WifiManager wifiManager;
	public List<ScanResult> scanResults = null;
	private volatile static WiFiDataManager wiFiDataManager = null;
	public MainActivity activity;

	public ArrayList<HashMap<Integer, Integer>> dataRssi = new ArrayList<HashMap<Integer, Integer>>(); // ÿ�д���һ��Wifi�ȵ㣬��Ӧһ��map��map�ĵ�һ��ֵ�����ݵ�index���ڶ���ֵ��rssi
	public HashMap<String, Integer> dataBssid = new HashMap<String, Integer>();
	public ArrayList<String> dataWifiNames = new ArrayList<String>();
	public int dataCount = 0;

	public static WiFiDataManager getInstance() {
		if (wiFiDataManager == null) {
			synchronized (WiFiDataManager.class) {
				if (wiFiDataManager == null) {
					wiFiDataManager = new WiFiDataManager();
				}
			}
		}
		return wiFiDataManager;
	}

	public void init(MainActivity activity) {
		this.activity = activity;
		if (wifiManager == null) {
			wifiManager = (WifiManager) activity
					.getSystemService(Context.WIFI_SERVICE);
		}
		if (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
			Toast.makeText(activity, "���ڿ���wifi�����Ժ�...", Toast.LENGTH_SHORT)
					.show();
			if (wifiManager == null) {
				wifiManager = (WifiManager) activity
						.getSystemService(Context.WIFI_SERVICE);
			}
			if (!wifiManager.isWifiEnabled()) {
				wifiManager.setWifiEnabled(true);
			}
		}
	}

	public void startCollecting(MainActivity activity) {
		wifiManager.startScan();
		GlobalPara.getInstance().timeOfStartScan = GlobalPara.getInstance().timeSinceStart;
		activity.registerReceiver(cycleWifiReceiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	private final BroadcastReceiver cycleWifiReceiver = new BroadcastReceiver() {
		@SuppressLint("UseSparseArrays")
		@Override
		public void onReceive(Context context, Intent intent) {
			scanResults = wifiManager.getScanResults();
			if (scanResults != null) {
				WiFiListAdapter adapter = new WiFiListAdapter(activity,
						scanResults);
				activity.listView.setAdapter(adapter);
			}
			// �����ȵ��б�ֻ��������˳�򲻱䣬ͬʱ��RSSI��¼����
			for (int i = 0; i < scanResults.size(); i++) {
				if (!dataBssid.containsKey(scanResults.get(i).BSSID)) { // ����һ��wifi�ȵ�
					dataBssid.put(scanResults.get(i).BSSID, dataBssid.size());
					dataWifiNames.add(scanResults.get(i).SSID);
					HashMap<Integer, Integer> tmp = new HashMap<Integer, Integer>();
					tmp.put(dataCount, scanResults.get(i).level);
					dataRssi.add(tmp);
				} else { // wifi�ȵ��Ѵ���
					dataRssi.get(dataBssid.get(scanResults.get(i).BSSID)).put(
							dataCount, scanResults.get(i).level);
				}
			}
			dataCount++;

			while (GlobalPara.getInstance().timeSinceStart
					- GlobalPara.getInstance().timeOfStartScan < 50) {
				// �ȴ���������������ʱ�䣬��1 * 10ms, �������ֻ�wifiɨ��һ�δ�Լ��һ����
			}
			GlobalPara.getInstance().timeOfStartScan = GlobalPara.getInstance().timeSinceStart;

			// �յ���ʼ��һ��ɨ�裬����һ��ʱ�䣬ÿ���������
			wifiManager.startScan();
			activity.toggleButton.setText("�ر�RSS���ݲɼ�" + "("
					+ String.valueOf(dataCount) + ")");
		}
	};

	public void endCollecting(MainActivity activity) {
		activity.unregisterReceiver(cycleWifiReceiver); // ȡ������
		SensorsDataManager.getInstance().updateSensorsData(); // ���ִ�������wifi���ݵĸ���ͬ��
		// Ȼ��洢���ݵ��ļ�
		new FileManager().saveData();

	}

}
