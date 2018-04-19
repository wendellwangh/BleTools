package com.ccl.bletools.bt;import java.util.ArrayList;

public abstract class Monitor implements MonitorInterface {
	/*
	 * 	Non-static functions
	 */
	private Object iSyncLock = new Object();
	
	private volatile boolean iMonitoring = false;
	private ArrayList<MonitorListener> iListeners = null;
	
	public boolean isMonitoring(){
		synchronized(iSyncLock){
			return iMonitoring;
		}
	}
	
	public boolean startMonitor(){
		synchronized(iSyncLock){
			if (iMonitoring == false){
				return iMonitoring = true;
			}
		}
		
		return false;
	}
	
	public boolean stopMonitor(boolean aForce){
		synchronized(iSyncLock){
			if (iMonitoring && (aForce || ((iListeners == null || iListeners.size() == 0)))){
				iMonitoring = false;
				
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public int getListenerCount(){
		if (iListeners != null){
			synchronized(iListeners){
				return iListeners.size();
			}
		}
		
		return 0;
	}
	
	@Override
	public MonitorListener getFirstListener(){
		if (iListeners != null){
			synchronized(iListeners){
				if (iListeners.size() > 0){
					return iListeners.get(0);
				}
			}
		}
		
		return null;
	}
	
	@Override
	public MonitorListener getNextListener(int aIndex){
		if (iListeners != null){
			aIndex++;
			synchronized(iListeners){
				if (iListeners.size() > aIndex){
					return iListeners.get(aIndex);
				}
			}
		}
		
		return null;
	}
	
	@Override
	public boolean addListener(MonitorListener aListener){
		if (aListener != null){
			if (iListeners == null){
				iListeners = new ArrayList<MonitorListener>();
			}else{
//				synchronized(iListeners){
					if (iListeners.contains(aListener)){
						return false;
					}
//				}
			}
			
			if (iListeners.add(aListener)){
				startMonitor();
				
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean removeListener(MonitorListener aListener){
		if (aListener != null &&iListeners != null){
//			synchronized(iListeners){
				if (iListeners.remove(aListener)){
					stopMonitor(false);
					
					return true;
				}
//			}
		}
		
		return false;
	}
}
