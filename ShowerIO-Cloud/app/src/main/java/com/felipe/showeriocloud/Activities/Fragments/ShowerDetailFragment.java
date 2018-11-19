package com.felipe.showeriocloud.Activities.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.felipe.showeriocloud.Activities.Authentication.LoginActivity;
import com.felipe.showeriocloud.Model.DeviceDO;
import com.felipe.showeriocloud.Model.DevicePersistance;
import com.felipe.showeriocloud.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShowerDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ShowerDetailFragment} factory method to
 * create an instance of this fragment.
 */
public class ShowerDetailFragment extends Fragment {


    private GridLayout mainGrid;
    private DeviceDO device;
    private TextView deviceTitle;
    public static String selectedShower;
    private SharedPreferences sharedPreferences;
    private final String SHOWERIO = "ShowerIO";
    private CardView cardViewPlay;
    private Boolean nameFlag;
    private ScrollView scrollView;
    private ColorStateList defaultColor;

    private OnFragmentInteractionListener mListener;

    public ShowerDetailFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_shower_detail, container, false);

        mainGrid = (GridLayout) view.findViewById(R.id.mainGrid);
        deviceTitle = view.findViewById(R.id.textGrid);
        cardViewPlay = view.findViewById(R.id.cardViewPlay);
        scrollView = view.findViewById(R.id.scrollView);
        nameFlag = false;
        defaultColor = cardViewPlay.getCardBackgroundColor();

        setSingleEvent(mainGrid);

        device = DevicePersistance.selectedDevice;
        if (device.getName().isEmpty() || device.getName().equals("UNAMED")) {
            deviceTitle.setText(R.string.noName);
            cardViewPlay.setCardBackgroundColor(Color.GRAY);
            nameFlag = true;
            helpUserSetName();
        } else {
            deviceTitle.setText(device.getName());
        }

        return view;



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
                            if (!nameFlag) {
//                                Log.i("ShowerDetailActivity", "case 1, opening ShowerIOActivity");
//                                Intent showerIO = new Intent(ShowerDetailActivity.this, ShowerIO.class);
//                                showerIO.putExtra("device", ShowerDetailActivity.selectedShower);
//                                startActivity(showerIO);
//                                finish();
//                                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                            }
                            break;
                        case 1:
//                            Log.i("ShowerDetailActivity", "case 2, opening NameDeviceActivity");
//                            Intent nameDeviceActivity = new Intent(ShowerDetailActivity.this, NameDeviceActivity.class);
//                            nameDeviceActivity.putExtra("device", ShowerDetailActivity.selectedShower);
//                            startActivity(nameDeviceActivity);
//                            finish();
//                            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                            break;
                        case 2:
//                            Log.i("ShowerDetailActivity", "case 2, opening LoginActivity");
//                            Intent loginActivity = new Intent(ShowerDetailActivity.this, LoginActivity.class);
//                            loginActivity.putExtra("device", ShowerDetailActivity.selectedShower);
//                            startActivity(loginActivity);
//                            finish();
//                            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                            break;

                    }
                }
            });
        }
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    private void helpUserSetName() {
        // showing snack bar to help user to use the application
        Snackbar snackbar = Snackbar
                .make(scrollView, "Nomeie seu dispositivo antes de comerçar!", Snackbar.LENGTH_LONG)
                .setDuration(5000);
        snackbar.show();
    }
}
