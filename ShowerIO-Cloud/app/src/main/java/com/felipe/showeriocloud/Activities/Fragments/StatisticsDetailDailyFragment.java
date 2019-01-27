package com.felipe.showeriocloud.Activities.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.felipe.showeriocloud.Model.BathStatisticsDailyDO;
import com.felipe.showeriocloud.Model.DevicePersistance;
import com.felipe.showeriocloud.R;
import com.felipe.showeriocloud.Utils.OnBackPressed;
import com.felipe.showeriocloud.Utils.ServerCallbackObjects;
import com.felipe.showeriocloud.Utils.StatisticsUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StatisticsDetailDailyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StatisticsDetailDailyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatisticsDetailDailyFragment extends Fragment implements OnBackPressed {

    private StatisticsUtils statisticsUtils;
    private OnFragmentInteractionListener mListener;
    private List<BathStatisticsDailyDO> bathStatisticsDaily;
    private List<BathStatisticsDailyDO> filteredList;
    private ProgressDialog loadingStatisProgressDialog;
    public RequestQueue requestQueue;

    @BindView(R.id.calendarView)
    public CalendarView calendarView;

    @BindView(R.id.statisticsFrameLayout)
    public FrameLayout statisticsFrameLayout;

    private AlertDialog alertEmpty;

    String[] dailyTableHeader = {"Horário", "Duração", "Litros"};
    String[][] statisticsData;


    public StatisticsDetailDailyFragment() {
        // Required empty public constructor
        this.statisticsUtils = new StatisticsUtils();
        this.bathStatisticsDaily = new ArrayList<>();
        this.filteredList = new ArrayList<>();
    }

    @Override
    public void onBackPressed() {

    }

    public static StatisticsDetailDailyFragment newInstance() {
        StatisticsDetailDailyFragment fragment = new StatisticsDetailDailyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_statistics_detail_daily, container, false);
        ButterKnife.bind(this, view);
        this.setDateChangerListener();
        helpUser();
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setDateChangerListener() {
        this.calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                String sMonth;
                String sDay;

                if (Integer.toString(month).length() < 2) {
                    month++;
                    sMonth = "0" + Integer.toString(month);
                } else {
                    month++;
                    sMonth = Integer.toString(month);
                }

                if (Integer.toString(dayOfMonth).length() < 2) {
                    sDay = "0" + Integer.toString(dayOfMonth);
                } else {
                    sDay = Integer.toString(dayOfMonth);
                }

                fetchStatistics(Integer.toString(year), sMonth, sDay);
            }
        });

    }

    public void fetchStatistics(String year, String month, String dayOfMonth) {
        this.requestQueue = Volley.newRequestQueue(this.getContext());
        loadingStatisProgressDialog = new ProgressDialog(getActivity());
        loadingStatisProgressDialog.setMessage("Buscando últimas estatísticas...");
        loadingStatisProgressDialog.setCanceledOnTouchOutside(false);
        loadingStatisProgressDialog.show();
        statisticsUtils.getDailyStatistics(year, month, dayOfMonth, DevicePersistance.selectedDevice, this.requestQueue, new ServerCallbackObjects() {
            @Override
            public void onServerCallbackObject(Boolean status, String response, List<Object> objects) {
                List<BathStatisticsDailyDO> results = (List<BathStatisticsDailyDO>) (List<?>) objects;
                bathStatisticsDaily = results;
                loadingStatisProgressDialog.dismiss();
                showTable();
            }
        });
    }

    public void showTable() {
        if (this.bathStatisticsDaily.size() > 0) {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(this.getContext());
            View mView = getLayoutInflater().inflate(R.layout.dialog_daily_statistics_table, null);
            final TableView<String[]> tb = (TableView<String[]>) mView.findViewById(R.id.dailyTable);
            tb.setColumnCount(3);
            tb.setHeaderBackgroundColor(Color.parseColor("#0f82d2"));

            //Populate Data
            statisticsData = new String[this.bathStatisticsDaily.size()][3];

            for (int i = 0; i < this.bathStatisticsDaily.size(); i++) {
                String duration;

                BathStatisticsDailyDO bathStats = this.bathStatisticsDaily.get(i);
                duration = String.valueOf(Math.floor(bathStats.getBathDuration()/60)).split("\\.")[0] + " m "
                        + String.valueOf(Math.floor(Double.parseDouble("0." + Double.toString(bathStats.getBathDuration() / 60).split("\\.")[1]) * 60)).split("\\.")[0] + " s";

                statisticsData[i][0] = bathStats.getBathTimestamp().toString().substring(11, 19);
                statisticsData[i][1] = duration;
                statisticsData[i][2] = bathStats.getLiters().toString();
            }

            //Adapters
            tb.setHeaderAdapter(new SimpleTableHeaderAdapter(this.getContext(), this.dailyTableHeader));
            tb.setDataAdapter(new SimpleTableDataAdapter(this.getContext(), statisticsData));

            //Build dialog UI
            mBuilder.setView(mView);
            final AlertDialog dialog = mBuilder.create();
            dialog.show();


        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.emptyStatistics);
            alertEmpty = builder.create();
            alertEmpty.show();
        }
    }

    private void helpUser() {
        // showing snack bar to help user to use the application
        Snackbar snackbar = Snackbar
                .make(statisticsFrameLayout, "Escolha uma data para verificar banhos e estatísticas", Snackbar.LENGTH_LONG)
                .setDuration(32000);
        snackbar.show();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
