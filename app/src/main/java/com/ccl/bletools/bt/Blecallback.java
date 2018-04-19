package com.ccl.bletools.bt;

public class Blecallback{
    /**
     * 连接成功
     */
    public void onConnectSuccess(){}

    /**
     * 连接失败
     */
    public void onConnectFaild(){}

    /**
     * 发现服务
     */
    public void onServiceDiscover(){}

    /**
     * 写入密码成功
     */
    public void onWritePwd(){}

    /**
     * 当读取到类型
     * @param type
     */
    public void onReadType(int type){}

    /**
     * 当写入类型
     * @param type
     */
    public void onWriteType(int type){}

    /**
     * 当读取到beacon的名字
     * @param name
     */
    public void onReadName(String name){}

    /**
     * 当写入beacon的名字
     * @param name
     */
    public void onWriteName(String name){}

    /**
     *当读取到TxPower
     * @param txpower
     */
    public void onReadTxPower(int txpower){}

    /**
     * 当写入TxPower
     * @param txpower
     */
    public void onWriteTxPower(int txpower){}

    /**
     * 当写入major和minor
     * @param mm
     */
    public void onWriteMM(int mm){

    }

    /**
     * 当读取到major和minor
     * @param mm
     */
    public void onReadMM(int mm){

    }

    /**
     * 读取Rssi
     * @param rssi
     */
    public void onReadRssi(int rssi){}


    /**
     * 读取间隔
     * @param interval
     */
    public void onReadInterval(int interval){}

    /**
     * 写入间隔
     * @param interval
     */
    public void onWriteInterval(int interval){}

}
