package com.ccl.bletools;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ccl.bletools.bt.BLEOperation;
import com.ccl.bletools.bt.BTBeacon;
import com.ccl.bletools.bt.Blecallback;
import com.ccl.bletools.utils.Messager;
import com.ccl.bletools.utils.UIUtils;
import com.ccl.bletools.view.BtItemView;

public class ConnectBTActivity extends AppCompatActivity {

    private BtItemView mBtItemView;
    private BLEOperation mBleOperation;
    private EditText mEt;
    private SeekBar mSbTxPower;
    private TextView mTvTxPower;
    private SeekBar mSbInterval;
    private TextView mTvInterval;
    private Button mBtnPaser;
    private Button mBtnWrite;
    private Button mBtnSmart;
    private Button mBtnIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_connect_bt);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_bt_activity));
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(true);
        supportActionBar.setTitle("");
        initView();
        initData();
    }

    private void initView() {
        mBtItemView = (BtItemView) findViewById(R.id.bt_item_view);
        mBtItemView.findViewById(R.id.btn).setEnabled(false);
        mEt = (EditText) findViewById(R.id.et);
        mBtItemView.setConnect(true);
        mSbTxPower = (SeekBar) findViewById(R.id.sb_txpower);
        mTvTxPower = (TextView) findViewById(R.id.tv_txpower);
        mSbInterval = (SeekBar) findViewById(R.id.sb_space);
        mTvInterval = (TextView) findViewById(R.id.tv_space);
        mBtnPaser = (Button) findViewById(R.id.btn_paser);
        mBtnWrite = (Button) findViewById(R.id.btn_write);
        mBtnSmart = (Button) findViewById(R.id.btn_smart);
        mBtnIntent = (Button) findViewById(R.id.btn_intent);
        mBtItemView.setBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String trim = (String) v.getTag();
                if (!TextUtils.isEmpty(trim)) {
                    try {
                        String[] split = trim.split(":");
                        int length = split.length;
                        String s = split[length - 1] + split[length - 2] + split[length - 3] + split[length - 4];
                        int mm = (int) Long.parseLong(s, 16);
                        mBleOperation.writeMM(mm);
                        return;
                    } catch (Exception e) {
                        Log.e("write", e.toString());
                    }
                }
                Messager.Show("写入失败", Toast.LENGTH_SHORT);
            }
        });
    }

    private void initData() {
        mBleOperation = BLEOperation.GetInstance();
        BTBeacon bTbeacon = mBleOperation.getBTbeacon();
        mBtItemView.setType(bTbeacon.getType());
        mBtItemView.setMac(bTbeacon.getiDevAddress());
        mBtItemView.setMM(bTbeacon.getMajor(), bTbeacon.getMinor());
        mBtItemView.setData(bTbeacon.getMeasuredPower(), bTbeacon.getBattery(), bTbeacon.getRssi());
        mBleOperation.setCallback(mBlecallback);
        mTvTxPower.setText("TxPower: "+bTbeacon.getMeasuredPower());
        mSbTxPower.setProgress(bTbeacon.getMeasuredPower());

        mSbTxPower.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mBleOperation.writeTxPower((byte) progress);
                }
                mTvTxPower.setText("TxPower: "+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSbInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mBleOperation.writeInterval((progress+1)*100);
                }
                mTvInterval.setText("广播间隔: "+(progress + 1)*100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        boolean result = mBleOperation.connectGatt();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            supportFinishAfterTransition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void paste(View view) {
        ClipboardManager myClipboard = (ClipboardManager) UIUtils.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData abc = myClipboard.getPrimaryClip();
        ClipData.Item item = abc.getItemAt(0);
        String text = item.getText().toString();
        mEt.setText(text);
        mBleOperation.readMM();
    }

    public void write(View view) {
        String trim = mEt.getText().toString().trim();
        if (!TextUtils.isEmpty(trim)) {
            try {
                String[] split = trim.split(":");
                int length = split.length;
                String s = split[length - 1] + split[length - 2] + split[length - 3] + split[length - 4];
                int mm = (int) Long.parseLong(s, 16);
                mBleOperation.writeMM(mm);
                return;
            } catch (Exception e) {
                Log.e("write", e.toString());
            }
        }
        Messager.Show("写入失败", Toast.LENGTH_SHORT);
    }

    public void smart(View view) {
        if (mBleOperation.checkType(BLEOperation.BEACON_TYPE_LOCATE)) {
            Messager.Show("写入成功");
        }
    }

    public void intent(View view) {
        if (mBleOperation.checkType(BLEOperation.BEACON_TYPE_ORIENT)) {
            Messager.Show("写入成功");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBleOperation.destroy();
    }

    private Blecallback mBlecallback = new Blecallback() {

        @Override
        public void onConnectSuccess() {
            Log.e("BleCallback", "Outside onConnectSuccess");
            Messager.Show("连接成功");
            mBtItemView.findViewById(R.id.btn).setEnabled(true);
            mBtnIntent.setEnabled(true);
            mBtnPaser.setEnabled(true);
            mBtnSmart.setEnabled(true);
            mBtnWrite.setEnabled(true);
            mSbInterval.setEnabled(true);
            mSbTxPower.setEnabled(true);
            mBleOperation.readInterval();
        }

        @Override
        public void onConnectFaild() {
            Log.e("BleCallback", "Outside onConnectFaild");
            Messager.Show("连接失败");
        }

        @Override
        public void onWriteMM(int mm) {
            Log.e("BleCallback", "Outside onWriteMM: " + mm + ", " + Integer.toHexString(mm));
            Messager.Show("写入MM成功");
        }

        @Override
        public void onReadMM(int mm) {
            Log.e("BleCallback", "Outside onReadMM: " + mm + ", " + Integer.toHexString(mm));
        }

        @Override
        public void onWriteType(int type) {
            super.onWriteType(type);
            Messager.Show("写入Type成功");
        }

        @Override
        public void onReadInterval(final int interval) {
            super.onReadInterval(interval);
            UIUtils.postTaskSafely(new Runnable() {
                @Override
                public void run() {
                    mSbInterval.setProgress(interval/100 - 1);
                    mTvInterval.setText("广播间隔: "+ interval);
                }
            });
        }

        @Override
        public void onWriteInterval(int interval) {
            super.onWriteInterval(interval);
            Messager.Show("写入间隔成功");
        }

        @Override
        public void onWriteTxPower(int txpower) {
            super.onWriteTxPower(txpower);
            Messager.Show("写入TxPower成功");
        }
    };
}
