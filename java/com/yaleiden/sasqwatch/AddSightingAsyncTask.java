package com.yaleiden.sasqwatch;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
//import com.yaleiden.sasqwatch.backend.bsightingEndpoint.BsightingEndpoint;
//import com.yaleiden.sasqwatch.backend.bsightingEndpoint.model.Bsighting;
import com.yaleiden.sasqwatch.backend.bsightingApi.BsightingApi;
import com.yaleiden.sasqwatch.backend.bsightingApi.model.Bsighting;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by Yale on 6/19/2015.
 */
public class AddSightingAsyncTask extends AsyncTask<Bsighting, Void, Bsighting> {
    private static BsightingApi myApiService = null;
    private Context context;
    private String TAG = "AddSightingAsyncTask";
    private MapsActivity activity;
    private ProgressDialog pd;
    private String server_address;

    AddSightingAsyncTask(Context context, MapsActivity activity) {
        this.context = context;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = new ProgressDialog(activity);
        pd.setTitle("Processing...");
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
    protected Bsighting doInBackground(Bsighting... params) {
        Bsighting result = null;
        Bsighting insertSigthing = params[0];
        if (myApiService == null) {  // Only do this once
            BsightingApi.Builder builder = new BsightingApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // options for running against local devappserver
                    // - 10.0.2.2 is localhost's IP address in Android emulator
                    // - turn off compression when running against local devappserver
                    // .setRootUrl("http://10.0.2.2:8080/_ah/api/") // Original in sample
                    //.setRootUrl("http://localhost:8080/_ah/api/")
                    //.setRootUrl("http://192.168.1.4:8080/_ah/api/") //local dev
                    //.setRootUrl("http://192.168.0.162:8080/_ah/api/") //local dev Shelly's
                    //.setRootUrl("https://" + "propane-atrium-97913"  + ".appspot.com/_ah/api/") //Live
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
            result = myApiService.insert(insertSigthing).execute();

            return result;

        } catch (IOException e) {

            return null;
        }
    }

    @Override
    protected void onPostExecute(Bsighting s) {
        super.onPostExecute(s);
        pd.dismiss();
        //Log.d(TAG, "onPostExecute result " + s.toString());
        if(s != null){
            infoToast("Upload successful.");
            new UserRatingAsyncTask(activity).execute(s.getOwnerid());
            activity.addSighting(s);
        }

        //activity.setUpMap();

    }

    private void infoToast(String s) {
        Toast toast = Toast.makeText(context,s,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }
}