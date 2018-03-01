package com.example.manuel.thingseedemo;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.manuel.thingseedemo.fragments.About;
import com.example.manuel.thingseedemo.fragments.Help;
import com.example.manuel.thingseedemo.fragments.History;
import com.example.manuel.thingseedemo.fragments.Logs;
import com.example.manuel.thingseedemo.fragments.Map;
import com.example.manuel.thingseedemo.fragments.Track;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //changing map fragment to default view, since it starts as empty, better fix needed if possible
        if (savedInstanceState == null) {
            navigationView = (NavigationView) findViewById(R.id.nav_view);
            MenuItem item =  navigationView.getMenu().getItem(0);
            onNavigationItemSelected(item);
        }



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.help) {
            Fragment fragment = new Help();
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragmentLoader,fragment);
            fragmentTransaction.commit();

            setTitle(item.getTitle());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment fragment = null;

        int id = item.getItemId();

        switch (id){

            case R.id.map:
                fragment = new Map();
                break;

            case R.id.logs:
                fragment = new Logs();
                break;

            case R.id.track:
                fragment = new Track();
                break;

            case R.id.history:
                fragment = new History();
                break;

            case R.id.settings:
                fragment = new Settings();
                break;

            case R.id.about:
                fragment = new About();
                break;

        }



        if(fragment!=null){
            android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragmentLoader,fragment);
            fragmentTransaction.commit();

            // make selected item darker, does not look very good
            item.setChecked(true);

            setTitle(item.getTitle());
        }
        else {
            Log.e("Error", "Not able create a fragment");
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);


        return true;
    }
}
