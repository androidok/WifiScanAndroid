package mili.wifiscanner;

import android.net.wifi.ScanResult;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ViewHolder> {
    private static final String TAG = "ScanAdapter";
    private List<ScanResult> localDataSet;

    public void swapData(List<ScanResult> results) {
        localDataSet.clear();

        if ((results != null) && (results.size() > 0)) {
            localDataSet.addAll(results);
        }
        notifyDataSetChanged();
        Log.d(TAG, localDataSet.size() + " APs discovered.");
//        String info = "First AP\nSSID: " + mAccessPoints.get(0).SSID + "\n"
//                + "BSSID: " + mAccessPoints.get(0).BSSID + "\n"
//                + "RSSI: " + mAccessPoints.get(0).level + " dBm\n";
//        Log.d(TAG, info);
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mBSSIDTextView;
        private final TextView mRSSITextView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            mBSSIDTextView = (TextView) view.findViewById(R.id.bssid_text_view);
            mRSSITextView = (TextView) view.findViewById(R.id.rssi_text_view);
        }

        public TextView getmBSSIDTextView() {
            return mBSSIDTextView;
        }
        public TextView getmRSSITextView() {
            return mRSSITextView;
        }

    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet List<ScanResult> containing the data to populate views to be used
     * by RecyclerView.
     */
    public ScanAdapter(List<ScanResult> dataSet) {
        localDataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycler_layout, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from the dataset at this position and replace the
        // contents of the view with that element
        if (position > 0) {
            ScanResult result = localDataSet.get(position-1);
            viewHolder.getmBSSIDTextView().setText(result.BSSID);
            viewHolder.getmRSSITextView().setText(result.level + " dBm");
        } else {
            viewHolder.getmBSSIDTextView().setText(R.string.recycler_row_header_label_bssid);
            viewHolder.getmRSSITextView().setText(R.string.recycler_row_header_label_rssi);
        }
        // Log.d(TAG, "Element " + position + " set.");
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size() + 1;
    }
}
