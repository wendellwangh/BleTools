package com.ccl.bletools.bt;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

public abstract class BTScanner {
	/*
	 * Interfaces
	 */
	public interface BTScanListener{
		public void onRecvScanResult(BTDevice aDevice);
	}
	
	/*
	 * Static functions
	 */
	public static BTScanner CreateScanner(Context aContext){
		if (android.os.Build.VERSION.SDK_INT >= 21){
			return new BLEScannerV3();
		}else if (android.os.Build.VERSION.SDK_INT >= 18){
			return new BLEScannerV2();
		}else{
			return new BLEScannerV1(aContext);
		}
	}
	
	/*
	 * Non-static functions
	 */
	private BluetoothAdapter iAdapter = null;
	private BTScanListener iListener = null;
	
	private boolean iPowerSave = true;
	private int iUpdateInterval = 500;
	
	private HashMap<String, BTDevice> iDevices = new HashMap<String, BTDevice>(32);
	
	private BTScanner(){
		iAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	public BluetoothAdapter getAdapter(){
		return BluetoothAdapter.getDefaultAdapter();
	}
	
	public boolean isScanning(){
		return iListener != null;
	}
	
	public boolean startScan(BTScanListener aListener){
		if (iListener != null){
			stopScan(iListener);
		}
		
		if (iAdapter != null && iAdapter.isEnabled()){
			iListener = aListener;
			
			if (iListener != null){				
				return true;
			}
		}
		
		return false;
	}
	
	public boolean stopScan(BTScanListener aListener){
		if (iListener != null){
			iListener = null;
			
			return true;
		}
		
		return false;
	}
	
	public boolean restartScan(){
		final BTScanListener tListener = iListener;
		
		if (tListener != null){
			stopScan(tListener);
			
			return startScan(tListener);
		}
		
		return false;
	}
	
	public boolean isSupportPowerSave(){
		return true;
	}

	public boolean isPowerSave(){
		if (isSupportPowerSave()){
			return iPowerSave;
		}else{
			return false;
		}
	}

	public boolean setPowerSave(boolean aPowerSave){
		if (isSupportPowerSave()){
			if (aPowerSave != iPowerSave){
				iPowerSave = aPowerSave;
				
				restartScan();
				
				return true;
			}
		}
		
		return false;
	}
	
	public void setUpdateInterval(int aInterval){
		iUpdateInterval = aInterval;
	}
	
	protected void notifyScanResult(BTDevice aDevice){
		final BTScanListener tListener = iListener;
		
		if (tListener != null){
			tListener.onRecvScanResult(aDevice);
		}
	}
	
	protected BTDevice getDevice(BluetoothDevice aDevice){
		if (aDevice != null){
			String tAddress = aDevice.getAddress();
			BTDevice tDevice = iDevices.get(tAddress);
			
			if (tDevice == null){
				tDevice = new BTDevice(tAddress);
				
				iDevices.put(tAddress, tDevice);
			}
			
			return tDevice;
		}
		
		return null;
	}
	
	protected void updateDeviceStatus(BluetoothDevice device, int rssi, byte[] scanRecord){
			BTDevice tDevice = getDevice(device);
			
			if (tDevice != null){
				if (iUpdateInterval == 0 || System.currentTimeMillis() - tDevice.getLastUpdateTime() > iUpdateInterval){
					//Debugger.Information("Update status " + tDevice.getAddress());
					
					tDevice.updateStatus(rssi, scanRecord);
					
					notifyScanResult(tDevice);
				}
			}
	}
	
	/*
	 * BLEScannerV1: Support all Android platform
	 */
	private static class BLEScannerV1 extends BTScanner{
		private Context iContext = null;
		private BluetoothAdapter iAdapter = null;
		
		BLEScannerV1(Context aContext){
			iContext = aContext;
			iAdapter = super.getAdapter();
		}
		
		private BroadcastReceiver iBTDiscoveryReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent != null){
					String tAction = intent.getAction();

					Log.e("Action", tAction);
					if (tAction.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
						
					}else if (tAction.equals(BluetoothDevice.ACTION_FOUND)){
						BluetoothDevice tDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
						short tRSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short) -1);

						updateDeviceStatus(tDevice, tRSSI, null);
					}else if (tAction.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
						Log.e("Action", "RestartDiscovery=" + iAdapter.startDiscovery());
					}
				}
			}					
		};
		
		@Override
		public boolean startScan(BTScanListener aListener) {
			if (iAdapter != null && iContext != null){
				if (super.startScan(aListener)){
					IntentFilter tFilter = new IntentFilter();
					
					tFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
					tFilter.addAction(BluetoothDevice.ACTION_FOUND);
					tFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
					
					iContext.registerReceiver(iBTDiscoveryReceiver, tFilter);

					Log.e("startScan", "startDiscovery=" + iAdapter.startDiscovery());
					
					return true;
				}
			}
			
			return false;
		}

		@Override
		public boolean stopScan(BTScanListener aListener) {
			super.stopScan(aListener);
			
			if (iAdapter != null && iContext != null){
				iContext.unregisterReceiver(iBTDiscoveryReceiver);

				Log.e("stopScan", "cancelDiscovery=" + iAdapter.cancelDiscovery());
				
				return true;
			}
			
			return false;
		}

		@Override
		public boolean isSupportPowerSave() {
			return false;
		}
	}
	
	/*
	 * BLEScannerV2: Support above Android SDK 18 ~20
	 */
	private static class BLEScannerV2 extends BTScanner implements BluetoothAdapter.LeScanCallback{
		private BluetoothAdapter iAdapter = null;
		
		BLEScannerV2(){
			iAdapter = super.getAdapter();
		}
		
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			updateDeviceStatus(device, rssi, scanRecord);
		}

		@SuppressWarnings("deprecation")
		@Override
		public boolean startScan(BTScanListener aListener) {
			if (iAdapter != null){
				if (super.startScan(aListener)){
					iAdapter.stopLeScan(this);
					
					return iAdapter.startLeScan(this);
				}
			}
			
			return false;
		}

		@SuppressWarnings("deprecation")
		@Override
		public boolean stopScan(BTScanListener aListener) {
			super.stopScan(aListener);
			
			if (iAdapter != null){
				iAdapter.stopLeScan(this);
				
				return true;
			}
			
			return false;
		}

		@Override
		public boolean isSupportPowerSave() {
			return false;
		}
	}
	
	/*
	 * BLEScannerV3 Support above Android SDK 21
	 */
	@SuppressLint("InlinedApi")
	private static class BLEScannerV3 extends BTScanner{
		private final static int KFORCE_FLUSH_COUNT = 3;
		private final static int KBATCH_RESULT_DELAY = 3000;
		
		private BluetoothAdapter iAdapter = null;
		private BluetoothLeScanner iScanner = null;
		private long iLastResultTime = 0;
		private int iEmptyBatchCount = 0;
		
		BLEScannerV3() {
			iAdapter = super.getAdapter();
			iAdapter.isOffloadedFilteringSupported();
		}

		private ScanCallback iBTScanCallback = new ScanCallback(){
			public void procScanResult(ScanResult result) {
				//Debugger.Debug(result.getDevice().getAddress());
				
				if (result != null){
					byte tAdvData[];
					ScanRecord tRecord = result.getScanRecord();
					
					if (tRecord != null){
						tAdvData = tRecord.getBytes();
					}else{
						tAdvData = null;
					}
					
					updateDeviceStatus(result.getDevice(), result.getRssi(), tAdvData);
				}
			}
			
			@Override
			public void onScanResult(int callbackType, ScanResult result) {
				//Debugger.Warning("Process result start");
				procScanResult(result);
			}
	
			@Override
			public void onScanFailed(int errorCode) {
			}

			@Override
			public void onBatchScanResults(List<ScanResult> results) {
				long tTime = System.currentTimeMillis();
				
				if (tTime - iLastResultTime >= KBATCH_RESULT_DELAY){
					iLastResultTime = tTime;
					
					if (results != null){
						int tCount = results.size();

						Log.e("onScanFailed", "onBatchScanResults: size=" + tCount);
						
						if (tCount > 0){
							iEmptyBatchCount = 0;
							for (int i = 0; i < tCount; i++){
								procScanResult(results.get(i));
							}
						}else{
							iEmptyBatchCount++;
							
							if (iEmptyBatchCount == KFORCE_FLUSH_COUNT){
								AsyncTask.execute(new Runnable(){
									@Override
									public void run() {
										if (iEmptyBatchCount >= KFORCE_FLUSH_COUNT){
											Log.e("onScanFailed", "Force flush pending scan results");
											
											iScanner.flushPendingScanResults(iBTScanCallback);
										}
									}
								});
							}
						}
					}
				}
			}
		};

		@Override
		public boolean startScan(BTScanListener aListener) {
			if (iAdapter != null && iScanner == null){
				iScanner = iAdapter.getBluetoothLeScanner();
			}
			
			if (iScanner != null){
				if (super.startScan(aListener)){
					ScanSettings tSetting;
					
					if (isPowerSave()){
//						if (iAdapter.isOffloadedScanBatchingSupported()){
//							tSetting = new ScanSettings.Builder()
//							.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).setReportDelay(KBATCH_RESULT_DELAY).build();
//						}else{
							tSetting = new ScanSettings.Builder()
							.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
//						}
					}else{
						tSetting = new ScanSettings.Builder()
								.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
					}
					
					iScanner.stopScan(iBTScanCallback);
					iScanner.startScan(null, tSetting, iBTScanCallback);
					
					return true;
				}
			}
			
			return false;
		}

		@Override
		public boolean stopScan(BTScanListener aListener) {
			super.stopScan(aListener);
			
			if (iScanner != null){
				try{
					iScanner.stopScan(iBTScanCallback);
				}catch (Exception e){
					
				}
				
				return true;
			}
			
			return false;
		}

		@Override
		public boolean isSupportPowerSave() {
			return true;
		}

		@Override
		public boolean setPowerSave(boolean aPowerSave) {
			if (super.setPowerSave(aPowerSave)){
				if (super.isScanning()){
					
				}
			}
			
			return false;
		}
	}
}
