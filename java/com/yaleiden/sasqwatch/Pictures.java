package com.yaleiden.sasqwatch.backend;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

import java.util.Date;

/**
 * Created by Yale on 6/29/2015.
 */
@Entity
public class Pictures {

    @Index
    Long ownerid;  //Posts
    @Id
    Long id;
    @Index
    private String pictitle;
    private String picnotes;
    private Blob image;
    private String imagetype;
    private Date uploaddate;
    private String username;

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

    public String getPictitle() {
        return pictitle;
    }

    public void setPictitle(String pictitle) {
        this.pictitle = pictitle;
    }

    public String getPicnotes() {
        return picnotes;
    }

    public void setPicnotes(String picnotes) {
        this.picnotes = picnotes;
    }

    public byte[] getImage() {
        if (image == null) {
            return null;
        }

        return image.getBytes();
    }

    public void setImage(byte[] bytes) {
        this.image = new Blob(bytes);
    }

    public String getImagetype() {
        return imagetype;
    }

    public void setImagetype(String imagetype) {
        this.imagetype = imagetype;
    }

    public Date getUploaddate() {
        return uploaddate;
    }

    public void setUploaddate(Date uploaddate) {
        this.uploaddate = uploaddate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
