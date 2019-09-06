package com.naples.rest;

import java.io.OutputStream;
import java.io.InputStream;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.naples.helper.Error;
import com.naples.util.LeakDetectAnalysisObject;
import com.naples.util.LeakDetectAnalysisResultObject;
import com.naples.util.LeakDetectVerificationObject;

@Path("/leak-detect")
public class LeakDetectService {

  // Location of the analyser command-line tool
  String analyser = "../pleak-leakage-detection-analysis/leakdetect.jar";

  public static boolean isAlive(Process p) {
    try {
      p.exitValue();
      return false;
    } catch (IllegalThreadStateException e) {
      return true;
    }
  }

  @POST
  @Path("/verification")
  @PermitAll
  @Consumes(MediaType.APPLICATION_JSON)
  public Response chooseVerification(LeakDetectVerificationObject object) {

    // Location of model
    String modelPath = System.getProperty("user.dir") + "/src/main/webapp/files/" + object.getModelId();

    String verificationType = object.getVerificationType();

    LeakDetectAnalysisResultObject resultObject = new LeakDetectAnalysisResultObject();

    String result = "";

    ProcessBuilder builder = new ProcessBuilder("java", "-jar", analyser);

    builder.redirectErrorStream(true);

    try {

      Process process = builder.start();
      InputStream out = process.getInputStream();
      OutputStream in = process.getOutputStream();
      InputStream err = process.getErrorStream();

      byte[] buffer = new byte[4000];

      while (isAlive(process)) {
        int no = out.available();
        if (no > 0) {
          int n = out.read(buffer, 0, Math.min(no, buffer.length));
          String output = new String(buffer, 0, n);
          System.out.println(output);
          if (output.contains("Insert file name")) {
            in.write((modelPath + "\n").getBytes());
            in.flush();
          } else if (output.contains("Select action:")) {
            in.write((verificationType + "\n").getBytes());
            in.flush();
          } else if (output.contains("All task in the model:") && output.contains("Choose task:")) {
            resultObject.setResult(output.replace("All task in the model:\n", "").replace("\nChoose task: \n", ""));
            return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();
          } else if (output.contains("PARTECIPANTS:") && output.contains("Choose partecipant:")) {
            resultObject.setResult(output.replace("PARTECIPANTS:\n", "").replace("\nChoose partecipant: \n", ""));
            process.destroy();
            return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();
          } else if (output.contains("No JSON file generated because there isn't a path to show")) {
            resultObject.setResult("false");
            in.write(("N\n").getBytes());
            in.flush();
            process.destroy();
            return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();
          } else if (output.contains("PATH") && output.contains("CONTINUE (Y/N)")) {
            resultObject.setResult(output.replace("\nCONTINUE (Y/N) :", "").replace("true\n", "").replace("PATH : ", ""));
            in.write(("N\n").getBytes());
            in.flush();
            process.destroy();
            return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();
          }
        }

        for (int i = 0; i < err.available(); i++) {
          System.out.println("" + err.read());
        }

        try {
          Thread.sleep(10);
        } catch (Exception e3) {
          e3.printStackTrace();
        }
      }

      return Response.ok(result).type(MediaType.APPLICATION_JSON).build();

    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
    }

  }

