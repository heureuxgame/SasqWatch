package com.yaleiden.sasqwatch;


import java.util.Date;

/**
 * Created by Yale on 6/29/2015.
 */

public class SquatchUsers {


    Long id;
    private String username;
    private Date joindate;
    private String userlocation;
    private Integer userrating;
    private Long userencounters;
    private String userabout;

    public String getUserabout() {
        return userabout;
    }

    public void setUserabout(String userabout) {
        this.userabout = userabout;
    }




    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getJoindate() {
        return joindate;
    }

    public void setJoindate(Date joindate) {
        this.joindate = joindate;
    }

    public String getUserlocation() {
        return userlocation;
    }

    public void setUserlocation(String userlocation) {
        this.userlocation = userlocation;
    }

    public Integer getUserrating() {
        return userrating;
    }

    public void setUserrating(Integer userrating) {
        this.userrating = userrating;
    }

    public Long getUserencounters() {
        return userencounters;
    }

    public void setUserencounters(Long userencounters) {
        this.userencounters = userencounters;
    }



}
