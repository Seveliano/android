package com.example.md_app.models;

import android.os.Parcelable;

import java.io.Serializable;

public class Model_Doctor implements Serializable {
    public String st_uid;
    public String st_name;
    public String st_imgUrl;
    public String st_hospital;
    public String st_qualification;

    public Model_Doctor() {
        this.st_uid = "";
        this.st_name = "";
        this.st_imgUrl = "https://";
        this.st_hospital = "";
        this.st_qualification = "";
    }

    public Model_Doctor(String st_uid, String st_name, String st_imgUrl, String st_hospital, String st_qualification) {
        this.st_uid = st_uid;
        this.st_name = st_name;
        this.st_imgUrl = st_imgUrl;
        this.st_hospital = st_hospital;
        this.st_qualification = st_qualification;
    }
}
