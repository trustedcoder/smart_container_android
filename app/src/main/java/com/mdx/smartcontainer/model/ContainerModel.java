package com.mdx.smartcontainer.model;

public class ContainerModel {
    private String name_item;
    private String remaining;
    private String name_container;
    private String percentage;
    private String public_id;
    private String image;

    public ContainerModel(String name_item, String remaining, String name_container, String percentage, String public_id, String image) {
        this.name_item = name_item;
        this.remaining = remaining;
        this.name_container = name_container;
        this.percentage = percentage;
        this.public_id = public_id;
        this.image = image;
    }

    public String getName_item() {
        return name_item;
    }

    public void setName_item(String name_item) {
        this.name_item = name_item;
    }

    public String getRemaining() {
        return remaining;
    }

    public void setRemaining(String remaining) {
        this.remaining = remaining;
    }

    public String getName_container() {
        return name_container;
    }

    public void setName_container(String name_container) {
        this.name_container = name_container;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public String getPublic_id() {
        return public_id;
    }

    public void setPublic_id(String public_id) {
        this.public_id = public_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
