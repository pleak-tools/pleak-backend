package com.naples.util;

import java.util.List;

public class SQLAnalyserDataObject {

    List<SQLAnalyserSchemaObject> schema;
    String query;

    public List<SQLAnalyserSchemaObject> getSchema() {
        return schema;
    }

    public void setSchema(List<SQLAnalyserSchemaObject> schema) {
        this.schema = schema;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

}