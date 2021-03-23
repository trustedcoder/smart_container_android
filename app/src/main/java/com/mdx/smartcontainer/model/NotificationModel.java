package com.mdx.smartcontainer.model;

public class NotificationModel {
    private String container_id;
    private int image;

    public String getContainer_id() {
        return container_id;
    }

    public void setContainer_id(String container_id) {
        this.container_id = container_id;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate_ago() {
        return date_ago;
    }

    public void setDate_ago(String date_ago) {
        this.date_ago = date_ago;
    }

    private String title;
    private String date_ago;

    public NotificationModel(String container_id, int image, String title, String date_ago) {
        this.container_id = container_id;
        this.image = image;
        this.title = title;
        this.date_ago = date_ago;
    }

}
