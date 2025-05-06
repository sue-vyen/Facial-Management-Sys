package com.example.spa_sys.models;

public class Services {
    private int id;
    private String name;
    private double price;
    private int duration;
    private String description;
    private String imagePath;
    private String ingredients;
    private String benefits;

    // Add constructor, getters, and setters
    public Services(int id, String name, double price, int duration, String description,
                    String imagePath, String ingredients, String benefits) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.duration = duration;
        this.description = description;
        this.imagePath = imagePath;
        this.ingredients = ingredients;
        this.benefits = benefits;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getDuration() {
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
}
