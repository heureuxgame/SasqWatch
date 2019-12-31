package com.yaleiden.sasqwatch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;

import android.location.Location;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.util.DateTime;

import com.yaleiden.sasqwatch.backend.bsightingApi.model.Bsighting;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.yaleiden.sasqwatch.util.HelperStep;
import com.yaleiden.sasqwatch.util.Inventory;
import com.yaleiden.sasqwatch.util.Purchase;
import com.yaleiden.sasqwatch.util.ResultHelp;

public class MapsActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMapLongClickListener, DialogEnterSighting.OnSightingSaveListener, GoogleMap.OnMapLoadedCallback {

    private String TAG = "MapsActivity";
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
    //displays users last location
    private TextView textViewMmapInfo;

    CameraPosition cp; //to save users previous position
    DialogEnterSighting editNameDialog;
    Bsighting newSighting;

    float startingZoom = 3f;
    private LatLng centerRange = new LatLng(53.4f, -109.2f);
    List<Bsighting> downloadedSightings;
    HashMap sightingMarkerMap;
    private AdView mAdView;
    private String nextCursor;
    private List<String> cursorList;
    private Button buttonNewer;
    private Button buttonOlder;
    private int cursorCount;
    HashMap<String, List<Bsighting>> cursorMap;
    boolean newer;
    boolean older;
    Bitmap sightingIcon;
    Bitmap audialIcon;
    Bitmap signIcon;

    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    static final String SKU_PREMIUM = "premium";

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    static final int SIGHTING_REQUEST = 10002;
    // The helper object
    HelperStep mHelper;
    int purchase_actions;
    // Does the user have the premium upgrade?
    private boolean mIsPremium = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate");
        setContentView(R.layout.activity_maps);
        nextCursor = null;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("SasqWatch Encounter Map");

        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        mAdView = (AdView) findViewById(R.id.ad_view);
        buttonOlder = (Button) findViewById(R.id.buttonOlder);
        buttonNewer = (Button) findViewById(R.id.buttonNewer);
        textViewMmapInfo = (TextView) findViewById(R.id.textViewMmapInfo);


        sightingIcon = BitmapFactory.decodeResource(getResources(), R.drawable.map_icon32);
        audialIcon = BitmapFactory.decodeResource(getResources(), R.drawable.map_icon48);
        //signIcon = BitmapFactory.decodeResource(getResources(), R.drawable.large_icon);

        loadData();//IAP

        cursorMap = new HashMap<String, List<Bsighting>>();
        cursorList = new ArrayList<>();
        sightingMarkerMap = new HashMap<Marker, Bsighting>();

        newer = false;
        older = false;

        //setUpMapIfNeeded();

        buildGoogleApiClient();

        buttonOlder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "Older");
                older = true;
                handleMap();

            }
        });
        buttonNewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Newer");
                newer = true;
                handleMap();

            }
        });

    }


