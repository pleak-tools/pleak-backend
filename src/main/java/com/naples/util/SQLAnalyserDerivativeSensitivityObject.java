package com.naples.util;

import java.util.List;

public class SQLAnalyserDerivativeSensitivityObject {

    List<SQLAnalyserDerivativeSensitivityDataObject> children;
    String queries;
    String schemas;
    String epsilon;
    String beta;

    public List<SQLAnalyserDerivativeSensitivityDataObject> getChildren() {
        return this.children;
    }

    public void setChildren(List<SQLAnalyserDerivativeSensitivityDataObject> children) {
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

    public String getEpsilon() {
        return this.epsilon;
    }

    public void setEpsilon(String epsilon) {
        this.epsilon = epsilon;
    }

    public String getBeta() {
        return this.beta;
    }

    public void setBeta(String beta) {
        this.beta = beta;
    }

}