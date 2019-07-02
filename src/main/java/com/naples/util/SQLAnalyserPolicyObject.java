package com.naples.util;

import java.util.List;

public class SQLAnalyserPolicyObject {

    List<SQLAnalyserDerivativeSensitivityAndPolicyDataObject> children;
    String queries;
    String schemas;
    String attackerSettings;
    String sensitiveAttributes;
    String epsilon;
    String numberOfQueries;
    String[] allQueries;
    String[] intermediates;
    String modelName;

    public List<SQLAnalyserDerivativeSensitivityAndPolicyDataObject> getChildren() {
        return this.children;
    }

    public void setChildren(List<SQLAnalyserDerivativeSensitivityAndPolicyDataObject> children) {
        this.children = children;
    }

    public String getQueries() {
        return this.queries;
    }

    public void setQueries(String queries) {
        this.queries = queries;
    }

    public String getSchemas() {
        return this.schemas;
    }

    public void setSchemas(String schemas) {
        this.schemas = schemas;
    }

    public String getAttackerSettings() {
        return this.attackerSettings;
    }

    public void setAttackerSettings(String attackerSettings) {
        this.attackerSettings = attackerSettings;
    }

    public String getSensitiveAttributes() {
        return this.sensitiveAttributes;
    }

    public void setSensitiveAttributes(String sensitiveAttributes) {
        this.sensitiveAttributes = sensitiveAttributes;
    }

    public String getEpsilon() {
        return this.epsilon;
    }

    public void setEpsilon(String epsilon) {
        this.epsilon = epsilon;
    }

    public String getNumberOfQueries() {
      return this.numberOfQueries;
    }

    public void setNumberOfQueries(String numberOfQueries) {
      this.numberOfQueries = numberOfQueries;
    }

    public String getModelName() {
      return this.modelName;
    }

    public String[] getIntermediates() {
      return this.intermediates;
    }

    public String[] getAllQueries() {
      return this.allQueries;
    }

}