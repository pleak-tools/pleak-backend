package com.naples.responses;

public class ResponseData {
    public Integer code;
    public String text;

    protected void setCode(Integer code) {
        this.code = code;
    }

    protected void setText(String text) {
        this.text = text;
    }
}