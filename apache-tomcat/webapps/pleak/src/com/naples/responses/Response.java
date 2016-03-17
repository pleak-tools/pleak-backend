package com.naples.responses;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.naples.responses.ResponseBase;
import com.naples.responses.ResponseText;
import com.naples.responses.ResponseTextDescription;
import com.naples.responses.ResponseList;
import com.naples.responses.ResponseFiles;

public class Response {

  protected ResponseBase response;

  public void setResponseSuccess() {
    response = new ResponseBase();
    response.setType("success");
  }

  public void setResponseText(String text) {
    response = new ResponseText();
    response.setType("success");
    ((ResponseText)response).setText(text);
  }

  public void setResponseTextDescription(String text, String description) {
    response = new ResponseTextDescription();
    response.setType("success");
    ((ResponseTextDescription)response).setText(text);
    ((ResponseTextDescription)response).setDescription(description);
  }

  public void setResponseList(List<String> list) {
    response = new ResponseList();
    response.setType("success");
    ((ResponseList)response).setList(list);
  }

  public void setResponseFiles(List<String> fileNames, List<String> fileModifiedDates) {
    response = new ResponseFiles();
    response.setType("success");
    ((ResponseFiles)response).setFileNames(fileNames);
    ((ResponseFiles)response).setFileModifiedDates(fileModifiedDates);
  }

  public void setResponseError(String text) {
    response = new ResponseText();
    response.setType("error");
    ((ResponseText)response).setText(text);
  }

  public void setResponseError(String text, String description) {
    response = new ResponseTextDescription();
    response.setType("error");
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
                      "  text: '" + e.getMessage() + "'\n" +
                      "}";
    }

    return jsonResponse;
  }
}