package com.yaleiden.sasqwatch;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.yaleiden.sasqwatch.backend.squatchUsersApi.SquatchUsersApi;
import com.yaleiden.sasqwatch.backend.squatchUsersApi.model.CollectionResponseSquatchUsers;
import com.yaleiden.sasqwatch.backend.squatchUsersApi.model.SquatchUsers;

import java.io.IOException;
import java.util.List;

/**
 * Created by Yale on 7/27/2015.
 */
public class UserNameAsyncTask extends AsyncTask <String, Void, CollectionResponseSquatchUsers> {
    private Context context;
    private Activity activity;
    private String TAG = "UserNameAsyncTask";
    private String server_address;
    private static SquatchUsersApi myApiService = null;

    public interface UserAsyncResponse {
        void postResult(String asyncresult);
    }

    public UserAsyncResponse delegate=null;

    UserNameAsyncTask(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;

        Log.d(TAG, "Constructor");
    }

    @Override
    protected CollectionResponseSquatchUsers doInBackground(String... params) {
        CollectionResponseSquatchUsers collectionResponseSquatchUsers = null;
        String requestName = params[0];

        boolean localRun = activity.getResources().getBoolean(R.bool.local_run);
        if (localRun == true) {
            server_address = activity.getResources().getString(R.string.run_local);
        }
        if (localRun == false) {
            server_address = activity.getResources().getString(R.string.run_server);
        }
        if (myApiService == null) {  // Only do this once
            SquatchUsersApi.Builder builder = new SquatchUsersApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
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
            collectionResponseSquatchUsers = myApiService.listusername(requestName).execute();
            return collectionResponseSquatchUsers;
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            return null;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(CollectionResponseSquatchUsers o) {
        super.onPostExecute(o);
        String msg = "";
        List<SquatchUsers> squatchUsers = null;

        if (o != null) {
            squatchUsers = o.getItems();

            if (squatchUsers == null) {
                Log.d(TAG, "checkUserName squatchUsers == null.");
                msg = "Success";
            }

            if (null != squatchUsers) {
                //Log.d(TAG, bsightings.toString());
                Log.d(TAG, "onPostExecute result size " + squatchUsers.size());

                if (squatchUsers.size() != 0) {
                    msg = "Taken";
                }
                if (squatchUsers.size() == 0) {

                    msg = "Success";
                }
            }
        } else {
            msg = "Error";
        }

        if(delegate!=null)
        {
            delegate.postResult(msg);
        }
        else
        {
            Log.e("ApiAccess", "You have not assigned IApiAccessResponse delegate");
        }
    }

}
