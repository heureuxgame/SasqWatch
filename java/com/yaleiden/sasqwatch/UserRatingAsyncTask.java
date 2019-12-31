package com.yaleiden.sasqwatch;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.yaleiden.sasqwatch.backend.squatchUsersApi.SquatchUsersApi;
import com.yaleiden.sasqwatch.backend.squatchUsersApi.model.SquatchUsers;

import java.io.IOException;

/**
 * Created by Yale on 7/8/2015.
 */
public class UserRatingAsyncTask extends AsyncTask<Long, Void, SquatchUsers> {
    private SquatchUsersApi myApiService = null;
    private Context context;
    private ProgressDialog pdp;
    private String server_address;
    SquatchUsers users;
    private String TAG = "UserRatingAsyncTask";
    //private List<Comments> comments;

    public UserRatingAsyncTask(
            Context context) {
        this.context = context;
        this.users = users;
        //this.usersid = usersid;
        Log.d(TAG, "SignUpAsync");
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "UpdateAsync onPreExecute");
        super.onPreExecute();
        pdp = new ProgressDialog(context);
        pdp.setTitle("Processing...");
        pdp.setMessage("Please wait.");
        pdp.setCancelable(false);
        pdp.setIndeterminate(true);
        pdp.show();

        boolean localRun = context.getResources().getBoolean(R.bool.local_run);
        if (localRun == true) {
            server_address = context.getResources().getString(R.string.run_local);
        }
        if (localRun == false) {
            server_address = context.getResources().getString(R.string.run_server);
        }
    }

    @Override
    protected SquatchUsers doInBackground(Long... params) {
        long userid = 0;

        SquatchUsers updateUser = null;
        SquatchUsers retUser = null;


        userid = params[0];

        if (myApiService == null) {  // Only do this once
            SquatchUsersApi.Builder builder = new SquatchUsersApi.Builder(AndroidHttp.newCompatibleTransport(),
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
            updateUser = myApiService.get(userid).execute();

        } catch (IOException e) {
            Log.e(TAG, "updateUsere" + e.toString());
            return null;
        }
        try {
            if(updateUser != null){
                if(updateUser.getUserrating() == null){
                    updateUser.setUserrating(1);
                }else {
                    updateUser.setUserrating(updateUser.getUserrating()+1);
                }
            }
            retUser = myApiService.update(userid, updateUser).execute();
        } catch (IOException e) {
            //Log.e(TAG, "retUser" + e.toString());
            return null;
        }
        return retUser;
    }

    @Override
    protected void onPostExecute(SquatchUsers user) {
        super.onPostExecute(user);

        Log.d(TAG, "UpdateAsync onPostExecute");
        String msg = "Message";
        pdp.dismiss();

        if (user != null) {
            //Log.d(TAG, "UpdateAsync user " + user.toString());
            msg = "Your user rank is improving!";
            if(user.getUserrating() != null){
                SharedPreferences userSettings = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = userSettings.edit();
                //editor.putString("user_name",username);
                //editor.putLong("user_id",userid);
                editor.putInt("user_rate",user.getUserrating());
                editor.commit();
            }

        }
        if (user == null) {
            msg = "Ranking update failed.";
        }

        infoToast(msg);
    }

    private void infoToast(String s) {
        Toast toast = Toast.makeText(context,s,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }
}