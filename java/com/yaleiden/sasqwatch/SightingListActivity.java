package com.yaleiden.sasqwatch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

//import com.yaleiden.sasqwatch.backend.postsApi.model.;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.GoogleMap;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.yaleiden.sasqwatch.backend.bsightingApi.BsightingApi;
import com.yaleiden.sasqwatch.backend.bsightingApi.model.Bsighting;
import com.yaleiden.sasqwatch.backend.bsightingApi.model.CollectionResponseBsighting;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Yale on 7/5/2015.
 */
public class SightingListActivity extends AppCompatActivity implements SightingListViewFragment.StartCommentListener {
    private String TAG = "SightingListActivity";
    SightingListViewFragment sightingListViewFragment;
    List<Bsighting> downloadedSightings;
    String nextCursor;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Oncreate");
        setContentView(R.layout.activity_sightings);

        Intent intent = getIntent();
        long postSigthing = intent.getLongExtra("sighting", 999);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("SasqWatch Encounter Reports");

        mAdView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        nextCursor = null;
        setupFragments(); //moved all of the setup to a method outside ONcreate
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Add the fragment to the 'fragment_container' FrameLayout
        downloadSightings();
    }

    private void setupFragments() {
        Log.d(TAG, "SetupFragments");
        // Create a new Fragment to be placed in the activity layout
        sightingListViewFragment = new SightingListViewFragment();

        FragmentManager setupFragmentManager = getSupportFragmentManager();
        FragmentTransaction setupTransaction = setupFragmentManager.beginTransaction();
        setupTransaction.add(R.id.container, sightingListViewFragment, "sightingListViewFragment");
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
                Intent mymapIntent = new Intent(SightingListActivity.this, MapsActivity.class);
                startActivity(mymapIntent);
                return true;

            case R.id.menu_profile:
                Intent myIntent = new Intent(SightingListActivity.this, SignUpActivity.class);
                startActivity(myIntent);
                return true;

            case R.id.menu_sightings:
                Intent sightIntent = new Intent(SightingListActivity.this, SightingListActivity.class);
                //myIntent.putExtra("key", value); //Optional parameters
                startActivity(sightIntent);
                return true;

            case R.id.menu_help:
                Intent helpIntent = new Intent(SightingListActivity.this, HelpActivity.class);
                //myIntent.putExtra("key", value); //Optional parameters
                startActivity(helpIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void downloadSightings() {
        Log.d(TAG, "downloadSightings");
        downloadedSightings = null;
        //infoToast("Downloading encounters.");
            new GetSightingsListAsyncTask(this).execute();

    }

    @Override
    public void commentIntent(long id) {
        Intent commentIntent = new Intent(SightingListActivity.this, CommentActivity.class);
        commentIntent.putExtra("sighting_id", id);
        startActivity(commentIntent);
    }

    public class GetSightingsListAsyncTask extends AsyncTask<Void, Void, CollectionResponseBsighting> {
        private BsightingApi myApiService = null;
        private Context context;
        private Activity activity;

        private ProgressDialog pd;
        private String server_address;
        private List<Bsighting> bsightings;

        GetSightingsListAsyncTask(
                Context context)
        {
            this.context = context;
            Log.d(TAG, "GetSightingsAsyncTask");
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute");
            super.onPreExecute();
            pd = new ProgressDialog(context);
            pd.setTitle("Getting encounters");
            pd.setMessage("Please wait.");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();

            boolean localRun = getResources().getBoolean(R.bool.local_run);
            if (localRun == true) {
                server_address = getResources().getString(R.string.run_local);
            }
            if (localRun == false) {
                server_address = getResources().getString(R.string.run_server);
            }

        }

        @Override
        protected CollectionResponseBsighting doInBackground(Void... params) {
            if (myApiService == null) {  // Only do this once
                BsightingApi.Builder builder = new BsightingApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        // options for running against local devappserver
                        // - 10.0.2.2 is localhost's IP address in Android emulator
                        // - turn off compression when running against local devappserver
                        //.setRootUrl("http://10.0.2.2:8080/_ah/api/")
                        //.setRootUrl("http://192.168.1.4:8080/_ah/api/") //local dev Lake
                        //.setRootUrl("http://192.168.0.162:8080/_ah/api/") //local dev Shelly's
                        //.setRootUrl("https://" + "propane-atrium-97913" + ".appspot.com/_ah/api/") //Live
                        .setRootUrl(server_address)
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                // end options for devappserver

                myApiService = builder.build();
            }

            try {
                //set the nextpage cursor and limit on records downloaded here
                return myApiService.list().setCursor(nextCursor).setLimit(12).execute();
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(CollectionResponseBsighting responseBsighting) {
            super.onPostExecute(responseBsighting);
            Log.d(TAG, "onPostExecute");
            String msg = "Message";
            pd.dismiss();

            //Get the CollectionResponse and check if it is null
            if (responseBsighting != null) {
                bsightings = responseBsighting.getItems();
                nextCursor = responseBsighting.getNextPageToken();

                if (bsightings == null) {
                    Log.d(TAG, "onPostExecute result null.");
                    msg = "Sorry. No records returned from the server.";
                }

                if (null != bsightings) {
                    //Log.d(TAG, bsightings.toString());
                    Log.d(TAG, "onPostExecute result size " + bsightings.size());

                    if (bsightings.size() != 0) {
                        sightingListViewFragment.updateSightings(bsightings);
                        msg = bsightings.size()+" encounters downloaded.";
                    }
                    if (bsightings.size() == 0) {

                        msg = "End of list.";
                    }
                }
            } else {
                msg = "Sorry. No response from the server.";
            }

            infoToast(msg);

        }
    }
    private void infoToast(String s) {
        Toast toast = Toast.makeText(this,s,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }
}
