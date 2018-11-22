package com.felipe.showeriocloud.Activities.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.felipe.showeriocloud.Activities.Authentication.LoginActivity;
import com.felipe.showeriocloud.Model.DeviceDO;
import com.felipe.showeriocloud.Model.DevicePersistance;
import com.felipe.showeriocloud.R;
import com.felipe.showeriocloud.Utils.ServerCallback;

import java.util.concurrent.locks.Condition;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShowerDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ShowerDetailFragment} factory method to
 * create an instance of this fragment.
 */
public class ShowerDetailFragment extends Fragment {

    private ProgressDialog mProgressDialog;
    private String oldName;

    @BindView(R.id.mainGrid)
    public GridLayout mainGrid;

    @BindView(R.id.textGrid)
    public TextView deviceTitle;

    private DeviceDO device;
    public static String selectedShower;
    private SharedPreferences sharedPreferences;
    private final String SHOWERIO = "ShowerIO";

    @BindView(R.id.cardViewPlay)
    public CardView cardViewPlay;

    @BindView(R.id.cardViewName)
    public CardView cardViewName;

    @BindView(R.id.cardViewReset)
    public CardView cardViewReset;

    @BindView(R.id.cardViewExit)
    public CardView cardViewExit;

    @BindView(R.id.cardStatistics)
    public CardView cardStatistics;

    @BindView(R.id.cardInfo)
    public CardView cardInfo;

    private Boolean nameFlag;

    @BindView(R.id.scrollViewDetail)
    public ScrollView scrollView;

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

        ButterKnife.bind(this, view);

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
            enableAllFeatures();
            deviceTitle.setText(device.getName());

        }

        return view;


    }

    void enableAllFeatures() {
        cardViewPlay.setCardBackgroundColor(Color.WHITE);
        cardViewName.setCardBackgroundColor(Color.WHITE);
        cardViewReset.setCardBackgroundColor(Color.WHITE);
        cardViewExit.setCardBackgroundColor(Color.WHITE);
        cardStatistics.setCardBackgroundColor(Color.WHITE);
        cardInfo.setCardBackgroundColor(Color.WHITE);
        nameFlag = false;
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
                                Log.i("ShowerDetailActivity", "case 0, opening ShowerIOActivity");
                            }
                            break;
                        case 1:
                            Log.i("ShowerDetailActivity", "case 1, opening ShowerIOActivity");
                            onSetNamePressed();
                            break;
                        case 2:
                            Log.i("ShowerDetailActivity", "case 2, opening ShowerIOActivity");
                            break;
                        case 3:
                            Log.i("ShowerDetailActivity", "case 3, opening ShowerIOActivity");
                            break;
                        case 4:
                            Log.i("ShowerDetailActivity", "case 4, opening ShowerIOActivity");
                            break;
                        case 5:
                            Log.i("ShowerDetailActivity", "case 5, opening ShowerIOActivity");
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

    private void onSetNamePressed() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this.getContext());
        View mView = getLayoutInflater().inflate(R.layout.dialog_name_form, null);
        final EditText mName = (EditText) mView.findViewById(R.id.etName);
        Button mSetName = (Button) mView.findViewById(R.id.btnSetName);
        mBuilder.setView(mView);
        final Context fragmentContext = this.getContext();
        final AlertDialog dialog = mBuilder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        mSetName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mName.getText().toString().isEmpty()){
                    dialog.dismiss();
                    setNewNameCall(mName.getText().toString());
                }else{
                    Toast.makeText(fragmentContext,
                            R.string.dialog_set_name_fail,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void setNewNameCall(final String name) {
        oldName = device.getName();
        mProgressDialog = new ProgressDialog(this.getContext());
        mProgressDialog.setMessage("Mudando nome do dispositivo...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
        device.setName(name);
        final Context fragmentContext = this.getContext();

        DevicePersistance.updateDevice(device, new ServerCallback() {
            @Override
            public void onServerCallback(Boolean status, String response) {
                if(status){
                    mProgressDialog.dismiss();
                    Toast.makeText(fragmentContext,
                            R.string.dialog_set_name_success,
                            Toast.LENGTH_SHORT).show();
                    deviceTitle.setText(name);
                } else {
                    mProgressDialog.dismiss();
                    Toast.makeText(fragmentContext,
                            R.string.dialog_set_name_fail,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}
