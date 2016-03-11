package com.naples.servlets;

import java.nio.file.*;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.naples.responses.Response;
import com.naples.helpers.FileException;
import com.naples.helpers.FileHelper;

public class BpmnFileOpen extends HttpServlet{

 @Override
 protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                  throws ServletException, IOException {

    Response response = new Response();
    FileHelper fh = new FileHelper();

    String fileName = req.getParameter("fileName");

    try {

      if ( !fh.isCorrectFileName(fileName) ) throw new FileException("Incorrect file name.");
      if ( !fh.isCorrectFileExtension(fileName) ) throw new FileException("Incorrect file extension.");

      String filePathStr = getServletContext().getRealPath(getServletContext().getInitParameter("bpmn-files-dir")) + fileName;
      Path filePath = Paths.get(filePathStr);

      if (Files.isReadable(filePath)) {
        response.setResponseText(new String(Files.readAllBytes(filePath)));
        resp.setStatus(HttpServletResponse.SC_OK);
      } else {
        response.setResponseError("File not found.");
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }

    }
    catch (Exception e) {
      response.setResponseError(e.getMessage());
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().write(response.toJson());

  }

}
