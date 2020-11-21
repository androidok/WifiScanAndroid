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


public class DataWriter {
    private static String TAG = "DataWriter";
    private static Path mFilePath;

    public DataWriter(String type, String folderName) {
        Path rootPath = getRootPath(folderName);
        String fileName = type + getDateUnderLine() + ".txt";
        String filePath = rootPath.toString() + "/" + fileName;
        try {
            Log.i(TAG, "Write to " + filePath);
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
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    public void writeToFiles(int roomId, List<ScanResult> results) {
        try {
            Log.i(TAG, "Write to " + mFilePath);
            Files.write(mFilePath, getTimeStamp().getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
