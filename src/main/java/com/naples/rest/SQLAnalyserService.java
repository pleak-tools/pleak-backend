package com.naples.rest;

import com.naples.helper.Error;
import com.naples.util.LeaksWhenObject;
import com.naples.util.LeaksWhenResultObject;
import com.naples.util.PropagationResultObject;
import com.naples.util.SQLAnalyserCombinedSensitivityObject;
import com.naples.util.SQLAnalyserDataObject;
import com.naples.util.SQLAnalyserDerivativeSensitivityAndPolicyDataObject;
import com.naples.util.SQLAnalyserDerivativeSensitivityObject;
import com.naples.util.SQLAnalyserDerivativeSensitivityResultObject;
import com.naples.util.SQLAnalyserPolicyObject;
import com.naples.util.SQLAnalyserSchemaObject;
import com.naples.util.SQLAnalyserSensitivities;
import com.naples.util.SQLAnalyserSensitivitiesObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.postgresql.core.JavaVersion;






@Path("/sql-privacy")
public class SQLAnalyserService {

    @POST
    @Path("/analyse")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response analyse(SQLAnalyserDataObject object) {

        // Location of SQL-analyser command-line tool
        String analyser = "../pleak-sql-analysis/globalsensitivity-cabal/dist/build/sqla/";

        // Location of temporary sql files that are created for SQL-analyser command-line tool
        String analyser_files = "src/main/webapp/analyser_files/";

        // Delimiter for schema string
        String delimiter = Character.toString((char) 31);

        String schemaFileID = UUID.randomUUID().toString();
        String queryFileID = UUID.randomUUID().toString();

        // Command for SQL-analyser command-line tool to get sensitivities based on schema and query files
        String command = analyser + "sqla -ap -s-1 " + analyser_files + schemaFileID + ".sql " + analyser_files + queryFileID
                + ".sql";

        String schemaString = "";

        StringBuffer output = new StringBuffer();

        try {

            // Create schema string
            for (SQLAnalyserSchemaObject tempObj : object.getSchema()) {
                schemaString += tempObj.getTableId() + delimiter + tempObj.getScript() + delimiter;
            }

            schemaString = schemaString.substring(0, schemaString.length() - 1);

            // Create schema and query files for SQL-analyser command-line tool
            File schemaFile = new File(analyser_files + schemaFileID + ".sql");
            FileOutputStream is1 = new FileOutputStream(schemaFile);
            OutputStreamWriter osw1 = new OutputStreamWriter(is1);
            Writer w1 = new BufferedWriter(osw1);
            w1.write(schemaString);
            w1.close();

            File queryFile = new File(analyser_files + queryFileID + ".sql");
            FileOutputStream is2 = new FileOutputStream(queryFile);
            OutputStreamWriter osw2 = new OutputStreamWriter(is2);
            Writer w2 = new BufferedWriter(osw2);
            w2.write(object.getQuery());
            w2.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Process p;
        try {

            // Read output from SQL-analyser command-line tool
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            // Convert output from analyser to JSON object
            SQLAnalyserSensitivitiesObject sensitivities = new SQLAnalyserSensitivitiesObject();

            String outputString = output.toString();

            String[] parts = outputString.split(delimiter);

            ArrayList<SQLAnalyserSensitivities> resultSet = new ArrayList<SQLAnalyserSensitivities>();

            for (int i = 0; i <= parts.length - 3; i++) {

                if ((i % 2) == 0) {

                    SQLAnalyserSensitivities sensitivity = new SQLAnalyserSensitivities();
                    parts[i] = parts[i].replaceAll("\\s+", "");
                    parts[i + 1] = parts[i + 1].replaceAll("\\s+", "");
                    int sensi = Integer.parseInt(parts[i + 1]);
                    sensitivity.setTableId(parts[i]);
                    sensitivity.setSensitivity(sensi);
                    resultSet.add(sensitivity);

                }
            }

            sensitivities.setResultSet(resultSet);

            String primaryKeysString = parts[parts.length-1].replaceAll("\\s+", "");
            ArrayList<Integer> primaryKeysSet = new ArrayList<Integer>();

            for (int j = 0; j < primaryKeysString.length(); j++) {
                int key = Character.getNumericValue(primaryKeysString.charAt(j));
                primaryKeysSet.add(key);
            }

            sensitivities.setPrimaryKeysSet(primaryKeysSet);

            return Response.ok(sensitivities).type(MediaType.APPLICATION_JSON).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
        } finally {
            // Delete temporary schema and query .sql files after use
            File sFile = new File(analyser_files + schemaFileID + ".sql");
            sFile.delete();

            File qFile = new File(analyser_files + queryFileID + ".sql");
            qFile.delete();
        }
    }

    @POST
    @Path("/propagate")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response propagate(SQLAnalyserPolicyObject object) {
      // same user as for ga
      // if schema is empty button for auto-fill apears
  
      // "create extension cube;";
      // "create extension earthdistance;";
      // Map<String, List<Map<String, Integer>>> tableCols = new HashMap<String, List<Map<String, Integer>>>();

      boolean isParametersCreated = false;
      PropagationResultObject resultObject = new PropagationResultObject();
      resultObject.tableSchemas = new HashMap<String, String>();
      HashMap<String, List<String>> tableColumns = new HashMap<String, List<String>>();
      resultObject.tableDatas = new HashMap<String, String>();
      resultObject.tableConstraints = new HashMap<String, String>();
      String commandError = "";

      String url = "jdbc:postgresql://localhost/ga_propagation";
      String user = "ga_propagation";
      String password = "ceec4eif7ya";
  
      String[][] intermediates = object.getIntermediates();
      String listTablesQuery = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';";
  
      try (Connection con = DriverManager.getConnection(url, user, password); Statement st = con.createStatement();) {
        ResultSet rs = st.executeQuery(listTablesQuery);
        while (rs.next()) {
          System.out.println("DROP " + rs.getString(1));
  
          String dropCurrentTableQuery = "DROP TABLE IF EXISTS " + rs.getString(1) + ";";
          Statement st2 = con.createStatement();
          st2.executeUpdate(dropCurrentTableQuery);
        }
        System.out.println("Database cleaned\n");
      } catch (SQLException ex) {
        Logger lgr = Logger.getLogger(JavaVersion.class.getName());
        lgr.log(Level.SEVERE, ex.getMessage(), ex);
      }
  
      // Clear temporary db, db follows the model name
      // try (Connection con = DriverManager.getConnection(url, user, password);
      // Statement st = con.createStatement();) {
      // st.executeUpdate(createDbSql);
      // System.out.println("Database created successfully...");
      // } catch (SQLException ex) {
      // Logger lgr = Logger.getLogger(JavaVersion.class.getName());
      // lgr.log(Level.SEVERE, ex.getMessage(), ex);
      // }
  
      List<String> visitedTables = new ArrayList<String>(); // for parameters, they are duplicated
      String[] propagationQueries = object.getAllQueries();
      for (int i = 0; i < propagationQueries.length; i++) {
  
        System.out.println(propagationQueries[i]);
        System.out.println("\n");
        System.out.println("\n");

        if(propagationQueries[i].startsWith("CREATE TABLE parameters")) {
          if(!isParametersCreated){
            isParametersCreated = true;
            // System.out.println("paramspam");
          }
          else{
            continue;
          }
        }

        Map<String, List<String>> typesInTable = new HashMap<String, List<String>>();

        try (Connection con = DriverManager.getConnection(url, user, password); Statement st = con.createStatement();) {
          if (propagationQueries[i].startsWith("create table") || propagationQueries[i].startsWith("CREATE TABLE")) {
            st.executeUpdate(propagationQueries[i]);
            String currentCrerateTableName = propagationQueries[i].substring(13).split(" ")[0];
  
            List<Map<String, Integer>> colsInDbOrder = new ArrayList<Map<String, Integer>>();

            for (SQLAnalyserDerivativeSensitivityAndPolicyDataObject tempObj : object.getChildren()) {
              String tableName = tempObj.getName();
              if (visitedTables.contains(tableName) || !currentCrerateTableName.equals(tableName))
                continue;
              
              visitedTables.add(tableName);
              String db = tempObj.getDb();
              // System.out.println(db);
              String dbPruned = db.substring(db.indexOf("\n", 0) + 1);
              // String colsDb = db.substring(0, db.indexOf("\n", 0) + 1);
  
              System.out.println("Filling in " + tableName);
              System.out.println("\n");
              // System.out.println(dbPruned);
  
              // typesInTable.put(tableName, new ArrayList<>());
              String[] headers = db.substring(0, db.indexOf("\n", 0)).split(" ");
              String[] parts = dbPruned.split("\n");
  
              for (int k = 0; k < parts.length; k++) {
                if (parts[k] == "")
                  continue;
  
                // System.out.println(parts[k]);
                String[] cols = parts[k].split(" ");
                String insert = "INSERT INTO " + tableName + " VALUES(";

                for (int m = 0; m < cols.length; m++) {
                  String data = cols[m];
                  Scanner n = new Scanner(data);
                  if (n.hasNextFloat()) {
                    insert += cols[m];
                  } else {
                    insert += "'" + cols[m] + "'";
                  }
                  if (m < cols.length - 1)
                    insert += ", ";
                }
                insert += ");";
                // System.out.println(insert);
                st.executeUpdate(insert);
              }
            }

            // tableCols.put(currentCrerateTableName, colsInDbOrder);

          } else {
            if (propagationQueries[i].contains("into ") || propagationQueries[i].contains("INTO ")) {
              st.executeUpdate(propagationQueries[i]);
              System.out.println("\n");
            } else {
              ResultSet rs = st.executeQuery(propagationQueries[i]);
              // System.out.println("\n");
            }
            // for(int j = 0; j < intermediates.length; j++) {
            // if(propagationQueries[i].contains("into " + intermediates[j])) {
            // while (rs.next()) {
            // System.out.println(rs.getString(1) + "\n" + rs.getString(2));
            // }
            // }
            // }
          }

          for (int j = 0; j < intermediates.length; j++) {
            if (propagationQueries[i].contains("into " + intermediates[j][0])) {
              String getSchemaSql = "select column_name, data_type from INFORMATION_SCHEMA.COLUMNS where table_name = '"
                  + intermediates[j][0] + "';";
              String getDataSql = "select * from " + intermediates[j][0] + ";";
  
              ResultSet rs2 = st.executeQuery(getSchemaSql);
  
              System.out.println(intermediates[j][0] + "\n");
              String tableSchema = "CREATE TABLE " + intermediates[j][0] + "(\n";
              Integer numberOfCols = 0;
              tableColumns.put(intermediates[j][1], new ArrayList<>());
  
              typesInTable.put(intermediates[j][0], new ArrayList<>());

              while (rs2.next()) {
                numberOfCols++;
                tableColumns.get(intermediates[j][1]).add(rs2.getString(1));
                tableSchema += rs2.getString(1) + " " + rs2.getString(2) + ",\n";
                System.out.println(rs2.getString(1) + " " + rs2.getString(2));
                typesInTable.get(intermediates[j][0]).add(rs2.getString(2));
              }
              tableSchema = tableSchema.substring(0, tableSchema.length() - 2);
              tableSchema += "\n);";
  
              resultObject.tableSchemas.put(intermediates[j][1], tableSchema);
  
              StringBuilder tableData = new StringBuilder("[[");
              List<String> cols = tableColumns.get(intermediates[j][1]);
              cols.forEach(col -> {
                tableData.append('"' + col + '"' + ", ");
              });
              tableData.delete(tableData.length() - 2, tableData.length());
              tableData.append("],");
              
              ResultSet rs3 = st.executeQuery(getDataSql);
              while (rs3.next()) {
                tableData.append("[");
                for (int k = 0; k < numberOfCols; k++) {
                  
                  // System.out.println(typesInTable.get(intermediates[j][0]).get(k));
                  // System.out.println("----------------------------------");
                  if(typesInTable.get(intermediates[j][0]).get(k).equals("boolean")) {
                    // System.out.println(rs3.getBoolean(k + 1) + "\n");
                    tableData.append('"' + Boolean.toString(rs3.getBoolean(k + 1)) + '"' + (k < numberOfCols - 1 ? ", " : ""));
                  }
                  else{
                    tableData.append('"' + rs3.getString(k + 1) + '"' + (k < numberOfCols - 1 ? ", " : ""));
                  }
                }
                tableData.append("],");
              }
  
              tableData.delete(tableData.length() - 1, tableData.length());
              tableData.append("]");
              resultObject.tableDatas.put(intermediates[j][1], tableData.toString());
            }
          }
        } catch (SQLException ex) {
          Logger lgr = Logger.getLogger(JavaVersion.class.getName());
          lgr.log(Level.SEVERE, ex.getMessage(), ex);
        }
      }
      
      String attackerSettingsString = object.getAttackerSettings();
      String attackerSettingsFileID = UUID.randomUUID().toString();
      String outputSchemaEmptyFileID = UUID.randomUUID().toString();
      String analyser_files = "src/main/webapp/derivative_analyser_files/";

      File[] outputs = new File[0];
    try{
      File outputSchemaFile = new File(analyser_files + outputSchemaEmptyFileID + ".sql");
      FileOutputStream q0 = new FileOutputStream(outputSchemaFile);

      File attackerSettingsFile = new File(analyser_files + attackerSettingsFileID + ".att");
      FileOutputStream a1 = new FileOutputStream(attackerSettingsFile);
      OutputStreamWriter a2 = new OutputStreamWriter(a1);
      Writer a3 = new BufferedWriter(a2);
      a3.write(attackerSettingsString);
      a3.close();

      // String str1 = object.getQueries();
      String cleanSql = object.getCleanSql();
      String queriesFileID = UUID.randomUUID().toString();
      File queriesFile = new File(analyser_files + queriesFileID + ".sql");
      FileOutputStream q1 = new FileOutputStream(queriesFile);
      OutputStreamWriter q2 = new OutputStreamWriter(q1);
      Writer q3 = new BufferedWriter(q2);
      q3.write(cleanSql);
      q3.close();



      String str2 = object.getSchemas();
      String schemasFileID = UUID.randomUUID().toString();
      File schemasFile = new File(analyser_files + schemasFileID + ".sql");
      FileOutputStream s1 = new FileOutputStream(schemasFile);
      OutputStreamWriter s2 = new OutputStreamWriter(s1);
      Writer s3 = new BufferedWriter(s2);
      s3.write(str2);
      s3.close();
      
      String command = "../pleak-sql-constraint-propagation/dist/build/sql-constraint-propagation/sql-constraint-propagation --connection dbname=ga_propagation --leak-mode if-exists -o " + 
      "output.att " + analyser_files + queriesFileID + ".sql " + analyser_files + attackerSettingsFileID + ".att " + 
      analyser_files + schemasFileID + ".sql " + analyser_files + outputSchemaEmptyFileID + ".sql";

      Process p;
      p = Runtime.getRuntime().exec(command);
      p.waitFor();
      int exitValue = p.waitFor();
      if (exitValue == 0) {
          File dir = new File(analyser_files);

          outputs = dir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String filename)
                        { return filename.endsWith(".att"); }
          });
      }
      else{
        InputStream fromError = p.getErrorStream();
        String outError = "";
        try {
          int in = -1;
          while ((in = fromError.read()) != -1) {
            outError += (char)in;
          }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        commandError = outError;
      }
    } catch (Exception e) {
        e.printStackTrace();
    }

