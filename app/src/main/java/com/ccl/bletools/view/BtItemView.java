package com.ccl.bletools.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ccl.bletools.R;
import com.ccl.bletools.utils.UIUtils;

public class BtItemView extends FrameLayout {

    private TextView mTvType;
    private TextView mTvMac;
    private TextView mTvMM;
    private TextView mTvData;

    private boolean isConnecct = false;
    private Button mBtn;
    private SignalView mSignalView;

    public BtItemView(Context context) {
        this(context, null);
    }

    public BtItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BtItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.item_bt_cardview, this);
        this.setBackgroundColor(Color.TRANSPARENT);
        mTvType = (TextView) findViewById(R.id.tv_cv_type);
        mTvMac = (TextView) findViewById(R.id.tv_cv_mac);
        mTvMM = (TextView) findViewById(R.id.tv_cv_mm);
        mTvData = (TextView) findViewById(R.id.tv_cv_data);
        mBtn = (Button) findViewById(R.id.btn);
        mSignalView = (SignalView) findViewById(R.id.sv_signal);
        if (Build.VERSION.SDK_INT >= 21) {
            setTransitionName(UIUtils.getString(R.string.transition_name));
        }
    }

    public void setBtnClickListener(View.OnClickListener listener){
        if(listener != null){
            mBtn.setOnClickListener(listener);
        }
    }

    public void setConnect(boolean connect){
        isConnecct = connect;
        if(isConnecct){
            mBtn.setText("还原");
        }else{
            mBtn.setText("复制");
        }
    }

    public void setType(String type) {
        mTvType.setText("类型: " + type);
    }

    public void setMac(String mac) {
        mBtn.setTag(mac);
        mTvMac.setText("Mac: " + mac);
    }

    public void setMM(short major, short minor) {
        mTvMM.setText("Major: " + (major & 0xffff) + "  Minor: " + (minor & 0xffff)+" --- "+minor);
    }

    public void setData(byte txpower, byte battery, int rssi) {
        mTvData.setText("TxPower: " + txpower + "  电量: " + battery + "  Rssi: " + rssi);
        mSignalView.setSignal(rssi);
    }

}