/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        //toastIt("onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }
*/





    void loadData() {
        SharedPreferences purchaseSettings = getSharedPreferences("BUY_PREFS", Context.MODE_PRIVATE);
        purchase_actions = purchaseSettings.getInt("actions", 0);
    }

    void saveData() {
        SharedPreferences purchaseSettings = getSharedPreferences("BUY_PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = purchaseSettings.edit();
        editor.putInt("actions", 0);
        editor.commit();
    }


    private void infoToast(String s) {
        Toast toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (sightingMarkerMap != null) {
            removeMarkers(sightingMarkerMap);
        }

        setUpMapIfNeeded();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
        Log.d(TAG, "onPause");
        if (mMap != null) {
            cp = mMap.getCameraPosition();
        }
        mMap = null;

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();

        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
        Log.d(TAG, "onDestroy");
    }

    // Initiating Menu XML file (menu.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Event Handling for Individual menu item selected
     * Identify single menu item by it's id
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_hybrid:
                // Single menu item is selected do something
                // Ex: launching new activity/screen or show alert message
                cp = mMap.getCameraPosition();
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                setUpMap();
                return true;

            case R.id.menu_normal:
                cp = mMap.getCameraPosition();
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                setUpMap();
                return true;

            case R.id.menu_profile:
                Intent myIntent = new Intent(MapsActivity.this, SignUpActivity.class);
                startActivity(myIntent);
                return true;

            case R.id.menu_sightings:
                Intent sightIntent = new Intent(MapsActivity.this, SightingListActivity.class);
                //myIntent.putExtra("key", value); //Optional parameters
                startActivity(sightIntent);
                return true;

            case R.id.menu_help:
                Intent helpIntent = new Intent(MapsActivity.this, HelpActivity.class);
                //myIntent.putExtra("key", value); //Optional parameters
                startActivity(helpIntent);
                return true;

            case R.id.menu_clear:
                SharedPreferences userSettings = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = userSettings.edit();
                editor.putString("user_name", "");
                editor.putLong("user_id", 999);

                editor.commit();
                return true;

            case R.id.menu_pref:
                Intent prefIntent = new Intent(MapsActivity.this, PrefsActivity.class);
                //myIntent.putExtra("key", value); //Optional parameters
                startActivity(prefIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        Log.d(TAG, "setUpMapIfNeeded");
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.setOnMapLoadedCallback(this);
        }
        // Check if we were successful in obtaining the map.
        if (mMap != null) {


            //set click listener

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker) {

                    marker.showInfoWindow();

                    return true;
                }
            });
            //set window adapter
            mMap.setInfoWindowAdapter(new MarkerWindowAdapter(this, sightingMarkerMap));
            //setUpMap();
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    public void setUpMap() {
        Log.d(TAG, "setUpMap");
        if (mMap == null) {
            Log.d(TAG, "setUpMap() mMap is null");
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }
        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            Log.d(TAG, "setUpMap() mMap is not null");

            downloadedSightings = null;

            //cursorList = new ArrayList<>();
            //sightingMarkerMap = new HashMap<Marker, Bsighting>();
            //cursorCount = 1;

            newer = false;
            older = false;
            handleMap();  //get sightings from app engine
            //handleMap();

            setUpMapUpdateLoc();
            mMap.setOnMapLongClickListener(MapsActivity.this);
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                   // Toast.makeText(MapsActivity.this, "The Window Was Clicked", Toast.LENGTH_LONG).show();
                    Bsighting clickSighting = (Bsighting) sightingMarkerMap.get(marker);
                    if (clickSighting != null && null != clickSighting.getId()) {
                        Intent commentIntent = new Intent(MapsActivity.this, CommentActivity.class);
                        commentIntent.putExtra("sighting_id", clickSighting.getId());
                        startActivity(commentIntent);
                    } else {
                        infoToast("Marker does not have any data.");
                    }

                }
            });

            //mMap.addMarker(new MarkerOptions().position(centerRange).title("Marker"));
            if (cp != null) {
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerRange, startingZoom));
            }

            cp = null;
        }
    }

    public void addSighting(Bsighting s) {
        if (null == downloadedSightings) {
            downloadedSightings = new ArrayList<Bsighting>();
        }
        downloadedSightings.add(s);
        placeMarkers(downloadedSightings);
    }
