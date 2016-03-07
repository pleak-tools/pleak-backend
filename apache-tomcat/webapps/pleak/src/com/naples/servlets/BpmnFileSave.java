package com.naples.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.naples.responses.JsonResponse;

public class BpmnFileSave extends HttpServlet{

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws ServletException, IOException {

    String result;

    JsonResponse jsonResponse = new JsonResponse();

    String fileName = req.getParameter("fileName");

    try {

      if ( !isCorrectFileName(fileName) ) throw new FileException("Incorrect file name.");
      if ( !isCorrectFileExtension(fileName) ) throw new FileException("Incorrect file extension.");

      Part filePart = req.getPart("file");
      InputStream fileContent = filePart.getInputStream();

      String filePathStr = getServletContext().getRealPath(getServletContext().getInitParameter("bpmn-files-dir")) + fileName;
      Path filePath = Paths.get(filePathStr);

      Files.copy(fileContent, filePath, StandardCopyOption.REPLACE_EXISTING);
      result = jsonResponse.getJsonResponseSuccess(200, "OK");
    }
    catch (Exception e) {
      result = jsonResponse.getJsonResponseError(400, e.getMessage());
    }

    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().write(result);
  }

  protected boolean isCorrectFileName(String fileName) {
    String WHITELIST = "[^0-9A-Za-z.]+";

    Pattern p = Pattern.compile(WHITELIST);
    Matcher m = p.matcher(fileName);

    return !m.find();
  }

  protected boolean isCorrectFileExtension(String fileName) {
    String CORRECT_FILE_EXTENSION = "\\.bpmn";

    Pattern p = Pattern.compile(CORRECT_FILE_EXTENSION);
    Matcher m = p.matcher(fileName);

    return m.find();
  }
}

class FileException extends Exception
{
  public FileException() {}

  public FileException(String message)
  {
    super(message);
  }
}