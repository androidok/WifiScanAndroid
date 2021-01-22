package mili.wifiscanner;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChartFragment extends Fragment {
    private static final String TAG = "ChartFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MyChartView mChartView;
    private ChipGroup mChipGroup;

    public ChartFragment() {
        // Required empty public constructor
        Log.d(TAG, "ChartFragment constructor called.");
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChartFragment newInstance(String param1, String param2) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mChartView = getActivity().findViewById(R.id.chart);
//        mChipGroup = getView().findViewById(R.id.chart_chip_group);
//        mChipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(ChipGroup group, int checkedId) {
//                switch (checkedId) {
//                    case R.id.chip_view:
//                        mChartView.setMode();
//                        break;
//                    case R.id.chip_draw:
//                        mChartView.setMode();
//                        break;
//                    case R.id.chip_select:
//                        break;
//                    case R.id.chip_reverse:
//                        mChartView.reverse();
//                        break;
//                    case R.id.chip_delete:
//                        mChartView.undo();
//                        break;
//                }
//                mChipGroup.clearCheck();
//            }
//        });
        Chip chipDraw = getView().findViewById(R.id.chip_draw);
        chipDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChartView.setMode("draw");
            }
        });
        Chip chipView = getView().findViewById(R.id.chip_view);
        chipView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChartView.setMode("view");
            }
        });
        Chip chipSelect = getView().findViewById(R.id.chip_select);
        chipSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChartView.setMode("select");
            }
        });
        Chip chipUndo = getView().findViewById(R.id.chip_delete);
        chipUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChartView.undo();
            }
        });

        Chip chipReverse = getView().findViewById(R.id.chip_reverse);
        chipReverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChartView.reverse();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chart, container, false);
    }

    public void setMode() {
        mChartView.reverse();
    }

    public List<Float> getPath() {
        return mChartView.getSeriesData();
    }
}