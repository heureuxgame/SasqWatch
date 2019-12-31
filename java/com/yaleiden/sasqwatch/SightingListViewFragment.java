package com.yaleiden.sasqwatch;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.yaleiden.sasqwatch.backend.bsightingApi.model.Bsighting;

import java.util.List;

/**
 * Created by Yale on 7/5/2015.
 */
public class SightingListViewFragment extends Fragment implements AbsListView.OnScrollListener {

    private static String TAG = "SightingListViewFragment";
    private ListView listView;
    AdapterSighting adapterSighting;
    List<Bsighting> allSightings;
    int lastPosition;
    StartCommentListener mCallback;
    AbsListView.OnScrollListener scrollListener;
    private int preLast;
    int itemsRemain;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_FLING) {
            Log.d(TAG, "onScroll Fling Triggered");
            // ((SightingListActivity) getActivity()).downloadSightings();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        switch (view.getId()) {
            case android.R.id.list:

                // Make your calculation stuff here. You have all your
                // needed info from the parameters of this function.

                // Sample calculation to determine if the last
                // item is fully visible.
                if(itemsRemain != 0){
                    final int lastItem = firstVisibleItem + visibleItemCount;
                    if (lastItem == totalItemCount) {
                        if (preLast != lastItem) { //to avoid multiple calls for last item
                            Log.d(TAG, "Last");
                            preLast = lastItem;
                            Log.d(TAG, "onScroll lastItem == totalItemCount");
                            lastPosition  = adapterSighting.getCount();
                            ((SightingListActivity) getActivity()).downloadSightings();
                        }

                    }
                }

        }
    }

    public interface downloadNext {
        public void downloadSightings();
    }

    public interface StartCommentListener {
        public void commentIntent(long id);
    }

    private void infoToast(String s) {
        Toast toast = Toast.makeText(getActivity(),s,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "OnCreateView");
        // Inflate the layout for this fragment
        View view = inflater
                .inflate(R.layout.fragment_listview, container, false);

        /*
        final GestureDetector gesture = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                final int SWIPE_MIN_DISTANCE = 120;
                final int SWIPE_MAX_OFF_PATH = 250;
                final int SWIPE_THRESHOLD_VELOCITY = 200;
                try {
                    if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH)
                        return false;
                    if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
                            && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                        Log.i(TAG, "Swipe Up");
                    } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                            && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        Log.i(TAG, "Left to Right");
                    }
                } catch (Exception e) {
                    // nothing
                }
                return super.onFling(e1, e2, velocityX, velocityY);

            }
        });


        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }
        });
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
        itemsRemain = 1;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "OnStop");
        //cancel asynctask here
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "OnActivityCreated");
        listView = (ListView) getView().findViewById(android.R.id.list); // android prefix..

        listView.setOnItemClickListener(myListClickListener); //
        //listView.setOnItemLongClickListener(myListLongClickListener); //
        listView.setOnScrollListener(this);
        //mylist.setCacheColorHint(R.color.transparent);
        Log.d(TAG, "before mylist.setAdapter set");
        //adapterSighting = new AdapterSighting(getActivity(), R.layout.sighting_marker_window, allSightings);
        //listView.setAdapter(adapterSighting);
        //mylist.setDivider(null);

        Log.d(TAG, "after getsupporloadermanager");
    }

    AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {

            Log.d(TAG, "index / arg3 " + " " + arg2 + " / " + arg3);
            Bsighting selectedSighting = adapterSighting.getItem(arg2);
            //Log.d(TAG, selectedSighting.toString());
            //long id  = selectedSighting.getId();

            mCallback.commentIntent(selectedSighting.getId());
        }
    };
/*
    AdapterView.OnItemLongClickListener myListLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View v, int index,
                                       final long arg3) {

            Log.d(TAG, "index / arg3 " + " " + index + " / " + arg3);
            Bsighting selectedSighting = adapterSighting.getItem(index);
            Log.d(TAG, selectedSighting.toString());
            //long id  = selectedSighting.getId();

            mCallback.commentIntent(selectedSighting.getId());

            return true;

        }

    };
*/
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (StartCommentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement StartCommentListener");
        }
    }

    public void updateSightings(List<Bsighting> newData) {
        Log.d(TAG, "updatePosts size is " + newData.size());
       //postses = data;
        //lastPosition = adapterSighting.getCount();
        if (newData == null) {
            Log.d(TAG, "onPostExecute result null.");
        }

        int count = 0;
        if (null != newData) {
            count = newData.size();
            //itemsRemain = count;
        }
        if (newData != null && newData.size() != 0) {
            if (allSightings == null) {
                allSightings = newData;
            } else {
               // lastPosition = adapterSighting.getCount();
                allSightings.addAll(newData);
            }
            itemsRemain = allSightings.size();
            adapterSighting = new AdapterSighting(getActivity(), R.layout.sighting_marker_window, allSightings);
            listView.setAdapter(adapterSighting);
            int setPos = lastPosition - 2;
            if(setPos < 0){
                setPos = 0;
            }
            listView.setSelectionFromTop(setPos, 0);
            //lastPosition  = adapterSighting.getCount();
        }
    }

    public void combineLists(List<Bsighting> newData) {

        if (newData != null && newData.size() != 0) {
            allSightings.addAll(newData);
        }

    }
}
