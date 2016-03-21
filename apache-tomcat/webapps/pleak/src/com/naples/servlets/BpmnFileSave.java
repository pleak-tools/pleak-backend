package com.naples.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;

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
    String fileMD5 = req.getParameter("fileMD5");
    Part filePart = req.getPart("file");

    try {
      if ( !fh.isCorrectFileName(fileName) ) throw new FileException("Incorrect file name.");
      if ( !fh.isCorrectFileExtension(fileName) ) throw new FileException("Incorrect file extension.");

      String filePathStr = getServletContext().getRealPath(getServletContext().getInitParameter("bpmn-files-dir")) + fileName;
      fh.saveFile(filePart, fileMD5, filePathStr);
      fileMD5 = fh.getMD5Hash(filePathStr);

      response.setResponseText(fileMD5);
      resp.setStatus(HttpServletResponse.SC_OK);
    }
    catch (FileException e) {
      response.setResponseError(e.toString());
      if (e.getCode() == 409) {
        resp.setStatus(HttpServletResponse.SC_CONFLICT);
      } else {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }
    }
    catch (Exception e) {
      response.setResponseError(e.toString());
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().write(response.toJson());
  }

}