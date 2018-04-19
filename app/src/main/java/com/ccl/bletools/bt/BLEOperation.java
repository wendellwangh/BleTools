package com.ccl.bletools.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.ccl.bletools.utils.Messager;
import com.ccl.bletools.utils.QueueInvoke;
import com.ccl.bletools.utils.UIUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BLEOperation {

    private static final String SERVICE_UUID = "0000ff00-0000-1000-8000-00805f9b34fb";
    private static final String TXPOWER_UUID = "0000fff1-0000-1000-8000-00805f9b34fb";
    private static final String BESCON_NAME_UUID = "0000fff2-0000-1000-8000-00805f9b34fb";
    private static final String BESCON_MM_UUID = "0000fff3-0000-1000-8000-00805f9b34fb";
    private static final String BESCON_PWD_UUID = "0000ff01-0000-1000-8000-00805f9b34fb";
    private static final String BESCON_TYPE_UUID = "0000ff11-0000-1000-8000-00805f9b34fb";
    private static final String BESCON_TYPE_INTERVAL = "0000fff4-0000-1000-8000-00805f9b34fb";
    private static BLEOperation mInstance;

    public static final String BEACON_NOUSED_UUID = "d8fb6843434c494f544c4f4341544f52";
    public static final String BEACON_ORIENT_UUID = "d8fb6843434c4f5249454e5441544f52";
    public static final String BEACON_LOCATE_UUID = "d8fb6843434c504f534954494f4e4552";

    public static final int BEACON_TYPE_NOUSED = 0;
    public static final int BEACON_TYPE_ORIENT = 1;
    public static final int BEACON_TYPE_LOCATE = 2;

    private BluetoothDevice mBleDevice;
    private BluetoothGatt mBluetoothGatt;
    private BTBeacon mBeacon;
    private BluetoothGattCharacteristic mTxPowerChara;
    private BluetoothGattCharacteristic mBeaconNameChara;
    private BluetoothGattCharacteristic mBeaconMMChara;
    private BluetoothGattCharacteristic mBeaconPwdChara;
    private BluetoothGattCharacteristic mBeaconTypeChara;
    private BluetoothGattCharacteristic mBeaconInterval;
    private BleOptThread mOptThread;

    public static BLEOperation GetInstance() {
        if (mInstance == null) {
            mInstance = new BLEOperation();
        }
        return mInstance;
    }

    private BLEOperation() {

    }

    public String getBeaconUUID() {
        return mBeacon != null ? mBeacon.getUUID() : null;
    }

    public int getTxPower() {
        return mBeacon != null ? mBeacon.getMeasuredPower() : 0;
    }

    /**
     * 设置从底层获取的ble设备和beacon, 需要先执行这一步才可以执行后面的操作
     *
     * @param bleDevice
     * @param beacon
     */
    public void setOperationBle(BluetoothDevice bleDevice, BTBeacon beacon) {
        releaseGatt();
        mBleDevice = bleDevice;
        mBeacon = beacon;
    }

    public BTBeacon getBTbeacon() {
        return mBeacon;
    }

    /**
     * 连接Beacon
     *
     * @return
     */
    public boolean connectGatt() {
        if (mBleDevice != null && mBluetoothGatt == null) {
            mBluetoothGatt = mBleDevice.connectGatt(UIUtils.getContext(), false, mBluetoothGattCallback);
        }
        if (mBluetoothGatt != null) {
            return true;
        }
        return false;
    }

    public boolean readBeaconName() {
        if (mOptThread != null) {
            mOptThread.sendMessage(BleOptThread.MSG_WHAT_OPT_READ_NAME);
            return true;
        }
        return false;
    }

    public boolean readTxPower() {
        if (mOptThread != null) {
            mOptThread.sendMessage(BleOptThread.MSG_WHAT_OPT_READ_TXPOWER);
            return true;
        }
        return false;
    }

    public boolean readType() {
        if (mOptThread != null) {
            mOptThread.sendMessage(BleOptThread.MSG_WHAT_OPT_READ_TYPE);
            return true;
        }
        return false;
    }

    public boolean readMM() {
        if (mOptThread != null) {
            mOptThread.sendMessage(BleOptThread.MSG_WHAT_OPT_READ_MM);
            return true;
        }
        return false;
    }

    public boolean readInterval() {
        if (mOptThread != null) {
            mOptThread.sendMessage(BleOptThread.MSG_WHAT_OPT_READ_INTERVAL);
            return true;
        }
        return false;
    }

    public boolean writeMM(int mm) {
        if (mOptThread != null) {
            Message obtain = Message.obtain();
            obtain.what = BleOptThread.MSG_WHAT_OPT_WRITE_MM;
            obtain.obj = mm;
            mOptThread.sendMessage(obtain);
            return true;
        }
        return false;
    }

    public boolean writeBeaconType(int type) {
        if (mOptThread != null) {
            Message obtain = Message.obtain();
            obtain.what = BleOptThread.MSG_WHAT_OPT_WRITE_TYPE;
            obtain.obj = type;
            mOptThread.sendMessage(obtain);
            return true;
        }
        return false;
    }

    public boolean writeBeaconName(String name) {
        if (mOptThread != null) {
            Message obtain = Message.obtain();
            obtain.what = BleOptThread.MSG_WHAT_OPT_WRITE_NAME;
            obtain.obj = name;
            mOptThread.sendMessage(obtain);
            return true;
        }
        return false;
    }

    public boolean writeTxPower(byte txpower) {
        if (mOptThread != null) {
            Message obtain = Message.obtain();
            obtain.what = BleOptThread.MSG_WHAT_OPT_WRITE_TXPOWER;
            obtain.obj = txpower;
            mOptThread.sendMessage(obtain);
            return true;
        }
        return false;
    }

    public boolean writeInterval(int interval) {
        Log.e("FUCK", "writeInterval 1");
        if (mOptThread != null) {
            Message obtain = Message.obtain();
            obtain.what = BleOptThread.MSG_WHAT_OPT_WRITE_INTERVAL;
            obtain.obj = interval;
            mOptThread.sendMessage(obtain);
            return true;
        }
        return false;
    }

    /**
     * 检查类型
     *
     * @param type
     * @return 是相同类型返回TRUE, 否则返回false(返回false需要写入类型)
     */
    public boolean checkType(int type) {
        switch (type) {
            case BEACON_TYPE_LOCATE:
                if (!BEACON_LOCATE_UUID.equals(mBeacon.proximityUuid)) {
                    writeBeaconType(BEACON_TYPE_LOCATE);
                    return false;
                }
                return true;
            case BEACON_TYPE_ORIENT:
                if (!BEACON_ORIENT_UUID.equals(mBeacon.proximityUuid)) {
                    writeBeaconType(BEACON_TYPE_ORIENT);
                    return false;
                }
                return true;
        }
        return false;
    }

    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

        //记录重连的次数
        private int reconnectCount = 0;

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, final int status, final int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {

                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.e("BleCallback", "连接成功");

                    reconnectCount = 0;
                    mReadRssiInvoke.invoke();
                    //查找Beacon的服务
                    gatt.discoverServices();
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    //断开连接
                    //                    Messager.Show(R.string.disconnect);
                    Log.e("BleCallback", "返回断开连接");
                    reconnect();

                } else {
                    Log.e("BleCallback", "未知错误 newState: 0x" + Integer.toHexString(newState));
                    //                    releaseGatt();
                    reconnect();
                }

            } else {
                Log.e("GERRT", "未知错误 status: 0x" + Integer.toHexString(status));
                //                    未知错误 status: 85
                Log.e("BleCallback", "releaseGatt  222");
                //                releaseGatt();
                //连接失败时自动重新连接6次
                reconnect();
            }
        }

        private void reconnect() {
            //连接失败时自动重新连接6次
            //            if (mBleDevice != null && /*mBluetoothGatt == null &&*/ reconnectCount < 6) {
            //                mBluetoothGatt = mBleDevice.connectGatt(UIUtils.getContext(), false, mBluetoothGattCallback);
            //                reconnectCount++;
            //            } else {
            //                reconnectCount = 0;
            //                if (mCallback != null) {
            //                    //连接失败
            if (mCallback != null) {
                mCallback.onConnectFaild();
            }
            //                }
            destroy();
            //            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = gatt.getServices();
                if (services != null && services.size() > 0) {
                    for (BluetoothGattService server : services) {
                        //获取需要的服务
                        if (server != null && SERVICE_UUID.equals(server.getUuid().toString())) {
                            List<BluetoothGattCharacteristic> characteristics = server.getCharacteristics();
                            if (characteristics != null && characteristics.size() > 0) {
                                for (BluetoothGattCharacteristic chara : characteristics) {
                                    if (chara != null) {
                                        chara.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                                        //获取需要的特征
                                        if (TXPOWER_UUID.equals(chara.getUuid().toString())) {
                                            mTxPowerChara = chara;
                                        } else if (BESCON_NAME_UUID.equals(chara.getUuid().toString())) {
                                            mBeaconNameChara = chara;
                                        } else if (BESCON_MM_UUID.equals(chara.getUuid().toString())) {
                                            mBeaconMMChara = chara;
                                        } else if (BESCON_PWD_UUID.equals(chara.getUuid().toString())) {
                                            mBeaconPwdChara = chara;
                                            mBeaconPwdChara.setValue(864237619, BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                                            Log.e("GERRT", "写入密码!");
                                            Messager.Show("写入密码");
                                            mBluetoothGatt.writeCharacteristic(mBeaconPwdChara);
                                        } else if (BESCON_TYPE_UUID.equals(chara.getUuid().toString())) {
                                            mBeaconTypeChara = chara;
                                        }else if (BESCON_TYPE_INTERVAL.equals(chara.getUuid().toString())) {
                                            mBeaconInterval = chara;
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            } else {
                Messager.Show("未知错误 onServicesDiscovered status: 0x" + Integer.toHexString(status));
                Log.e("GERRT", "未知错误 onServicesDiscovered status: 0x" + Integer.toHexString(status));
            }
            if (mCallback != null) {
                mCallback.onServiceDiscover();
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic != null) {
                if (TXPOWER_UUID.equals(characteristic.getUuid().toString())) {
                    Integer intValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    if (mOptThread != null) {
                        Message obtain = Message.obtain();
                        obtain.what = BleOptThread.MSG_WHAT_CALLBACK_READ_TXPOWER;
                        obtain.obj = intValue;
                        mOptThread.sendMessage(obtain);
                    }
                } else if (BESCON_NAME_UUID.equals(characteristic.getUuid().toString())) {
                    String stringValue = characteristic.getStringValue(0);
                    if (mOptThread != null) {
                        Message obtain = Message.obtain();
                        obtain.what = BleOptThread.MSG_WHAT_CALLBACK_READ_NAME;
                        obtain.obj = stringValue;
                        mOptThread.sendMessage(obtain);
                    }
                } else if (BESCON_MM_UUID.equals(characteristic.getUuid().toString())) {
                    Integer intValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                    if (mOptThread != null) {
                        Message obtain = Message.obtain();
                        obtain.what = BleOptThread.MSG_WHAT_CALLBACK_READ_MM;
                        obtain.obj = intValue;
                        mOptThread.sendMessage(obtain);
                    }
                } else if (BESCON_TYPE_UUID.equals(characteristic.getUuid().toString())) {
                    Integer intValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    if (mOptThread != null) {
                        Message obtain = Message.obtain();
                        obtain.what = BleOptThread.MSG_WHAT_CALLBACK_READ_TYPE;
                        obtain.obj = intValue;
                        mOptThread.sendMessage(obtain);
                    }
                } else if (BESCON_TYPE_INTERVAL.equals(characteristic.getUuid().toString())) {
                    Integer intValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                    if (mOptThread != null) {
                        Message obtain = Message.obtain();
                        obtain.what = BleOptThread.MSG_WHAT_CALLBACK_READ_INTERVAL;
                        obtain.obj = intValue;
                        mOptThread.sendMessage(obtain);
                    }
                }

            } else {
                Log.e("GERRT", "onCharacteristicRead status: 0x" + Integer.toHexString(status));
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic != null) {
                if (TXPOWER_UUID.equals(characteristic.getUuid().toString())) {
                    Integer intValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    if (mOptThread != null) {
                        Message obtain = Message.obtain();
                        obtain.what = BleOptThread.MSG_WHAT_CALLBACK_WRITE_TXPOWER;
                        obtain.obj = intValue;
                        mOptThread.sendMessage(obtain);
                    }
                } else if (BESCON_NAME_UUID.equals(characteristic.getUuid().toString())) {
                    String stringValue = characteristic.getStringValue(0);
                    if (mOptThread != null) {
                        Message obtain = Message.obtain();
                        obtain.what = BleOptThread.MSG_WHAT_CALLBACK_WRITE_NAME;
                        obtain.obj = stringValue;
                        mOptThread.sendMessage(obtain);
                    }
                } else if (BESCON_MM_UUID.equals(characteristic.getUuid().toString())) {
                    Integer intValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);

                    if (mOptThread != null) {
                        Message obtain = Message.obtain();
                        obtain.what = BleOptThread.MSG_WHAT_CALLBACK_WRITE_MM;
                        obtain.obj = intValue;
                        mOptThread.sendMessage(obtain);
                    }

                } else if (BESCON_PWD_UUID.equals(characteristic.getUuid().toString())) {

                    if (mOptThread == null) {
                        mOptThread = new BleOptThread();
                        mOptThread.start();
                    }

                } else if (BESCON_TYPE_UUID.equals(characteristic.getUuid().toString())) {
                    Integer intValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    switch (intValue) {
                        case BEACON_TYPE_NOUSED:
                            mBeacon.proximityUuid = BEACON_NOUSED_UUID;
                            break;
                        case BEACON_TYPE_LOCATE:
                            mBeacon.proximityUuid = BEACON_LOCATE_UUID;
                            break;
                        case BEACON_TYPE_ORIENT:
                            mBeacon.proximityUuid = BEACON_ORIENT_UUID;
                            break;
                    }

                    mBeacon.iStringUUID = null;

                    if (mOptThread != null) {
                        Message obtain = Message.obtain();
                        obtain.what = BleOptThread.MSG_WHAT_CALLBACK_WRITE_TYPE;
                        obtain.obj = intValue;
                        mOptThread.sendMessage(obtain);
                    }
                } else if (BESCON_TYPE_INTERVAL.equals(characteristic.getUuid().toString())) {
                    Integer intValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);

                    if (mOptThread != null) {
                        Message obtain = Message.obtain();
                        obtain.what = BleOptThread.MSG_WHAT_CALLBACK_WRITE_INTERVAL;
                        obtain.obj = intValue;
                        mOptThread.sendMessage(obtain);
                    }

                }

            } else {
                Log.e("GERRT", "onCharacteristicWrite status: 0x" + Integer.toHexString(status));
                Messager.Show("Write mBeaconTxPower error status: 0x" + Integer.toHexString(status), 0);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, final int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {

                if (mCallback != null) {
                    mCallback.onReadRssi(mBeacon.getRssi());
                }
            } else {
                Log.e("GERRT", "onReadRemoteRssi status: 0x" + Integer.toHexString(status));
            }
        }
    };

    //读取Rssi值的任务队列
    private QueueInvoke mReadRssiInvoke = new QueueInvoke("Read Rssi", 2000, new QueueInvoke.InvokeListener() {
        @Override
        public void onInvoke() {
            if (mBluetoothGatt != null) {
                mBluetoothGatt.readRemoteRssi();
            }
        }
    });

    //释放Gatt
    public void releaseGatt() {
        Log.e("BleCallback", "releaseGatt");
        //        mBleDevice = null;
        //        mBeacon = null;
        if (mBluetoothGatt != null) {
            try {
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
                //这里主要针对部分手机连接Beacon时会出现错误代码为133的问题
                Method localMethod = mBluetoothGatt.getClass().getMethod("refresh");
                if (localMethod != null) {
                    localMethod.invoke(mBluetoothGatt);
                }
            } catch (Exception localException) {
                Log.e("refreshServices()", "An exception occured while refreshing device");
            }
            mBluetoothGatt = null;
        }

        if (mOptThread != null) {
            mOptThread.stopRun();
            mOptThread = null;
        }
        mReadRssiInvoke.cancel();
    }

    public void destroy() {
        Log.e("BleCallback", "releaseGatt  222");
        releaseGatt();
        mInstance = null;
    }

    private Blecallback mCallback;

    public void setCallback(Blecallback callback) {
        mCallback = callback;
    }

    private class BleOptThread extends Thread {
        public static final int MSG_WHAT_OPT_READ_NAME = 1000;
        public static final int MSG_WHAT_OPT_READ_TXPOWER = 1001;
        public static final int MSG_WHAT_OPT_READ_TYPE = 1002;
        public static final int MSG_WHAT_OPT_READ_MM = 1003;
        public static final int MSG_WHAT_OPT_WRITE_MM = 1004;
        public static final int MSG_WHAT_OPT_WRITE_NAME = 1005;
        public static final int MSG_WHAT_OPT_WRITE_TXPOWER = 1006;
        public static final int MSG_WHAT_OPT_WRITE_TYPE = 1007;
        public static final int MSG_WHAT_OPT_READ_INTERVAL = 1008;
        public static final int MSG_WHAT_OPT_WRITE_INTERVAL = 1009;

        public static final int MSG_WHAT_CALLBACK_READ_NAME = 2000;
        public static final int MSG_WHAT_CALLBACK_READ_TXPOWER = 2001;
        public static final int MSG_WHAT_CALLBACK_READ_TYPE = 2002;
        public static final int MSG_WHAT_CALLBACK_READ_MM = 2003;
        public static final int MSG_WHAT_CALLBACK_WRITE_MM = 2004;
        public static final int MSG_WHAT_CALLBACK_WRITE_NAME = 2005;
        public static final int MSG_WHAT_CALLBACK_WRITE_TXPOWER = 2006;
        public static final int MSG_WHAT_CALLBACK_WRITE_TYPE = 2007;
        public static final int MSG_WHAT_CALLBACK_READ_INTERVAL = 2008;
        public static final int MSG_WHAT_CALLBACK_WRITE_INTERVAL = 2009;

        private Handler mHandler;

        private ArrayList<Message> msgs = new ArrayList<Message>();
        private final Timer mTimer;
        private final TimerTask mTimerTask;

        private boolean isBlock = false;

        public BleOptThread() {

            mTimer = new Timer();
            mTimerTask = new TimerTask() {
                @Override
                public void run() {

                    if (mHandler != null && !isBlock) {
                        if (msgs.size() > 0) {
                            Message message = msgs.get(0);
                            msgs.remove(0);

                            mHandler.sendMessage(message);
                        } else {
                            mHandler.sendEmptyMessage(MSG_WHAT_OPT_READ_TYPE);
                        }
                    }
                }
            };

            mTimer.schedule(mTimerTask, 0000, 5000);
        }

        public void sendMessage(Message msg) {
            if (mHandler != null && msg != null) {
                mHandler.sendMessage(msg);
            }
        }

        public void sendMessage(int what) {
            if (mHandler != null) {
                mHandler.sendEmptyMessage(what);
            }
        }

        public void stopRun() {
            if (mHandler != null) {
                mHandler.getLooper().quit();
            }
            mTimer.cancel();
            mHandler = null;
        }

        @Override
        public void run() {
            Looper.prepare();
            mHandler = new Handler() {

                @Override
                public void handleMessage(Message msg) {
                    Log.e("BleCallback", "run onInvoke: " + msg.what);
                    if (msg.what <= MSG_WHAT_OPT_WRITE_TYPE && msg.what >= MSG_WHAT_OPT_READ_NAME) {
                        if (!isBlock) {
                            isBlock = true;
                        } else {
                            if (MSG_WHAT_OPT_READ_TYPE != msg.what) {
                                Message obtain = Message.obtain();
                                obtain.what = msg.what;
                                obtain.obj = msg.obj;
                                msgs.add(obtain);
                            }

                            Log.e("BleCallback", "run onInvoke: 44 " + msg.what);
                            msg = null;
                        }
                    } else if (msg.what <= MSG_WHAT_CALLBACK_WRITE_TYPE && msg.what >= MSG_WHAT_CALLBACK_READ_NAME) {
                        if (isBlock) {
                            isBlock = false;
                        }
                    }

                    if (msg != null) {
                        switch (msg.what) {
                            case MSG_WHAT_OPT_READ_NAME:
                                if (mBeaconNameChara != null && mBluetoothGatt != null) {
                                    Log.e("BleCallback", "读名字指令");
                                    mBluetoothGatt.readCharacteristic(mBeaconNameChara);
                                }
                                break;
                            case MSG_WHAT_OPT_READ_TXPOWER:
                                if (mTxPowerChara != null && mBluetoothGatt != null) {
                                    Log.e("BleCallback", "读txpower指令");
                                    mBluetoothGatt.readCharacteristic(mTxPowerChara);
                                }
                                break;
                            case MSG_WHAT_OPT_READ_TYPE:
                                if (mBeaconTypeChara != null && mBluetoothGatt != null) {
                                    Log.e("BleCallback", "读类型指令");
                                    mBluetoothGatt.readCharacteristic(mBeaconTypeChara);
                                }
                                break;
                            case MSG_WHAT_OPT_READ_MM:
                                if (mBeaconMMChara != null && mBluetoothGatt != null) {
                                    Log.e("BleCallback", "读MM指令");
                                    mBluetoothGatt.readCharacteristic(mBeaconMMChara);
                                }
                                break;
                            case MSG_WHAT_OPT_READ_INTERVAL:
                                if (mBeaconInterval != null && mBluetoothGatt != null) {
                                    Log.e("BleCallback", "读间隔指令");
                                    mBluetoothGatt.readCharacteristic(mBeaconInterval);
                                }
                                break;
                            case MSG_WHAT_OPT_WRITE_INTERVAL:
                                if (mBeaconInterval != null && mBluetoothGatt != null) {
                                    mBeaconInterval.setValue((Integer) msg.obj, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                                    Log.e("BleCallback", "写间隔指令");
                                    mBluetoothGatt.writeCharacteristic(mBeaconInterval);
                                }
                                break;
                            case MSG_WHAT_OPT_WRITE_MM:
                                if (mBeaconMMChara != null && mBluetoothGatt != null) {
                                    mBeaconMMChara.setValue((Integer) msg.obj, BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                                    Log.e("BleCallback", "写MM指令");
                                    mBluetoothGatt.writeCharacteristic(mBeaconMMChara);
                                }
                                break;
                            case MSG_WHAT_OPT_WRITE_NAME:
                                if (mBeaconNameChara != null && mBluetoothGatt != null) {
                                    mBeaconNameChara.setValue((String) msg.obj);
                                    Log.e("BleCallback", "写名字指令");
                                    mBluetoothGatt.writeCharacteristic(mBeaconNameChara);
                                }

                                break;
                            case MSG_WHAT_OPT_WRITE_TXPOWER:
                                if (mTxPowerChara != null && mBluetoothGatt != null) {
                                    mTxPowerChara.setValue(new byte[]{(Byte) msg.obj});
                                    Log.e("BleCallback", "写txpwer指令");
                                    mBluetoothGatt.writeCharacteristic(mTxPowerChara);
                                }

                                break;
                            case MSG_WHAT_OPT_WRITE_TYPE:
                                if (mBeaconTypeChara != null && mBluetoothGatt != null) {
                                    mBeaconTypeChara.setValue((Integer) msg.obj, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                                    Log.e("BleCallback", "写类型指令");
                                    mBluetoothGatt.writeCharacteristic(mBeaconTypeChara);
                                }

                                break;
                            case MSG_WHAT_CALLBACK_READ_NAME:
                                if (mCallback != null) {
                                    Log.e("BleCallback", "读名字回调");
                                    mCallback.onReadName((String) msg.obj);
                                }
                                break;
                            case MSG_WHAT_CALLBACK_READ_TXPOWER:
                                if (mCallback != null) {
                                    Log.e("BleCallback", "读txpower回调");
                                    mCallback.onReadTxPower((Integer) msg.obj);
                                }
                                break;
                            case MSG_WHAT_CALLBACK_READ_TYPE:
                                if (mCallback != null) {
                                    Log.e("BleCallback", "读类型回调");
                                    mCallback.onReadType((Integer) msg.obj);
                                }
                                break;
                            case MSG_WHAT_CALLBACK_READ_MM:
                                if (mCallback != null) {
                                    Log.e("BleCallback", "读MM回调");
                                    mCallback.onReadMM((Integer) msg.obj);
                                }
                                break;
                            case MSG_WHAT_CALLBACK_WRITE_MM:
                                if (mCallback != null) {
                                    Log.e("BleCallback", "写MM回调");
                                    mCallback.onWriteMM((Integer) msg.obj);
                                }
                                break;
                            case MSG_WHAT_CALLBACK_WRITE_NAME:
                                if (mCallback != null) {
                                    Log.e("BleCallback", "写名字回调");
                                    mCallback.onWriteName((String) msg.obj);
                                }
                                break;
                            case MSG_WHAT_CALLBACK_WRITE_TXPOWER:
                                if (mCallback != null) {
                                    Log.e("BleCallback", "写txpower回调");
                                    mCallback.onWriteTxPower((Integer) msg.obj);
                                }
                                break;
                            case MSG_WHAT_CALLBACK_WRITE_TYPE:
                                if (mCallback != null) {
                                    Log.e("BleCallback", "写type回调");
                                    mCallback.onWriteType((Integer) msg.obj);
                                }
                                break;
                            case MSG_WHAT_CALLBACK_WRITE_INTERVAL:
                                if (mCallback != null) {
                                    Log.e("BleCallback", "写间隔回调");
                                    mCallback.onWriteInterval((Integer) msg.obj);
                                }
                                break;
                            case MSG_WHAT_CALLBACK_READ_INTERVAL:
                                if (mCallback != null) {
                                    Log.e("BleCallback", "读间隔回调");
                                    mCallback.onReadInterval((Integer) msg.obj);
                                }
                                break;
                        }
                    }

                }

            };

            UIUtils.postTaskSafely(new Runnable() {
                @Override
                public void run() {
                    //连接成功
                    if (mCallback != null) {
                        mCallback.onConnectSuccess();
                    }
                }
            });
            Looper.loop();
        }
    }

    public static int parseMM(int minor, int major) {
        return ((minor >> 8 | (minor & 0xff) << 8) << 16) | (major >> 8 | (major & 0xff) << 8);
    }

    public static int[] parseMM(int mm) {
        int[] mmArr = new int[2];
        mmArr[0] = ((mm & 0xff) << 8) | ((mm & 0xff00) >> 8);
        mmArr[1] = ((mm & 0xff0000) >> 8) | ((mm & 0xff000000) >> 24);
        return mmArr;
    }

}
