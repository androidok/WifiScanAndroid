package mili.wifiscanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION_CODE = 2;
    private Handler mHandler;
    private Runnable mRunnable;
    private TextView mOutputTextView;
    private RecyclerView mRecyclerView;
    private ScanAdapter mAdapter;

    int delay = 30000; // 1000 milliseconds == 1 second
    private WifiManager mWifiManager;
    private WifiScanReceiver mWifiScanReceiver;
    List<ScanResult> mAccessPoints;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("myViewTag", (int) mOutputTextView.getTag());
        outState.putCharSequence("MyViewText", mOutputTextView.getText());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOutputTextView = findViewById(R.id.access_point_summary_text_view);
        mOutputTextView.setTag(1);
        mOutputTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int status =(Integer) v.getTag();
                if(status == 1) {

                    v.setTag(0); //pause
                    Log.i(TAG, v.getTag() + ": start scanning...");
                    logToUi(getString(R.string.retrieving_access_points));
                    mWifiManager.startScan();
                    startWifiScanner();

                } else {

                    v.setTag(1);
                    Log.i(TAG, v.getTag() + ": stop scanning...");
                    logToUi(getString(R.string.click_info));
                    stopWifiScanner();
                }

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
                Log.i(TAG, "scan once...");
                logToUi(getString(R.string.retrieving_access_points));
                mWifiManager.startScan();
                mHandler.postDelayed(mRunnable, delay);
            }
        };
    }

    private void startWifiScanner() {
        mHandler.postDelayed(mRunnable, delay);
    }

    void stopWifiScanner() {
        mHandler.removeCallbacks(null);
        Log.i(TAG, "Scan stopped");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mOutputTextView.setText(savedInstanceState.getCharSequence("MyViewText"));
        mOutputTextView.setTag("MyViewTag");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        registerReceiver(
                mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
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
            mAccessPoints = mWifiManager.getScanResults();
            mAdapter.swapData(mAccessPoints);
            mAdapter.notifyDataSetChanged();
            logToUi(mAccessPoints.size() + " APs discovered.");
        }
    }

    private void logToUi(final String message) {
        if (!message.isEmpty()) {
            Log.d(TAG, message);
            mOutputTextView.setText(message);
        }
    }
}