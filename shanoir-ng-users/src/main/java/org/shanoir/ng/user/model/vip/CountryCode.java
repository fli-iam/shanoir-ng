package org.shanoir.ng.user.model.vip;

import java.util.Arrays;

public enum CountryCode {

    fr("France");

    private String name;

    private CountryCode(String name) {
        this.name = name;
    }

    public String getCountryName() {
        return name;
    }

    public static CountryCode searchIgnoreCase(String countryName) {
        return Arrays.stream(values())
                .filter(countryCode ->
                        countryName.equalsIgnoreCase(countryCode.getCountryName()))
                .findFirst().orElse(null);

    }
}