    for(int i = 0; i < outputs.length; i++){
      try{
        byte[] encoded = Files.readAllBytes(Paths.get(outputs[i].toPath().toString()));
        resultObject.tableConstraints.put(outputs[i].getName(), new String(encoded));
      }
      catch(Exception e){}
    }
  
      resultObject.commandError = commandError;
      return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Path("/analyze-guessing-advantage")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response analyzePolicy(SQLAnalyserPolicyObject object) {

        // Location of SQL derivative sensitivity (and policy) analyser command-line tool
        String analyser = "../pleak-sql-analysis/banach/dist/build/banach/";

        // Location of temporary sql, att, plc and db files that are created for analyser command-line tool
        String analyser_files = "src/main/webapp/derivative_analyser_files/";

        String queriesFileID = UUID.randomUUID().toString();

        String schemasFileID = UUID.randomUUID().toString();

        String attackerSettingsFileID = UUID.randomUUID().toString();

        String policyFileID = UUID.randomUUID().toString();

        StringBuffer output = new StringBuffer();

        String epsilon = "--epsilon " + Float.parseFloat(object.getEpsilon());
        String beta = (Float.parseFloat(object.getBeta()) > 0) ? "--beta " + object.getBeta() : "";
        String policy = "--policy=" + analyser_files + policyFileID + ".plc";

        String errorUB = (Float.parseFloat(object.getErrorUB()) > 0) ? "--errorUB " + object.getErrorUB() : ""; // 0.9
        String sigmoidBeta = (Float.parseFloat(object.getSigmoidBeta()) > 0) ? "--sigmoid-beta " + object.getSigmoidBeta() : ""; // 0.01
        String sigmoidPrecision = (Float.parseFloat(object.getSigmoidPrecision()) > 0) ? "--sigmoid-precision " + object.getSigmoidPrecision() : ""; // 5.0
        String dateStyle = (!object.getDateStyle().toString().equals("-1")) ? "--datestyle " + object.getDateStyle() : ""; // European

        String numberOfQueries = (Integer.parseInt(object.getNumberOfQueries()) >= 1) ? "--numOfQueries " + Integer.parseInt(object.getNumberOfQueries()) : "--numOfQueries 1";

        // Command for SQL derivative sensitivity (and policy) analyser command-line tool to get results based on schemas, queries, attacker settings, plc and db files
        String command = analyser + "banach -QDpa --db-create-tables " + analyser_files + schemasFileID + ".sql " + analyser_files + queriesFileID + ".sql " + analyser_files + attackerSettingsFileID + ".att" + " " + policy + " " + epsilon + " " + beta + " " + numberOfQueries + " " + errorUB + " " + sigmoidBeta + " " + sigmoidPrecision + " " + dateStyle + "";

        try {

            String queriesString = object.getQueries();
            String schemasString = object.getSchemas();
            String attackerSettingsString = object.getAttackerSettings();
            String policyString = object.getSensitiveAttributes();

            File queriesFile = new File(analyser_files + queriesFileID + ".sql");
            FileOutputStream is0 = new FileOutputStream(queriesFile);
            OutputStreamWriter osw0 = new OutputStreamWriter(is0);
            Writer w0 = new BufferedWriter(osw0);
            w0.write(queriesString);
            w0.close();

            File schemasFile = new File(analyser_files + schemasFileID + ".sql");
            FileOutputStream is0_2 = new FileOutputStream(schemasFile);
            OutputStreamWriter osw0_2 = new OutputStreamWriter(is0_2);
            Writer w0_2 = new BufferedWriter(osw0_2);
            w0_2.write(schemasString);
            w0_2.close();

            File attackerSettingsFile = new File(analyser_files + attackerSettingsFileID + ".att");
            FileOutputStream is0_3 = new FileOutputStream(attackerSettingsFile);
            OutputStreamWriter osw0_3 = new OutputStreamWriter(is0_3);
            Writer w0_3 = new BufferedWriter(osw0_3);
            w0_3.write(attackerSettingsString);
            w0_3.close();

            File  policyFile = new File(analyser_files +  policyFileID + ".plc");
            FileOutputStream is0_4 = new FileOutputStream(policyFile);
            OutputStreamWriter osw0_4 = new OutputStreamWriter(is0_4);
            Writer w0_4 = new BufferedWriter(osw0_4);
            w0_4.write(policyString);
            w0_4.close();

            for (SQLAnalyserDerivativeSensitivityAndPolicyDataObject tempObj : object.getChildren()) {
                String name = tempObj.getName();
                String db = tempObj.getDb();

                File dbFile = new File(analyser_files + name + ".db");
                FileOutputStream is2 = new FileOutputStream(dbFile);
                OutputStreamWriter osw2 = new OutputStreamWriter(is2);
                Writer w2 = new BufferedWriter(osw2);
                w2.write(db);
                w2.close();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Process p;
        try {

            SQLAnalyserDerivativeSensitivityResultObject resultObject = new SQLAnalyserDerivativeSensitivityResultObject();

            // Read output from SQL derivative sensitivity (and policy) analyser command-line tool
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            int exitValue = p.waitFor();
            if (exitValue == 0) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

                String line = "";
                while ((line = reader.readLine()) != null) {
                    output.append(line + "\n");
                }

                String outputString = output.toString();

                resultObject.setResult(outputString);

                return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();

            } else {
                try (BufferedReader b = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    String line;
                    while ((line = b.readLine()) != null) {
                        output.append(line + "\n");
                    }
                    String outputString = output.toString();

                    resultObject.setResult(outputString);
                } catch (IOException e) {
                    resultObject.setResult("Analyzer failure");
                }
            }

            return Response.status(409).entity(new Error(resultObject.getResult())).type(MediaType.APPLICATION_JSON).build();

        } catch (Exception e) {
            return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
        } finally {
            // Delete temporary files after use
            File queriesFile = new File(analyser_files + queriesFileID + ".sql");
            queriesFile.delete();

            File schemasFile = new File(analyser_files + schemasFileID + ".sql");
            schemasFile.delete();

            File attackerSettingsFile = new File(analyser_files + attackerSettingsFileID + ".att");
            attackerSettingsFile.delete();

            File policyFile = new File(analyser_files + policyFileID + ".plc");
            policyFile.delete();

            for (SQLAnalyserDerivativeSensitivityAndPolicyDataObject tempObj : object.getChildren()) {
                String name = tempObj.getName();

                File dbFile = new File(analyser_files + name + ".db");
                dbFile.delete();

            }
        }

    }

    @POST
    @Path("/analyze-derivative-sensitivity")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response analyzeDerivativeSensitivity(SQLAnalyserDerivativeSensitivityObject object) {

        // Location of SQL derivative sensitivity analyser command-line tool
        String analyser = "../pleak-sql-analysis/banach/dist/build/banach/";

        // Location of temporary sql, nrm and db files that are created for SQL-analyser command-line tool
        String analyser_files = "src/main/webapp/derivative_analyser_files/";

        String attackerSettingsFileID = UUID.randomUUID().toString();

        String queriesFileID = UUID.randomUUID().toString();

        String schemasFileID = UUID.randomUUID().toString();

        StringBuffer output = new StringBuffer();

        String beta = "";

        if (Float.parseFloat(object.getBeta()) > 0) {
          beta = "--beta " + Float.parseFloat(object.getBeta());
        }

        String epsilon = "--epsilon " + Float.parseFloat(object.getEpsilon());

        String errorUB = (Float.parseFloat(object.getErrorUB()) > 0) ? "--errorUB " + object.getErrorUB() : ""; // 0.9
        String sigmoidBeta = (Float.parseFloat(object.getSigmoidBeta()) > 0) ? "--sigmoid-beta " + object.getSigmoidBeta() : ""; // 0.01
        String sigmoidPrecision = (Float.parseFloat(object.getSigmoidPrecision()) > 0) ? "--sigmoid-precision " + object.getSigmoidPrecision() : ""; // 5.0
        String dateStyle = (!object.getDateStyle().toString().equals("-1")) ? "--datestyle " + object.getDateStyle() : ""; // European

        // Command for SQL derivative sensitivity analyser command-line tool to get sensitivities based on schemas, queries, nrm and db files
        String command = analyser + "banach -QDa --db-create-tables " + analyser_files + schemasFileID + ".sql " + analyser_files + queriesFileID + ".sql " + analyser_files + attackerSettingsFileID + ".att" + " " + epsilon + " " + beta + " " + errorUB + " " + sigmoidBeta + " " + sigmoidPrecision + " " + dateStyle + "";

        try {

            String queriesString = object.getQueries();
            String schemasString = object.getSchemas();
            String attackerSettingsString = object.getAttackerSettings();

            File queriesFile = new File(analyser_files + queriesFileID + ".sql");
            FileOutputStream is0 = new FileOutputStream(queriesFile);
            OutputStreamWriter osw0 = new OutputStreamWriter(is0);
            Writer w0 = new BufferedWriter(osw0);
            w0.write(queriesString);
            w0.close();

            File schemasFile = new File(analyser_files + schemasFileID + ".sql");
            FileOutputStream is0_2 = new FileOutputStream(schemasFile);
            OutputStreamWriter osw0_2 = new OutputStreamWriter(is0_2);
            Writer w0_2 = new BufferedWriter(osw0_2);
            w0_2.write(schemasString);
            w0_2.close();

            File attackerSettingsFile = new File(analyser_files + attackerSettingsFileID + ".att");
            FileOutputStream is0_3 = new FileOutputStream(attackerSettingsFile);
            OutputStreamWriter osw0_3 = new OutputStreamWriter(is0_3);
            Writer w0_3 = new BufferedWriter(osw0_3);
            w0_3.write(attackerSettingsString);
            w0_3.close();

            for (SQLAnalyserDerivativeSensitivityAndPolicyDataObject tempObj : object.getChildren()) {
                String name = tempObj.getName();
                String nrm = tempObj.getNrm();
                String db = tempObj.getDb();

                File nrmFile = new File(analyser_files + name + ".nrm");
                FileOutputStream is1 = new FileOutputStream(nrmFile);
                OutputStreamWriter osw1 = new OutputStreamWriter(is1);
                Writer w1 = new BufferedWriter(osw1);
                w1.write(nrm);
                w1.close();

                File dbFile = new File(analyser_files + name + ".db");
                FileOutputStream is2 = new FileOutputStream(dbFile);
                OutputStreamWriter osw2 = new OutputStreamWriter(is2);
                Writer w2 = new BufferedWriter(osw2);
                w2.write(db);
                w2.close();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Process p;
        try {

            SQLAnalyserDerivativeSensitivityResultObject resultObject = new SQLAnalyserDerivativeSensitivityResultObject();

            // Read output from SQL derivative sensitivity analyser command-line tool
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            int exitValue = p.waitFor();
            if (exitValue == 0) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

                String line = "";
                while ((line = reader.readLine()) != null) {
                    output.append(line + "\n");
                }

                String outputString = output.toString();

                resultObject.setResult(outputString);

                return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();

            } else {
                try (BufferedReader b = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    String line;
                    while ((line = b.readLine()) != null) {
                        output.append(line + "\n");
                    }
                    String outputString = output.toString();

                    resultObject.setResult(outputString);
                } catch (IOException e) {
                    resultObject.setResult("Analyzer failure");
                }
            }

            return Response.status(409).entity(new Error(resultObject.getResult())).type(MediaType.APPLICATION_JSON).build();

        } catch (Exception e) {
            return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
        } finally {
            // Delete temporary files after use
            File queriesFile = new File(analyser_files + queriesFileID + ".sql");
            queriesFile.delete();

            File schemasFile = new File(analyser_files + schemasFileID + ".sql");
            schemasFile.delete();

            File attackerSettingsFile = new File(analyser_files + attackerSettingsFileID + ".att");
            attackerSettingsFile.delete();

            for (SQLAnalyserDerivativeSensitivityAndPolicyDataObject tempObj : object.getChildren()) {
                String name = tempObj.getName();

                File nrmFile = new File(analyser_files + name + ".nrm");
                nrmFile.delete();

                File dbFile = new File(analyser_files + name + ".db");
                dbFile.delete();

            }
        }

    }

    @POST
    @Path("/analyze-leaks-when")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response analyzeLeaksWhen(LeaksWhenObject object) {

        // Location of combined sensitivity analyser command-line tool
        String analyser = "../pleak-leaks-when-analysis/src/";

        String analyser_input_files = "src/main/webapp/leaks_when_files/inputs/";

        String analyser_result_files = "src/main/webapp/leaks_when_files/results/";

        String modelFileID = UUID.randomUUID().toString();

        StringBuffer output = new StringBuffer();

        // Command for combined sensitivity analyser command-line tool to get sensitivities based on schemas, queries, nrm and db files
        String command = analyser + "GrbDriver.native " + analyser_result_files + " " + analyser_input_files + modelFileID + ".bpmn";

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
                ret.append((char)c);
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

    @POST
    @Path("/analyze-combined-sensitivity")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response analyzeCombinedSensitivity(SQLAnalyserCombinedSensitivityObject object) {

        // Location of combined sensitivity analyser command-line tool
        String analyser = "../pleak-sql-analysis/banach/dist/build/banach/";

        // Location of temporary sql, nrm and db files that are created for SQL-analyser command-line tool
        String analyser_files = "src/main/webapp/derivative_analyser_files/";

        String attackerSettingsFileID = UUID.randomUUID().toString();

        String queriesFileID = UUID.randomUUID().toString();

        String schemasFileID = UUID.randomUUID().toString();

        StringBuffer output = new StringBuffer();

        String beta = "";

        if (Float.parseFloat(object.getBeta()) > 0) {
          beta = "--beta " + Float.parseFloat(object.getBeta());
        }

        String epsilon = "--epsilon " + Float.parseFloat(object.getEpsilon());
        String gdistance = "--distance-G 1.0"; // + Float.parseFloat(object.getDistanceG());
        String local = "--localsenspath=../pleak-sql-analysis/banach/";

        String errorUB = (Float.parseFloat(object.getErrorUB()) > 0) ? "--errorUB " + object.getErrorUB() : ""; // 0.9
        String sigmoidBeta = (Float.parseFloat(object.getSigmoidBeta()) > 0) ? "--sigmoid-beta " + object.getSigmoidBeta() : ""; // 0.01
        String sigmoidPrecision = (Float.parseFloat(object.getSigmoidPrecision()) > 0) ? "--sigmoid-precision " + object.getSigmoidPrecision() : ""; // 5.0
        String dateStyle = (!object.getDateStyle().toString().equals("-1")) ? "--datestyle " + object.getDateStyle() : ""; // European

        // Command for combined sensitivity analyser command-line tool to get sensitivities based on schemas, queries, nrm and db files
        String command = analyser + "banach -QDca --db-create-tables " + analyser_files + schemasFileID + ".sql " + analyser_files + queriesFileID + ".sql " + analyser_files + attackerSettingsFileID + ".att" + " " + local + " " + epsilon + " " + beta + " " + gdistance + " " + errorUB + " " + sigmoidBeta + " " + sigmoidPrecision + " " + dateStyle + "";

        try {

            String queriesString = object.getQueries();
            String schemasString = object.getSchemas();
            String attackerSettingsString = object.getAttackerSettings();

            File queriesFile = new File(analyser_files + queriesFileID + ".sql");
            FileOutputStream is0 = new FileOutputStream(queriesFile);
            OutputStreamWriter osw0 = new OutputStreamWriter(is0);
            Writer w0 = new BufferedWriter(osw0);
            w0.write(queriesString);
            w0.close();

            File schemasFile = new File(analyser_files + schemasFileID + ".sql");
            FileOutputStream is0_2 = new FileOutputStream(schemasFile);
            OutputStreamWriter osw0_2 = new OutputStreamWriter(is0_2);
            Writer w0_2 = new BufferedWriter(osw0_2);
            w0_2.write(schemasString);
            w0_2.close();

            File attackerSettingsFile = new File(analyser_files + attackerSettingsFileID + ".att");
            FileOutputStream is0_3 = new FileOutputStream(attackerSettingsFile);
            OutputStreamWriter osw0_3 = new OutputStreamWriter(is0_3);
            Writer w0_3 = new BufferedWriter(osw0_3);
            w0_3.write(attackerSettingsString);
            w0_3.close();

            for (SQLAnalyserDerivativeSensitivityAndPolicyDataObject tempObj : object.getChildren()) {
                String name = tempObj.getName();
                String nrm = tempObj.getNrm();
                String db = tempObj.getDb();

                File nrmFile = new File(analyser_files + name + ".nrm");
                FileOutputStream is1 = new FileOutputStream(nrmFile);
                OutputStreamWriter osw1 = new OutputStreamWriter(is1);
                Writer w1 = new BufferedWriter(osw1);
                w1.write(nrm);
                w1.close();

                File dbFile = new File(analyser_files + name + ".db");
                FileOutputStream is2 = new FileOutputStream(dbFile);
                OutputStreamWriter osw2 = new OutputStreamWriter(is2);
                Writer w2 = new BufferedWriter(osw2);
                w2.write(db);
                w2.close();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Process p;
        try {

            SQLAnalyserDerivativeSensitivityResultObject resultObject = new SQLAnalyserDerivativeSensitivityResultObject();

            // Read output from combined sensitivity analyser command-line tool
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            int exitValue = p.waitFor();
            if (exitValue == 0) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

                String line = "";
                while ((line = reader.readLine()) != null) {
                    output.append(line + "\n");
                }

                String outputString = output.toString();

                resultObject.setResult(outputString);

                return Response.ok(resultObject).type(MediaType.APPLICATION_JSON).build();

            } else {
                try (BufferedReader b = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                    String line;
                    while ((line = b.readLine()) != null) {
                        output.append(line + "\n");
                    }
                    String outputString = output.toString();

                    resultObject.setResult(outputString);
                } catch (IOException e) {
                    resultObject.setResult("Analyzer failure");
                }
            }

            return Response.status(409).entity(new Error(resultObject.getResult())).type(MediaType.APPLICATION_JSON).build();

        } catch (Exception e) {
            return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
        } finally {
            // Delete temporary files after use
            File queriesFile = new File(analyser_files + queriesFileID + ".sql");
            queriesFile.delete();

            File schemasFile = new File(analyser_files + schemasFileID + ".sql");
            schemasFile.delete();

            File attackerSettingsFile = new File(analyser_files + attackerSettingsFileID + ".att");
            attackerSettingsFile.delete();

            for (SQLAnalyserDerivativeSensitivityAndPolicyDataObject tempObj : object.getChildren()) {
                String name = tempObj.getName();

                File nrmFile = new File(analyser_files + name + ".nrm");
                nrmFile.delete();

                File dbFile = new File(analyser_files + name + ".db");
                dbFile.delete();

            }
        }

    }

}
