package com.yaleiden.sasqwatch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

/**
 * Created by Yale on 7/8/2015.
 */
public class RankUser {

    private String rankString;
    private Bitmap rankImage;
    Context context;
    int user;

    public RankUser(Context context){
        this.context = context;
    }

    public String getRank(int user){
        int rank = 0;
        if(user >200){
            rank = 5;
            rankString = "Squatch General";
        }
        else if(user > 100){
            rank = 4;
            rankString = "Researcher";
        }
        else if(user >50){
            rank = 3;
            rankString = "Tracker";
        }
        else if(user >20){
            rank = 2;
            rankString = "Hobbyist";
        }
        else if(user >10){
            rank = 1;
            rankString = "Tenderfoot";
        }
        else {
            rank = 0;
            rankString = "First-timer";
        }
        return rankString;
    }

    public Bitmap getRankIcon(int user){
        int rank = 0;
        if(user >200){
            rank = 5;
            rankString = "Squatch General";
            rankImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.user_rank5);
        }
        else if(user > 100){
            rank = 4;
            rankString = "Researcher";
            rankImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.user_rank4);
        }
        else if(user >50){
            rank = 3;
            rankString = "Tracker";
            rankImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.user_rank3);
        }
        else if(user >20){
            rank = 2;
            rankString = "Hobbyist";
            rankImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.user_rank2);
        }
        else if(user >10){
            rank = 1;
            rankString = "Tenderfoot";
            rankImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.user_rank1);
        }
        else {
            rank = 0;
            rankString = "First-timer";
            rankImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.user_rank0);
        }
        return rankImage;
    }

}
