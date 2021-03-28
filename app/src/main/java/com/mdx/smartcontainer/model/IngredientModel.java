package com.mdx.smartcontainer.model;

public class IngredientModel {
    public IngredientModel(String image, String name, String container_id, double quantity, boolean isChecked, String unit) {
        this.image = image;
        this.name = name;
        this.container_id = container_id;
        this.quantity = quantity;
        this.isChecked = isChecked;
        this.unit = unit;
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

    public String getContainer_id() {
        return container_id;
    }

    public void setContainer_id(String container_id) {
        this.container_id = container_id;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    private String image;
    private String name;
    private String container_id;
    private double quantity;
    private boolean isChecked;
    private String unit;

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
