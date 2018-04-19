package com.ccl.bletools.utils;

import java.util.Timer;
import java.util.TimerTask;

public class QueueInvoke {
    public interface InvokeListener {
        public void onInvoke();
    }

    /*
     * 	Non-static functions
     */
    private Object iSyncLock = new Object();

    private String iName = null;
    private int iDelay = 0;
    private int iPeriod = 0;
    private InvokeListener iListener = null;

    private volatile boolean iInvoking = false;
    private Timer iTimer = null;
    private TimerTask iTask = null;

    public QueueInvoke(String aName, int period, int aDelay, InvokeListener aListener) {
        iName = aName;
        iDelay = aDelay;
        iListener = aListener;
        iPeriod = period;
    }

    public QueueInvoke(String aName, int period, InvokeListener aListener) {
        iName = aName;
        iDelay = period;
        iListener = aListener;
    }

    public void invoke() {
        if (iListener != null) {
            synchronized (iSyncLock) {
                if (iInvoking == false) {
                    if (iTimer == null) {
                        iTimer = new Timer(iName != null ? iName + " thread" : "Invoke buffer thread");
                    }

                    if (iTask != null) {
                        iTask.cancel();
                    }

                    if (iInvoking == false) {
                        iTask = new TimerTask() {
                            @Override
                            public void run() {
                                synchronized (iSyncLock) {
                                    iListener.onInvoke();
                                }
                            }
                        };

                        iTimer.schedule(iTask, iPeriod, iDelay);
                    }
                }
            }
        }
    }

    public void cancel() {
        synchronized (iSyncLock) {
            if (iTask != null) {
                iTask.cancel();
                iTask = null;
            }
            if (iTimer != null) {
                iTimer.cancel();
                iTimer = null;
            }
        }
    }
}
