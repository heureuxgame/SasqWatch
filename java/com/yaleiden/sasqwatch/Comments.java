package com.yaleiden.sasqwatch;



import java.util.Date;

/**
 * Created by Yale on 7/6/2015.
 */

public class Comments {


    Long id;

    Long ownerid;  //Bsighitng
    private String ownername; //SquatchUsers
    private long posterid; //SquatchUsers
    private int posterrating; //SquatchUsers

    private Date date;
    private String content;

    public int getPosterrating() {
        return posterrating;
    }

    public void setPosterrating(int posterrating) {
        this.posterrating = posterrating;
    }

    public long getPosterid() {
        return posterid;
    }

    public void setPosterid(long posterid) {
        this.posterid = posterid;
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

    public String getOwnername() {
        return ownername;
    }

    public void setOwnername(String ownername) {
        this.ownername = ownername;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}