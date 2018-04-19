package com.ccl.bletools.bt;import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

public class ScreenMonitor extends Monitor {
	public interface OnScreenChangeListener extends MonitorListener{
		public void onScreenChange(boolean aOn);
	}
	
	/*
	 * Static functions
	 */
	private static ScreenMonitor gDefault = null;
	
	public static void Initialize(Context aContext){
		if (gDefault == null){
			gDefault = new ScreenMonitor(aContext);
		}
	}
	
	public static boolean InstanceOf(Monitor aMonitor){
		return aMonitor == gDefault;
	}
	
	public static boolean IsScreenOn(){
		if (gDefault != null){
			return gDefault.isScreenOn();
		}
		
		return false;
	}
	
	public static boolean AddOnScreenChangeListener(OnScreenChangeListener aListener){
		if (gDefault != null){
			return gDefault.addListener(aListener);
		}
		
		return false;
	}
	
	public static boolean RemoveOnScreenChangeListener(OnScreenChangeListener aListener){
		if (gDefault != null){
			return gDefault.removeListener(aListener);
		}
		
		return false;
	}
	
	/*
	 * 	Non-static functions
	 */
	private Context iContext = null;
	
	private PowerManager iPowerManager = null;
	
	private ScreenMonitor(Context aContext){
		iContext = aContext;
		
		if (aContext != null){
			iPowerManager = (PowerManager) aContext.getSystemService(Context.POWER_SERVICE);
		}
	}
	
	public boolean isScreenOn(){
		if (iPowerManager != null){
			return iPowerManager.isScreenOn();
		}
		
		return true;
	}
	
	private void onScreenChange(boolean aOn){
		int tIndex = 0;
		OnScreenChangeListener tListener;
		
		tListener = (OnScreenChangeListener) getFirstListener();
		while(tListener != null){
			tListener.onScreenChange(aOn);
			tListener = (OnScreenChangeListener) getNextListener(tIndex++);
		}
	}
	
	private BroadcastReceiver iScreenListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
				onScreenChange(true);
			}else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
				onScreenChange(false);
			}
		}
	};
	
	@Override
	public boolean startMonitor(){
		if (iContext != null){
			if (super.startMonitor()){
				IntentFilter tFilter = new IntentFilter();
				
				tFilter.addAction(Intent.ACTION_SCREEN_ON);
				tFilter.addAction(Intent.ACTION_SCREEN_OFF);
				iContext.registerReceiver(iScreenListener, tFilter);
				
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean stopMonitor(boolean aForce){
		if (super.stopMonitor(aForce)){
			if (iContext != null){
				iContext.unregisterReceiver(iScreenListener);
			}
			
			return true;
		}
		
		return false;
	}
}
