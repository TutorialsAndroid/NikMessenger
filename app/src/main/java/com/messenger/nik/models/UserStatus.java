package com.messenger.nik.models;

public class UserStatus {

    private String name;
    private String vn;
    private String status_url;

    public UserStatus() {}

    public UserStatus(String name, String vn, String status_url) {
        this.name = name;
        this.vn = vn;
        this.status_url = status_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVn() {
        return vn;
    }

    public void setVn(String vn) {
        this.vn = vn;
    }

    public String getStatus_url() {
        return status_url;
    }

    public void setStatus_url(String status_url) {
        this.status_url = status_url;
    }
}
