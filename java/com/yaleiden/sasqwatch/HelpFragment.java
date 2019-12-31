package com.yaleiden.sasqwatch;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.yaleiden.sasqwatch.backend.postsApi.model.Posts;
import com.yaleiden.sasqwatch.backend.repliesApi.model.Replies;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yale on 7/6/2015.
 */
public class HelpFragment  extends Fragment {

    private static String TAG = "HelpFragment";
    private ListView listView;
    Activity mActivity;
    ImageView imageViewRed;
    ImageView imageViewYellow;
    ImageView imageViewOrange;

    //OnScoreSavedListener sCallback;


    public HelpFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "OnCreateView");
        // Inflate the layout for this fragment
        View view = inflater
                .inflate(R.layout.fragment_help, container, false);
        imageViewRed = (ImageView) view.findViewById(R.id.imageViewRed);
        imageViewYellow = (ImageView) view.findViewById(R.id.imageViewYellow);
        imageViewOrange = (ImageView) view.findViewById(R.id.imageViewOrange);
/*
        imageViewRed.setImageResource(R.drawable.default_user);
        imageViewYellow.setImageResource(R.drawable.default_user);
        imageViewOrange.setImageResource(R.drawable.default_user);
*/
        return view;
    }

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");


    }
}
