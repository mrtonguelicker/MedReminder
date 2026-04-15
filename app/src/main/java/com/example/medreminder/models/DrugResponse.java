package com.example.medreminder.models;

import java.util.List;

public class DrugResponse {

    public List<Result> results;

    public static class Result {
        public OpenFDA openfda;
        public List<String> dosage_form;
    }

    public static class OpenFDA {
        public List<String> brand_name;
    }
}