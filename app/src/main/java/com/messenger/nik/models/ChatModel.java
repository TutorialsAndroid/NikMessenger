package com.messenger.nik.models;

public class ChatModel {

    private String vn; //virtual_number
    private String message;
    private String tm; //tagged message
    private String gm; //gif message
    private String timeStamp;
    private FileModel fileModel;

    public ChatModel() { }

    public ChatModel(String vn, String message, String tm, String gm, String timeStamp, FileModel fileModel) {
        this.vn = vn;
        this.message = message;
        this.tm = tm;
        this.gm = gm;
        this.timeStamp = timeStamp;
        this.fileModel = fileModel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getVn() {
        return vn;
    }

    public void setVn(String vn) {
        this.vn = vn;
    }

    public String getTm() {
        return tm;
    }

    public void setTm(String tm) {
        this.tm = tm;
    }

    public String getGm() {
        return gm;
    }

    public void setGm(String gm) {
        this.gm = gm;
    }

    public FileModel getFileModel() {
        return fileModel;
    }

    public void setFileModel(FileModel fileModel) {
        this.fileModel = fileModel;
    }
}