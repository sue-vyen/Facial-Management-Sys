package com.example.spa_sys.models;

public class Facial {

    private String name;
    private String description;
    private String price;
    private String duration;
    private String ingredients;
    private String benefits;
    private String imagePath;

    public Facial(String name, String description, String price, String duration, String ingredients, String benefits, String imagePath) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.duration = duration;
        this.ingredients = ingredients;
        this.benefits = benefits;
        this.imagePath = imagePath;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public String getDuration() {
        return duration;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getBenefits() {
        return benefits;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
