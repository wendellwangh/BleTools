package com.ccl.bletools.bt;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class AlarmMonitor extends BroadcastReceiver implements MonitorInterface {
	/**
	 *	Register this action for the receiver in the AndroidManifest.xml as below
	 *
	 <receiver android:name="com.ccl.AlarmMonitor" android:exported="false">
	 <intent-filter>
	 <!-- AlarmMonitor.KRECEIVER_ACTION -->
	 <action android:name="com.ccl.AlarmMonitor.onAlarm"/>
	 </intent-filter>
	 </receiver>
	 */
	public final static String KRECEIVER_ACTION = "com.ccl.AlarmMonitor.onAlarm";
	private final static String KRECEIVER_ID = "ID";
	private final static String KRECEIVER_ACTIVITY = "Activity";
	private final static String KRECEIVER_SERVICE = "Service";

	/*
	 * 	Classes
	 */
	public static abstract class AlarmListener implements MonitorListener{
		private long iTime;
		private long iRepeatInterval;
		private boolean iWakeup;
		private boolean iCanceled = false;
		private String iActivityClass = null;
		private String iServiceClass = null;

		AlarmListener(long aTime, long aRepeatInterval, boolean aWakeup){
			iTime = aTime;
			iRepeatInterval = aRepeatInterval;
			iWakeup = aWakeup;
		}

		public long getTime(){
			return iTime;
		}

		public long getTriggerTime(){
			return iTime;
		}

		public boolean isRepeat(){
			return iRepeatInterval != 0;
		}

		public long getRepeatInterval(){
			return iRepeatInterval;
		}

		public boolean needWakeup(){
			return iWakeup;
		}

		public boolean isCanceled(){
			return iCanceled;
		}

		public void activate(){
			iCanceled =false;
		}

		public void cancel(){
			iCanceled = true;
		}

		public void setTimeupActivity(Class<?> aActivity){
			if (aActivity != null){
				iActivityClass = aActivity.getName();
			}
		}

		public String getTimeupActivity(){
			return iActivityClass;
		}

		public void setTimeupService(Class<?> aService){
			if (aService != null){
				iServiceClass = aService.getName();
			}
		}

		public String getTimeupService(){
			return iServiceClass;
		}

		public abstract void onTimeup(AlarmListener aListener);
	}

	public static abstract class CountDownAlarmListener extends AlarmListener{
		public CountDownAlarmListener(long aTime, long aRepeatInterval, boolean aWakeup) {
			super(aTime, aRepeatInterval, aWakeup);
		}

		@Override
		public long getTriggerTime() {
			return System.currentTimeMillis() + super.getTime();
		}
	}

	/*
	 * 	Static functions
	 */
	private static AlarmMonitor gDefault = null;

	public static void Initialize(Context aContext){
		if (gDefault == null){
			gDefault = new AlarmMonitor(aContext);
		}
	}

	public static boolean InstanceOf(MonitorInterface aMonitor){
		return aMonitor == gDefault;
	}

	public static boolean AddAlarmListener(AlarmListener aListener){
		if (gDefault != null){
			return gDefault.addListener(aListener);
		}

		return false;
	}

	public static boolean RemoveAlarmListener(AlarmListener aListener){
		if (gDefault != null){
			return gDefault.removeListener(aListener);
		}

		return false;
	}


	/*
	 * 	Non-static functions
	 */
	private Context iContext = null;
	private AlarmManager iAlarmMan = null;

	public AlarmMonitor(){
	}

	private AlarmMonitor(Context aContext){
		initialize(aContext);
	}

	private void initialize(Context aContext){
		if (iContext == null && aContext != null){
			iContext = aContext;

			if (aContext != null){
				if (iAlarmMan == null){
					iAlarmMan = (AlarmManager) aContext.getSystemService(Service.ALARM_SERVICE);
				}
			}
		}
	}

	@Override
	public void onReceive(Context aContext, Intent aIntent) {
		initialize(aContext);

		if (aIntent != null){
			int tID = aIntent.getIntExtra(KRECEIVER_ID, 0);
			AlarmListener tListener = null;

			if (tID != 0 && gDefault != null){	//Alarm holder application still running
				int tIndex = 0;

				tListener = (AlarmListener) gDefault.getFirstListener();
				while(tListener != null){
					if (tListener.hashCode() == tID){
						break;
					}

					tListener = (AlarmListener) gDefault.getNextListener(tIndex++);
				}

				if (tListener != null){
					if (tListener.isRepeat() == false){
						gDefault.removeListener(tListener);
					}

					tListener.onTimeup(tListener);
				}
			}

			if (tListener == null){	//Alarm holder application die and restart, So cancel this alarm
				if (aContext != null){
					AlarmManager tAlarmMan = (AlarmManager) aContext.getSystemService(Service.ALARM_SERVICE);

					if (tAlarmMan != null){
						PendingIntent tPending = PendingIntent.getBroadcast(aContext, 0, aIntent, PendingIntent.FLAG_NO_CREATE);

						if (tPending != null){
							tAlarmMan.cancel(tPending);
						}
					}
				}
			}

			if (aContext != null){
				String tActivity = aIntent.getStringExtra(KRECEIVER_ACTIVITY);
				String tService = aIntent.getStringExtra(KRECEIVER_SERVICE);

				if (tActivity != null){
					Intent tIntent = new Intent();

					tIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					tIntent.setClassName(aContext, tActivity);

					aContext.startActivity(tIntent);
				}

				if (tService != null){
					Intent tIntent = new Intent();

					tIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					tIntent.setClassName(aContext, tService);

					aContext.startService(tIntent);
				}
			}

			if (android.os.Build.VERSION.SDK_INT >= 19){
				if (tListener != null && tListener.isCanceled() == false && tListener.isRepeat()){
					addAlarm(tListener);
				}
			}
		}
	}
	private Monitor iMonitor = new Monitor(){
		@Override
		public boolean addListener(MonitorListener aListener) {
			if (super.addListener(aListener)){
				if (addAlarm((AlarmListener) aListener)){
					return true;
				}else{
					super.removeListener(aListener);
				}
			}

			return false;
		}

		@Override
		public boolean removeListener(MonitorListener aListener) {
			if (super.removeListener(aListener)){
				cancelAlarm((AlarmListener) aListener);

				return true;
			}

			return false;
		}
	};

	private Intent genAlarmIntent(AlarmListener aListener){
		if (iContext != null && aListener != null){
			Intent tIntent = new Intent(KRECEIVER_ACTION);

			tIntent.setClass(iContext, this.getClass());
			tIntent.setData(Uri.parse(KRECEIVER_ACTION + ":" + aListener.hashCode()));
			tIntent.putExtra(KRECEIVER_ID, aListener.hashCode());
			if (aListener.getTimeupActivity() != null){
				tIntent.putExtra(KRECEIVER_ACTIVITY, aListener.getTimeupActivity());
			}

			return tIntent;
		}

		return null;
	}

	private boolean addAlarm(AlarmListener aListener){
		if (iContext != null && iAlarmMan != null && aListener != null){
			Intent tIntent = genAlarmIntent(aListener);


			if (tIntent != null){
				int tAlarmType = aListener.needWakeup() ? AlarmManager.RTC_WAKEUP : AlarmManager.RTC;
				PendingIntent tPending = PendingIntent.getBroadcast(iContext, 0, tIntent, PendingIntent.FLAG_CANCEL_CURRENT);

				if (tPending != null){
					if (aListener.isRepeat()){
						if (android.os.Build.VERSION.SDK_INT >= 19){
							iAlarmMan.setWindow(tAlarmType, aListener.getTriggerTime(), aListener.getRepeatInterval(), tPending);
						}else{
							iAlarmMan.setRepeating(tAlarmType, aListener.getTriggerTime(), aListener.getRepeatInterval(), tPending);
						}
					}else{
						iAlarmMan.set(tAlarmType, aListener.getTriggerTime(), tPending);
					}

					aListener.activate();

					return true;
				}
			}
		}

		return false;
	}

	private boolean cancelAlarm(AlarmListener aListener){
		if (iContext != null && iAlarmMan != null && aListener != null){
			Intent tIntent = genAlarmIntent(aListener);

			aListener.cancel();

			if (tIntent != null){
				PendingIntent tPending = PendingIntent.getBroadcast(iContext, 0, tIntent, PendingIntent.FLAG_NO_CREATE);

				if (tPending != null){
					iAlarmMan.cancel(tPending);

					return true;
				}
			}
		}

		return false;
	}

	@Override
	public int getListenerCount() {
		return iMonitor.getListenerCount();
	}

	@Override
	public MonitorListener getFirstListener() {
		return iMonitor.getFirstListener();
	}

	@Override
	public MonitorListener getNextListener(int aIndex) {
		return iMonitor.getNextListener(aIndex);
	}

	@Override
	public boolean addListener(MonitorListener aListener) {
		return iMonitor.addListener(aListener);
	}

	@Override
	public boolean removeListener(MonitorListener aListener) {
		return iMonitor.removeListener(aListener);
	}
}
