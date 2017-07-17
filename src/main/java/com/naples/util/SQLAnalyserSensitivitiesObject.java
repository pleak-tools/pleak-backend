package com.naples.util;

import java.util.ArrayList;

public class SQLAnalyserSensitivitiesObject {

    public ArrayList<SQLAnalyserSensitivities> resultSet;
    public ArrayList<Integer> primaryKeysSet;

    public ArrayList<SQLAnalyserSensitivities> getResultSet() {
        return resultSet;
    }

    public void setResultSet(ArrayList<SQLAnalyserSensitivities> resultSet) {
        this.resultSet = resultSet;
    }

    public ArrayList<Integer> getPrimaryKeysSet() {
        return primaryKeysSet;
    }

    public void setPrimaryKeysSet(ArrayList<Integer> primaryKeysSet) {
        this.primaryKeysSet = primaryKeysSet;
    }

}
