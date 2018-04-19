package com.ccl.bletools.bt;

import android.content.Context;
import android.util.SparseArray;

import java.util.Arrays;
import java.util.HashMap;

public class BTBeaconMonitor extends BTMonitor {
    /*
     * 	Interfaces
     */
    public interface BTBeaconListener extends BTListener {
        public void onBeaconStateChange(BTBeaconMonitor aMonitor, BTBeacon aBeacon);
    }

    /*
     * 	Static functions
     */
    private static BTBeaconMonitor gDefault = null;

    public static void Initialize(Context aContext) {
        if (gDefault == null) {
            gDefault = new BTBeaconMonitor(aContext);
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

    @Deprecated
    public static boolean AddBeaconMap(String aAddress, BTBeacon aBeacon) {
        if (gDefault != null) {
            return gDefault.addBeaconMap(aAddress, aBeacon);
        }

        return false;
    }

    public static boolean AddBTDeviceListener(BTBeaconListener aListener) {
        if (gDefault != null) {
            return gDefault.addListener(aListener);
        }

        return false;
    }

    public static boolean RemoveBTDeviceListener(BTBeaconListener aListener) {
        if (gDefault != null) {
            return gDefault.removeListener(aListener);
        }

        return false;
    }

    /*
     * 	Non-static functions
     */
    private Context iContext = null;
    private HashMap<String, BTBeacon> iBeaconMaps = null;
    private HashMap<String, SparseArray<BTBeacon>> iBeacons = new HashMap<String, SparseArray<BTBeacon>>();

    private BTBeaconMonitor(Context aContext) {
        super();

        iContext = aContext;

        BTDeviceMonitor.Initialize(aContext);
    }

    public void setPowerSave(boolean aPowerSave) {
        BTDeviceMonitor.SetPowerSave(aPowerSave);
    }

    /*
     * This function use to force map the specified BT device MAC to the specified beacon, Normally use when the Android SDK not support BLE(SDK Version less then 4.3)
     */
    @Deprecated
    public boolean addBeaconMap(String aAddress, BTBeacon aBeacon) {
        if (aAddress != null && aBeacon != null) {
            if (iBeaconMaps == null) {
                iBeaconMaps = new HashMap<String, BTBeacon>();
            }

            iBeaconMaps.put(aAddress, aBeacon);

            return true;
        }

        return false;
    }

    private BTBeacon getBeacon(byte aUUID[], short aMajor, short aMinor, byte aMeasuredPower, String devAddress) {
        String tUUID = Arrays.toString(aUUID);
        int tID = ((int) aMajor << 16) | ((int) aMinor);
        SparseArray<BTBeacon> tBeacons = iBeacons.get(tUUID);
        BTBeacon tBeacon;

        if (tBeacons == null) {
            tBeacons = new SparseArray<BTBeacon>();

            iBeacons.put(tUUID, tBeacons);

            tBeacon = null;
        } else {
            tBeacon = tBeacons.get(tID);
        }

        if (tBeacon == null) {
            tBeacon = new BTBeacon(aUUID, aMajor, aMinor, aMeasuredPower, devAddress);

//            tBeacons.put(tID, tBeacon);
        }

        return tBeacon;
    }

    public static final short CU8(byte aInt8) {
        if (aInt8 < 0) {
            return (short) ((short) aInt8 + (short) 0xFF + (short) 1);
        } else {
            return aInt8;
        }
    }

    private BTDeviceMonitor.BTDeviceListener iBTDeviceListener = new BTDeviceMonitor.BTDeviceListener() {
        @Override
        public void onDeviceStateChange(BTDeviceMonitor aMonitor, BTDevice aDevice) {
            byte tData[] = aDevice.getAdvData(BTDevice.GAP_ADTYPE_MANUFACTURER_SPECIFIC);
            BTBeacon tBeacon = null;

            if (tData != null && tData.length == 25){
                if (tData[0] == 0x4C && tData[1] == -0x0 && tData[2] == 0x02 && tData[3] == 0x15){	//iBeacon
                    byte tUUID[] = new byte[16];

                    System.arraycopy(tData, 4, tUUID, 0, 16);

                    tBeacon = getBeacon(tUUID, (short)(CU8(tData[21]) | (CU8(tData[20]) << 8)), (short)(CU8(tData[23]) | (CU8(tData[22]) << 8)), tData[24], aDevice.getAddress());
                }
            }

            tData = aDevice.getAdvData(BTDevice.GAP_ADTYPE_SERVICE_DATA);

            if (tData != null && (tData.length == 23 || tData.length == 24)){
                if (tData[0] == (byte)0xCC && tData[1] == (byte)0xCC){	//CCLBeacon
                    if (tBeacon == null){
                        byte tUUID[] = new byte[16];

                        System.arraycopy(tData, 2, tUUID, 0, 16);

                        tBeacon = getBeacon(tUUID, (short)(CU8(tData[19]) | (CU8(tData[18]) << 8)), (short)(CU8(tData[21]) | (CU8(tData[20]) << 8)), tData[22], aDevice.getAddress());
                    }

                    if (tBeacon != null){
                        if (tData.length == 24){
                            tBeacon.setBattery(tData[23]);
                        }
                    }
                }
            }

            if (tBeacon == null && iBeaconMaps != null){
                tBeacon = iBeaconMaps.get(aDevice.getAddress());
            }

            if (tBeacon != null){
                int tIndex = 0;
                BTBeaconListener tListener;

                tBeacon.updateRSSI(aDevice.getRSSI());

                //Debugger.Error("Process result completed");

                tListener = (BTBeaconListener) getFirstListener();
                while(tListener != null){
                    tListener.onBeaconStateChange(BTBeaconMonitor.this, tBeacon);
                    tListener = (BTBeaconListener) getNextListener(tIndex++);
                }
            }
        }
    };

    @Override
    public boolean startMonitor() {
        if (iContext != null) {
            if (super.startMonitor()) {
                BTDeviceMonitor.AddBTDeviceListener(iBTDeviceListener);

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean stopMonitor(boolean aForce) {
        if (super.stopMonitor(aForce)) {
            BTDeviceMonitor.RemoveBTDeviceListener(iBTDeviceListener);

            return true;
        }

        return false;
    }
}
