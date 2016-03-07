package com.naples.responses;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.naples.responses.Response;
import com.naples.responses.ResponseDataSuccess;
import com.naples.responses.ResponseDataError;

public class JsonResponse {

  protected String getResponse(Response response) {
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

  public String getJsonResponseSuccess(Integer code, String text) {
    Response response = new Response();
    response.setType("success");

    ResponseDataSuccess success = new ResponseDataSuccess();
    success.setCode(code);
    success.setText(text);
    response.setData(success);

    return getResponse(response);
  }

  public String getJsonResponseError(Integer code, String text) {
    return getJsonResponseError(code, text, "");
  }

  public String getJsonResponseError(Integer code, String text, String description) {
    Response response = new Response();
    response.setType("error");

    ResponseDataError error = new ResponseDataError();
    error.setCode(code);
    error.setText(text);
    error.setDescription(description);
    response.setData(error);

    return getResponse(response);
  }
}