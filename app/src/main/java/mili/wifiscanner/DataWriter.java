package mili.wifiscanner;

import android.net.wifi.ScanResult;
import android.os.Environment;
import android.content.res.Resources;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static mili.wifiscanner.MainActivity.mSortedBssid;
import static mili.wifiscanner.MainActivity.mTestPath;


public class DataWriter {
    private static String TAG = "DataWriter";
    private static Path mFilePath;
    
    public DataWriter(CharSequence type, String folderName) {
        Path rootPath = getRootPath(folderName + "/" + type);
        String fileName = type + getDateUnderLine() + ".txt";
        String filePath = rootPath.toString() + "/" + fileName;
        try {
            Files.createDirectories(rootPath);
            File file = new File(filePath);
            file.createNewFile();
            mFilePath = FileSystems.getDefault().getPath(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path getRootPath(String folderName) {
        return FileSystems.getDefault().getPath(Environment.getExternalStorageDirectory().getPath(), folderName);
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
            Files.write(mFilePath, stringBuilder.toString().getBytes(), StandardOpenOption.APPEND);

            StringBuilder svmBuilder = new StringBuilder();
            svmBuilder.append(roomId + " ");
            int[] rssi = new int[mSortedBssid.length];
            for (int i = 0; i<mSortedBssid.length; i++) {
                rssi[i] = -100;
            }
            for (int i = 0; i<results.size(); i++) {
                for (int j = 0; j<mSortedBssid.length; j++) {
                    if (mSortedBssid[j].equals(results.get(i).BSSID)) {
                        rssi[j] = results.get(i).level;
                    }
                }
            }
            for (int i = 0; i<mSortedBssid.length; i++) {
                svmBuilder.append(i + ":" + rssi[i] + " ");
            }
            svmBuilder.append("\n");
            File file = new File(mTestPath);
            file.createNewFile();
            Files.write(FileSystems.getDefault().getPath(mTestPath),
                    svmBuilder.toString().getBytes(),
                    StandardOpenOption.TRUNCATE_EXISTING);
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
