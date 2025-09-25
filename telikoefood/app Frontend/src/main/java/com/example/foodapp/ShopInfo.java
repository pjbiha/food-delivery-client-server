package com.example.foodapp;

import java.io.Serializable;
import java.util.ArrayList;

public class ShopInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    public String name;
    public String category;
    public String priceRange;
    public int reviews;
    public int noOfReviews;
    public double latitude;
    public double longitude;
    public ArrayList<Product> foodItems;

    public ShopInfo(String category, String priceRange, ArrayList<Product> foodItems, int reviews, int noOfReviews, double latitude, double longitude) {
        this.category = category;
        this.priceRange = priceRange;
        this.foodItems = foodItems;
        this.reviews = reviews;
        this.noOfReviews = noOfReviews;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
