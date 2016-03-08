package com.naples.responses;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.naples.responses.ResponseBase;
import com.naples.responses.ResponseText;
import com.naples.responses.ResponseTextDescription;
import com.naples.responses.ResponseList;

public class Response {

  protected ResponseBase response;

  public void setResponseSuccess(Integer code) {
    response = new ResponseBase();
    response.setType("success");
    response.setCode(code);
  }

  public void setResponseText(Integer code, String text) {
    response = new ResponseText();
    response.setType("success");
    response.setCode(code);
    ((ResponseText)response).setText(text);
  }

  public void setResponseTextDescription(Integer code, String text, String description) {
    response = new ResponseTextDescription();
    response.setType("success");
    response.setCode(code);
    ((ResponseTextDescription)response).setText(text);
    ((ResponseTextDescription)response).setDescription(description);
  }

  public void setResponseList(Integer code, List<String> list) {
    response = new ResponseList();
    response.setType("success");
    ((ResponseList)response).setCode(code);
    ((ResponseList)response).setList(list);
  }

  public void setResponseError(Integer code, String text) {
    response = new ResponseText();
    response.setType("error");
    response.setCode(code);
    ((ResponseText)response).setText(text);
  }

  public void setResponseError(Integer code, String text, String description) {
    response = new ResponseTextDescription();
    response.setType("error");
    response.setCode(code);
    ((ResponseTextDescription)response).setText(text);
    ((ResponseTextDescription)response).setDescription(description);
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