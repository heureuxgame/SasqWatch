package com.yaleiden.sasqwatch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
//import com.yaleiden.sasqwatch.backend.bsightingEndpoint.model.Bsighting;
import com.yaleiden.sasqwatch.backend.bsightingApi.model.Bsighting;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Yale on 6/22/2015.
 */
class MarkerWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private String TAG = "MarkerWindowAdapter";
    private final View mymarkerview;
    private HashMap<Marker, Bsighting> bsightingHashMap;
    private Bsighting bsighting;
    TextView textViewdate;
    TextView textViewstate;
    TextView textViewbehavior;
    TextView textViewcomment;
    TextView textViewTitle;
    TextView textViewencounter;
    TextView textViewsign;
    TextView textViewuser;
    ImageView imageViewSighting;
    String state;
    String beh;
    String comm;
    String date;
    String encount;
    String sign;
    String markerTitle;
    String username;
    Context context;
    Bitmap bmp;
    int commentcount;
    PicUtils picUtils = new PicUtils();

    //MarkerWindowAdapter(Context context, String lat, String lng, String hab, String beh, String comm, String date) {
    MarkerWindowAdapter(Context context, HashMap<Marker, Bsighting> bs) {
        Log.d(TAG, "MarkerWindowAdapter");
        this.bsightingHashMap = bs;
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        mymarkerview = inflater.inflate(R.layout.sighting_marker_window, null);
        Log.d(TAG, "MarkerWindowAdapter called");

        // ,downloadedSightings.get(i).getLng().toString(),
        //       downloadedSightings.get(i).getHabitat(),downloadedSightings.get(i).getBehavior(),downloadedSightings.get(i).getComment(),downloadedSightings.get(i).getDate().toString()
    }

    private void infoToast(String s) {
        Toast toast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    public View getInfoWindow(Marker marker) {

        Log.d(TAG, "getInfoContents");

        if (null == bsightingHashMap.get(marker)) {
            infoToast("Could not retrieve data from server");
            return null;
        }
        bsighting = bsightingHashMap.get(marker);
        this.markerTitle = marker.getTitle();
        if (markerTitle.equals("Squatch")) {


            this.state = bsighting.getState();
            this.beh = bsighting.getBehavior();
            this.comm = bsighting.getComment();
            this.encount = bsighting.getEncounter();
            this.sign = bsighting.getSigntype();
            String tdate = bsighting.getDate().toString();

            this.username = bsighting.getOwnername();
            this.commentcount = bsighting.getCommentcount();

            if (null != bsighting.getImage()) {
                this.bmp = picUtils.byteToPic(bsighting.getImage());
            }
            if (null == bsighting.getImage()) {
                this.bmp = null;
            }

            Date date = null;
            try {
                date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(tdate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                this.date = format.format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        render(marker, mymarkerview);
        Log.d(TAG, "getInfoWindow date " + date);
        return mymarkerview;

        //return null;
    }

    public View getInfoContents(Marker marker) {

        return null;
    }

    private void render(Marker marker, View view) {
        Log.d(TAG, "render");
        //marker = mMarker;
        // Add the code to set the required values
        // for each element in your custominfowindow layout file
        textViewTitle = (TextView) view.findViewById(R.id.textViewTitle);
        textViewdate = (TextView) view.findViewById(R.id.textViewdate);
        textViewstate = (TextView) view.findViewById(R.id.textViewstate);
        textViewbehavior = (TextView) view.findViewById(R.id.textViewbehavior);
        textViewcomment = (TextView) view.findViewById(R.id.textViewcomment);
        textViewencounter = (TextView) view.findViewById(R.id.textViewencounter);
        textViewsign = (TextView) view.findViewById(R.id.textViewsign);
        textViewuser = (TextView) view.findViewById(R.id.textViewuser);
        imageViewSighting = (ImageView) view.findViewById(R.id.imageViewSighting);



        if (null != markerTitle) {
            if (markerTitle.equals("You")) {
                textViewTitle.setText("You");
                textViewdate.setText("Date: " );
                textViewstate.setText("Location: " );
                textViewbehavior.setText("Behavior: " );
                textViewcomment.setText("");
                textViewencounter.setText("Encounter type:");
                textViewsign.setText("Sign type: " );
                textViewuser.setText("User: " );
                imageViewSighting.setImageBitmap(null);
            }
            if (markerTitle.equals("Squatch")) {
                textViewTitle.setText("SasqWatch Encounter" + " (" + commentcount + ")");
                textViewdate.setText("Date: " + date);
                textViewstate.setText("Location: " + state);
                textViewbehavior.setText("Behavior: " + beh);
                textViewcomment.setText(comm);
                textViewencounter.setText("Encounter type: " + encount);
                textViewsign.setText("Sign type: " + sign);
                textViewuser.setText("User: " + username);
                imageViewSighting.setImageBitmap(bmp);
            }
            Log.d(TAG, "marker title = " + marker.getTitle());
        }



    }
}