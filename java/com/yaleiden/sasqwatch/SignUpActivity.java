package com.yaleiden.sasqwatch;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.ads.AdView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.util.DateTime;
import com.yaleiden.sasqwatch.backend.squatchUsersApi.SquatchUsersApi;
import com.yaleiden.sasqwatch.backend.squatchUsersApi.model.CollectionResponseSquatchUsers;
import com.yaleiden.sasqwatch.backend.squatchUsersApi.model.SquatchUsers;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.util.Base64;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Yale on 6/17/2015.
 */
public class SignUpActivity extends AppCompatActivity implements UserNameAsyncTask.UserAsyncResponse {

    private String TAG = "SignUpActivity";

    private EditText editTextUserName;
    private AutoCompleteTextView autoCompleteTextLocation;
    private ImageView imageViewContactPic;
    private EditText editTextUserAbout;
    private TextView textViewUserRank;
    private String rankString;
    private ImageView imageViewUserRank;
    private Bitmap rankImage;
    private Bitmap contactBm;
    private String username;
    private String userlocation;
    private String userabout;
    byte[] picByteArray;
    Long userId;
    int UserRank;
    SquatchUsers newUser;
    Button buttonProfile;

    private static final int SELECT_PHOTO = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signup_layout);
        ActionBar actionBar = getSupportActionBar();
        UserRank = 0;
        editTextUserAbout = (EditText) findViewById(R.id.editTextUserAbout);
        editTextUserName = (EditText) findViewById(R.id.editTextUserName);
        autoCompleteTextLocation = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextLocation);
        imageViewContactPic = (ImageView) findViewById(R.id.imageViewContactPic);
        textViewUserRank = (TextView) findViewById(R.id.textViewUserRank);
        imageViewUserRank = (ImageView) findViewById(R.id.imageViewUserRank);
        buttonProfile = (Button) findViewById(R.id.buttonProfile);

        String[] states = getResources().getStringArray(R.array.states);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, states);
        autoCompleteTextLocation.setAdapter(adapter);

        imageViewContactPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });

        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "button clicked");
                newUser = new SquatchUsers();

                username = editTextUserName.getText().toString();
                userlocation = autoCompleteTextLocation.getText().toString();
                userabout = editTextUserAbout.getText().toString();

                java.util.Date now = new Date();

                if (null != contactBm) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    contactBm.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    picByteArray = stream.toByteArray();
                    newUser.setImage(Base64.encodeToString(picByteArray, Base64.DEFAULT));
                }
                newUser.setJoindate(new DateTime(now));
                newUser.setUserlocation(userlocation);
                newUser.setUsername(username);
                newUser.setUserabout(userabout);

                if (isUserSetup()) {
                    Log.d(TAG, "userId " + userId);
                    updateUser(newUser);
                }
                if (!isUserSetup()) {
                    newUser.setUserrating(0);

                    signUpCall(newUser);
                    //postSignup(newUser);
                }

            }

        });

        if (isUserSetup()) {
            Log.d(TAG, "userId " + userId);
            actionBar.setTitle("Update SasqWatch Profile");
            buttonProfile.setText("Update Profile");
            getTheUser();
        }
        if (!isUserSetup()) {
            actionBar.setTitle("Create SasqWatch Profile");
            buttonProfile.setText("Create Profile");
        }
    }

    private void prepdisplay(SquatchUsers user) {
        if (null != user.getImage()) {
            PicUtils picUtils = new PicUtils();
            Bitmap bmp = picUtils.byteToPic(user.getImage());
            imageViewContactPic.setImageBitmap(bmp);
        }
        RankUser rankUser = new RankUser(SignUpActivity.this);
        UserRank = user.getUserrating();
        rankString = rankUser.getRank(UserRank);
        rankImage = rankUser.getRankIcon(UserRank);

        textViewUserRank.setText("Rank: " + rankString);
        imageViewUserRank.setImageBitmap(rankImage);
        editTextUserAbout.setText(user.getUserabout());
        editTextUserName.setText(user.getUsername());
        autoCompleteTextLocation.setText(user.getUserlocation());
    }

    private void postSignup(SquatchUsers newUser) {
        new SignUpAsyncTask(this).execute(newUser);
    }

    private void getTheUser() {
        Log.d(TAG, "getTheUser id " + userId);
        new GetUserAsyncTask(this).execute(userId);
    }

    private void updateUser(SquatchUsers upUser) {
        new UpdateAsyncTask(this, upUser, userId).execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    InputStream imageStream = null;
                    try {
                        //imageStream = getContentResolver().openInputStream(selectedImage);
                        contactBm = decodeUri(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    //Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                    imageViewContactPic.setImageBitmap(contactBm);
                }
        }
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 150;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
    }

    public void startMainActivity() {
        Intent myIntent = new Intent(SignUpActivity.this, MapsActivity.class);
        //myIntent.putExtra("key", value); //Optional parameters
        startActivity(myIntent);
    }


    public class SignUpAsyncTask extends AsyncTask<SquatchUsers, Void, SquatchUsers> {
        private SquatchUsersApi myApiService = null;
        private Context context;
        private ProgressDialog pdp;
        private String server_address;
        //private List<Comments> comments;

        SignUpAsyncTask(
                Context context) {
            this.context = context;
            Log.d(TAG, "SignUpAsync");
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "SignUpAsync onPreExecute");
            super.onPreExecute();
            pdp = new ProgressDialog(context);
            pdp.setTitle("Processing...");
            pdp.setMessage("Please wait.");
            pdp.setCancelable(false);
            pdp.setIndeterminate(true);
            pdp.show();

            boolean localRun = getResources().getBoolean(R.bool.local_run);
            if (localRun == true) {
                server_address = getResources().getString(R.string.run_local);
            }
            if (localRun == false) {
                server_address = getResources().getString(R.string.run_server);
            }
        }

        @Override
        protected SquatchUsers doInBackground(SquatchUsers... params) {

            SquatchUsers postComment = params[0];
            SquatchUsers loadedUser = null;

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
                loadedUser = myApiService.insert(postComment).execute();

            } catch (IOException e) {
                return null;
            }
            return loadedUser;
        }

        @Override
        protected void onPostExecute(SquatchUsers user) {
            super.onPostExecute(user);

            Log.d(TAG, "SignUpAsync onPostExecute");
            String msg = "Message";
            pdp.dismiss();
            Log.d(TAG, "SignUpAsync user " + user.toString());
            if (user != null) {
                String username = user.getUsername();
                Long userid = user.getId();
                Integer userrate = user.getUserrating();

                SharedPreferences userSettings = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = userSettings.edit();
                editor.putString("user_name", username);
                editor.putLong("user_id", userid);
                editor.putInt("user_rate", userrate);
                editor.commit();
                msg = "Profile setup successful.";
                startMainActivity();
            }
            if (user == null) {
                msg = "Profile setup failed, please try again.";
            }


            infoToast(msg);
        }
    }

    public class GetUserAsyncTask extends AsyncTask<Long, Void, SquatchUsers> {
        private SquatchUsersApi myApiService = null;
        private Context context;
        private ProgressDialog pdp;
        private String server_address;
        //private List<Comments> comments;

        GetUserAsyncTask(
                Context context) {
            this.context = context;
            Log.d(TAG, "GetUserAsync");
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "GetUserAsync onPreExecute");
            super.onPreExecute();
            pdp = new ProgressDialog(context);
            pdp.setTitle("Processing...");
            pdp.setMessage("Please wait.");
            pdp.setCancelable(false);
            pdp.setIndeterminate(true);
            pdp.show();

            boolean localRun = getResources().getBoolean(R.bool.local_run);
            if (localRun == true) {
                server_address = getResources().getString(R.string.run_local);
            }
            if (localRun == false) {
                server_address = getResources().getString(R.string.run_server);
            }
        }

        @Override
        protected SquatchUsers doInBackground(Long... params) {

            Long userID = params[0];
            SquatchUsers loadedUser = null;

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
                loadedUser = myApiService.get(userID).execute();

            } catch (IOException e) {
                return null;
            }
            return loadedUser;
        }

        @Override
        protected void onPostExecute(SquatchUsers user) {
            super.onPostExecute(user);
            pdp.dismiss();
            Log.d(TAG, "GetUserAsync onPostExecute");
            String msg = "Something went wrong.";

            Log.d(TAG, "GetUserAsync calling Get");

            if (user != null) {
                if (null != user.getUserrating()) {
                    RankUser rankUser = new RankUser(SignUpActivity.this);
                    rankUser.getRank(user.getUserrating());
                    //getRank(user.getUserrating());
                    msg = "Profile loaded.";
                }
                prepdisplay(user);
                Log.d(TAG, "GetUser not null. Prep display");
            }

            infoToast(msg);
        }
    }

    public class UpdateAsyncTask extends AsyncTask<Void, Void, SquatchUsers> {
        private SquatchUsersApi myApiService = null;
        private Context context;
        private ProgressDialog pdp;
        private String server_address;
        SquatchUsers users;
        long usersid;
        //private List<Comments> comments;

        public UpdateAsyncTask(
                Context context, SquatchUsers users, long usersid) {
            this.context = context;
            this.users = users;
            this.usersid = usersid;
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

            boolean localRun = getResources().getBoolean(R.bool.local_run);
            if (localRun == true) {
                server_address = getResources().getString(R.string.run_local);
            }
            if (localRun == false) {
                server_address = getResources().getString(R.string.run_server);
            }
        }

        @Override
        protected SquatchUsers doInBackground(Void... params) {
            //userId
            //long postID = params[0];
            //SquatchUsers postComment = params[1];

            SquatchUsers loadedUser = null;

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
                loadedUser = myApiService.update(usersid, users).execute();

            } catch (IOException e) {
                return null;
            }
            return loadedUser;
        }

        @Override
        protected void onPostExecute(SquatchUsers user) {
            super.onPostExecute(user);

            Log.d(TAG, "UpdateAsync onPostExecute");
            String msg = "Message";
            pdp.dismiss();

            if (user != null) {
                Log.d(TAG, "UpdateAsync user " + user.toString());
                String username = user.getUsername();
                Long userid = user.getId();

                SharedPreferences userSettings = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = userSettings.edit();
                editor.putString("user_name", username);
                editor.putLong("user_id", userid);
                editor.commit();
                msg = "Profile update successful.";
                startMainActivity();
            }
            if (user == null) {
                msg = "Profile update failed, please try again.";
            }

            infoToast(msg);
        }
    }

    private void infoToast(String s) {
        Toast toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    boolean isUserSetup() {
        boolean result = false;
        SharedPreferences userSettings = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        String username = userSettings.getString("user_name", null);
        Long userid = userSettings.getLong("user_id", 999);
        Log.d(TAG, "isUserSetup " + userid);
        if (userid != 999) {
            result = true;
            userId = userid;
        }
        return result;
    }


    /*
    AsyncTask to check if a username is already taken
     */
    public class CheckNameAsyncTask extends AsyncTask<String, Void, Integer> {
        private SquatchUsersApi myApiService = null;
        private Context context;
        private ProgressDialog pdp;
        private String server_address;
        //private List<Comments> comments;

        CheckNameAsyncTask(
                Context context) {
            this.context = context;
            Log.d(TAG, "SignUpAsync");
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "SignUpAsync onPreExecute");
            super.onPreExecute();
            pdp = new ProgressDialog(context);
            pdp.setTitle("Processing...");
            pdp.setMessage("Please wait.");
            pdp.setCancelable(false);
            pdp.setIndeterminate(true);
            pdp.show();

            boolean localRun = getResources().getBoolean(R.bool.local_run);
            if (localRun == true) {
                server_address = getResources().getString(R.string.run_local);
            }
            if (localRun == false) {
                server_address = getResources().getString(R.string.run_server);
            }
        }

        @Override
        protected Integer doInBackground(String... params) {
            CollectionResponseSquatchUsers squatchUsersCollectionResponse;
            String checkuser = params[0];
            Integer records = 0;

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
                squatchUsersCollectionResponse = myApiService.listusername(checkuser).execute();


            } catch (IOException e) {
                return null;
            }
            records = squatchUsersCollectionResponse.getItems().size();

            return records;
        }

        @Override
        protected void onPostExecute(Integer user) {
            super.onPostExecute(user);

            Log.d(TAG, "SignUpAsync onPostExecute");
            String msg = "Message";
            pdp.dismiss();
            Log.d(TAG, "SignUpAsync user " + user);
            if (user == 0) {
                //Continue registration
            }
            if (user == 1) {
                //User needs to pick a new name
                msg = "That user name is taken, please choose a different name.";
            }
            if (user == null) {
                //Network error.......
                msg = "Network error........";
            }


            infoToast(msg);
        }
    }

    private void signUpCall(SquatchUsers newUser) {
        UserNameAsyncTask userNameAsyncTask = new UserNameAsyncTask(this, SignUpActivity.this);
        userNameAsyncTask.delegate = this;
        userNameAsyncTask.execute(newUser.getUsername());
    }

    @Override
    public void postResult(String asyncresult) {

        //Error, Taken, Success

        if (asyncresult.equals("Error")) {
            infoToast("Oops. Something did not go correctly.");
        } else if (asyncresult.equals("Taken")) {
            infoToast("Sorry that username is already taken, please choose another name.");
        } else if (asyncresult.equals("Success")) {
            postSignup(newUser);
        } else {
            infoToast("Oops. Something did not go correctly.");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}