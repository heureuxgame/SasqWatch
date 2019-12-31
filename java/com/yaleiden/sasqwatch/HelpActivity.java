package com.yaleiden.sasqwatch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Created by Yale on 7/13/2015.
 */
public class HelpActivity extends AppCompatActivity{
    private String TAG = "HelpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_blank);

        setupFragment();
    }

    private void setupFragment(){

            Log.d(TAG, "SetupFragments");
            // Create a new Fragment to be placed in the activity layout
            HelpFragment helpFragment = new HelpFragment();

            FragmentManager setupFragmentManager = getSupportFragmentManager();
            FragmentTransaction setupTransaction = setupFragmentManager.beginTransaction();
            setupTransaction.add(R.id.container, helpFragment, "helpFragment");
            setupTransaction.commit();
            Log.d(TAG, " oncreate fragment_container != null");


        }
    // Initiating Menu XML file (menu.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_basic, menu);
        return true;
    }

    /**
     * Event Handling for Individual menu item selected
     * Identify single menu item by it's id
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_map:
                Intent mymapIntent = new Intent(HelpActivity.this, MapsActivity.class);
                startActivity(mymapIntent);
                return true;

            case R.id.menu_profile:
                Intent myIntent = new Intent(HelpActivity.this, SignUpActivity.class);
                startActivity(myIntent);
                return true;

            case R.id.menu_sightings:
                Intent sightIntent = new Intent(HelpActivity.this, SightingListActivity.class);
                startActivity(sightIntent);
                return true;

            case R.id.menu_help:
                Intent helpIntent = new Intent(HelpActivity.this, HelpActivity.class);
                //myIntent.putExtra("key", value); //Optional parameters
                startActivity(helpIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}