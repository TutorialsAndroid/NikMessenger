package com.messenger.nik.models;

public class RCModel {

    private String name; //name of the person to whom user is current chatting
    private String avatar; //avatar of the person to whom user is current chatting
    private String vn; //virtual number
    private String groupVn;
    private String timeStamp;
    private String crID; //chat room ID
    private String notification_key;
    private ChatModel chatModel;

    public RCModel() {}

    public RCModel(String name, String avatar, String vn, String groupVn, String timeStamp, String crID, String notification_key, ChatModel chatModel) {
        this.name = name;
        this.avatar = avatar;
        this.vn = vn;
        this.groupVn = groupVn;
        this.timeStamp = timeStamp;
        this.crID = crID;
        this.notification_key = notification_key;
        this.chatModel = chatModel;
    }

    public String getVn() {
        return vn;
    }

    public void setVn(String vn) {
        this.vn = vn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChatModel getChatModel() {
        return chatModel;
    }

    public void setChatModel(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getCrID() {
        return crID;
    }

    public void setCrID(String crID) {
        this.crID = crID;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getGroupVn() {
        return groupVn;
    }

    public void setGroupVn(String groupVn) {
        this.groupVn = groupVn;
    }

    public String getNotification_key() {
        return notification_key;
    }

    public void setNotification_key(String notification_key) {
        this.notification_key = notification_key;
    }
}
