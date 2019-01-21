package com.felipe.showeriocloud.Activities.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.felipe.showeriocloud.Model.BathStatisticsDailyDO;
import com.felipe.showeriocloud.Model.DevicePersistance;
import com.felipe.showeriocloud.R;
import com.felipe.showeriocloud.Utils.ServerCallbackObjects;
import com.felipe.showeriocloud.Utils.StatisticsUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StatisticsDetailDailyFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StatisticsDetailDailyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatisticsDetailDailyFragment extends Fragment {

    private StatisticsUtils statisticsUtils;
    private OnFragmentInteractionListener mListener;
    private List<BathStatisticsDailyDO> bathStatisticsDaily;
    private ProgressDialog loadingStatisProgressDialog;
    public RequestQueue requestQueue;

    public StatisticsDetailDailyFragment() {
        // Required empty public constructor
        this.statisticsUtils = new StatisticsUtils();
        this.bathStatisticsDaily = new ArrayList<>();
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
        // TODO - Bind calendar and UI Elements
        this.fetchStatistics();

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


    public void fetchStatistics() {
        this.requestQueue = Volley.newRequestQueue(this.getContext());
        loadingStatisProgressDialog = new ProgressDialog(getActivity());
        loadingStatisProgressDialog.setMessage("Buscando últimas estatísticas...");
        loadingStatisProgressDialog.setCanceledOnTouchOutside(false);
        loadingStatisProgressDialog.show();
        statisticsUtils.getDailyStatistics(DevicePersistance.selectedDevice, this.requestQueue, new ServerCallbackObjects() {
            @Override
            public void onServerCallbackObject(Boolean status, String response, List<Object> objects) {

            }
        });
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
