package com.ccl.bletools.bt;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BTStateMonitor extends BTMonitor {
	/*
	 * 	Static constants
	 */
    public static final int BT_STATE_UNKNOWN = 0;
    public static final int BT_STATE_OFF = BluetoothAdapter.STATE_OFF;
    public static final int BT_STATE_TURNING_ON = BluetoothAdapter.STATE_TURNING_ON;
    public static final int BT_STATE_ON = BluetoothAdapter.STATE_ON;
    public static final int BT_STATE_TURNING_OFF = BluetoothAdapter.STATE_TURNING_OFF;
    
    /*
     * 	Interfaces
     */
	public interface OnBTStateChangeListener extends BTListener{
		public void onBTStateChange(BTStateMonitor aMonitor, int aPreviousState, int aNewState);
	}
	
	/*
	 * 	Static functions
	 */
	private static BTStateMonitor gDefault = null;
	
	public static void Initialize(Context aContext){
		if (gDefault == null){
			gDefault = new BTStateMonitor(aContext);
		}
	}
	
	public static boolean InstanceOf(Monitor aMonitor){
		return aMonitor == gDefault;
	}
	
	public static int GetBTState(){
		if (gDefault != null){
			return gDefault.getBTState();
		}
		
		return BT_STATE_UNKNOWN;
	}

	public static boolean AddOnBTStateChangeListener(OnBTStateChangeListener aListener){
		if (gDefault != null){
			return gDefault.addListener(aListener);
		}
		
		return false;
	}
	
	public static boolean RemoveOnBTStateChangeListener(OnBTStateChangeListener aListener){
		if (gDefault != null){
			return gDefault.removeListener(aListener);
		}
		
		return false;
	}
	
	/*
	 * 	Non-static functions
	 */
	private Context iContext = null;
	
	private BTStateMonitor(Context aContext){
		super();
		
		iContext = aContext;
	}
	
	public int getBTState(){
		if (iAdapter != null){
			return iAdapter.getState();
		}else{
			return BT_STATE_UNKNOWN;
		}
	}
	
	private BroadcastReceiver iBTStateReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null){
				//if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
					int tPrevState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BT_STATE_UNKNOWN);
					int tState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BT_STATE_UNKNOWN);
					int tIndex = 0;
					OnBTStateChangeListener tListener;
										
					tListener = (OnBTStateChangeListener) getFirstListener();
					while(tListener != null){
						tListener.onBTStateChange(BTStateMonitor.this, tPrevState, tState);
						tListener = (OnBTStateChangeListener) getNextListener(tIndex++);
					}
				//}
			}
		}					
	};
	
	@Override
	public boolean startMonitor(){
		if (iContext != null){
			if (super.startMonitor()){
				IntentFilter tFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
				
				iContext.registerReceiver(iBTStateReceiver, tFilter);
				
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean stopMonitor(boolean aForce){
		if (super.stopMonitor(aForce)){
			if (iContext != null){
				iContext.unregisterReceiver(iBTStateReceiver);
			}
			
			return true;
		}
		
		return false;
	}
}
