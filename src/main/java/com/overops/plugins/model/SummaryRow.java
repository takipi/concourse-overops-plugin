package com.overops.plugins.model;

public class SummaryRow {
    private String gateName;
    private String total;
    private boolean passed;

    public SummaryRow(QualityGate qualityGate) {
        this.gateName = qualityGate.getSummaryGateName();
        this.passed = qualityGate.passed();
        this.total = qualityGate.getReportCollection() != null ? String.valueOf(qualityGate.getReportCollection().size()) : "0";
    }

    public String getGateName() {
        return gateName;
    }

    public String getGateStatus() {
        return passed ? "Passed" : "Failed";
    }

    public String getTotal() {
        return total;
    }
}
