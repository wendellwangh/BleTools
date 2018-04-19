package com.ccl.bletools.bt;

import android.bluetooth.BluetoothDevice;
import android.util.SparseArray;

public class BTDevice {
	public static final byte GAP_ADTYPE_FLAGS												= 0x01 ; //!< Discovery Mode: @ref GAP_ADTYPE_FLAGS_MODES
	public static final byte GAP_ADTYPE_16BIT_MORE										= 0x02 ; //!< Service: More 16-bit UUIDs available
	public static final byte GAP_ADTYPE_16BIT_COMPLETE								= 0x03 ; //!< Service: Complete list of 16-bit UUIDs
	public static final byte GAP_ADTYPE_32BIT_MORE										= 0x04 ; //!< Service: More 32-bit UUIDs available
	public static final byte GAP_ADTYPE_32BIT_COMPLETE								= 0x05 ; //!< Service: Complete list of 32-bit UUIDs
	public static final byte GAP_ADTYPE_128BIT_MORE									= 0x06 ; //!< Service: More 128-bit UUIDs available
	public static final byte GAP_ADTYPE_128BIT_COMPLETE							= 0x07 ; //!< Service: Complete list of 128-bit UUIDs
	public static final byte GAP_ADTYPE_LOCAL_NAME_SHORT						= 0x08 ; //!< Shortened local name
	public static final byte GAP_ADTYPE_LOCAL_NAME_COMPLETE				= 0x09 ; //!< Complete local name
	public static final byte GAP_ADTYPE_POWER_LEVEL									= 0x0A ; //!< TX Power Level: 0xXX: -127 to +127 dBm
	public static final byte GAP_ADTYPE_OOB_CLASS_OF_DEVICE 					= 0x0D ; //!< Simple Pairing OOB Tag: Class of device (3 octets)
	public static final byte GAP_ADTYPE_OOB_SIMPLE_PAIRING_HASHC 		= 0x0E ; //!< Simple Pairing OOB Tag: Simple Pairing Hash C (16 octets)
	public static final byte GAP_ADTYPE_OOB_SIMPLE_PAIRING_RANDR		= 0x0F ; //!< Simple Pairing OOB Tag: Simple Pairing Randomizer R (16 octets)
	public static final byte GAP_ADTYPE_SM_TK 												= 0x10 ; //!< Security Manager TK Value
	public static final byte GAP_ADTYPE_SM_OOB_FLAG									= 0x11 ; //!< Secutiry Manager OOB Flags
	public static final byte GAP_ADTYPE_SLAVE_CONN_INTERVAL_RANGE	= 0x12 ; //!< Min and Max values of the connection interval (2 octets Min, 2 octets Max) (0xFFFF indicates no conn interval min or max)
	public static final byte GAP_ADTYPE_SIGNED_DATA									= 0x13 ; //!< Signed Data field
	public static final byte GAP_ADTYPE_SERVICES_LIST_16BIT						= 0x14 ; //!< Service Solicitation: list of 16-bit Service UUIDs
	public static final byte GAP_ADTYPE_SERVICES_LIST_128BIT					= 0x15 ; //!< Service Solicitation: list of 128-bit Service UUIDs
	public static final byte GAP_ADTYPE_SERVICE_DATA									= 0x16 ; //!< Service Data
	public static final byte GAP_ADTYPE_APPEARANCE									= 0x19 ; //!< Appearance
	public static final byte GAP_ADTYPE_MANUFACTURER_SPECIFIC 			= (byte) 0xFF ; //!< Manufacturer Specific Data: first 2 octets contain the Company Identifier Code followed by the additional manufacturer specific data
	
	private String iAddress;
	private int iRSSI;
	private SparseArray<byte[]> iAdvData = new SparseArray<byte[]>();
	private long iLastUpdateTime;
	
	public BTDevice(String aAddress){
		if (aAddress != null){
			iAddress = aAddress;
		}else{
			iAddress = "";
		}
	}
	
	public BTDevice(BluetoothDevice aDevice){
		if (aDevice != null){
			iAddress = aDevice.getAddress();
		}else{
			iAddress = "";
		}
	}
	
	public String getAddress(){
		return iAddress;
	}
	
	public int getRSSI(){
		return iRSSI;
	}
	
	public boolean haveAdvData(){
		return iAdvData.size() > 0;
	}
	
	public byte[] getAdvData(byte aAdvType){
		return iAdvData.get(aAdvType);
	}
	
	public long getLastUpdateTime(){
		return iLastUpdateTime;
	}
	
	public boolean updateStatus(int aRSSI){
		iLastUpdateTime = System.currentTimeMillis();
		
		iRSSI = aRSSI;
		
		return true;
	}
	
	public boolean updateStatus(int aRSSI, byte aAdvData[]){
		updateStatus(aRSSI);
		
		if (aAdvData != null && aAdvData.length > 0){
			int tLen = aAdvData.length - 1;
			
			for (int i = 0; i < tLen; i++){
				if (aAdvData[i] > 0){
					if (aAdvData[i] == 1){
						iAdvData.put(aAdvData[i + 1], null);
					}else if (i + aAdvData[i] < aAdvData.length){
						byte tData[] = new byte[aAdvData[i] - 1];
						
						System.arraycopy(aAdvData, i + 2, tData, 0, tData.length);
						
						iAdvData.put(aAdvData[i + 1], tData);
					}
					
					i += aAdvData[i];
				}
			}
		}
		
		
		return true;
	}
}
