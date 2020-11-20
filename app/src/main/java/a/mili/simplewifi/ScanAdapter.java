package a.mili.simplewifi;

import android.content.Context;
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
    List<ScanResult> mAccessPoints;


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String info = "BSSID: " + mAccessPoints.get(position).BSSID + "\t"
                + "RSSI: " + mAccessPoints.get(position).level + " dBm";
//        String info = "SSID: " + mAccessPoints.get(0).SSID + "\n"
//                + "BSSID: " + mAccessPoints.get(position).BSSID + "\n"
//                + "RSSI: " + mAccessPoints.get(position).level + " dBm\n";
        holder.getTextView().setText(info);
    }

    @Override
    public int getItemCount() {
        return mAccessPoints.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }

        public TextView getTextView() {
            return textView;
        }
    }
}
