package com.yaleiden.sasqwatch;


import java.util.Date;

/**
 * Created by Yale on 6/29/2015.
 */

public class Posts {


    Long id;

    Long ownerid;  //SquatchUsers

    private Long replytopost;
    private Date date;
    private Long replies;
    private String content;
    //private Pictures pictures;
    private String ownername;
    private String title;



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOwnername() {
        return ownername;
    }

    public void setOwnername(String ownername) {
        this.ownername = ownername;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerid() {
        return ownerid;
    }

    public void setOwnerid(Long ownerid) {
        this.ownerid = ownerid;
    }

    public Long getReplytopost() {
        return replytopost;
    }

    public void setReplytopost(Long replytopost) {
        this.replytopost = replytopost;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getReplies() {
        return replies;
    }

    public void setReplies(Long replies) {
        this.replies = replies;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
/*
    public Pictures getPictures() {
        return pictures;
    }

    public void setPictures(Pictures pictures) {
        this.pictures = pictures;
    }
*/
}
