package com.naples.responses;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.naples.responses.Response;
import com.naples.responses.ResponseData;
import com.naples.responses.ResponseDataText;
import com.naples.responses.ResponseDataList;

public class Response {

  protected ResponseBase response;

  public void setResponseSuccess(Integer code) {
    response = new ResponseBase();
    response.setType("success");

    ResponseData success = new ResponseData();
    success.setCode(code);
    response.setData(success);
  }

  public void setResponseText(Integer code, String text) {
    response = new ResponseBase();
    response.setType("success");

    ResponseDataText success = new ResponseDataText();
    success.setCode(code);
    success.setText(text);
    response.setData(success);
  }

  public void setResponseTextDescription(Integer code, String text, String description) {
    response = new ResponseBase();
    response.setType("success");

    ResponseDataTextDescription success = new ResponseDataTextDescription();
    success.setCode(code);
    success.setText(text);
    success.setDescription(description);
    response.setData(success);
  }

  public void setResponseList(Integer code, List<String> list) {
    response = new ResponseBase();
    response.setType("success");

    ResponseDataList success = new ResponseDataList();
    success.setCode(code);
    success.setList(list);
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
    ResponseDataTextDescription responseData = (ResponseDataTextDescription) response.getData();
    responseData.setDescription(description);
  }

  public String toJson() {
    String jsonResponse = "";

    try {
      ObjectMapper mapper = new ObjectMapper();
      jsonResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
    } catch (Exception e) {
      jsonResponse =  "{\n" +
                      "  type: 'error',\n" +
                      "  content: {\n" +
                      "    code: 400,\n" +
                      "    text: '" + e.getMessage() + "'\n" +
                      "  }\n" +
                      "}";
    }

    return jsonResponse;
  }
}