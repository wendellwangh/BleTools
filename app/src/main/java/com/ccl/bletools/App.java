package com.ccl.bletools;


import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.ccl.bletools.utils.Messager;

public class App extends Application {
    private static Context mContext;
    private static Handler mHandler;
    private static long mMainThreadId;

    public static Context getContext(){
        return mContext;
    }

    public static Handler getHandler(){
        return mHandler;
    }

    public static long getMainThreadId(){
        return mMainThreadId;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mHandler = new Handler();
        mMainThreadId = android.os.Process.myTid();
        Messager.Initialize(getContext());
    }

}
