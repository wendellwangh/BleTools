package com.ccl.bletools.bt;import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class AlarmReceiver extends BroadcastReceiver {
//	public static final String KACTION_ALARM = AlarmReceiver.class.getName();
	public static final String KACTION_ALARM = "com.ccl.iot.monitor.AlarmReceiver";
	public static final String KALARM_RECEIVER = "Receiver";
	public static final String KALARM_REPEATING = "Repeating";
	public static final String KALARM_TAG = "Tag";
	
	public interface AlarmListener{
		public void onAlarm(long aAlarmID, long aTime, String aTag);
	}
	
	private static Context gContext = null;
	private static Object gSyncLock = new Object();
	private static ListMap<Long, AlarmListener> gListener = null;
	
	public static boolean Initialize(Context aContext){
		gContext = aContext;
		
		return gContext != null;
	}
	
	private static Intent GetAlarmIntent(long aAlarmID, boolean aRepeating, String aTag){
		if (gContext != null && aAlarmID != 0){
			Intent tIntent = new Intent();
			
			tIntent.setClass(gContext, AlarmReceiver.class);
			tIntent.setAction(KACTION_ALARM);
			tIntent.setData(Uri.parse(KACTION_ALARM + ":" + aAlarmID));
			tIntent.putExtra(KALARM_RECEIVER, aAlarmID);
			tIntent.putExtra(KALARM_REPEATING, aRepeating);
			
			if (aTag != null){
				tIntent.putExtra(KALARM_TAG, aTag);
			}
			
			return tIntent;
		}
		
		return null;
	}
	
	public static long AddAlarm(AlarmListener aListener, long aTime, boolean aRepeating, boolean aWakeup, String aTag){
		synchronized(gSyncLock){
			long tID = 0;
			
			if (gContext != null){
				tID = System.currentTimeMillis();
				
				if (gListener == null){
					gListener = new ListMap<Long, AlarmListener>();
				}
				
				while(gListener.containsKey(tID)){
					tID++;
				}
				
				if (gListener.add(tID, aListener)){
					AlarmManager tAlarm = (AlarmManager) gContext.getSystemService(Service.ALARM_SERVICE);
					Intent tIntent = GetAlarmIntent(tID, aRepeating, aTag);
	
					if (tIntent != null){
						PendingIntent tPending = PendingIntent.getBroadcast(gContext, 0, tIntent, PendingIntent.FLAG_CANCEL_CURRENT);
						
						if (tPending != null){
							if (aRepeating){
								tAlarm.setRepeating(aWakeup ? AlarmManager.RTC_WAKEUP : AlarmManager.RTC, tID + aTime, aTime, tPending);
							}else{
								tAlarm.set(aWakeup ? AlarmManager.RTC_WAKEUP : AlarmManager.RTC, tID + aTime, tPending);
							}
						}else{
							tID = 0;
						}
					}else{
						tID = 0;
					}
				}else{
					tID = 0;
				}
			}
			
			return tID;
		}
	}
	
	private static boolean CancelAlarm(long aAlarmID, boolean aRepeating, String aTag){
		if (gContext != null && aAlarmID != 0){
			AlarmManager tAlarm = (AlarmManager) gContext.getSystemService(Service.ALARM_SERVICE);
			Intent tIntent = GetAlarmIntent(aAlarmID, aRepeating, aTag);
			
			if (tIntent != null){
				PendingIntent tPending = PendingIntent.getBroadcast(gContext, 0, tIntent, PendingIntent.FLAG_NO_CREATE);
				
				if (tPending != null){
					tAlarm.cancel(tPending);
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static boolean RemoveAlarm(long aAlarmID, boolean aRepeating, String aTag){
		synchronized(gSyncLock){
			if (aAlarmID != 0 && gListener != null && gListener.containsKey(aAlarmID)){
				CancelAlarm(aAlarmID, aRepeating, aTag);
				
				return gListener.remove(aAlarmID);
			}
		}
		
		return false;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		long tTime = System.currentTimeMillis();
		
		if (intent != null && intent.getAction() != null){
			if (intent.getAction().equals(KACTION_ALARM)){
				long tID = intent.getLongExtra(KALARM_RECEIVER, 0);
				boolean tRepeating = intent.getBooleanExtra(KALARM_REPEATING, false);
				String tTag = intent.getStringExtra(KALARM_TAG);
				
				synchronized(gSyncLock){
					if (gListener != null && gListener.containsKey(tID)){
						AlarmListener tListener = gListener.getValue(tID);
						
						if (tListener != null){
							tListener.onAlarm(tID, tTime, tTag);
						}
					}else{
						tRepeating = false;
					}
				}
				
				if (tRepeating == false){
					RemoveAlarm(tID, tRepeating, tTag);
				}
			}
		}
	}
	
}
