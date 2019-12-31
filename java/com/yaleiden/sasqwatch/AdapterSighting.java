package com.yaleiden.sasqwatch;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yaleiden.sasqwatch.backend.bsightingApi.model.Bsighting;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Yale on 7/5/2015.
 */
public class AdapterSighting extends ArrayAdapter<Bsighting> {
    private Activity activity;
    private List<Bsighting> bsightings;
    private static LayoutInflater inflater = null;
    private String TAG = "AdapterPost";


    public AdapterSighting(Activity activity, int textViewResourceId, List<Bsighting> bsightings) {
        super(activity, textViewResourceId, bsightings);
        try {
            this.activity = activity;
            this.bsightings = bsightings;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        } catch (Exception e) {
            Log.d(TAG, "AdapterPost exception " + e.toString());
        }

    }

    public int getCount() {
        return bsightings.size();
    }

    public Bsighting getItem(Bsighting position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {

        TextView textViewTitle;
        TextView textViewdate;
        TextView textViewstate;
        TextView textViewbehavior;
        TextView textViewcomment;
        TextView textViewencounter;
        TextView textViewsign;
        TextView textViewuser;
        ImageView imageViewSighting;
        ImageView imageViewRank;

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        Bitmap bmp;
        byte[] pic;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.sighting_marker_window, null);
                holder = new ViewHolder();
                holder.textViewTitle = (TextView) vi.findViewById(R.id.textViewTitle);
                holder.textViewdate = (TextView) vi.findViewById(R.id.textViewdate);
                holder.textViewstate = (TextView) vi.findViewById(R.id.textViewstate);
                holder.textViewbehavior = (TextView) vi.findViewById(R.id.textViewbehavior);
                holder.textViewcomment = (TextView) vi.findViewById(R.id.textViewcomment);
                holder.textViewencounter = (TextView) vi.findViewById(R.id.textViewencounter);
                holder.textViewsign = (TextView) vi.findViewById(R.id.textViewsign);
                holder.textViewuser = (TextView) vi.findViewById(R.id.textViewuser);
                holder.imageViewSighting = (ImageView) vi.findViewById(R.id.imageViewSighting);

                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            if (null != bsightings.get(position).getImage()) {
                pic = Base64.decode(bsightings.get(position).getImage(), Base64.DEFAULT);
                bmp = BitmapFactory.decodeByteArray(pic, 0, pic.length);
            } else {
                bmp = null;
            }

            String newDate = "No Date";
            Date date = null;
            try {
                date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(bsightings.get(position).getDate().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                newDate = format.format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.textViewTitle.setText("SasqWatch Encounter Report" + " (" + bsightings.get(position).getCommentcount() + ")");
            holder.textViewdate.setText("Date: " + newDate);
            holder.textViewstate.setText("Location: " + bsightings.get(position).getState());
            holder.textViewbehavior.setText("Behavior: " + bsightings.get(position).getBehavior());
            holder.textViewcomment.setText(bsightings.get(position).getComment());
            holder.textViewencounter.setText("Encounter type: " + bsightings.get(position).getEncounter());
            holder.textViewsign.setText("Sign type: " + bsightings.get(position).getSigntype());
            holder.textViewuser.setText("User: " + bsightings.get(position).getOwnername());
            holder.imageViewSighting.setImageBitmap(bmp);

        } catch (Exception e) {

            Log.e(TAG, e.toString());
        }
        return vi;
    }
}
