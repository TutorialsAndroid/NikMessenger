package com.messenger.nik.models;

public class UserModel {

    private String user_name;
    private String user_virtual_number;
    private String user_profile_photo;
    private String timeStamp;

    public UserModel() {}

    public UserModel(String user_name, String user_virtual_number, String user_profile_photo, String timeStamp) {
        this.user_name = user_name;
        this.user_virtual_number = user_virtual_number;
        this.user_profile_photo = user_profile_photo;
        this.timeStamp = timeStamp;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_virtual_number() {
        return user_virtual_number;
    }

    public void setUser_virtual_number(String user_virtual_number) {
        this.user_virtual_number = user_virtual_number;
    }

    public String getUser_profile_photo() {
        return user_profile_photo;
    }

    public void setUser_profile_photo(String user_profile_photo) {
        this.user_profile_photo = user_profile_photo;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
