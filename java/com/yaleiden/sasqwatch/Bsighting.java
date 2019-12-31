package com.yaleiden.sasqwatch;


import java.util.Date;

/**
 * Created by Yale on 6/18/2015.
 */

public class Bsighting {

    //private LatLng latLng;

    Long id;

    private Date date;

    Long ownerid;  //SquatchUsers
    String ownername; //SquatchUsers
    private int ownerrating; //SquatchUsers
    private double lat;
    private double lng;
    private String comment;
    private String behavior;

    private String encounter;
    private String signtype;

    private String state;
    private int commentcount;
    private Date datereply;


    public Bsighting() {
    }

    public int getCommentcount() {
        return commentcount;
    }

    public void setCommentcount(int commentcount) {
        this.commentcount = commentcount;
    }

    public Date getDatereply() {
        return datereply;
    }

    public void setDatereply(Date datereply) {
        this.datereply = datereply;
    }

    public int getOwnerrating() {
        return ownerrating;
    }

    public void setOwnerrating(int ownerrating) {
        this.ownerrating = ownerrating;
    }



    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


    public String getOwnername() {
        return ownername;
    }

    public void setOwnername(String ownername) {
        this.ownername = ownername;
    }


    public Long getOwnerid() {
        return ownerid;
    }

    public void setOwnerid(Long ownerid) {
        this.ownerid = ownerid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getBehavior() {
        return behavior;
    }

    public void setBehavior(String behavior) {
        this.behavior = behavior;
    }

    /*
        public LatLng getLatLng() {
            return latLng;
        }

        public void setLatLng(LatLng latLng) {
            this.latLng = latLng;
        }
    */
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getEncounter() {
        return encounter;
    }

    public void setEncounter(String encounter) {
        this.encounter = encounter;
    }

    public String getSigntype() {
        return signtype;
    }

    public void setSigntype(String signtype) {
        this.signtype = signtype;
    }
}
