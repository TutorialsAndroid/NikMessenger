package com.messenger.nik.models;

public class RegisteredUserModel {

    private String name;
    private String avatar;
    private String ip;
    private String date_joined;

    public RegisteredUserModel() { }

    public RegisteredUserModel(String name, String avatar, String ip, String date_joined) {
        this.name = name;
        this.avatar = avatar;
        this.ip = ip;
        this.date_joined = date_joined;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDate_joined() {
        return date_joined;
    }

    public void setDate_joined(String date_joined) {
        this.date_joined = date_joined;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
