package mili.wifiscanner;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION_CODE = 2;

    private TextView mScanTextView;

    private MaterialButtonToggleGroup mTypeToggle;
    private ChipGroup mRoomToggle;
    private Chip mUnknownChip;
    private static CharSequence mDataType = "unknown";
    private static int mRoomNum = 5;
    private static CharSequence mRoomID = "unknown";
    private static double mUserInput = 0;
    private static int mSettingID = -1;

    private WifiManager mWifiManager;
    private WifiScanReceiver mWifiScanReceiver;
    public List<ScanResult> mAccessPoints;

    private Handler mHandler;
    private Runnable mRunnable;
    private static int mInterval = 5000; // 1000 milliseconds == 1 second
    private static boolean mScanStarted = false;

    private DataWriter mDataWriter;

    public static String[] mSortedBssid;
    public static List<String> mRooms;
    private static final int mMinRSSI = -100;
    private static String mSystemPath;
    private boolean mPredictMode = false;
    private Instances mDataUnpredicted;
    private int[] mRSSI;
    private static Classifier mClassifier = null;

//    private MyCanvasView myCanvasView;


    private ChartFragment mChartFragment;
    private RecyclerFragment mRecyclerFragment;
    private boolean mRecyclerShown = true;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("MyViewText", mScanTextView.getText());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mChartFragment = new ChartFragment();
        mRecyclerFragment = new RecyclerFragment();
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                .add(R.id.fragment_container_view, mRecyclerFragment, null)
                .commit();

