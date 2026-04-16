package com.example.medreminder.services;

import com.example.medreminder.models.OpenFoodResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface OpenFoodApiService {

    @GET("api/v0/product/{barcode}.json")
    Call<OpenFoodResponse> getProduct(@Path("barcode") String barcode);
}