package com.naples.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.*;

public class BpmnFileSave extends HttpServlet{

 @Override
 protected void doPost(HttpServletRequest req, HttpServletResponse resp)
                  throws ServletException, IOException {

     String result = "{save: ";

     String fileName = req.getParameter("fileName");
     // TODO: Some checks to ensure that file name doesn't try to break out of the directory with "../sthsth", has
     // correct file extension etc.

     Part filePart = req.getPart("file");
     InputStream fileContent = filePart.getInputStream();

     String filePathStr = getServletContext().getRealPath(getServletContext().getInitParameter("bpmn-files-dir")) + fileName;
     Path filePath = Paths.get(filePathStr);

     try {
         Files.copy(fileContent, filePath);
         result = result + "'ok'";
     }
     catch (Exception e) {
         result = result + "'error', errorMessage: '" + e + "'";
     }

     result = result + "}";

    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().write(result);
 }

}
