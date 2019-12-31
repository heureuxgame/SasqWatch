package com.yaleiden.sasqwatch;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Yale on 7/6/2015.
 */
public class AdapterComment extends ArrayAdapter<Comments> {
    private Activity activity;
    private List<Comments> comments;
    private static LayoutInflater inflater = null;
    private String TAG = "AdapterComment";
    RankUser rankUser;

    public AdapterComment(Activity activity, int textViewResourceId, List<Comments> comments) {
        super(activity, textViewResourceId, comments);
        try {
            this.activity = activity;
            this.comments = comments;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        } catch (Exception e) {
            Log.d(TAG, "AdapterPost exception " + e.toString());
        }

    }

    public int getCount() {
        return comments.size();
    }

    public Comments getItem(Comments position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {

        TextView textViewRank;
        TextView textViewDate;
        TextView textViewComment;
        TextView textViewOwner;
        ImageView imageView;

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        Bitmap bmp;
        byte[] pic;
        Bitmap defaultIcon = BitmapFactory.decodeResource(activity.getResources(), R.drawable.user_rank0);
        rankUser = new RankUser(activity);
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.item_comment, null);
                holder = new ViewHolder();

                holder.textViewRank = (TextView) vi.findViewById(R.id.textViewRank);
                holder.textViewDate = (TextView) vi.findViewById(R.id.textViewDate);
                holder.textViewComment = (TextView) vi.findViewById(R.id.textViewComment);
                holder.textViewOwner = (TextView) vi.findViewById(R.id.textViewOwner);
                holder.imageView = (ImageView) vi.findViewById(R.id.imageView);

                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            String newDate = "No Date";
            Date date = null;
            try {
                date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(comments.get(position).getDate().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                newDate = format.format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.textViewRank.setText("Rank: " + rankUser.getRank(comments.get(position).getPosterrating()));
            holder.textViewDate.setText("Date: " + newDate);
            holder.textViewComment.setText(comments.get(position).getContent());
            holder.textViewOwner.setText("User: " + comments.get(position).getOwnername());
            holder.imageView.setImageBitmap(rankUser.getRankIcon(comments.get(position).getPosterrating()));

        } catch (Exception e) {

            Log.e(TAG, e.toString());
        }
        return vi;
    }
}