package com.ccl.bletools;

import android.Manifest;
import android.animation.Animator;
import android.app.ActivityOptions;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.widget.Toast;

import com.ccl.bletools.bt.BLEOperation;
import com.ccl.bletools.bt.BTBeacon;
import com.ccl.bletools.bt.BTBeaconMonitor;
import com.ccl.bletools.utils.UIUtils;
import com.ccl.bletools.view.BTContainerView;

public class MainActivity extends AppCompatActivity {

    private BTContainerView mScrollView;
    private SwipeRefreshLayout mRefreshLayout;
    private CardView mCardViewFilter;
    private Animator mFilterViewHideAnimator;
    private Animator mFilterViewShowAnimator;
    private BluetoothAdapter mAdapter;

    private boolean inited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mScrollView = (BTContainerView) findViewById(R.id.scrollview);
        mScrollView.setOnItemClickListener(mOnClickListener);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mCardViewFilter = (CardView) findViewById(R.id.cardview_filter);
    }

    private void initData() {
        BTBeaconMonitor.Initialize(this);
    }

    private void initListener() {
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScrollView.refresh();
                mRefreshLayout.setRefreshing(false);
            }
        });

        mRefreshLayout.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 26 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    11);
        } else {
            checkBle();
        }
    }

    private void checkBle() {
        IntentFilter iFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        getBaseContext().registerReceiver(mReceiver, iFilter);
        inited = true;
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mAdapter = bluetoothManager.getAdapter();
        if (mAdapter != null) {
            if (mAdapter.getState() == BluetoothAdapter.STATE_ON) {
                mScrollView.startScanBt();
            } else if (mAdapter.getState() != BluetoothAdapter.STATE_TURNING_ON || mAdapter.getState() != BluetoothAdapter.STATE_ON) {
                //开启蓝牙
                mAdapter.enable();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 11: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    checkBle();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplication(), "权限被拒绝", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (inited) {
            unregisterReceiver(mReceiver);
            mScrollView.stopScanBt();
        }
        inited = false;
    }

    private void changeFilterViewState() {
        if (mCardViewFilter.getVisibility() != View.VISIBLE) {
            if (Build.VERSION.SDK_INT >= 21) {
                mCardViewFilter.setVisibility(View.VISIBLE);
                mFilterViewHideAnimator = ViewAnimationUtils.createCircularReveal(
                        mCardViewFilter,
                        0,
                        0,
                        0,
                        (float) Math.hypot(mCardViewFilter.getWidth(), mCardViewFilter.getHeight()));
                mFilterViewHideAnimator.setInterpolator(new AccelerateInterpolator());
                mFilterViewHideAnimator.setDuration(500);
                mFilterViewHideAnimator.start();
            } else {
                mCardViewFilter.setVisibility(View.VISIBLE);
            }
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mFilterViewShowAnimator = ViewAnimationUtils.createCircularReveal(
                        mCardViewFilter,
                        0,
                        0,
                        (float) Math.hypot(mCardViewFilter.getWidth(), mCardViewFilter.getHeight()),
                        0);
                mFilterViewShowAnimator.setInterpolator(new AccelerateInterpolator());
                mFilterViewShowAnimator.setDuration(500);
                mFilterViewShowAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mCardViewFilter.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                mFilterViewShowAnimator.start();
            } else {
                mCardViewFilter.setVisibility(View.GONE);
            }
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            BLEOperation operation = BLEOperation.GetInstance();
            BTBeacon btBeacon = (BTBeacon) v.getTag();
            operation.setOperationBle(mAdapter.getRemoteDevice(btBeacon.getiDevAddress()), btBeacon);
            Intent intent = new Intent(getBaseContext(), ConnectBTActivity.class);
            if (Build.VERSION.SDK_INT >= 21) {
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, v, UIUtils.getString(R.string.transition_name)).toBundle();
                startActivity(intent, bundle);
            } else {
                startActivity(intent);
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == BluetoothAdapter.ACTION_STATE_CHANGED) {
                switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.e("TNAME", "STATE_TURNING_ON");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.e("TNAME", "STATE_ON");
                        mScrollView.startScanBt();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.e("TNAME", "STATE_TURNING_OFF");
                        mScrollView.stopScanBt();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.e("TNAME", "STATE_OFF");
                        break;
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            mRefreshLayout.setRefreshing(true);
            mScrollView.refresh();
            mRefreshLayout.setRefreshing(false);
            return true;
        } else if (id == R.id.action_filter) {
            changeFilterViewState();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
