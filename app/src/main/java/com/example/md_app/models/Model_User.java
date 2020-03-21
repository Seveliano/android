package com.example.md_app.models;

public class Model_User {
    public String st_userName;
    public String st_email;
    public String st_imgUrl;

    public Model_User(){

    }

    public Model_User(String userName, String email, String st_imgUrl){
        this.st_userName = userName;
        this.st_email = email;
        this.st_imgUrl = st_imgUrl;
    }
}
