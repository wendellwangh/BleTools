package com.ccl.bletools.bt;

import android.bluetooth.BluetoothAdapter;

public abstract class BTMonitor extends Monitor {
    /*
     * 	Interfaces
     */
	public interface BTListener extends MonitorListener{
	}
	
	/*
	 * 	Non-static functions
	 */
	protected BluetoothAdapter iAdapter = null;
	
	protected BTMonitor(){
		iAdapter = BluetoothAdapter.getDefaultAdapter();
	}
}
