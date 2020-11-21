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

public class ScanAdapter  extends RecyclerView.Adapter<ScanAdapter.ViewHolder>{
    private static final String TAG = "ScanAdapter";
    private List<ScanResult> mAccessPoints;

    public ScanAdapter(List<ScanResult> results) {
        mAccessPoints = results;
        Log.d(TAG, getItemCount() + " APs discovered.");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "Element " + position + " set.");
//        String info = "BSSID: " + mAccessPoints.get(position).BSSID + "\t"
//                + "RSSI: " + mAccessPoints.get(position).level + " dBm";
//        String info = "SSID: " + mAccessPoints.get(0).SSID + "\n"
//                + "BSSID: " + mAccessPoints.get(position).BSSID + "\n"
//                + "RSSI: " + mAccessPoints.get(position).level + " dBm\n";
        holder.mBSSIDTextView.setText(mAccessPoints.get(position).BSSID);
        holder.mRSSITextView.setText(mAccessPoints.get(position).level);
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
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mRSSITextView;
        public TextView mBSSIDTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mRSSITextView = itemView.findViewById(R.id.rssi_text_view);
            mBSSIDTextView = itemView.findViewById(R.id.bssid_text_view);
        }

    }
}
