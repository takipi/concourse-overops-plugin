package com.overops.plugins.model;

public class SummaryRow {
    private String gateName;
    private String total;
    private boolean passed;

    public SummaryRow(QualityGate qualityGate) {
        this.gateName = qualityGate.getSummaryGateName();
        this.passed = qualityGate.passed();
        this.total = passed ? "-" : String.valueOf(qualityGate.getReportCollection().size());
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
