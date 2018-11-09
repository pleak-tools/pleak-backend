package com.naples.util;

import java.util.List;

public class SQLAnalyserPolicyObject {

    List<SQLAnalyserDerivativeSensitivityAndPolicyDataObject> children;
    String queries;
    String schemas;
    String attackerSettings;
    String sensitiveAttributes;
    String epsilon;

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

}