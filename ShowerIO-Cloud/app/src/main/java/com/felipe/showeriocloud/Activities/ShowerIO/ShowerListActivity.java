package com.felipe.showeriocloud.Activities.ShowerIO;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.felipe.showeriocloud.Adapter.ShowerListAdapter;
import com.felipe.showeriocloud.Helper.RecyclerItemTouchHelper;
import com.felipe.showeriocloud.Model.DeviceDO;
import com.felipe.showeriocloud.Model.DevicePersistance;
import com.felipe.showeriocloud.R;
import com.github.ybq.android.spinkit.style.WanderingCubes;

import java.util.ArrayList;
import java.util.List;

public class ShowerListActivity extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private RecyclerView recyclerView;
    private CoordinatorLayout coordinatorLayout;
    public List<DeviceDO> showerDevicesList;
    private ShowerListAdapter mAdapter;
    private RequestQueue requestQueue;
    private final String SHOWERIO = "ShowerIO";
    private static final String TAG = "ShowerListActivity";
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.fragment_shower_list);

        toolbar = findViewById(R.id.toolbar);

        Log.d(TAG, "onCreate(): Defining and setting Toolbar title and configurations");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.list_showers));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.d(TAG, "onCreate(): Referencing recyclerView and coordinatorLayout by their xml layout id's");
        recyclerView = findViewById(R.id.recyclerShower);
        coordinatorLayout = findViewById(R.id.coordinator_layout);

        Log.d(TAG, "onCreate(): Instanciating an Array list_back of the java POJO ShowerDevice");
        showerDevicesList = new ArrayList<>();

        Log.d(TAG, "onCreate(): Instanciating ShowerListAdapter which will iterate the showerDevicesList through UI");
        mAdapter = new ShowerListAdapter(this, showerDevicesList);

        Log.d(TAG, "onCreate(): Instanciating RecyclerView and adding the decorators");
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
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
    }

    public void fetchDevicesInUI() {
        showerDevicesList.clear();
        showerDevicesList.addAll(DevicePersistance.lastUpdateUserDevices);

        if (showerDevicesList.size() == 0) {
            Log.d(TAG, "fetchDevicesInUI(): Any devices were found, calling toast to alert user");
            Toast.makeText(getApplicationContext(), "Nenhum dispositivo foi adicionado, efetue uma nova busca", Toast.LENGTH_LONG).show();
        } else {
            Log.i(TAG, "fetchDevicesInUI(): Adding found devices List<ShowerDevice> and notifying Adapter that new data was inserted");
            mAdapter.notifyDataSetChanged();
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
                toolbar.setVisibility(View.GONE);

/*                String selectedDeviceAsString = new Gson().toJson(selectedDevice);
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

    private void helpUser() {
        // showing snack bar to help user to use the application
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Deslize para esquerda para escolher um chuveiro!", Snackbar.LENGTH_LONG)
                .setDuration(32000);
        snackbar.show();
    }

}