  @POST
  @Path("/analysis")
  @PermitAll
  @Consumes(MediaType.APPLICATION_JSON)
  public Response analyse(LeakDetectAnalysisObject object) {

    // Location of model
    String modelPath = System.getProperty("user.dir") + "/src/main/webapp/files/" + object.getModelId();

    String verificationType = object.getVerificationType();

    String analysisTarget = object.getAnalysisTarget();

    LeakDetectAnalysisResultObject resultObject = new LeakDetectAnalysisResultObject();

    String result = "";

    ProcessBuilder builder = new ProcessBuilder("java", "-jar", analyser);

    builder.redirectErrorStream(true);

    try {

      Process process = builder.start();
      InputStream out = process.getInputStream();
      OutputStream in = process.getOutputStream();
      InputStream err = process.getErrorStream();

      byte[] buffer = new byte[4000];

      while (isAlive(process)) {
        int no = out.available();
        if (no > 0) {
          int n = out.read(buffer, 0, Math.min(no, buffer.length));
          String output = new String(buffer, 0, n);
          System.out.println(output);
          if (output.contains("Insert file name")) {
            in.write((modelPath + "\n").getBytes());
            in.flush();
          } else if (output.contains("Select action:")) {
            in.write((verificationType + "\n").getBytes());
            in.flush();
          } else if (output.contains("All task in the model:") && output.contains("Choose task:")) {
            in.write((analysisTarget + "\n").getBytes());
            in.flush();
          } else if (output.contains("PARTECIPANTS:") && output.contains("Choose partecipant:")) {
            in.write((analysisTarget + "\n").getBytes());
            in.flush();
          } else if (output.contains("All data in the model") && output.contains("Choose data (, in the middle):")) {
            resultObject.setResult(output.replace("All data in the model ", "").replace("\n\nChoose data (, in the middle):", ""));
            process.destroy();
            return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();
          }
        }

        for (int i = 0; i < err.available(); i++) {
          System.out.println("" + err.read());
        }

        try {
          Thread.sleep(10);
        } catch (Exception e3) {
          e3.printStackTrace();
        }
      }

      return Response.ok(result).type(MediaType.APPLICATION_JSON).build();

    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
    }

  }

  @POST
  @Path("/analysis2")
  @PermitAll
  @Consumes(MediaType.APPLICATION_JSON)
  public Response analyse2(LeakDetectAnalysisObject object) {

    // Location of model
    String modelPath = System.getProperty("user.dir") + "/src/main/webapp/files/" + object.getModelId();

    String verificationType = object.getVerificationType();

    String analysisTarget = object.getAnalysisTarget();

    String analysisFinalTargets = object.getAnalysisFinalTargets();

    LeakDetectAnalysisResultObject resultObject = new LeakDetectAnalysisResultObject();

    String result = "";

    ProcessBuilder builder = new ProcessBuilder("java", "-jar", analyser);

    builder.redirectErrorStream(true);

    try {

      Process process = builder.start();
      InputStream out = process.getInputStream();
      OutputStream in = process.getOutputStream();
      InputStream err = process.getErrorStream();

      byte[] buffer = new byte[4000];

      while (isAlive(process)) {
        int no = out.available();
        if (no > 0) {
          int n = out.read(buffer, 0, Math.min(no, buffer.length));
          String output = new String(buffer, 0, n);
          System.out.println(output);
          if (output.contains("Insert file name")) {
            in.write((modelPath + "\n").getBytes());
            in.flush();
          } else if (output.contains("Select action:")) {
            in.write((verificationType + "\n").getBytes());
            in.flush();
          } else if (output.contains("All task in the model:") && output.contains("Choose task:")) {
            in.write((analysisTarget + "\n").getBytes());
            in.flush();
          } else if (output.contains("PARTECIPANTS:") && output.contains("Choose partecipant:")) {
            in.write((analysisTarget + "\n").getBytes());
            in.flush();
          } else if (output.contains("All data in the model") && output.contains("Choose data (, in the middle):")) {
            in.write((analysisFinalTargets + "\n").getBytes());
            in.flush();
          } else if (output.contains("NEVER HAS THIS NUMBER OF PARAMETERS")) {
            resultObject.setResult("NEVER HAS THIS NUMBER OF PARAMETERS");
            in.write(("N\n").getBytes());
            in.flush();
            process.destroy();
            return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();
          } else if (output.contains("No JSON file generated because there isn't a path to show")) {
            resultObject.setResult("false");
            in.write(("N\n").getBytes());
            in.flush();
            process.destroy();
            return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();
          } else if (output.contains("PATH") && output.contains("CONTINUE (Y/N)")) {
            resultObject.setResult(output.replace("\nCONTINUE (Y/N) :", "").replace("true\n", "").replace("PATH : ", ""));
            in.write(("N\n").getBytes());
            in.flush();
            process.destroy();
            return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();
          }
        }

        for (int i = 0; i < err.available(); i++) {
          System.out.println("" + err.read());
        }

        try {
          Thread.sleep(10);
        } catch (Exception e3) {
          e3.printStackTrace();
        }
      }

      return Response.ok(result).type(MediaType.APPLICATION_JSON).build();

    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
    }

  }

}