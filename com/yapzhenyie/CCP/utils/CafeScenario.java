package com.yapzhenyie.CCP.utils;

public enum CafeScenario {

    NORMAL_LOAD("Normal Customers Load"),
    BUSY_DAY("Busy Day Simulation"),
    NO_CUSTOMER("Without Customers Entering the Cafe"),
    NO_WAITER("Without the Waiter Working for the Day");

    private String description;

    private CafeScenario(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
