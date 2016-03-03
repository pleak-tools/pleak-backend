package com.naples.servlets;

import java.io.IOException;
import java.nio.file.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BpmnFilesList extends HttpServlet{

 @Override
 protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                  throws ServletException, IOException {

     String pathStr = getServletContext().getRealPath(getServletContext().getInitParameter("bpmn-files-dir"));
     Path filesPath = Paths.get(pathStr);

     String files = "{\"files\": [";
     try (DirectoryStream<Path> stream = Files.newDirectoryStream(filesPath, "*.bpmn")) {
         for (Path entry: stream) {
             files = files + "\"" + entry.getFileName() + "\", ";
         }
     } catch (DirectoryIteratorException ex) {
//         throw ex.getCause();
     }
     if (!files.equals("{\"files\": [")) {
         files = files.substring(0, files.length() - 2);
     }
     files = files + "]}";

     resp.setContentType("application/json");
     resp.setCharacterEncoding("UTF-8");
     resp.getWriter().write(files);

 }

}
