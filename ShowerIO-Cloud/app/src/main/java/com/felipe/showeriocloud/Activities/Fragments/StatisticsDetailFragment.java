package com.felipe.showeriocloud.Activities.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.espressif.iot.esptouch.task.__IEsptouchTask;
import com.felipe.showeriocloud.Model.BathStatisticsMonthly;
import com.felipe.showeriocloud.Model.DeviceDO;
import com.felipe.showeriocloud.Model.DevicePersistance;
import com.felipe.showeriocloud.R;
import com.felipe.showeriocloud.Utils.ServerCallbackObject;
import com.felipe.showeriocloud.Utils.StatisticsUtils;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.util.Date;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.codecrafters.tableview.TableView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StatisticsDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StatisticsDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatisticsDetailFragment extends Fragment {

    private static final String TAG = "StatisticsDetail";
    private OnFragmentInteractionListener mListener;
    private DeviceDO device;
    private StatisticsUtils statisticsUtils;
    public RequestQueue requestQueue;
    private ProgressDialog loadingStatisProgressDialog;
    private AlertDialog alertStatistics;

    @BindView(R.id.mainGridStats)
    public GridLayout mainGridStats;

    @BindView(R.id.cardViewDaily)
    public CardView cardViewDaily;

    @BindView(R.id.cardViewMonthly)
    public CardView cardViewMonthly;


    public StatisticsDetailFragment() {
        // Required empty public constructor
    }

    public static StatisticsDetailFragment newInstance(String param1, String param2) {
        StatisticsDetailFragment fragment = new StatisticsDetailFragment();
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
        View view = inflater.inflate(R.layout.fragment_statistics_detail, container, false);
        ButterKnife.bind(this, view);
        device = DevicePersistance.selectedDevice;
        this.requestQueue = Volley.newRequestQueue(this.getContext());
        statisticsUtils = new StatisticsUtils();

        cardViewDaily.setCardBackgroundColor(Color.WHITE);
        cardViewMonthly.setCardBackgroundColor(Color.WHITE);
        this.setSingleEvent(this.mainGridStats);
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onDailyStatisticsSelected();
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


    private void setSingleEvent(GridLayout mainGrid) {
        //Loop all child item of Main Grid
        for (int i = 0; i < mainGrid.getChildCount(); i++) {
            //You can see , all child item is CardView , so we just cast object to CardView
            CardView cardView = (CardView) mainGrid.getChildAt(i);
            final int finalI = i;

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (finalI) {
                        case 0:
                            Log.i("StatisticsDetail", "case 0, opening daily statistics");
                            mListener.onDailyStatisticsSelected();
                            break;
                        case 1:
                            Log.i("StatisticsDetail", "case 1, opening monthly statistics");
                            onMonthlyStatisticsSelected();
                            break;
                    }
                }
            });
        }
    }

    public void onMonthlyStatisticsSelected() {
        Calendar cal = Calendar.getInstance();
        final Date date = cal.getTime();

        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(this.getContext(),
                new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) { // on date set }
                        String sMonth;
                        if (Integer.toString(selectedMonth).length() < 2) {
                            selectedMonth++;
                            sMonth = "0" + Integer.toString(selectedMonth);
                        } else {
                            selectedMonth++;
                            sMonth = Integer.toString(selectedMonth);
                        }

                        loadingStatisProgressDialog = new ProgressDialog(getActivity());
                        loadingStatisProgressDialog.setMessage("Buscando últimas estatísticas...");
                        loadingStatisProgressDialog.setCanceledOnTouchOutside(false);
                        loadingStatisProgressDialog.show();
                        statisticsUtils.getMonthlyStatistics(Integer.toString(selectedYear), sMonth, DevicePersistance.selectedDevice, requestQueue, new ServerCallbackObject() {
                            @Override
                            public void onServerCallbackObject(Boolean status, String response, Object object) {
                                BathStatisticsMonthly bathStatisticsMonthly = (BathStatisticsMonthly) object;
                                loadingStatisProgressDialog.dismiss();

                                if (bathStatisticsMonthly.getTotalTime() > 0) {

                                    String totalHoursText = String.valueOf(Math.floor(bathStatisticsMonthly.getTotalTime() / 3600)).split("\\.")[0] + " horas e "
                                            + String.valueOf(Math.floor(Double.parseDouble("0." + Double.toString(bathStatisticsMonthly.getTotalTime() / 3600).split("\\.")[1]) * 60)).split("\\.")[0] + " minutos";

                                    String totalLitersText = bathStatisticsMonthly.getTotalLiters().toString() + " litros de água";

                                    String aproximateElectricalEnergyText = "R$ " + Double.toString((bathStatisticsMonthly.getTotalTime()/3600)*6800*bathStatisticsMonthly.getEnergyPrice()/1000);


                                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
                                    View mView = getLayoutInflater().inflate(R.layout.dialog_monthly_statistics, null);
                                    final TextView textViewHours  = (TextView) mView.findViewById(R.id.hoursResponse);
                                    final TextView textViewLiters  = (TextView) mView.findViewById(R.id.litersResponse);
                                    final TextView textViewEnergyPrice  = (TextView) mView.findViewById(R.id.coastResponse);

                                    textViewHours.setText(totalHoursText);
                                    textViewLiters.setText(totalLitersText);
                                    textViewEnergyPrice.setText(aproximateElectricalEnergyText);

                                    mBuilder.setView(mView);
                                    final AlertDialog dialog = mBuilder.create();
                                    dialog.setCanceledOnTouchOutside(false);
                                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getActivity().getText(android.R.string.cancel),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    dialog.show();


                                }
                            }
                        });
                    }

                }, date.getYear(), date.getMonth());

        builder.setActivatedMonth(Calendar.JULY)
                .setMinYear(1990)
                .setActivatedYear(2019)
                .setMaxYear(2030)
                .setMinMonth(Calendar.JANUARY)
                .setTitle("Selecione o mês")
                .setMonthRange(Calendar.JANUARY, Calendar.DECEMBER)
                // .setMaxMonth(Calendar.OCTOBER)
                // .setYearRange(1890, 1890)
                // .setMonthAndYearRange(Calendar.FEBRUARY, Calendar.OCTOBER, 1890, 1890)
                //.showMonthOnly()
                // .showYearOnly()
                .setOnMonthChangedListener(new MonthPickerDialog.OnMonthChangedListener() {
                    @Override
                    public void onMonthChanged(int selectedMonth) {


                    }
                })
                .setOnYearChangedListener(new MonthPickerDialog.OnYearChangedListener() {
                    @Override
                    public void onYearChanged(int selectedYear) {

                    }
                })
                .build()
                .show();
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
        void onDailyStatisticsSelected();
    }
}