/*
Get last user location. Camera pos to center. Add user location to map.
*/

    private void setUpMapUpdateLoc() {
        Log.d(TAG, "setUpMapUpdateLoc");
        if (mLastLocation != null) {
            if (mMap != null) {

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerRange, startingZoom));

                mapTheUser();
            }

        }
    }

    private Location getLocation() {
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        return currentLocation;
    }

    private void mapTheUser() {
        Log.d(TAG, "mapTheUser");
        //Toast.makeText(this, "mapTheUser called", Toast.LENGTH_LONG).show();
        mLastLocation = getLocation();
        if (mMap != null) {
            //Toast.makeText(this, "mapTheUser called MAP not null", Toast.LENGTH_LONG).show();
            if (mLastLocation != null) {

               // Toast.makeText(this, "mapTheUser called location not null", Toast.LENGTH_LONG).show();

                LatLng newPos = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                Marker you = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(newPos).title("You"));
                Bsighting yourLoc = new Bsighting().setBehavior(" ").setComment("Your last location").setState(" ").setLat(mLastLocation.getLatitude())
                        .setLng(mLastLocation.getLongitude()).setDate(new DateTime(new Date()));
                //Marker m = mMap.addMarker(new MarkerOptions().position(pos));
               // Toast.makeText(this, "position " + newPos.toString(), Toast.LENGTH_LONG).show();
                if (sightingMarkerMap != null) {
                    sightingMarkerMap.put(you, yourLoc);
                }


            }

        }
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            //mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            //setUpMapUpdateLoc();
            mapTheUser();
        } else {

            infoToast("No location detected");
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    boolean isUserSetup() {
        boolean result = false;
        SharedPreferences userSettings = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        String username = userSettings.getString("user_name", null);
        Long userid = userSettings.getLong("user_id", 999);

        if (userid != 999) {
            result = true;
        }

        return result;
    }

    @Override
    public void onMapLongClick(LatLng point) {
        Log.d(TAG, "onMapLongClick");
//Check if user is setup -- should have SquatchUsers id
        if (isUserSetup() == false) {
            Intent myIntent = new Intent(MapsActivity.this, SignUpActivity.class);
            //myIntent.putExtra("key", value); //Optional parameters
            startActivity(myIntent);
        } else {

            newSighting = new Bsighting();
            newSighting.setLat(point.latitude);
            newSighting.setLng(point.longitude);
            mMap.addMarker(new MarkerOptions()
                    .position(point)
                    .title("Squatch")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            showSightingDialog();
        }
    }

    private void showSightingDialog() {
        Log.d(TAG, "showSightingDialog");
        FragmentManager fm = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putDouble("latitude", newSighting.getLat());
        bundle.putDouble("longitude", newSighting.getLng());
        // set Fragmentclass Arguments

        editNameDialog = new DialogEnterSighting();
        //editNameDialog.setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        editNameDialog.setArguments(bundle);
        editNameDialog.show(fm, "fragment_sighting");
    }

    @Override
    public void OnSightingSaved(String comment, String state, String behavior, String encounter, String sign, String image) {
        //Toast.makeText(this, "Button clicked " + habitat + " " + " " + comment, Toast.LENGTH_LONG).show();
        editNameDialog.dismiss();
        Log.d(TAG, "OnSightingSaved");
        SharedPreferences userSettings = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        String username = userSettings.getString("user_name", null);
        Long userid = userSettings.getLong("user_id", 999);
        Integer userrate = userSettings.getInt("user_rate", 0);
        newSighting.setOwnerrating(userrate);
        newSighting.setOwnername(username);
        newSighting.setOwnerid(userid);
        newSighting.setState(state);
        newSighting.setBehavior(behavior);
        newSighting.setComment(comment);
        newSighting.setEncounter(encounter);
        newSighting.setSigntype(sign);
        java.util.Date now = new Date();
        newSighting.setDate(new DateTime(now));
        newSighting.setImage(image);
        newSighting.setCommentcount(0);

        cp = mMap.getCameraPosition();

        new AddSightingAsyncTask(this, MapsActivity.this).execute(newSighting);
    }

    /*
        public void downloadSightings() {
            Log.d(TAG, "downloadSightings");

            //infoToast("Downloading list of Squatch encounters.");
            new GetSightingsAsyncTask(this, MapsActivity.this, nextCursor).execute();

        }
    */
    private void handleMap() {
        int listSize = cursorList.size();
        int resultsize = cursorMap.size();
        Log.d(TAG, "handleMap cursorList = " + listSize + " cursorMap = " + resultsize);
        Log.d(TAG, "cursorCount = " + cursorCount);
        Log.d(TAG, "older " + older + " newer " + newer);

        if (older == false && newer == false) {
            cursorMap = new HashMap<String, List<Bsighting>>();
            cursorList = new ArrayList<>();
            nextCursor = null;
            new GetSightingsAsyncTask(MapsActivity.this, MapsActivity.this, nextCursor).execute();
        }
        if (older) {
            Log.d(TAG, "older true");
            int getcount = cursorCount + 1;

            Log.d(TAG, "getcount / cursorCount + 1 " + getcount + " / " + getcount);
            if (getcount >= listSize) {
                Log.d(TAG, "getcount > listSize");
                new GetSightingsAsyncTask(MapsActivity.this, MapsActivity.this, nextCursor).execute();
            }
            if (getcount < listSize) {
                Log.d(TAG, "getcount <= listSize ");
                cursorCount = getcount;
                if (sightingMarkerMap != null) {
                    removeMarkers(sightingMarkerMap);
                }
                placeMarkers(cursorMap.get(cursorList.get(getcount)));
            }
        }
        if (newer) {
            Log.d(TAG, "cursorList size / cursorCount" + cursorList.size() + "/" + cursorCount);
            //Toast.makeText(MapsActivity.this, "Newer cursorCount = " + "cursorList size / cursorCount" + cursorList.size() + "/" + cursorCount, Toast.LENGTH_LONG).show();
            int grabcursor = cursorCount - 1;
            if (grabcursor < 0) {
                grabcursor = 0;
                cursorCount = 0;
                infoToast("Most recent encounters are already displayed.");
            } else {
                cursorCount = grabcursor;
            }
            if (sightingMarkerMap != null) {
                removeMarkers(sightingMarkerMap);
            }
            //***Debugging stuff
            /*
            Log.d(TAG, "Newer Cursor Debigging");
            printMap(cursorMap);
            for (int i = 0; i < cursorList.size(); i++) {
                Log.d(TAG, "cursorList " + cursorList.get(i) + " / " + i);
            }
*/
            //Log.d(TAG, "Newer listsize = " + listSize + " cursor# " + grabcursor + " cursor " + cursorMap.get(cursorList.get(grabcursor)));
            placeMarkers(cursorMap.get(cursorList.get(grabcursor)));
        }
    }

    /*
    Called in postexecute of the GetSightingsAsyncTask
     */
    public void addDownloadMarkerList(List<Bsighting> result, String nextcursor, String lastCursor) {
        Log.d(TAG, "addDownloadMarkerList");
        if (mMap != null) {

            //downloadedSightings = result;
            nextCursor = nextcursor;
            cursorMap.put(lastCursor, result);
            Log.d(TAG, "cursorList size " + cursorList.size());

            if (result.size() > 0) {
                downloadedSightings = null;
                //This will be the first run when the activity starts
                if (older == false && newer == false) {

                    cursorList.add("first");
                    cursorMap.put("first", result);
                    cursorCount = 1;
                    Log.d(TAG, "First Run cursorList size " + cursorList.size());

                }
                if (older == true && newer == false) {
                    cursorMap.put(lastCursor, result);
                    cursorList.add(lastCursor);
                    cursorCount = cursorCount + 1;
                }
                //***Debugging stuff
                /*
                Log.d(TAG, "Cursor Debigging");
                printMap(cursorMap);
                for (int i = 0; i < cursorList.size(); i++) {
                    Log.d(TAG, cursorList.get(i) + " / " + i);
                }
                */
                //***Debugging stuff
                if (sightingMarkerMap != null) {
                    removeMarkers(sightingMarkerMap);
                }
                downloadedSightings = result;
                placeMarkers(result);
                int listSize = cursorList.size();
                int resultsize = cursorMap.size();
                Log.d(TAG, "cursorList = " + listSize + " cursorMap = " + resultsize);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_REQUEST) {
            editNameDialog.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == SIGHTING_REQUEST) {
            if (mHelper == null) return;

            // Pass on the activity result to the helper for handling
            if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
                // not handled, so handle it ourselves (here's where you'd
                // perform any handling of activity results not related to in-app
                // billing...
                super.onActivityResult(requestCode, resultCode, data);
            } else {
                Log.d(TAG, "onActivityResult handled by IABUtil.");
            }
        }

    }

    private void placeMarkers(List<Bsighting> bs) {
        if (mMap != null) {
            Marker m = null;

            for (int i = 0; i < bs.size(); i++) {
                LatLng pos = new LatLng(bs.get(i).getLat(), bs.get(i).getLng());

                if (i == 0) {
                    String newDate = "No Date";
                    Date date = null;
                    try {
                        date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(bs.get(0).getDate().toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    try {
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        newDate = format.format(date);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    textViewMmapInfo.setText(newDate);
                }

                //Color code encounter types on map
                if (bs.get(i).getEncounter().equals("Sighting")) {
                    m = mMap.addMarker(new MarkerOptions().position(pos)
                            //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_icon_red))
                            .title("Squatch"));
                }
                if (bs.get(i).getEncounter().equals("Audial")) {
                    m = mMap.addMarker(new MarkerOptions().position(pos)
                            //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_icon_orange))
                            .title("Squatch"));
                }
                if (bs.get(i).getEncounter().equals("Sign")) {
                    m = mMap.addMarker(new MarkerOptions().position(pos)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_icon_yellow))
                            .title("Squatch"));
                }

                sightingMarkerMap.put(m, bs.get(i));

            }
            mapTheUser();
            newer = false;
            older = false;
        }

    }

    //Remove all markers from the list of displayed markers
    private void removeMarkers(HashMap<Marker, Bsighting> bs) {

        for (Marker key : bs.keySet()) {
            key.remove();
        }
    }

    private void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //System.out.println(pair.getKey() + " = " + pair.getValue());
            Log.d(TAG, "Cursor " + pair.getKey() + " = " + "result " + pair.getValue());
            //it.remove(); // avoids a ConcurrentModificationException
        }
    }

    @Override
    public void onMapLoaded() {
        Log.d(TAG, "onMapLoaded");
        //setUpMapIfNeeded();

        setUpMap();

        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("YOUR_DEVICE_HASH")
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);
        infoToast("Long press the map to record an encounter.");
    }


}
