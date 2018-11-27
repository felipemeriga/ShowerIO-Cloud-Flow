package com.felipe.showeriocloud.Activities.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.felipe.showeriocloud.Activities.Authentication.LoginActivity;
import com.felipe.showeriocloud.Activities.Home.SplashScreen;
import com.felipe.showeriocloud.Activities.ShowerIO.ShowerNavigationDrawer;
import com.felipe.showeriocloud.Activities.SmartConfig.SearchForDevices;
import com.felipe.showeriocloud.Adapter.SpinnerHandler;
import com.felipe.showeriocloud.Aws.AwsIotCoreManager;
import com.felipe.showeriocloud.Aws.CognitoSyncClientManager;
import com.felipe.showeriocloud.Model.DeviceDO;
import com.felipe.showeriocloud.Model.DevicePersistance;
import com.felipe.showeriocloud.R;
import com.felipe.showeriocloud.Utils.ConvertIntegerToOption;
import com.felipe.showeriocloud.Utils.ServerCallback;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ShowerDetailFragment extends Fragment implements ConvertIntegerToOption {

    private static final String TAG = "ShowerDetailFragment";
    private Map<String, Integer> convertPositions;

    private ProgressDialog mProgressDialog;
    private String oldName;
    private ProgressDialog mqttProgressDialog;

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

    private AwsIotCoreManager awsIotCoreManager;

    public ShowerDetailFragment() {
        // Required empty public constructor
        awsIotCoreManager = new AwsIotCoreManager();
        convertPositions = new HashMap<String, Integer>();
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
            nameFlag = true;
            helpUserSetName();
        } else {
            deviceTitle.setText(device.getName());

        }
        enableAllFeatures();
        connectToMQTTclient();
        convertPositions = fillPositionsMap();
        return view;


    }

    void enableAllFeatures() {
        if (nameFlag) {
            cardViewName.setCardBackgroundColor(Color.WHITE);
        } else {
            cardViewPlay.setCardBackgroundColor(Color.WHITE);
            cardViewName.setCardBackgroundColor(Color.WHITE);
            cardViewReset.setCardBackgroundColor(Color.WHITE);
            cardViewExit.setCardBackgroundColor(Color.WHITE);
            cardStatistics.setCardBackgroundColor(Color.WHITE);
            cardInfo.setCardBackgroundColor(Color.WHITE);
        }
    }

    void connectToMQTTclient() {
        mqttProgressDialog = new ProgressDialog(getActivity());
        mqttProgressDialog.setMessage("Carregando...");
        mqttProgressDialog.setCanceledOnTouchOutside(false);
        mqttProgressDialog.show();
        awsIotCoreManager.initializeIotCore(device.getUserId(), device.getIotCoreEndPoint(), getActivity(), new ServerCallback() {
            @Override
            public void onServerCallback(final boolean status, String response) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (status) {
                            mqttProgressDialog.dismiss();
                        } else {
                            mqttProgressDialog.dismiss();
                            Toast toast = new Toast(getContext());
                            toast.setText(R.string.mqtt_conection_failed);
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.show();
                            Intent listOfDevices = new Intent(getContext(), ShowerNavigationDrawer.class);
                            startActivity(listOfDevices);
                            getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                            getActivity().finish();
                        }

                    }
                });
            }
        });
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
                                onStartPressed();
                            }
                            break;
                        case 1:
                            Log.i("ShowerDetailActivity", "case 1, opening ShowerIOActivity");
                            onSetNamePressed();
                            break;
                        case 2:
                            if (!nameFlag) {
                                Log.i("ShowerDetailActivity", "case 2, opening ShowerIOActivity");
                            }
                            break;
                        case 3:
                            if (!nameFlag) {
                                Log.i("ShowerDetailActivity", "case 3, opening ShowerIOActivity");
                            }
                            break;
                        case 4:
                            if (!nameFlag) {
                                Log.i("ShowerDetailActivity", "case 4, opening ShowerIOActivity");
                            }
                            break;
                        case 5:
                            if (!nameFlag) {
                                Log.i("ShowerDetailActivity", "case 5, opening ShowerIOActivity");
                            }
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
        // showing toast to help user to use the application
        Toast toast = new Toast(getContext());
        toast.setText(R.string.dialog_name_hint);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

    private void onStartPressed() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this.getContext());
        View mView = getLayoutInflater().inflate(R.layout.dialog_control_device, null);
        Spinner mSpinnerBathTime = (Spinner) mView.findViewById(R.id.spinnerBathTime);
        Spinner mSpinnerBathPosTime = (Spinner) mView.findViewById(R.id.spinnerBathPosTime);
        Spinner mSpinnerBathDuringTime = (Spinner) mView.findViewById(R.id.spinnerBathDuringTime);

        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.times));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinnerBathTime.setAdapter(mAdapter);
        mSpinnerBathPosTime.setAdapter(mAdapter);
        mSpinnerBathDuringTime.setAdapter(mAdapter);


        mSpinnerBathTime.setPrompt("B1");
        mSpinnerBathPosTime.setPrompt("B2");
        mSpinnerBathDuringTime.setPrompt("B3");

        SpinnerHandler listener = new SpinnerHandler();

        mSpinnerBathTime.setOnTouchListener(listener);
        mSpinnerBathTime.setOnItemSelectedListener(listener);
        mSpinnerBathPosTime.setOnTouchListener(listener);
        mSpinnerBathPosTime.setOnItemSelectedListener(listener);
        mSpinnerBathDuringTime.setOnTouchListener(listener);
        mSpinnerBathDuringTime.setOnItemSelectedListener(listener);

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

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
                if (!mName.getText().toString().isEmpty()) {
                    dialog.dismiss();
                    setNewNameCall(mName.getText().toString());
                } else {
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
            public void onServerCallback(final boolean status, String response) {

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (status) {
                            mProgressDialog.dismiss();
                            deviceTitle.setText(name);
                            nameFlag = false;
                        } else {
                            mProgressDialog.dismiss();
                            Toast.makeText(fragmentContext,
                                    R.string.dialog_set_name_fail,
                                    Toast.LENGTH_SHORT).show();
                            deviceTitle.setText(oldName);
                            device.setName(oldName);
                        }
                        enableAllFeatures();
                    }
                });
            }
        });
    }

    @Override
    public  Map<String,Integer> fillPositionsMap() {
        Map<String,Integer> convertPositionsMap = new HashMap<>();
        List<String> timesArray = Arrays.asList(getResources().getStringArray(R.array.times));
        int position = 0;
        for(String option: timesArray){
            convertPositionsMap.put(option,position);
            position++;
        }
        return convertPositionsMap;
    }

    @Override
    public int convert(int time) {


        return 0;
    }
}



