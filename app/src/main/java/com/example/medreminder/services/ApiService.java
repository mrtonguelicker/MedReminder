package com.example.medreminder.services;


import com.example.medreminder.models.DrugResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    @GET("drug/label.json")
    Call<DrugResponse> getDrugInfo(@Query("search") String query);
}