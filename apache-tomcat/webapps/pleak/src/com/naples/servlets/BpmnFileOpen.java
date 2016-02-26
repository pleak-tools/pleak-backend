package com.naples.servlets;

import java.nio.file.*;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BpmnFileOpen extends HttpServlet{

 @Override
 protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                  throws ServletException, IOException {

    String fileName = req.getParameter("fileName");

    String filePathStr = getServletContext().getRealPath(getServletContext().getInitParameter("bpmn-files-dir")) + fileName;
    Path filePath = Paths.get(filePathStr);

    String fileContents = "";
    try {
      if (Files.isReadable(filePath)) {
        fileContents = new String(Files.readAllBytes(filePath));
      }
    }
    catch (Exception e) {}

  resp.setContentType("application/json");
  resp.setCharacterEncoding("UTF-8");
  resp.getWriter().write(fileContents);

 }

}
