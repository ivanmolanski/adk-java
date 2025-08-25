package com.google.adk.model;

public class ApiError {
    private String error;
    private String details;

    public ApiError(String error, String details) {
        this.error = error;
        this.details = details;
    }

    public String getError() { return error; }
    public String getDetails() { return details; }
}
