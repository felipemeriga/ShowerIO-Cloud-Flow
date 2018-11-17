package com.felipe.showeriocloud.Activities.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.felipe.showeriocloud.Adapter.ShowerListAdapter;
import com.felipe.showeriocloud.Helper.RecyclerItemTouchHelper;
import com.felipe.showeriocloud.Model.DeviceDO;
import com.felipe.showeriocloud.Model.DevicePersistance;
import com.felipe.showeriocloud.R;
import com.github.ybq.android.spinkit.style.WanderingCubes;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShowerListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ShowerListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShowerListFragment extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private static final String TAG = "ShowerListActivity";
    private CoordinatorLayout coordinatorLayout;
    public List<DeviceDO> showerDevicesList;
    private ShowerListAdapter mAdapter;


    private OnFragmentInteractionListener mListener;

    public ShowerListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ShowerListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShowerListFragment newInstance(String param1, String param2) {
        ShowerListFragment fragment = new ShowerListFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shower_list, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.spin_kit);
        WanderingCubes wanderingCubes = new WanderingCubes();
        progressBar.setIndeterminateDrawable(wanderingCubes);
        progressBar.setVisibility(View.GONE);

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


    //As ShowerListActivity implements RecyclerItemTouchHelperListener, the method onSwiped is being Overrided
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ShowerListAdapter.MyViewHolder) {
            // get the removed shower name to display it in snack bar
            final DeviceDO selectedDevice = showerDevicesList.get(viewHolder.getAdapterPosition());
            String name = showerDevicesList.get(viewHolder.getAdapterPosition()).getName();

            if (selectedDevice.getStatus().equals("ONLINE")) {
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
        /*
                toolbar.setVisibility(View.GONE);
                String selectedDeviceAsString = new Gson().toJson(selectedDevice);
                Log.i("ShowerListActivity", "onServerCallback(), request to credentials went successful");
                Intent loginActivity = new Intent(ShowerListActivity.this, LoginActivity.class);
                loginActivity.putExtra("device", selectedDeviceAsString);
                startActivity(loginActivity);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);*/
                // TODO - Handle the device selection


            } else {

                // TODO - Handle when the device is OFFLINE but some reason

            }
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void helpUser() {
        // showing snack bar to help user to use the application
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Deslize para esquerda para escolher um chuveiro!", Snackbar.LENGTH_LONG)
                .setDuration(32000);
        snackbar.show();
    }
}
