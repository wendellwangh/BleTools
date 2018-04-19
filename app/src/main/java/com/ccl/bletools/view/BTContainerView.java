package com.ccl.bletools.view;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ccl.bletools.bt.BTBeacon;
import com.ccl.bletools.bt.BTBeaconMonitor;
import com.ccl.bletools.utils.UIUtils;

import java.util.HashMap;

public class BTContainerView extends NestedScrollView {
    HashMap<String, BtItemView> mAllItems;
    private LinearLayout mLinearLayout;
    private OnClickListener mActivityListener;

    public BTContainerView(Context context) {
        this(context, null);
    }

    public BTContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BTContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initView();
    }

    private void init() {
        mAllItems = new HashMap<>();
    }

    private void initView() {
        mLinearLayout = new LinearLayout(getContext());
        mLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        this.addView(mLinearLayout);
    }

    public void setOnItemClickListener(OnClickListener clickListener){
        mActivityListener = clickListener;
    }

    public void refresh(){
        mAllItems.clear();
        mLinearLayout.removeAllViews();
    }

    private BTBeaconMonitor.BTBeaconListener mBeaconListener = new BTBeaconMonitor.BTBeaconListener() {
        @Override
        public void onBeaconStateChange(BTBeaconMonitor aMonitor, BTBeacon aBeacon) {
            BtItemView btItemView = mAllItems.get(aBeacon.getiDevAddress());
            if(btItemView == null){
                BtItemView itemView = buildItemView(aBeacon);
                mAllItems.put(aBeacon.getiDevAddress(), itemView);
            }else{
                btItemView.setTag(aBeacon);
                btItemView.setType(aBeacon.getType());
                btItemView.setMM(aBeacon.getMajor(), aBeacon.getMinor());
                btItemView.setData(aBeacon.getMeasuredPower(), aBeacon.getBattery(), aBeacon.getRssi());
            }
        }
    };

    private BtItemView buildItemView(BTBeacon beacon){
        BtItemView itemView = new BtItemView(getContext());
        itemView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UIUtils.dp2px(72)));
        itemView.setOnClickListener(mOnClickListener);
        mLinearLayout.addView(itemView);
        itemView.setTag(beacon);
        itemView.setType(beacon.getType());
        itemView.setMac(beacon.getiDevAddress());
        itemView.setMM(beacon.getMajor(), beacon.getMinor());
        itemView.setData(beacon.getMeasuredPower(), beacon.getBattery(), beacon.getRssi());
        itemView.setBtnClickListener(mBtnClickListener);
        return itemView;
    }

    private OnClickListener mBtnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String mac = (String) v.getTag();
            ClipboardManager myClipboard = (ClipboardManager)UIUtils.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData myClip = ClipData.newPlainText("text", mac);
            myClipboard.setPrimaryClip(myClip);
        }
    };

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mActivityListener != null){
                mActivityListener.onClick(v);
            }
        }
    };

    public void startScanBt(){
        BTBeaconMonitor.AddBTDeviceListener(mBeaconListener);
    }

    public void stopScanBt(){
        BTBeaconMonitor.RemoveBTDeviceListener(mBeaconListener);
    }
}
