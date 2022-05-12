package com.centradatabase.consumerapp.model;

public enum EnumStatus {
    STATUS_UPLOADED("UPLOADED"),  STATUS_VALIDATING("VALIDATING"),
    STATUS_VALIDATED("VALIDATED");

    private String status;

     EnumStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
