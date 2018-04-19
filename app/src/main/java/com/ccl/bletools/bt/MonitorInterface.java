package com.ccl.bletools.bt;
public interface MonitorInterface {
	public interface MonitorListener{
	}
	
	public int getListenerCount();
	
	public MonitorListener getFirstListener();
	
	public MonitorListener getNextListener(int aIndex);
	
	public boolean addListener(MonitorListener aListener);
	
	public boolean removeListener(MonitorListener aListener);
}
