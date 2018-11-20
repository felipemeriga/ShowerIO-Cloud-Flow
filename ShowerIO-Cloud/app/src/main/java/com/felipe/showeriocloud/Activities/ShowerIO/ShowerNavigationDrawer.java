package com.felipe.showeriocloud.Activities.ShowerIO;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.felipe.showeriocloud.Activities.Fragments.HelpFragment;
import com.felipe.showeriocloud.Activities.Fragments.ShowerDetailFragment;
import com.felipe.showeriocloud.Activities.Fragments.ShowerListFragment;
import com.felipe.showeriocloud.Activities.Home.SplashScreen;
import com.felipe.showeriocloud.Activities.SmartConfig.SearchForDevices;
import com.felipe.showeriocloud.Model.DeviceDO;
import com.felipe.showeriocloud.Model.DevicePersistance;
import com.felipe.showeriocloud.R;
import com.felipe.showeriocloud.Utils.FacebookInformationSeeker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ShowerNavigationDrawer extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, HelpFragment.OnFragmentInteractionListener, ShowerListFragment.OnFragmentInteractionListener, ShowerDetailFragment.OnFragmentInteractionListener {

    protected NavigationView navigationView;
    private ImageView imageView;
    private TextView usernameTitle;
    private LinearLayout linearLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shower_base);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        final View hView =  navigationView.getHeaderView(0);


        // TODO - FACEBOOK AND COGNITO NAME - CHANGE HERE
        linearLayout = (LinearLayout) hView.findViewById(R.id.nav_header_linear);
        imageView = (ImageView) hView.findViewById(R.id.imageView);
        usernameTitle = (TextView) hView.findViewById(R.id.username);

        usernameTitle.setText(FacebookInformationSeeker.facebookName);
//        Picasso.get().load(FacebookInformationSeeker.facebookProfilePhotoUrl).into(imageView);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }


    private void startBaseFragment() {
        Fragment fragment = new ShowerListFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.base, fragment).commit();
        navigationView.getMenu().getItem(0).setChecked(true);
        setTitle(getString(R.string.nav_list_of_devices));
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.shower_base, menu);
        startBaseFragment();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment fragment = null;
        Class fragmentClass;
        fragmentClass = ShowerListFragment.class;

        switch (item.getItemId()) {
            case R.id.nav_find_devices:
                Intent findDevices = new Intent(ShowerNavigationDrawer.this, SearchForDevices.class);
                startActivity(findDevices);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                finish();
            case R.id.nav_manage:
//                fragmentClass = SecondFragment.class;
                break;
            case R.id.nav_help:
                fragmentClass = HelpFragment.class;
                break;
            case R.id.nav_account:
//                fragmentClass = ThirdFragment.class;
                break;
            case R.id.nav_share:
//                fragmentClass = ThirdFragment.class;
                break;
            default:
                fragmentClass = ShowerListFragment.class;
        }


        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.base, fragment).commit();

        item.setChecked(true);

        setTitle(item.getTitle());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onSelectedDevice(DeviceDO deviceDO) {
        DevicePersistance.selectedDevice = deviceDO;
        Fragment detailFragment = new ShowerDetailFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().remove(fragmentManager.getFragments().get(0));
        fragmentManager.beginTransaction().replace(R.id.base, detailFragment).commit();
        navigationView.getMenu().getItem(0).setChecked(false);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
