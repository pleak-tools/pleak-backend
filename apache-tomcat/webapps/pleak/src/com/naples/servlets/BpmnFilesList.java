package com.naples.servlets;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.naples.responses.Response;

public class BpmnFilesList extends HttpServlet{

 @Override
 protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                  throws ServletException, IOException {

    Response response = new Response();

    String pathStr = getServletContext().getRealPath(getServletContext().getInitParameter("bpmn-files-dir"));
    Path filesPath = Paths.get(pathStr);

    List<String> files = new ArrayList<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(filesPath, "*.bpmn")) {
      for (Path entry: stream) {
        files.add(entry.getFileName().toString());
      }
      response.setResponseList(files);
      resp.setStatus(HttpServletResponse.SC_OK);
    } catch (Exception ex) {
//         throw ex.getCause();
      response.setResponseError(ex.getMessage());
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().write(response.toJson());

  }

}
