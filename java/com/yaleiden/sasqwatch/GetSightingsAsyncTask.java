package com.yaleiden.sasqwatch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
//import com.yaleiden.sasqwatch.backend.bsightingEndpoint.model.Bsighting;
//import com.yaleiden.sasqwatch.backend.bsightingEndpoint.BsightingEndpoint;
import com.yaleiden.sasqwatch.backend.bsightingApi.BsightingApi;
import com.yaleiden.sasqwatch.backend.bsightingApi.model.Bsighting;
import com.yaleiden.sasqwatch.backend.bsightingApi.model.CollectionResponseBsighting;
import com.yaleiden.sasqwatch.backend.postsApi.model.Posts;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by Yale on 6/19/2015.
 */
public class GetSightingsAsyncTask extends AsyncTask<Void, Void, CollectionResponseBsighting> {
    private static BsightingApi myApiService = null;
    private Context context;
    private MapsActivity activity;
    private String nextCursor;
    private String TAG = "SightingAsyncTask";
    private ProgressDialog pd;
    OnSightingsFetchedListner sCallback;
    private String server_address;
    private List<Bsighting> bsightings;
    private String lastCursor;

    GetSightingsAsyncTask(Context context, MapsActivity activity, String nextCursor) {
        this.context = context;
        this.activity = activity;
        this.nextCursor = nextCursor;
        lastCursor = nextCursor;
        Log.d(TAG, "GetSightingsAsyncTask");
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "onPreExecute");
        super.onPreExecute();
        pd = new ProgressDialog(activity);
        pd.setTitle("Getting latest encounters.");
        pd.setMessage("Please wait.");
        pd.setCancelable(false);
        pd.setIndeterminate(true);
        pd.show();
        boolean localRun = activity.getResources().getBoolean(R.bool.local_run);
        if (localRun == true) {
            server_address = activity.getResources().getString(R.string.run_local);
        }
        if (localRun == false) {
            server_address = activity.getResources().getString(R.string.run_server);
        }
    }

    @Override
    protected CollectionResponseBsighting doInBackground(Void... params) {
        CollectionResponseBsighting responseBsighting = null;
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
            responseBsighting = myApiService.list().setCursor(nextCursor).setLimit(12).execute();
            return responseBsighting;
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(CollectionResponseBsighting responseBsighting) {
        super.onPostExecute(responseBsighting);
        Log.d(TAG, "onPostExecute");
        String msg = "Message";
        pd.dismiss();
        int count = 0;

        //Get the CollectionResponse and check if it is null
        if (responseBsighting != null) {
            bsightings = responseBsighting.getItems();
            nextCursor = responseBsighting.getNextPageToken();

            if (bsightings == null) {
                Log.d(TAG, "onPostExecute result null.");
                msg = "Sorry. No records returned from the server.";
            }
            if (null != bsightings) {
                count = bsightings.size();
                //Log.d(TAG, bsightings.toString());
                Log.d(TAG, "onPostExecute result size " + bsightings.size());

                if (bsightings.size() != 0) {
                    activity.addDownloadMarkerList(bsightings, nextCursor, lastCursor);
                    msg = count+" encounters downloaded.";
                }
                if (bsightings.size() == 0) {

                    msg = "End of list.";
                }
            }
        } else {
            msg = "Sorry. No response from the server.";
        }
        //List<Bsighting> theList = result;
        infoToast(msg);

    }

    private void infoToast(String s) {
        Toast toast = Toast.makeText(context,s,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    public interface OnSightingsFetchedListner {
        void updateSightings(List<Bsighting> data);
    }
}
