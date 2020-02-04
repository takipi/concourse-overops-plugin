package com.overops.plugins.model;

public class SummaryRow {
    private String gateName;
    private String total;
    private boolean passed;

    public String getGateName() {
        return gateName;
    }

    public void setGateName(String gateName) {
        this.gateName = gateName;
    }

    public String getGateStatus() {
        return passed ? "Passed" : "Failed";
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public SummaryRow(String gateName, boolean passed, String total) {
        this.gateName = gateName;
        this.passed = passed;
        this.total = total;
    }
}
