package com.naples.servlets;

import java.nio.file.*;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.naples.responses.JsonResponse;
import com.naples.helpers.FileException;
import com.naples.helpers.FileHelper;

public class BpmnFileOpen extends HttpServlet{

 @Override
 protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                  throws ServletException, IOException {

    String result = "";

    JsonResponse jsonResponse = new JsonResponse();
    FileHelper fh = new FileHelper();

    String fileName = req.getParameter("fileName");

    try {

      if ( !fh.isCorrectFileName(fileName) ) throw new FileException("Incorrect file name.");
      if ( !fh.isCorrectFileExtension(fileName) ) throw new FileException("Incorrect file extension.");

      String filePathStr = getServletContext().getRealPath(getServletContext().getInitParameter("bpmn-files-dir")) + fileName;
      Path filePath = Paths.get(filePathStr);

      if (Files.isReadable(filePath)) {
        result = new String(Files.readAllBytes(filePath));
      }

    }
    catch (Exception e) {
      result = jsonResponse.getJsonResponseError(400, e.getMessage());
    }

    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().write(result);

 }

}
