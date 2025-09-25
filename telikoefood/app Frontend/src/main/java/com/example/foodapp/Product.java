package com.example.foodapp;

import java.io.Serializable;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private String onoma;
    private double kostos;
    private int diathesimiPosotita;
    public int quantity = 0;

    public Product(String onoma, double kostos, int diathesimiPosotita) {
        this.onoma = onoma;
        this.kostos = kostos;
        this.diathesimiPosotita = diathesimiPosotita;
    }

    public String getName() {
        return onoma;
    }

    public double getPrice() {
        return kostos;
    }

    public int getAvailableAmount() {
        return diathesimiPosotita;
    }

    public void setAvailableAmount(int amount) {
        this.diathesimiPosotita = amount;
    }
}