//        myCanvasView = findViewById(R.id.canvas_view);

        mScanTextView = findViewById(R.id.scan_text_view);
        mScanTextView.setOnClickListener(v -> {
            mScanStarted = !mScanStarted;
            if (mScanStarted) {
                Log.d(TAG, "Start scanning...");
                startWifiScanner();
            } else {
                Log.d(TAG, "Stop scanning...");
                stopWifiScanner();
            }
        });

        mTypeToggle = findViewById(R.id.type_toggle);
        mTypeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            Button checkedButton = findViewById(checkedId);
            checkedButton.playSoundEffect(SoundEffectConstants.CLICK);
            mDataType = checkedButton.getText();
            Log.d(TAG, "Collecting " + mDataType + " data...");
        });
        mTypeToggle.check(R.id.train_button);

        mRoomToggle = findViewById(R.id.room_toggle);
        createRoomChips();
        mRoomToggle.setOnCheckedChangeListener((group, checkedId) -> {
            Chip checkedChip = findViewById(checkedId);
            mRoomID = checkedChip.getText();
//                myCanvasView.setImage(checkedId-1);
            Log.d(TAG, "Collecting room " + mRoomID + " data...");
        });

        ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSION_CODE);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiScanReceiver = new WifiScanReceiver();

        mAccessPoints = new ArrayList<>();


        mHandler = new Handler();
        mRunnable = new Runnable() {
            public void run() {
                if (mScanStarted) {
                    Log.d(TAG, "Scan once...");
                    logToUi(mAccessPoints.size()
                            + " APs discovered.\n" + getString(R.string.retrieving_access_points));
                    mWifiManager.startScan();
                    mHandler.postDelayed(this, mInterval);
                } else {
                    stopWifiScanner();
                }
            }
        };



        mSortedBssid = getResources().getStringArray(R.array.sorted_bssid);
        mRooms = Arrays.asList(getResources().getStringArray(R.array.rooms));
        mSystemPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + getString(R.string.app_name) + "/";
        Log.d(TAG, Arrays.asList(mSortedBssid).toString());
        Log.d(TAG, mRooms.toString());


        AssetManager assetManager = getAssets();
        try {
            mClassifier = (Classifier) weka.core.SerializationHelper.read(assetManager.open("android_rnd.model"));
            Log.d(TAG, "Model loaded.");
            Toast.makeText(this, "Model loaded.", Toast.LENGTH_SHORT).show();
            mPredictMode = true;
            // Instances(...) requires ArrayList<> instead of List<>...
            ArrayList<Attribute> attributeList = new ArrayList<Attribute>(2) {
                {
                    for (String bssid : mSortedBssid) {
                        add(new Attribute(bssid));
                    }
                    Attribute attributeClass = new Attribute("@@class@@", mRooms);
                    add(attributeClass);
                }
            };
            // unpredicted data sets (reference to sample structure for new instances)
            mDataUnpredicted = new Instances("TestInstances",
                    attributeList, 1);
            // last feature is target variable
            mDataUnpredicted.setClassIndex(mDataUnpredicted.numAttributes() - 1);
            Log.d(TAG, mDataUnpredicted.toSummaryString());

        } catch (Exception e) {
            Log.d(TAG, "Model not found.");
            e.printStackTrace();
        }


    }



    private void startWifiScanner() {
        if (mRoomToggle.getCheckedChipId() == ChipGroup.NO_ID) {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(R.string.start_warning)
                    .setPositiveButton(R.string.dialog_positive, null)
                    .show();
            mScanTextView.callOnClick();
        } else {
            mDataWriter = new DataWriter(mDataType, getString(R.string.app_name));
            if (mDataType == getString(R.string.train_text)) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(mDataWriter.info())
                        .setPositiveButton(R.string.dialog_positive, null)
                        .show();
                if (!mRecyclerShown) {
                    DataWriter pathWriter = new DataWriter("path",  getString(R.string.app_name));
                    pathWriter.writePath(mChartFragment.getPath());
                }
            }
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
        if (mRecyclerShown) {
            mRecyclerFragment.update();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
        unregisterReceiver(mWifiScanReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_draw) {
            if (!mRecyclerShown) {
                // logToUi("Let's draw");
                mChartFragment.setMode();
            }
        } else if (id == R.id.action_fragment) {
            if (mRecyclerShown) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_view, mChartFragment)
                        .commit();
                mTypeToggle.setVisibility(View.GONE);
                mRoomToggle.check(mUnknownChip.getId());
                mRoomToggle.setVisibility(View.GONE);
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_view, mRecyclerFragment)
                        .commit();
                mTypeToggle.setVisibility(View.VISIBLE);
                mRoomToggle.setVisibility(View.VISIBLE);
            }
            mRecyclerShown = !mRecyclerShown;
        } else if (id == R.id.action_settings) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.pick_setting_item)
                    .setItems(R.array.items_array, (dialog, which) -> {
                        mSettingID = which;
                        changeSettingItem();
                    }).show();
        } else if (id == R.id.action_help) {
            String message =  String.format(getString(R.string.help_info), mRoomNum, mInterval/1000.0);
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(message)
                    .setPositiveButton(R.string.dialog_positive, null)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }



    private class WifiScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mScanStarted) {
                mAccessPoints = mWifiManager.getScanResults();
                if (mRecyclerShown) {
                    mRecyclerFragment.update();
                }
                logToUi(getString(R.string.stop_scan_info)
                        + "\nCurrent scan delay: "
                        + (mInterval / 1000.0) + " s"
                );
                mDataWriter.writeToFiles(mRoomID, mAccessPoints);

                if (mDataType.equals(getString(R.string.test_text)) && mPredictMode) {
                    mRSSI = new int[mSortedBssid.length];
                    for (int i = 0; i<mRSSI.length; i++) {
                        mRSSI[i] = mMinRSSI;
                        for (int j = 0; j<mAccessPoints.size(); j++) {
                            if (mSortedBssid[i].equals(mAccessPoints.get(j).BSSID)) {
                                mRSSI[i] = mAccessPoints.get(j).level;
                                break;
                            }
                        }
                    }
                    DenseInstance newInstance = new DenseInstance(mDataUnpredicted.numAttributes()) {
                        {
                            int i= 0;
                            for (String bssid : mSortedBssid) {
                                setValue(mDataUnpredicted.attribute(bssid), mRSSI[i]);
                                i++;
                            }
                        }
                    };
                    // reference to dataset
                    newInstance.setDataset(mDataUnpredicted);
                    Log.d(TAG, newInstance.toStringNoWeight());
                    try {
                        Double result = mClassifier.classifyInstance(newInstance);
                        // String className = mRooms.get(result.intValue());
                        // String msg = "predicted: " + className + ", actual: room" + mRoomID;
                        String msg = "predicted: " + mRooms.get(result.intValue());
                        logToUi(msg);
                    } catch (Exception e) {
                        Log.d(TAG, "prediction failed");
                        e.printStackTrace();
                    }
                }
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
        int childCount = mRoomToggle.getChildCount();
        Log.d(TAG, childCount + " children");
        for (int i = childCount - 1; i > 0; i--) {
            View currentChild = mRoomToggle.getChildAt(i);
            // Change ImageView with your desired type view
            if (currentChild instanceof Chip) {
                mRoomToggle.removeView(currentChild);
            }
        }

        for (int i = 1; i < mRoomNum+1; i++) {
            Chip chip = new Chip(mRoomToggle.getContext());
            chip.setText(String.valueOf(i));
            chip.setCheckable(true);
            mRoomToggle.addView(chip);
        }

        mUnknownChip = new Chip(mRoomToggle.getContext());
        mUnknownChip.setText("unknown");
        mUnknownChip.setCheckable(true);
        mRoomToggle.addView(mUnknownChip);
    }

    private void changeSettingItem() {
        String[] items = getResources().getStringArray(R.array.items_array);
        String settingItem = items[mSettingID];
        Log.d(TAG, settingItem + " is chosen");
        // get dialog_setting layout view
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View dialogView = inflater.inflate(R.layout.dialog_setting, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(dialogView);

        final TextView textView = dialogView
                .findViewById(R.id.setting_item_text_view);
        textView.setText(settingItem);


        final EditText userInput = (EditText) dialogView
                .findViewById(R.id.setting_item_text_edit);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_positive,
                        (dialog, id) -> {
                            String input = userInput.getText().toString();
                            if (input.isEmpty()) {
                                dialog.cancel();
                            } else {
                                mUserInput = Double.parseDouble(input);
                                Log.d(TAG, "User Input Value: " + mUserInput);
                                if (mSettingID == 0) {
                                    mRoomNum = (int) mUserInput;
                                    createRoomChips();
                                } else if (mSettingID == 1) {
                                    mInterval = (int) (mUserInput * 1000);
                                    mHandler.removeCallbacks(null);
                                    Log.d(TAG, "User Input Value: " + mInterval);
                                }
                            }
                        })
                .setNegativeButton(R.string.dialog_negative,
                        (dialog, id) -> dialog.cancel());

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }
}