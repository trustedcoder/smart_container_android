package com.mdx.smartcontainer.model;

public class MealModel {
    public MealModel(String meal_id, String image, String name, String cook_time) {
        this.meal_id = meal_id;
        this.image = image;
        this.name = name;
        this.cook_time = cook_time;
    }

    public String getMeal_id() {
        return meal_id;
    }

    public void setMeal_id(String meal_id) {
        this.meal_id = meal_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCook_time() {
        return cook_time;
    }

    public void setCook_time(String cook_time) {
        this.cook_time = cook_time;
    }

    private String meal_id;
    private String image;
    private String name;
    private String cook_time;
}
