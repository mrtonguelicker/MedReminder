package com.example.medreminder.models;

public class OpenFoodResponse {

    public Product product;

    public static class Product {
        public String product_name;
        public String quantity;
    }
}