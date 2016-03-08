package com.naples.responses;

import com.naples.responses.ResponseData;

public class ResponseDataText extends ResponseData {
    public String text;
    public String description;

    protected String getText() {
        return text;
    }

    protected void setText(String text) {
        this.text = text;
    }

    protected String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

}