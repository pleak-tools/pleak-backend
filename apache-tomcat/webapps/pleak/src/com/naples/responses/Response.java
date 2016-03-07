package com.naples.responses;

import com.naples.responses.ResponseData;

public class Response {
    public String type;
    public ResponseData data;

    protected void setType(String type) {
        this.type = type;
    }

    protected void setData(ResponseData data) {
        this.data = data;
    }
}