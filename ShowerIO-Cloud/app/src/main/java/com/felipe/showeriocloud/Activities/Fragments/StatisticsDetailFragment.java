package com.felipe.showeriocloud.Activities.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import com.felipe.showeriocloud.Model.DeviceDO;
import com.felipe.showeriocloud.Model.DevicePersistance;
import com.felipe.showeriocloud.R;

import butterknife.BindView;
import butterknife.ButterKnife;


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
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onDailyStatisticsSelected(uri);
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
        void onDailyStatisticsSelected(Uri uri);
    }
}
