package com.example.q.project2;

import android.net.Uri;

public class ContactItem {
    private Uri img;
    private String name;
    private String phone;

    public void setImg(Uri imgUri){
        img = imgUri;
    }
    public void setName(String nm){
        name = nm;
    }
    public void setPhone(String pn){
        phone = pn;
    }

    public Uri getImg(){
        return img;
    }
    public String getName(){
        return name;
    }
    public String getPhone(){
        return phone;
    }
}
