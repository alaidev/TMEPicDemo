package com.example.tmepicdemo;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Image {
    private String name;
    private Bitmap bitmap;
    private String location;
    private Long time;
    private Integer type;

    protected Image(Parcel in) {
        name = in.readString();
        bitmap = in.readParcelable(Bitmap.class.getClassLoader());
        location = in.readString();
        if (in.readByte() == 0) {
            time = null;
        } else {
            time = in.readLong();
        }
        if (in.readByte() == 0) {
            type = null;
        } else {
            type = in.readInt();
        }
        desc = in.readString();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    private String desc;

    public String getLocation() {
        return location;
    }

    public Image(String name, Bitmap bitmap, String desc, String location, long time, int type) {
        this.type = type;
        this.time = time;
        this.name = name;
        this.location = location;
        this.bitmap = bitmap;
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
