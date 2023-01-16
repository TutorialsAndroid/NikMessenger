package com.messenger.nik.models;

public class FileModel {

    private String type;
    private String url;
    private String name;
    private String size;

    public FileModel() {}

    public FileModel(String type, String url, String name, String size) {
        this.type = type;
        this.url = url;
        this.name = name;
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
