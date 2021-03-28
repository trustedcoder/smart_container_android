package com.mdx.smartcontainer.model;

public class ShopItem {
    private String image;

    public ShopItem(String image, String container_id, String title, String weight_level_remaining, String percent_remaining, boolean is_bought) {
        this.image = image;
        this.container_id = container_id;
        this.title = title;
        this.weight_level_remaining = weight_level_remaining;
        this.percent_remaining = percent_remaining;
        this.is_bought = is_bought;
    }

    private String container_id;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContainer_id() {
        return container_id;
    }

    public void setContainer_id(String container_id) {
        this.container_id = container_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWeight_level_remaining() {
        return weight_level_remaining;
    }

    public void setWeight_level_remaining(String weight_level_remaining) {
        this.weight_level_remaining = weight_level_remaining;
    }

    public String getPercent_remaining() {
        return percent_remaining;
    }

    public void setPercent_remaining(String percent_remaining) {
        this.percent_remaining = percent_remaining;
    }

    public boolean isIs_bought() {
        return is_bought;
    }

    public void setIs_bought(boolean is_bought) {
        this.is_bought = is_bought;
    }

    private String title;
    private String weight_level_remaining;
    private String percent_remaining;
    private boolean is_bought;
}
