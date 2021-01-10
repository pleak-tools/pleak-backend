package com.naples.util;

import java.util.List;

public class SQLAnalyserCombinedSensitivityObject {

  List<SQLAnalyserDerivativeSensitivityAndPolicyDataObject> children;
  String queries;
  String schemas;
  String attackerSettings;
  String epsilon;
  String beta;
  String distanceG;
  String errorUB;
  String dpDelta;
  String sigmoidBeta;
  String sigmoidPrecision;
  String dateStyle;

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

  public String getDistanceG() {
    return this.distanceG;
  }

  public void setDistanceG(String distanceG) {
    this.distanceG = distanceG;
  }

  public String getErrorUB() {
    return this.errorUB;
  }

  public void setErrorUB(String errorUB) {
    this.errorUB = errorUB;
  }

  public String getDpDelta() {
    return this.dpDelta;
  }

  public void setDpDelta(String dpDelta) {
    this.dpDelta = dpDelta;
  }

  public String getSigmoidBeta() {
    return this.sigmoidBeta;
  }

  public void setSigmoidBeta(String sigmoidBeta) {
    this.sigmoidBeta = sigmoidBeta;
  }

  public String getSigmoidPrecision() {
    return this.sigmoidPrecision;
  }

  public void setSigmoidPrecision(String sigmoidPrecision) {
    this.sigmoidPrecision = sigmoidPrecision;
  }

  public String getDateStyle() {
    return this.dateStyle;
  }

  public void setDateStyle(String dateStyle) {
    this.dateStyle = dateStyle;
  }

}
