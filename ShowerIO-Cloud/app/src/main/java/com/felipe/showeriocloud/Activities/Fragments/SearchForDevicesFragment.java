package com.felipe.showeriocloud.Activities.Fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.util.ByteUtil;
import com.espressif.iot.esptouch.util.EspNetUtil;
import com.felipe.showeriocloud.Activities.Home.SplashScreen;
import com.felipe.showeriocloud.Activities.ShowerIO.ShowerNavigationDrawer;
import com.felipe.showeriocloud.Model.DevicePersistance;
import com.felipe.showeriocloud.Processes.RegisterNewDevices;
import com.felipe.showeriocloud.R;
import com.felipe.showeriocloud.Utils.EspUtils;
import com.felipe.showeriocloud.Utils.ServerCallback;
import com.felipe.showeriocloud.Utils.ServerCallbackObjects;
import com.github.ybq.android.spinkit.style.WanderingCubes;
import com.google.gson.Gson;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SearchForDevicesFragment extends Fragment implements View.OnClickListener {

    public static int RETRY = 0;
    private static int SPLASH_TIME_OUT = 4000;
    private Gson gson;
    public RequestQueue requestQueue;
    private static final String TAG = "SearchForDevices";
    private static final int REQUEST_PERMISSION = 0x01;

    private RegisterNewDevices registerNewDevices;
    @BindView(R.id.relative_layout)
    public RelativeLayout relativeLayout;
    @BindView(R.id.spin_kitSearch)
    public ProgressBar progressBar;


    private AlertDialog alertHelp;
    private AlertDialog alertSuccess;
    @BindView(R.id.ap_bssid_text)
    public TextView mApBssidTV;

    @BindView(R.id.WifiName)
    public TextView mApSsidTV;
    @BindView(R.id.ssidLayout)
    public TextInputLayout ssidLayout;
    @BindView(R.id.passwordLayout)
    public TextInputLayout passwordLayout;

    @BindView(R.id.textView2)
    public TextView findDevicesTV;
    @BindView(R.id.WifiPass)
    public EditText mApPasswordET;


    @BindView(R.id.confirm_btn)
    public Button mConfirmBtn;
    @BindView(R.id.device_count_edit)
    public EditText mDeviceCountET;
    @BindView(R.id.package_mode_group)
    public RadioGroup mPackageModeGroup;

    @BindView(R.id.message)
    public TextView mMessageTV;

    private OnFragmentInteractionListener mListener;


    public IEsptouchListener myListener = new IEsptouchListener() {

        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
            onEsptoucResultAddedPerform(result);
        }
    };

    public EsptouchAsyncTask4 mTask;

    private boolean mReceiverRegistered = false;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            assert wifiManager != null;

            switch (action) {
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    WifiInfo wifiInfo;
                    if (intent.hasExtra(WifiManager.EXTRA_WIFI_INFO)) {
                        wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                    } else {
                        wifiInfo = wifiManager.getConnectionInfo();
                    }
                    onWifiChanged(wifiInfo);
                    break;
                case LocationManager.PROVIDERS_CHANGED_ACTION:
                    onWifiChanged(wifiManager.getConnectionInfo());
                    onLocationChanged();
                    break;
            }
        }
    };

    private boolean mDestroyed = false;

    public SearchForDevicesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search_for_devices, container, false);
        ButterKnife.bind(this, view);

        WanderingCubes wanderingCubes = new WanderingCubes();
        progressBar.setIndeterminateDrawable(wanderingCubes);
        progressBar.setVisibility(View.GONE);

        Log.d(TAG, "Tnitializing Smart Config");
        mDeviceCountET.setText("1");
        mConfirmBtn.setEnabled(false);
        mConfirmBtn.setOnClickListener(this);
        findDevicesTV.setVisibility(View.GONE);
        mApSsidTV.setEnabled(false);

        //Calling help user to display a alert to help the user to search for a device
        this.helpUser();
        //Validation of password field, when there is nothing in the field, the confirm button is disabled
        this.validatePasswordField();

        if (isSDKAtLeastP()) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = {
                        Manifest.permission.ACCESS_COARSE_LOCATION
                };

                ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_PERMISSION);
            } else {
                registerBroadcastReceiver();
            }

        } else {
            registerBroadcastReceiver();
        }

        // Inflate the layout for this fragment
        return view;
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

    private boolean isSDKAtLeastP() {
        return Build.VERSION.SDK_INT >= 28;
    }

    private void onLocationChanged() {
        boolean enable;
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            enable = false;
        } else {
            boolean locationGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean locationNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            enable = locationGPS || locationNetwork;
        }

        if (!enable) {
            mMessageTV.setText(R.string.location_disable_message);
        }
    }

    private void onWifiChanged(WifiInfo info) {
        if (info == null) {
            mApSsidTV.setText("");
            mApSsidTV.setTag(null);
            mApBssidTV.setTag("");
            mMessageTV.setText("");
            mConfirmBtn.setEnabled(false);

            if (mTask != null) {
                mTask.cancelEsptouch();
                mTask = null;
                new AlertDialog.Builder(getContext())
                        .setMessage("Wifi disconnected or changed")
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        } else {
            String ssid = info.getSSID();
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            mApSsidTV.setText(ssid);
            mApSsidTV.setTag(ByteUtil.getBytesByString(ssid));
            byte[] ssidOriginalData = EspUtils.getOriginalSsidBytes(info);
            mApSsidTV.setTag(ssidOriginalData);

            String bssid = info.getBSSID();
            mApBssidTV.setText(bssid);

            if (mApPasswordET.getText().toString().equals("")) {
                mConfirmBtn.setEnabled(false);
            } else {
                mConfirmBtn.setEnabled(true);
            }
            mMessageTV.setText("");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int frequence = info.getFrequency();
                if (frequence > 4900 && frequence < 5900) {
                    // Connected 5G wifi. Device does not support 5G
                    mMessageTV.setText(R.string.wifi_5g_message);
                }
            }
        }
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        if (isSDKAtLeastP()) {
            filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        }
        getActivity().registerReceiver(mReceiver, filter);
        mReceiverRegistered = true;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    @Override
    public void onClick(View v) {

        if (v == mConfirmBtn) {
            byte[] ssid = mApSsidTV.getTag() == null ? ByteUtil.getBytesByString(mApSsidTV.getText().toString())
                    : (byte[]) mApSsidTV.getTag();
            byte[] password = ByteUtil.getBytesByString(mApPasswordET.getText().toString());
            byte[] bssid = EspNetUtil.parseBssid2bytes(mApBssidTV.getText().toString());
            byte[] deviceCount = mDeviceCountET.getText().toString().getBytes();
            byte[] broadcast = {(byte) (mPackageModeGroup.getCheckedRadioButtonId() == R.id.package_broadcast
                    ? 1 : 0)};

            if (mTask != null) {
                mTask.cancelEsptouch();
            }
            mTask = new EsptouchAsyncTask4(this, new ServerCallbackObjects() {
                @Override
                public void onServerCallbackObject(Boolean status, String response, List<Object> objects) {
                    List<IEsptouchResult> results = (List<IEsptouchResult>) (List<?>) objects;
                    onSearchSuccessfull(results);
                }
            });
            mTask.execute(ssid, bssid, password, deviceCount, broadcast);
        }

    }

    private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
//                String text = result.getBssid() + " is connected to the wifi";
//                Toast.makeText(SearchForDevices.this, text,
//                        Toast.LENGTH_LONG).show();
            }

        });
    }

    private void helpUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setPositiveButton(android.R.string.ok, null);
        builder.setMessage(R.string.find_devices_help);
        alertHelp = builder.create();
        alertHelp.show();
    }

    private void validatePasswordField() {
        mApPasswordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (mApPasswordET.getText().toString().equals("")) {
                    mConfirmBtn.setEnabled(false);
                } else {
                    mConfirmBtn.setEnabled(true);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")) {
                    mConfirmBtn.setEnabled(false);
                } else {
                    mConfirmBtn.setEnabled(true);
                }
            }
        });
    }

    public void onSearchSuccessfull(final List<IEsptouchResult> results) {
        final int countDevices = results.size();
        DevicePersistance.getAllDevicesFromUser(new ServerCallbackObjects() {
            @Override
            public void onServerCallbackObject(Boolean status, String response, List<Object> objects) {
                if (!status) {
                    Snackbar snackbar = Snackbar
                            .make(relativeLayout, "Erro ao contatar o servidor, tente novamente!", Snackbar.LENGTH_LONG)
                            .setDuration(32000);
                    snackbar.show();
                    onServerError();
                } else {
                    for (final IEsptouchResult item : results) {
                        registerNewDevices.createNewDevice(item, requestQueue, new ServerCallback() {
                            @Override
                            public void onServerCallback(final boolean status, String response) {
                                if (!status) {
                                    Snackbar snackbar = Snackbar
                                            .make(relativeLayout, "Erro ao contatar o servidor, tente novamente!", Snackbar.LENGTH_LONG)
                                            .setDuration(32000);
                                    snackbar.show();
                                    if (results.indexOf(item) == countDevices - 1) {
                                        onServerError();
                                    }
                                } else {
                                    Log.i(TAG, "onSearchSuccessfull() successfully saved the device!");
                                    if (results.indexOf(item) == countDevices - 1) {
                                        Intent listOfDevices = new Intent(getContext(), ShowerNavigationDrawer.class);
                                        startActivity(listOfDevices);
                                        getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                                        getActivity().finish();
                                    }
                                }

                            }
                        });
                    }
                }

            }
        });

    }

    void onServerError() {
        Intent splashScreen = new Intent(getContext(), SplashScreen.class);
        startActivity(splashScreen);
        getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        getActivity().finish();
    }
}
