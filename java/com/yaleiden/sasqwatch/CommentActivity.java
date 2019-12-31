package com.yaleiden.sasqwatch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.util.DateTime;

import com.yaleiden.sasqwatch.backend.bsightingApi.BsightingApi;
import com.yaleiden.sasqwatch.backend.bsightingApi.model.Bsighting;
import com.yaleiden.sasqwatch.backend.commentsApi.CommentsApi;
import com.yaleiden.sasqwatch.backend.commentsApi.model.CollectionResponseComments;
import com.yaleiden.sasqwatch.backend.commentsApi.model.Comments;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Yale on 6/30/2015.
 */
public class CommentActivity extends AppCompatActivity {
    private String TAG = "CommentActivity";
    String nextCursor;
    long postSigthing;
    private ListView listView;
    List<Comments> comments;
    Bsighting displaySighting;

    AdapterComment adapterComment;
    EditText editTextComment;
    Button buttonComment;
    private AdView mAdView;

    TextView textViewdate;
    TextView textViewstate;
    TextView textViewbehavior;
    TextView textViewcomment;
    TextView textViewencounter;
    TextView textViewsign;
    TextView textViewuser;
    //ImageView imageViewSighting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Oncreate");
        setContentView(R.layout.activity_comment);

        mAdView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("YOUR_DEVICE_HASH")
                .build();
        mAdView.loadAd(adRequest);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("SasqWatch Encounter Comments");

        Intent intent = getIntent();
        postSigthing = intent.getLongExtra("sighting_id", 999L);

        new GetOneSightingAsyncTask(this).execute(postSigthing);

        nextCursor = null;

        listView = (ListView) findViewById(android.R.id.list); // android prefix..

        editTextComment = (EditText) findViewById(R.id.editTextComment);
        buttonComment = (Button) findViewById(R.id.buttonComment);

        textViewdate = (TextView) findViewById(R.id.textViewdate);
        textViewstate = (TextView) findViewById(R.id.textViewstate);
        textViewbehavior = (TextView) findViewById(R.id.textViewbehavior);
        textViewcomment = (TextView) findViewById(R.id.textViewcomment);
        textViewencounter = (TextView) findViewById(R.id.textViewencounter);
        textViewsign = (TextView) findViewById(R.id.textViewsign);
        textViewuser = (TextView) findViewById(R.id.textViewuser);
        //imageViewSighting = (ImageView) findViewById(R.id.imageViewSighting);

        buttonComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "button click");
                if (isUserSetup() == false) {
                    Intent myIntent = new Intent(CommentActivity.this, SignUpActivity.class);
                    startActivity(myIntent);
                } else {
                    String comment = editTextComment.getText().toString();
                    Log.d(TAG, "OnClick comment is " + comment);
                    Log.d(TAG, "OnClick comment is " + editTextComment.getText().toString());
                    long fakeid = 0;
                    saveComment(comment);

                }
            }
        });

        listView.setOnItemClickListener(myListClickListener); //
        listView.setOnItemLongClickListener(myListLongClickListener); //
        //mylist.setCacheColorHint(R.color.transparent);
        Log.d(TAG, "before mylist.setAdapter set");
        //listView.setAdapter(adapterComment);
        //mylist.setDivider(null);

        Log.d(TAG, "after getsupporloadermanager");
    }

    AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            // Get information for selected archer

        }
    };

    AdapterView.OnItemLongClickListener myListLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View v, int index,
                                       final long arg3) {

            return true;
        }

    };

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
                Intent mymapIntent = new Intent(CommentActivity.this, MapsActivity.class);
                startActivity(mymapIntent);
                return true;

            case R.id.menu_profile:
                Intent myIntent = new Intent(CommentActivity.this, SignUpActivity.class);
                startActivity(myIntent);
                return true;

            case R.id.menu_sightings:
                Intent sightIntent = new Intent(CommentActivity.this, SightingListActivity.class);
                startActivity(sightIntent);
                return true;

            case R.id.menu_help:
                Intent helpIntent = new Intent(CommentActivity.this, HelpActivity.class);
                //myIntent.putExtra("key", value); //Optional parameters
                startActivity(helpIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume postSighting is " + postSigthing);
        new GetCommentsListAsyncTask(this).execute(postSigthing);
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    public void saveComment(String comment) {
        Log.d(TAG, "saveComment comment " + comment);
        editTextComment.setText("");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextComment.getWindowToken(), 0);
        SharedPreferences userSettings = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        String username = userSettings.getString("user_name", null);
        Long userid = userSettings.getLong("user_id", 999);
        Integer userrate = userSettings.getInt("user_rate", 0);

        Comments postComment = new Comments();
        java.util.Date now = new Date();
        postComment.setDate(new DateTime(now));
        postComment.setOwnerid(postSigthing);
        postComment.setPosterid(userid);
        postComment.setOwnername(username);
        postComment.setContent(comment);
        postComment.setPosterrating(userrate);
        //Log.d(TAG, "saveComment " + comment.toString());
        new PostCommentAsyncTask(this).execute(postComment);
    }

    public class GetCommentsListAsyncTask extends AsyncTask<Long, Void, CollectionResponseComments> {
        private CommentsApi myApiService = null;
        private Context context;

        private ProgressDialog pdg;
        private String server_address;
        private List<Comments> comments;

        GetCommentsListAsyncTask(Context context) {
            this.context = context;
            Log.d(TAG, "GetCommentsListAsyncTask");
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "GetCommentsListAsyncTask onPreExecute");
            super.onPreExecute();
            pdg = new ProgressDialog(context);
            pdg.setTitle("Loading comments...");
            pdg.setMessage("Please wait.");
            pdg.setCancelable(false);
            pdg.setIndeterminate(true);
            pdg.show();

            boolean localRun = context.getResources().getBoolean(R.bool.local_run);
            if (localRun == true) {
                server_address = context.getResources().getString(R.string.run_local);
            }
            if (localRun == false) {
                server_address = context.getResources().getString(R.string.run_server);
            }
        }

        @Override
        protected CollectionResponseComments doInBackground(Long... params) {
            Long sightingId = params[0];
            if (myApiService == null) {  // Only do this once
                CommentsApi.Builder builder = new CommentsApi.Builder(AndroidHttp.newCompatibleTransport(),
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
                return myApiService.listbyowner(sightingId).setCursor(nextCursor).execute();
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(CollectionResponseComments responseComments) {
            super.onPostExecute(responseComments);
            Log.d(TAG, "GetCommentsListAsyncTask onPostExecute");
            String msg = "Message";
            pdg.dismiss();
            int count = 0;
            List<Comments> commentsList = null;
            //Get the CollectionResponse and check if it is null
            if (commentsList == null) {
                Log.d(TAG, "onPostExecute result null.");
                msg = "Sorry. No records returned from the server.";
            }
            if (responseComments != null) {
                commentsList = responseComments.getItems();
                nextCursor = responseComments.getNextPageToken();

                if (null != commentsList) {
                    //Log.d(TAG, commentsList.toString());
                    count = commentsList.size();
                    Log.d(TAG, "onPostExecute result size " + count);

                    if (count != 0) {
                        updateComments(commentsList);
                        msg = count + " comments downloaded.";
                    }
                    if (count == 0) {
                        //sightingListViewFragment.updateSightings(bsightings);
                        msg = "End of list.";
                    }
                }

            } else {
                msg = "Sorry. No response from the server.";
            }


            infoToast(msg);
        }
    }

    public class PostCommentAsyncTask extends AsyncTask<Comments, Void, Comments> {
        private CommentsApi myApiService = null;
        private Context context;
        private ProgressDialog pdp;
        private String server_address;
        private List<Comments> comments;

        PostCommentAsyncTask(
                Context context) {
            this.context = context;
            Log.d(TAG, "PostAsyncTask");
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "PostAsync onPreExecute");
            super.onPreExecute();
            pdp = new ProgressDialog(context);
            pdp.setTitle("Posting your comment...");
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
        protected Comments doInBackground(Comments... params) {
            Comments finishedcomment = null;
            Comments postComment = params[0];
            if (myApiService == null) {
                CommentsApi.Builder builder = new CommentsApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        .setRootUrl(server_address)
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                myApiService = builder.build();
            }

            try {
                finishedcomment = myApiService.insert(postComment).execute();
                //Log.d(TAG, "returned post " + finishedcomment.toString());
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                return null;
            }
            return finishedcomment;
        }

        @Override
        protected void onPostExecute(Comments comments) {
            super.onPostExecute(comments);
            //Log.d(TAG, "PostAsync onPostExecute comments " + comments.toString());
            String msg = "Message";
            pdp.dismiss();
            if (comments != null) {
                new UserRatingAsyncTask(context).execute(comments.getPosterid());
                new UpdateSightingAsyncTask(context).execute(displaySighting);
            }
            combineList(comments);
            infoToast(msg);
        }
    }

    public class GetOneSightingAsyncTask extends AsyncTask<Long, Void, Bsighting> {

        private BsightingApi myApiService = null;
        private Context context;
        private ProgressDialog pdp;
        private String server_address;

        GetOneSightingAsyncTask(
                Context context) {
            this.context = context;
            Log.d(TAG, "GetOneSightingAsyncTask");
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "GetOneSightingAsyncTask onPreExecute");
            super.onPreExecute();
            pdp = new ProgressDialog(context);
            pdp.setTitle("Loading encounter...");
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
        protected Bsighting doInBackground(Long... params) {
            Long sightid = params[0];
            Bsighting retsighting = null;
            if (myApiService == null) {
                BsightingApi.Builder builder = new BsightingApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        .setRootUrl(server_address)
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                myApiService = builder.build();
            }

            try {
                retsighting = myApiService.get(sightid).execute();
                //Log.d(TAG, "returned sighting " + retsighting.toString());
            } catch (IOException e) {
                Log.e(TAG, sightid+" "+e.toString());
                return null;
            }
            return retsighting;
        }

        @Override
        protected void onPostExecute(Bsighting result) {

            super.onPostExecute(result);
            Log.d(TAG, "GetOneSightingAsyncTask");
            String msg = "Message";
            pdp.dismiss();
            if (result != null) {
                //Log.d(TAG, "SightingAsync onPostExecute comments " + result.toString());
                displaySighting = result;
                String newDate = "No Date";
                Date date = null;
                try {
                    date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(result.getDate().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                try {
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    newDate = format.format(date);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                textViewdate.setText(newDate);
                textViewstate.setText("Location: " + result.getState());
                textViewbehavior.setText("Behavior: " + result.getBehavior());
                textViewcomment.setText("Comment: " + result.getComment());
                textViewencounter.setText("Encounter: " + result.getEncounter());
                textViewsign.setText("Sign: " + result.getSigntype());
                textViewuser.setText("User: " + result.getOwnername());
            }

            //imageViewSighting = (ImageView) findViewById(R.id.imageViewSighting);
            infoToast(msg);
        }
    }

    public class UpdateSightingAsyncTask extends AsyncTask<Bsighting, Void, Bsighting> {

        private BsightingApi myApiService = null;
        private Context context;
        private ProgressDialog pdp;
        private String server_address;

        UpdateSightingAsyncTask(
                Context context) {
            this.context = context;
            Log.d(TAG, "GetOneSightingAsyncTask");
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "UpdateSightingAsyncTask onPreExecute");
            super.onPreExecute();
            pdp = new ProgressDialog(context);
            pdp.setTitle("Updating encounter...");
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
        protected Bsighting doInBackground(Bsighting... params) {
            Bsighting sightid = params[0];

            java.util.Date now = new Date();
            sightid.setDatereply(new DateTime(now));

            int commentcount = 1;
            if(null == sightid.getCommentcount()){
                commentcount = 1;
            }else {
                commentcount = sightid.getCommentcount();
                sightid.setCommentcount(commentcount + 1);
            }

            Bsighting retsighting = null;
            if (myApiService == null) {
                BsightingApi.Builder builder = new BsightingApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        .setRootUrl(server_address)
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                myApiService = builder.build();
            }

            try {
                retsighting = myApiService.update(sightid.getId(),sightid).execute();
                //Log.d(TAG, "returned sighting " + retsighting.toString());
            } catch (IOException e) {
                Log.e(TAG, sightid+" "+e.toString());
                return null;
            }
            return retsighting;

        }

        @Override
        protected void onPostExecute(Bsighting result) {

            super.onPostExecute(result);
            Log.d(TAG, "GetOneSightingAsyncTask");
            String msg = "Message";
            pdp.dismiss();
            if (result != null) {
                msg = "";

            }

            //imageViewSighting = (ImageView) findViewById(R.id.imageViewSighting);
            //infoToast(msg);
        }
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



    public void updateComments(List<Comments> data) {
        Log.d(TAG, "updatePosts");
        comments = data;
        if (comments == null) {
            Log.d(TAG, "onPostExecute result null.");
            infoToast("No comments available.");
        }
        if (comments != null && comments.size() == 0) {
            Log.d(TAG, "onPostExecute result size 0");
            infoToast("No comments downloaded.");
        }
        if (comments != null && comments.size() != 0) {
            Log.d(TAG, "onPostExecute result size >0");

            adapterComment = new AdapterComment(this, R.layout.item_comment, comments);
            listView.setAdapter(adapterComment);
        }
    }

    public void combineList(Comments newData) {

        if (newData != null && newData.size() != 0) {
            if (comments != null) {
                comments.add(newData);
            }
            if (comments == null) {
                comments = new ArrayList<Comments>();
                comments.add(newData);
            }
            adapterComment = new AdapterComment(this, R.layout.item_comment, comments);
            listView.setAdapter(adapterComment);
        }
    }

    private void infoToast(String s) {
        Toast toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }
}

