package com.naples.util;

public class LeakDetectAnalysisObject {

    String modelId;
    String verificationType;
    String analysisTarget;
    String analysisFinalTargets;

    public String getModelId() {
      return this.modelId;
    }

    public void setModelId(String modelId) {
      this.modelId = modelId;
    }

    public String getVerificationType() {
      return this.verificationType;
    }

    public void setVerificationType(String verificationType) {
      this.verificationType = verificationType;
    }

    public String getAnalysisTarget() {
      return this.analysisTarget;
    }

    public void setAnalysisTarget(String analysisTarget) {
      this.analysisTarget = analysisTarget;
    }

    public String getAnalysisFinalTargets() {
      return this.analysisFinalTargets;
    }

    public void setAnalysisFinalTargets(String analysisFinalTargets) {
      this.analysisFinalTargets = analysisFinalTargets;
    }

}