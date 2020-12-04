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

    String[] sorted_bssid = { "00:0d:67:93:52:a4",  "02:54:af:d2:55:21",  "02:54:af:ee:4d:e4",  "02:54:af:f3:e6:d0",  "02:54:af:f5:0f:0e",  "02:54:af:f9:d1:da",  "04:bd:88:76:f4:20",  "04:bd:88:76:f4:21",  "04:bd:88:76:f4:22",  "04:bd:88:76:f4:30",  "04:bd:88:76:f4:31",  "04:bd:88:76:f4:32",  "04:bd:88:76:f4:c0",  "04:bd:88:76:f4:c1",  "04:bd:88:76:f4:c2",  "04:bd:88:76:f5:80",  "04:bd:88:76:f5:81",  "04:bd:88:76:f5:82",  "04:bd:88:76:f5:90",  "04:bd:88:76:f5:91",  "04:bd:88:76:f5:92",  "04:bd:88:76:f6:02",  "04:bd:88:76:f6:80",  "04:bd:88:76:f6:81",  "04:bd:88:76:f6:82",  "04:bd:88:76:f6:92",  "04:bd:88:76:f6:e0",  "04:bd:88:76:f6:e1",  "04:bd:88:76:f6:e2",  "04:bd:88:76:f7:20",  "04:bd:88:76:f7:21",  "04:bd:88:76:f7:22",  "04:bd:88:76:f9:e0",  "04:bd:88:76:f9:e1",  "04:bd:88:76:f9:e2",  "04:bd:88:76:f9:f0",  "04:bd:88:76:f9:f1",  "04:bd:88:76:f9:f2",  "04:bd:88:77:05:e2",  "04:bd:88:77:07:20",  "04:bd:88:77:07:21",  "04:bd:88:77:07:22",  "04:bd:88:77:07:30",  "04:bd:88:77:07:31",  "04:bd:88:77:07:32",  "04:bd:88:77:07:62",  "04:bd:88:77:08:00",  "04:bd:88:77:08:01",  "04:bd:88:77:08:02",  "04:bd:88:77:08:20",  "04:bd:88:77:08:21",  "04:bd:88:77:08:22",  "04:bd:88:77:08:30",  "04:bd:88:77:08:31",  "04:bd:88:77:08:32",  "04:bd:88:77:f2:a2",  "0e:99:41:7c:0c:7b",  "10:da:43:0e:46:de",  "12:d0:7a:c5:05:b5",  "16:ae:db:f9:8e:5a",  "18:64:72:29:09:c0",  "18:64:72:29:09:c1",  "18:64:72:29:09:c2",  "18:64:72:29:09:d0",  "18:64:72:29:09:d1",  "18:64:72:29:09:d2",  "20:32:33:88:4f:51",  "20:a6:cd:fd:07:42",  "22:ad:56:22:d9:a1",  "22:ad:56:2d:4e:0c",  "22:ad:56:36:3f:69",  "22:ad:56:40:80:b2",  "22:ad:56:43:39:b3",  "22:f5:43:71:f4:a6",  "2c:4c:c6:a0:35:08",  "32:b4:b8:28:16:b5",  "32:b4:b8:2b:51:e4",  "34:af:2c:57:6e:1d",  "36:79:cb:51:ba:40",  "3c:84:6a:81:e0:4b",  "46:b0:58:af:91:1d",  "46:e4:ee:3e:83:d0",  "4a:f1:7f:a7:32:0d",  "58:70:c6:ca:4f:21",  "58:cb:52:ad:23:c6",  "60:38:e0:bd:e6:29",  "62:45:b4:f6:94:92",  "62:45:b6:ce:ac:21",  "62:45:b7:78:a1:82",  "66:c2:de:8a:7e:3b",  "6c:f3:7f:7f:7c:c2",  "70:3a:0e:45:93:00",  "70:3a:0e:45:93:01",  "70:3a:0e:45:93:02",  "70:8b:cd:5e:4f:f0",  "70:f2:20:6a:68:b3",  "76:40:bb:7c:af:33",  "78:61:7c:22:11:d4",  "80:a0:36:e0:38:e3",  "84:a9:3e:fc:0f:08",  "84:d4:7e:49:82:e2",  "84:d4:7e:4a:1c:42",  "84:d4:7e:4a:4a:e0",  "84:d4:7e:4a:4a:e1",  "84:d4:7e:4a:4a:e2",  "84:d4:7e:4a:4b:e0",  "84:d4:7e:4a:4b:e1",  "84:d4:7e:4a:4b:e2",  "84:d4:7e:4a:4b:f0",  "84:d4:7e:4a:4b:f1",  "84:d4:7e:4a:4b:f2",  "84:d4:7e:4a:4c:00",  "84:d4:7e:4a:4c:01",  "84:d4:7e:4a:4c:02",  "84:d4:7e:4a:4c:10",  "84:d4:7e:4a:4c:11",  "84:d4:7e:4a:4c:12",  "84:d4:7e:4a:68:00",  "84:d4:7e:4a:68:01",  "84:d4:7e:4a:68:02",  "84:d4:7e:4a:68:10",  "84:d4:7e:4a:68:11",  "84:d4:7e:4a:68:12",  "84:d4:7e:4a:6a:22",  "84:d4:7e:4a:6d:a2",  "84:d4:7e:4a:77:62",  "84:d4:7e:4a:7a:62",  "84:d4:7e:4a:92:80",  "84:d4:7e:4a:92:81",  "84:d4:7e:4a:92:82",  "84:d4:7e:4a:92:90",  "84:d4:7e:4a:92:91",  "84:d4:7e:4a:92:92",  "84:d4:7e:4a:a8:02",  "84:d4:7e:4a:ae:82",  "84:d4:7e:4a:b9:42",  "84:d4:7e:4a:bd:42",  "84:d4:7e:4a:bf:02",  "84:d4:7e:4a:c0:02",  "84:d4:7e:4a:c2:c2",  "84:d4:7e:4a:ce:82",  "84:d4:7e:4a:d1:02",  "84:d4:7e:4a:d1:e0",  "84:d4:7e:4a:d1:e1",  "84:d4:7e:4a:d1:e2",  "84:d4:7e:4a:d4:42",  "84:d4:7e:4a:d4:a0",  "84:d4:7e:4a:d4:a1",  "84:d4:7e:4a:d4:a2",  "84:d4:7e:4a:d5:42",  "84:d4:7e:4a:dc:02",  "84:d4:7e:4a:dc:c2",  "84:d4:7e:4a:dd:e2",  "86:2a:fd:60:d4:6f",  "86:ce:f8:ed:61:36",  "88:dc:96:62:d1:b6",  "8e:49:62:39:84:48",  "8e:6d:1a:d2:14:f0",  "94:10:3e:b1:7b:2e",  "98:e7:f4:45:9b:4a",  "a0:04:60:43:a8:44",  "a0:04:60:6c:92:94",  "a0:8c:fd:6d:ee:72",  "a6:15:61:97:b0:a2",  "aa:9f:c8:41:cd:c8",  "ae:89:db:b1:dd:4b",  "ae:ae:19:57:45:64",  "ba:61:49:27:a6:54",  "bc:a5:11:70:8e:3f",  "be:21:d9:c7:c6:4c",  "c2:7b:3c:fc:29:80",  "c4:49:bb:6f:b4:62",  "c4:49:bb:93:c2:ec",  "cc:40:d0:00:cc:48",  "d8:eb:97:ed:c8:36",  "da:13:99:be:66:e4",  "da:d5:a7:1c:36:b4",  "de:16:91:6c:42:9a",  "e2:b1:15:bd:13:08",  "ea:6f:38:06:5f:16",  "ea:b4:76:3b:c9:b1",  "ea:e8:b7:62:84:7f",  "f0:51:36:4c:d0:33",  "f4:30:b9:ef:9b:50",  "f4:bf:80:d4:15:fb",  "fa:4f:ad:43:5a:cf",  "fa:8f:ca:61:cc:1e",  "fe:f5:c4:82:95:d6"  };

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
            int[] rssi = new int[sorted_bssid.length];
            for (int i = 0; i<sorted_bssid.length; i++) {
                rssi[i] = -100;
            }
            for (int i = 0; i<results.size(); i++) {
                for (int j = 0; j<sorted_bssid.length; j++) {
                    if (sorted_bssid[j].equals(results.get(i).BSSID)) {
                        rssi[j] = results.get(i).level;
                    }
                }
            }
            for (int i = 0; i<sorted_bssid.length; i++) {
                svmBuilder.append(i + ":" + rssi[i] + " ");
            }
            svmBuilder.append("\n");
            Files.write(FileSystems.getDefault().getPath(MainActivity.mSystemPath + "test.txt"), svmBuilder.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
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
