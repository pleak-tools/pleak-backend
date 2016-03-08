package com.naples.responses;

import com.naples.responses.ResponseData;

public class ResponseDataTextDescription extends ResponseDataText {

    public String description;

    protected String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

}