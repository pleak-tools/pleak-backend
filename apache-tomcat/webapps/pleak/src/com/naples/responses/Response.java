package com.naples.responses;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.naples.responses.Response;
import com.naples.responses.ResponseData;
import com.naples.responses.ResponseDataText;
import com.naples.responses.ResponseDataFiles;

public class Response {

  protected ResponseBase response;

  public void setResponseSuccess(Integer code) {
    response = new ResponseBase();
    response.setType("success");

    ResponseData success = new ResponseData();
    success.setCode(code);
    response.setData(success);
  }

  public void setResponseSuccess(Integer code, String text) {
    response = new ResponseBase();
    response.setType("success");

    ResponseDataText success = new ResponseDataText();
    success.setCode(code);
    success.setText(text);
    response.setData(success);
  }

  public void setResponseFiles(Integer code, List<String> files) {
    response = new ResponseBase();
    response.setType("success");

    ResponseDataFiles success = new ResponseDataFiles();
    success.setCode(code);
    success.setFiles(files);
    response.setData(success);
  }

  public void setResponseError(Integer code, String text) {
    response = new ResponseBase();
    response.setType("error");

    ResponseDataText error = new ResponseDataText();
    error.setCode(code);
    error.setText(text);
    response.setData(error);
  }

  public void setResponseError(Integer code, String text, String description) {
    setResponseError(code, text);
    ResponseDataText responseData = (ResponseDataText) response.getData();
    responseData.setDescription(description);
  }

  public String toJson() {
    String jsonResponse = "";

    try {
      ObjectMapper mapper = new ObjectMapper();
      jsonResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
    } catch (Exception e) {
      jsonResponse = "{" +
                       "\n\terror: '" + e.getMessage() + "'" +
                     "}";
    }

    return jsonResponse;
  }
}