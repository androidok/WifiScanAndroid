package a.mili.simplewifi;

import android.net.wifi.ScanResult;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScanAdapter  extends RecyclerView.Adapter<ScanAdapter.ScanViewHolder>{
    private static final String TAG = "ScanAdapter";
    private List<ScanResult> mAccessPoints;

    public ScanAdapter(List<ScanResult> results) {
        mAccessPoints = results;
        Log.d(TAG, getItemCount() + " APs discovered.");
    }

    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_layout, parent, false);
        return new ScanViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanViewHolder holder, int position) {
        Log.d(TAG, "Element " + position + " set.");
        ScanResult currentScanResult = mAccessPoints.get(position);
        holder.mBSSIDTextView.setText(currentScanResult.BSSID);
        holder.mRSSITextView.setText(currentScanResult.level + " dBm");
    }

    @Override
    public int getItemCount() {
        return mAccessPoints.size();
    }

    public void swapData(List<ScanResult> results) {
        mAccessPoints.clear();

        if ((results != null) && (results.size() > 0)) {
            mAccessPoints.addAll(results);
        }
        notifyDataSetChanged();
        Log.d(TAG, getItemCount() + " APs discovered.");
        String info = "First AP\nSSID: " + mAccessPoints.get(0).SSID + "\n"
                + "BSSID: " + mAccessPoints.get(0).BSSID + "\n"
                + "RSSI: " + mAccessPoints.get(0).level + " dBm\n";
        Log.d(TAG, info);
    }

    public class ScanViewHolder extends RecyclerView.ViewHolder {
        public TextView mBSSIDTextView;
        public TextView mRSSITextView;

        public ScanViewHolder(@NonNull View itemView) {
            super(itemView);
            mBSSIDTextView = itemView.findViewById(R.id.bssid_text_view);
            mRSSITextView = itemView.findViewById(R.id.rssi_text_view);
        }
    }
}
