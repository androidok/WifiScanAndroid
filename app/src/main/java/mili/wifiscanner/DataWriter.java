package mili.wifiscanner;

import android.net.wifi.ScanResult;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class DataWriter {
    private static String TAG = "DataWriter";
    private String mFilePath;
    
    public DataWriter(CharSequence type, String folderName) {
        String rootPath = getRootPath(folderName + "/" + type);
        String fileName = type + getDateUnderLine() + ".txt";
        mFilePath = rootPath.toString() + "/" + fileName;
        try {
            File file = new File(mFilePath);
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getRootPath(String folderName) {
        return Environment.getExternalStorageDirectory().getPath() + "/" + folderName;
    }

    private static String getDateUnderLine() {
        SimpleDateFormat formatter = new SimpleDateFormat("MM_dd_HH:mm");
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    private static String getTimeStamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    public void writeToFiles(CharSequence roomId, List<ScanResult> results) {
        try {
            Log.d(TAG, "Write to " + mFilePath);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("room " + roomId + " " + getTimeStamp());
            stringBuilder.append("\n                BSSID  RSSI\n");
            for (int i = 0; i<results.size(); i++) {
                stringBuilder.append(i + " " + results.get(i).BSSID + " " + results.get(i).level + "\n");
            }
            stringBuilder.append("\n");
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(mFilePath, true));
            outputStreamWriter.write(stringBuilder.toString());
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writePath(List<Float> path) {
        try {
            Log.d(TAG, "Write to " + mFilePath);
            StringBuilder stringBuilder = new StringBuilder();
            for (Float f : path) {
                stringBuilder.append(f + "\n");
            }
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(mFilePath, true));
            outputStreamWriter.write(stringBuilder.toString());
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String info() {
        String info = "Data will be stored in " + mFilePath;
        info = info.replace(Environment.getExternalStorageDirectory().getPath(), "");
        Log.d(TAG, info);
        return info;
    }
}
