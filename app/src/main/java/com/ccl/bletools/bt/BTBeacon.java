package com.ccl.bletools.bt;


import java.util.Arrays;

public class BTBeacon {
    public static final byte KCCL_LOCATOR_UUID[] = {(byte) 0xD8, (byte) 0xFB, (byte) 0x68, 'C', 'C', 'L', 'I', 'O', 'T', 'L', 'O', 'C', 'A', 'T', 'O', 'R'};
    public static final byte KCCL_ORIENTOR_UUID[] = {(byte) 0xD8, (byte) 0xFB, 0x68, 'C', 'C', 'L', 'O', 'R', 'I', 'E', 'N', 'T', 'A', 'T', 'O', 'R'};
    public static final byte KCCL_POSITIONER_UUID[] = {(byte) 0xD8, (byte) 0xFB, 0x68, 'C', 'C', 'L', 'P', 'O', 'S', 'I', 'T', 'I', 'O', 'N', 'E', 'R'};

    public String proximityUuid;
    public String iStringUUID;
    private byte iUUID[];
    private short iMajor;
    private short iMinor;
    private byte iMeasuredPower;
    private byte iBattery = -1;        //-1 mean unknown, 0~100 mean battery power level
    private int iRSSI = 0;
    private String iDevAddress;

    public BTBeacon(byte aUUID[], short aMajor, short aMinor, byte aMeasuredPower, String devAddress) {
        iUUID = aUUID;
        if (iUUID != null) {
            StringBuilder tUUID = new StringBuilder();

            for (int i = 0; i < iUUID.length; i++) {
                tUUID.append(String.format("%02x", iUUID[i]));
            }

            proximityUuid = tUUID.toString();
        }

        iMajor = aMajor;
        iMinor = aMinor;
        //		iMeasuredPower = aMeasuredPower;
        iMeasuredPower = (byte) (12 - ((-50 - aMeasuredPower) / 2));
        iDevAddress = devAddress;
    }

    public String getUUID() {
        if (iStringUUID == null) {
            iStringUUID = proximityUuid + "-" + iMajor + "-" + iMinor;
        }
        return iStringUUID;
    }

    //	public byte[] getUUID(){
    //		return iUUID;
    //	}

    //	public String getStringUUID(){
    //		return iStringUUID;
    //	}

    public short getMajor() {
        return iMajor;
    }

    public short getMinor() {
        return iMinor;
    }

    public byte getMeasuredPower() {
        return iMeasuredPower;
    }

    public void setBattery(byte aBattery) {
        iBattery = aBattery;
    }

    public byte getBattery() {
        return iBattery;
    }

    public void updateRSSI(int aRSSI) {
        iRSSI = aRSSI;
    }

    public int getRssi() {
        return iRSSI;
    }

    public String getiDevAddress() {
        return iDevAddress;
    }

    public void setiDevAddress(String iDevAddress) {
        this.iDevAddress = iDevAddress;
    }

    public String getType(){
        String tName;
        if (Arrays.equals(iUUID, KCCL_LOCATOR_UUID)) {
            tName = "CCLLocator";
        } else if (Arrays.equals(iUUID, KCCL_ORIENTOR_UUID)) {
            tName = "CCLOrientor";
        } else if (Arrays.equals(iUUID, KCCL_POSITIONER_UUID)) {
            tName = "CCLPositioner";
        } else {
            tName = iStringUUID;
        }
        return tName;
    }

    @Override
    public String toString() {
        if (iUUID != null) {
            String tName;

            if (Arrays.equals(iUUID, KCCL_LOCATOR_UUID)) {
                tName = "CCLLocator";
            } else if (Arrays.equals(iUUID, KCCL_ORIENTOR_UUID)) {
                tName = "CCLOrientor";
            } else if (Arrays.equals(iUUID, KCCL_POSITIONER_UUID)) {
                tName = "CCLPositioner";
            } else {
                tName = iStringUUID;
            }

            return tName + ": " + String.format("%04X", iMajor) + "-" + String.format("%04X", iMinor);
        }

        return super.toString();
    }
}
