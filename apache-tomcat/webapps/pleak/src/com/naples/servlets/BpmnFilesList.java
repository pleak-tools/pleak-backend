package com.naples.servlets;

import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.naples.responses.Response;
import com.naples.helpers.FileHelper;

public class BpmnFilesList extends HttpServlet{

 @Override
 protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                  throws ServletException, IOException {

    Response response = new Response();
    FileHelper fh = new FileHelper();

    String pathStr = getServletContext().getRealPath(getServletContext().getInitParameter("bpmn-files-dir"));
    Path filesPath = Paths.get(pathStr);

    List<Path> filePaths = new ArrayList<>();
    List<String> fileNames = new ArrayList<>();
    List<String> fileModifiedDates = new ArrayList<>();
    try {
      filePaths = fh.getFilePaths(filesPath);
      fileNames = fh.getFileNames(filePaths);
      fileModifiedDates = fh.getFileModifiedDates(filePaths);

      response.setResponseFiles(fileNames, fileModifiedDates);
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
