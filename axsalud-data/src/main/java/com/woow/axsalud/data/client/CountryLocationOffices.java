package com.woow.axsalud.data.client;

public enum CountryLocationOffices {

    MX("MX");

    private final String locationOffices;
    CountryLocationOffices(String locationOffices) {
        this.locationOffices = locationOffices;
    }

    public String getLocationOffices() {
        return this.locationOffices;
    }
}
