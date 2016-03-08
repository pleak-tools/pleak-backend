package com.naples.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.*;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.naples.responses.Response;
import com.naples.helpers.FileException;
import com.naples.helpers.FileHelper;

public class BpmnFileSave extends HttpServlet{

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws ServletException, IOException {

    Response response = new Response();
    FileHelper fh = new FileHelper();

    String fileName = req.getParameter("fileName");

    try {

      if ( !fh.isCorrectFileName(fileName) ) throw new FileException("Incorrect file name.");
      if ( !fh.isCorrectFileExtension(fileName) ) throw new FileException("Incorrect file extension.");

      Part filePart = req.getPart("file");
      InputStream fileContent = filePart.getInputStream();

      String filePathStr = getServletContext().getRealPath(getServletContext().getInitParameter("bpmn-files-dir")) + fileName;
      Path filePath = Paths.get(filePathStr);

      Files.copy(fileContent, filePath, StandardCopyOption.REPLACE_EXISTING);
      response.setResponseSuccess(200);

    }
    catch (Exception e) {
      response.setResponseError(400, e.getMessage());
    }

    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().write(response.toJson());
  }

}