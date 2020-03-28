package com.naples.rest;

import com.naples.helper.Error;
import com.naples.util.LeaksWhenObject;
import com.naples.util.LeaksWhenResultObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.OutputStream;
import com.naples.util.LeakDetectAnalysisObject;
import com.naples.util.LeakDetectAnalysisResultObject;
import com.naples.util.LeakDetectVerificationObject;

// Used in PE-BPMN & Leaks-When editor

@Path("/pe-bpmn-leaks-when")
public class PEBPMNLeaksWhenService {

  // Location of the leakage detection analyser command-line tool
  String leakageDetectionAnalyser = "../pleak-leakage-detection-analysis/leakdetect.jar";

  public static boolean isAlive(Process p) {
    try {
      p.exitValue();
      return false;
    } catch (IllegalThreadStateException e) {
      return true;
    }
  }

  @POST
  @Path("/leakage-detection-analysis-verification")
  @PermitAll
  @Consumes(MediaType.APPLICATION_JSON)
  public Response chooseVerification(LeakDetectVerificationObject object) {

    // Location of model
    String modelPath = System.getProperty("user.dir") + "/src/main/webapp/files/" + object.getModelId();

    String verificationType = object.getVerificationType();

    LeakDetectAnalysisResultObject resultObject = new LeakDetectAnalysisResultObject();

    String result = "";

    ProcessBuilder builder = new ProcessBuilder("java", "-jar", leakageDetectionAnalyser);

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
            resultObject.setResult(output.replace("All task in the model:\n", "").replace("\n\n Choose task: \n\n", "")
                .replace("\nChoose task: \n\n", "").replace("\nChoose task:\n", ""));
            return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();
          } else if (output.contains("PARTECIPANTS:") && output.contains("Choose partecipant:")) {
            resultObject.setResult(output.replace("PARTECIPANTS:\n", "").replace("\n\n Choose partecipant: \n \n", ""));
            process.destroy();
            return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();
          } else if (output.contains("No JSON file generated because there isn't a path to show")
              || output.contains("SSSHARING IS PRESERVED") || output.contains("NOT RECOSTRUCTED")
              || output.contains("ENCRYPTION IS PRESERVED")) {
            resultObject.setResult("false");
            in.write(("N\n").getBytes());
            in.flush();
            process.destroy();
            return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();
          } else if (output.contains("No SSsharing PET over this model")) {
            resultObject.setResult("No SSsharing PET over this model");
            process.destroy();
            return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();
          } else if (output.contains("NO RECOSTRUCTION ACTION TASK IN THE MODEL")) {
            resultObject.setResult("No reconstruction task in the model");
            process.destroy();
            return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();
          } else if (output.contains("PATH") && output.contains("CONTINUE (Y/N)")) {
            resultObject
                .setResult(output.replace("\nCONTINUE (Y/N) :", "").replace("true\n", "").replace("PATH : ", ""));
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
  @Path("/leakage-detection-analysis-step1")
  @PermitAll
  @Consumes(MediaType.APPLICATION_JSON)
  public Response analyse(LeakDetectAnalysisObject object) {

    // Location of model
    String modelPath = System.getProperty("user.dir") + "/src/main/webapp/files/" + object.getModelId();

    String verificationType = object.getVerificationType();

    String analysisTarget = object.getAnalysisTarget();

    LeakDetectAnalysisResultObject resultObject = new LeakDetectAnalysisResultObject();

    String result = "";

    ProcessBuilder builder = new ProcessBuilder("java", "-jar", leakageDetectionAnalyser);

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
            System.out.println(analysisTarget);
            in.write((analysisTarget + "\n").getBytes());
            in.flush();
          } else if (output.contains("PARTECIPANTS:") && output.contains("Choose partecipant:")) {
            System.out.println(analysisTarget);
            in.write((analysisTarget + "\n").getBytes());
            in.flush();
          } else if (output.contains("All data in the model") && output.contains("Choose data (, in the middle):")) {
            resultObject.setResult(
                output.replace("All data in the model ", "").replace("\n\nChoose data (, in the middle):", ""));
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
  @Path("/leakage-detection-analysis-step2")
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

    ProcessBuilder builder = new ProcessBuilder("java", "-jar", leakageDetectionAnalyser);

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
            System.out.println(analysisTarget);
            in.write((analysisTarget + "\n").getBytes());
            in.flush();
          } else if (output.contains("PARTECIPANTS:") && output.contains("Choose partecipant:")) {
            System.out.println(analysisTarget);
            in.write((analysisTarget + "\n").getBytes());
            in.flush();
          } else if (output.contains("All data in the model") && output.contains("Choose data (, in the middle):")) {
            System.out.println(analysisFinalTargets);
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
            resultObject
                .setResult(output.replace("\nCONTINUE (Y/N) :", "").replace("true\n", "").replace("PATH : ", ""));
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
  @Path("/bpmn-leaks-when-analysis")
  @PermitAll
  @Consumes(MediaType.APPLICATION_JSON)
  public Response analyzeLeaksWhen(LeaksWhenObject object) {

    // Location of combined sensitivity analyser command-line tool
    String analyser = "../pleak-leaks-when-analysis/src/";

    String analyser_input_files = "src/main/webapp/leaks_when_files/inputs/";

    String analyser_result_files = "src/main/webapp/leaks_when_files/results/";

    String modelFileID = UUID.randomUUID().toString();

    StringBuffer output = new StringBuffer();

    // Command for combined sensitivity analyser command-line tool to get
    // sensitivities based on schemas, queries, nrm and db files
    String command = analyser + "GrbDriver.native " + analyser_result_files + " " + analyser_input_files + modelFileID
        + ".bpmn";

    try {

      String modelString = object.getModel();

      File modelFile = new File(analyser_input_files + modelFileID + ".bpmn");
      FileOutputStream is0 = new FileOutputStream(modelFile);
      OutputStreamWriter osw0 = new OutputStreamWriter(is0);
      Writer w0 = new BufferedWriter(osw0);
      w0.write(modelString);
      w0.close();

    } catch (Exception e) {
      e.printStackTrace();
    }

    try {

      LeaksWhenResultObject resultObject = new LeaksWhenResultObject();

      try {

        StringBuffer ret = new StringBuffer();
        Process child = Runtime.getRuntime().exec(command);
        // Get the input stream and read from it
        InputStream in = child.getInputStream();
        int c;
        while ((c = in.read()) != -1) {
          ret.append((char) c);
        }
        in.close();
        System.out.println(ret.toString());

      } catch (IOException e) {
        e.printStackTrace();
        resultObject.setResult("Analyzer failure");
      }

      BufferedReader reader = Files.newBufferedReader(Paths.get(analyser_result_files + "flowcheckresults"));

      String line = "";
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }

      String outputString = output.toString();

      resultObject.setResult(outputString);

      return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();

    } catch (Exception e) {
      return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
    } finally {
      // Delete temporary file after use
      File modelFile = new File(analyser_input_files + modelFileID + ".bpmn");
      modelFile.delete();

      File resultFile = new File(analyser_result_files + "flowcheckresults");
      resultFile.delete();

      File dir = new File("tempfiles");
      for (File file : dir.listFiles()) {
        if (!file.isDirectory()) {
          file.delete();
        }
      }
    }

  }

}
