package com.naples.responses;

import com.naples.responses.ResponseData;

public class ResponseDataError extends ResponseData {
    public String description;

    protected void setDescription(String description) {
        this.description = description;
    }
}