package com.ccl.bletools.bt;

import android.content.Context;
import android.util.Log;

public class BTDeviceMonitor extends BTMonitor {
    /*
     * Static functions
     */
    public static final int KBT_CHECK_INTERVAL = 3000;

    /*
     * 	Interfaces
     */
    public interface BTDeviceListener extends BTListener {
        public void onDeviceStateChange(BTDeviceMonitor aMonitor, BTDevice aDevice);
    }

    /*
     * 	Static functions
     */
    private static BTDeviceMonitor gDefault = null;

    public static void Initialize(Context aContext) {
        if (gDefault == null) {
            gDefault = new BTDeviceMonitor(aContext);
        }
    }

    public static boolean InstanceOf(Monitor aMonitor) {
        return aMonitor == gDefault;
    }

    public static void SetPowerSave(boolean aPowerSave) {
        if (gDefault != null) {
            gDefault.setPowerSave(aPowerSave);
        }
    }

    public static boolean AddBTDeviceListener(BTDeviceListener aListener) {
        if (gDefault != null) {
            return gDefault.addListener(aListener);
        }

        return false;
    }

    public static boolean RemoveBTDeviceListener(BTDeviceListener aListener) {
        if (gDefault != null) {
            return gDefault.removeListener(aListener);
        }

        return false;
    }

    /*
     * 	Non-static functions
     */
    private Context iContext = null;
    private boolean iMonitoring = false;
    private BTScanner iScanner = null;
    private int iResultCount = 0;


    private BTDeviceMonitor(Context aContext) {
        super();

        iContext = aContext;
        iScanner = BTScanner.CreateScanner(aContext);

        ScreenMonitor.Initialize(aContext);
        BTStateMonitor.Initialize(aContext);
        AlarmMonitor.Initialize(aContext);
    }

    public void setPowerSave(boolean aPowerSave) {
        iScanner.setPowerSave(aPowerSave);
    }

    private BTScanner.BTScanListener iBTScanListener = new BTScanner.BTScanListener() {
        @Override
        public void onRecvScanResult(BTDevice aDevice) {
            int tIndex = 0;
            BTDeviceListener tListener;

            iResultCount++;

            tListener = (BTDeviceListener) getFirstListener();
            while (tListener != null) {
                tListener.onDeviceStateChange(BTDeviceMonitor.this, aDevice);
                tListener = (BTDeviceListener) getNextListener(tIndex++);
            }
        }
    };

    private ScreenMonitor.OnScreenChangeListener iScreenListener = new ScreenMonitor.OnScreenChangeListener() {
        @Override
        public void onScreenChange(boolean aOn) {
            if (iMonitoring) {
                iScanner.setPowerSave(!aOn);
            }
        }
    };

    private AlarmMonitor.CountDownAlarmListener iRestartScanAlarm = new AlarmMonitor.CountDownAlarmListener(100, 0, true) {
        @Override
        public void onTimeup(AlarmMonitor.AlarmListener aListener) {
            if (iMonitoring) {
                iScanner.startScan(iBTScanListener);
            }
        }
    };

    private AlarmMonitor.CountDownAlarmListener iBTCheckAlarm = new AlarmMonitor.CountDownAlarmListener(KBT_CHECK_INTERVAL, KBT_CHECK_INTERVAL, true) {
        private long iLsatTime = 0;

        @Override
        public void onTimeup(AlarmMonitor.AlarmListener aListener) {
            long timeMillis = System.currentTimeMillis();
            long diffTime = timeMillis - iLsatTime;
            Log.e("onTimeup", "onTimeup.....: " + diffTime);
            if (diffTime >= KBT_CHECK_INTERVAL) {
                iLsatTime = timeMillis;
                if (iResultCount == 0) {
                    iScanner.stopScan(iBTScanListener);

                    if (iMonitoring) {
                        Log.e("AlarmMonitor", "Restart scan");

                        AlarmMonitor.AddAlarmListener(iRestartScanAlarm);
                    }
                } else {
                    iResultCount = 0;
                }
            }
        }
    };

    private void startScan() {
        if (iMonitoring) {
            iScanner.startScan(iBTScanListener);

            AlarmMonitor.AddAlarmListener(iBTCheckAlarm);
        }
    }

    private void onStateChange(int aState) {
        if (aState == BTStateMonitor.BT_STATE_ON) {
            if (iMonitoring == false) {
                iMonitoring = true;

                iScanner.setPowerSave(!ScreenMonitor.IsScreenOn());

                startScan();

                ScreenMonitor.AddOnScreenChangeListener(iScreenListener);
            }
        } else {
            if (iMonitoring) {
                iMonitoring = false;

                AlarmMonitor.RemoveAlarmListener(iBTCheckAlarm);

                ScreenMonitor.RemoveOnScreenChangeListener(iScreenListener);
                iScanner.stopScan(iBTScanListener);
            }
        }
    }

    private BTStateMonitor.OnBTStateChangeListener iBTStateListener = new BTStateMonitor.OnBTStateChangeListener() {
        @Override
        public void onBTStateChange(BTStateMonitor aMonitor, int aPreviousState, int aNewState) {
            if (isMonitoring()) {
                onStateChange(aNewState);
            }
        }
    };

    @Override
    public boolean startMonitor() {
        if (iContext != null) {
            if (super.startMonitor()) {
                BTStateMonitor.AddOnBTStateChangeListener(iBTStateListener);

                onStateChange(BTStateMonitor.GetBTState());

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean stopMonitor(boolean aForce) {
        if (super.stopMonitor(aForce)) {
            BTStateMonitor.RemoveOnBTStateChangeListener(iBTStateListener);

            onStateChange(BTStateMonitor.BT_STATE_OFF);

            return true;
        }

        return false;
    }
}
