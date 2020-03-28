package com.naples.rest;

import com.naples.helper.Error;
import com.naples.util.SQLAnalyserCombinedSensitivityObject;
import com.naples.util.SQLAnalyserDataObject;
import com.naples.util.SQLAnalyserDerivativeSensitivityAndPolicyDataObject;
import com.naples.util.SQLAnalyserDerivativeSensitivityObject;
import com.naples.util.SQLAnalyserDerivativeSensitivityResultObject;
import com.naples.util.SQLAnalyserSchemaObject;
import com.naples.util.SQLAnalyserSensitivities;
import com.naples.util.SQLAnalyserSensitivitiesObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.UUID;
import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// Used in Sensitivities editor

@Path("/sensitivities")
public class SensitivitiesService {

    @POST
    @Path("/global-sensitivity-analysis")
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
    @Path("/derivative-sensitivity-analysis")
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
    @Path("/combined-sensitivity-analysis")
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
