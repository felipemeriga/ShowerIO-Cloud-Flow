package com.felipe.showeriocloud.Activities.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.felipe.showeriocloud.Activities.Authentication.LoginActivity;
import com.felipe.showeriocloud.Activities.Home.SplashScreen;
import com.felipe.showeriocloud.Activities.ShowerIO.ShowerNavigationDrawer;
import com.felipe.showeriocloud.Activities.SmartConfig.SearchForDevices;
import com.felipe.showeriocloud.Adapter.ShowerListAdapter;
import com.felipe.showeriocloud.Helper.RecyclerItemTouchHelper;
import com.felipe.showeriocloud.Model.DeviceDO;
import com.felipe.showeriocloud.Model.DevicePersistance;
import com.felipe.showeriocloud.R;
import com.felipe.showeriocloud.Utils.ServerCallback;
import com.github.ybq.android.spinkit.style.WanderingCubes;

import java.util.ArrayList;
import java.util.List;

public class ShowerListFragment extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private RecyclerView recyclerView;
    private static final String TAG = "ShowerListActivity";
    private CoordinatorLayout coordinatorLayout;
    public List<DeviceDO> showerDevicesList;
    private ShowerListAdapter mAdapter;


    private OnFragmentInteractionListener mListener;

    public ShowerListFragment() {
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
        View view = inflater.inflate(R.layout.fragment_shower_list, container, false);
        WanderingCubes wanderingCubes = new WanderingCubes();


        Log.d(TAG, "onCreate(): Referencing recyclerView and coordinatorLayout by their xml layout id's");
        recyclerView = view.findViewById(R.id.recyclerShower);
        coordinatorLayout = view.findViewById(R.id.coordinator_layout);

        Log.d(TAG, "onCreate(): Instanciating an Array list_back of the java POJO ShowerDevice");
        showerDevicesList = new ArrayList<>();

        Log.d(TAG, "onCreate(): Instanciating ShowerListAdapter which will iterate the showerDevicesList through UI");
        mAdapter = new ShowerListAdapter(getContext(), showerDevicesList);

        Log.d(TAG, "onCreate(): Instanciating RecyclerView and adding the decorators");
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        // adding item touch helper
        // only ItemTouchHelper.LEFT added to detect Right to Left swipe
        // if you want both Right -> Left and Left -> Right
        // add pass ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT as param

        //Where a new instance RecyclerItemTouchHelper is created, as the ShowerListAcitivty implements the interface RecyclerItemTouchHelperListener, it can be passed as a parameter
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        Log.i(TAG, "onCreate(): Calling fetchDevicesInUI, to populate RecyclerView ");
        helpUser();
        fetchDevicesInUI();
        return view;
    }

    public void fetchDevicesInUI() {
        showerDevicesList.clear();
        showerDevicesList.addAll(DevicePersistance.lastUpdateUserDevices);

        if (showerDevicesList.size() == 0) {
            Log.d(TAG, "fetchDevicesInUI(): Any devices were found, calling toast to alert user");
            Toast.makeText(getContext(), "Nenhum dispositivo foi adicionado, efetue uma nova busca", Toast.LENGTH_LONG).show();
        } else {
            Log.i(TAG, "fetchDevicesInUI(): Adding found devices List<ShowerDevice> and notifying Adapter that new data was inserted");
            mAdapter.notifyDataSetChanged();
        }

    }

    // Rename method, update argument and hook method into UI event
    public void onlineDeviceSelected(DeviceDO device) {
        if (mListener != null) {
            mListener.onSelectedDevice(device);
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


    //As ShowerListActivity implements RecyclerItemTouchHelperListener, the method onSwiped is being Overrided
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ShowerListAdapter.MyViewHolder) {
            // get the removed shower name to display it in snack bar
            final DeviceDO selectedDevice = showerDevicesList.get(viewHolder.getAdapterPosition());
            String name = showerDevicesList.get(viewHolder.getAdapterPosition()).getName();

            if (selectedDevice.getStatus().equals("ONLINE")) {
                recyclerView.setVisibility(View.GONE);
                onlineDeviceSelected(selectedDevice);
            } else {
                handleOfflineDevice();
            }
        }
    }

    void handleOfflineDevice() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.offline_title);
        builder.setMessage(R.string.offline_text);
        builder.setCancelable(false);
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent loginActivity = new Intent(getActivity(), SearchForDevices.class);
                startActivity(loginActivity);
                getActivity().finish();
            }
        });

        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent navegationDrawer = new Intent(getActivity(), ShowerNavigationDrawer.class);
                startActivity(navegationDrawer);
                getActivity().finish();
            }
        });
        builder.show();

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onSelectedDevice(DeviceDO deviceDO);
    }

    private void helpUser() {
        // showing snack bar to help user to use the application
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Deslize para esquerda para escolher um chuveiro!", Snackbar.LENGTH_LONG)
                .setDuration(32000);
        snackbar.show();
    }
}
