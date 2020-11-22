package mili.wifiscanner;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION_CODE = 2;

    private TextView mScanTextView;
    private RecyclerView mRecyclerView;
    private ScanAdapter mAdapter;
    private MaterialButtonToggleGroup mTypeToggle;
    private ChipGroup mRoomToggle;
    private static CharSequence mDataType = "unknown";
    private static int mRoomNum = 4;
    private static CharSequence mRoomID = "unknown";

    private WifiManager mWifiManager;
    private WifiScanReceiver mWifiScanReceiver;
    List<ScanResult> mAccessPoints;

    private Handler mHandler;
    private Runnable mRunnable;
    private static int mDelay = 30000; // 1000 milliseconds == 1 second
    private static boolean mScanStarted = false;

    private DataWriter mDataWriter;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("MyViewText", mScanTextView.getText());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScanTextView = findViewById(R.id.scan_text_view);
        mScanTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mScanStarted) {
                    mScanStarted = !mScanStarted;
                    Log.d(TAG,  "Start scanning...");
                    startWifiScanner();

                } else {
                    mScanStarted = !mScanStarted;
                    Log.d(TAG, "Stop scanning...");
                    stopWifiScanner();
                }
            }
        });

        mTypeToggle = findViewById(R.id.type_toggle);
        mTypeToggle.check(R.id.train_button);
        mTypeToggle.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                Button checkedButton = findViewById(checkedId);
                checkedButton.playSoundEffect(SoundEffectConstants.CLICK);
                mDataType = checkedButton.getText();
                Log.d(TAG, "Collecting " + mDataType + " data...");
            }
        });

        mRoomToggle = findViewById(R.id.room_toggle);
        createRoomChips();
        mRoomToggle.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                Chip checkedChip = findViewById(checkedId);
                mRoomID = checkedChip.getText();
                Log.d(TAG, "Collecting room " + mRoomID + " data...");
            }
        });

        ActivityCompat.requestPermissions(
                this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSION_CODE);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiScanReceiver = new WifiScanReceiver();

        mAccessPoints = new ArrayList<>();
        mAdapter = new ScanAdapter(mAccessPoints);

        mRecyclerView = findViewById(R.id.access_point_information_recycler_view);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        mHandler = new Handler();
        mRunnable = new Runnable() {
            public void run() {
                if(mScanStarted) {
                    Log.d(TAG, "Scan once...");
                    logToUi(getString(R.string.retrieving_access_points));
                    mWifiManager.startScan();
                    mHandler.postDelayed(this, mDelay);
                } else {
                    stopWifiScanner();
                }
            }
        };


    }

    private void startWifiScanner() {
        if (mRoomToggle.getCheckedChipId() == ChipGroup.NO_ID) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage(R.string.start_warning)
                            .setPositiveButton("OK", null)
                            .show();
                }
            });
            mScanTextView.callOnClick();
        } else {
            mDataWriter = new DataWriter(mDataType, getString(R.string.app_name));
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage(mDataWriter.info())
                            .setPositiveButton("OK", null)
                            .show();
                }
            });
            mHandler.post(mRunnable);
        }
    }

    void stopWifiScanner() {
        mHandler.removeCallbacks(null);
        logToUi(getString(R.string.start_scan_info));
        Log.d(TAG, "Scan stopped");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mScanTextView.setText(savedInstanceState.getCharSequence("MyViewText"));
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        registerReceiver(
                mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mAdapter.swapData(mAccessPoints);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
        unregisterReceiver(mWifiScanReceiver);
    }

    private class WifiScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mScanStarted) {
                mAccessPoints = mWifiManager.getScanResults();
                mAdapter.swapData(mAccessPoints);
                mAdapter.notifyDataSetChanged();
                logToUi( getString(R.string.stop_scan_info)
                        + "\n" + mAccessPoints.size()
                        + " APs discovered.\nCurrent scan delay: "
                        + (mDelay/1000) + "s"
                );
                mDataWriter.writeToFiles(mRoomID, mAccessPoints);
            }
        }
    }

    private void logToUi(final String message) {
        if (!message.isEmpty()) {
            Log.d(TAG, message);
            mScanTextView.setText(message);
        }
    }

    private void createRoomChips() {
        for (int i = 0; i<mRoomNum; i++) {
            Chip chip = new Chip(mRoomToggle.getContext());
            chip.setText(String.valueOf(i+1));
            chip.setCheckable(true);
            mRoomToggle.addView(chip);
        }
    }

}