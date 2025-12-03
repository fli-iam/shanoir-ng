/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.importer.eeg.edf;

/**
 * This class represents the complete data records of an EDF-File.
 */
public class EDFSignal {

    private Double[] unitsInDigit;

    private short[][] digitalValues;

    private double[][] valuesInUnits;

    public Double[] getUnitsInDigit() {
        return unitsInDigit;
    }

    public short[][] getDigitalValues() {
        return digitalValues;
    }

    public double[][] getValuesInUnits() {
        return valuesInUnits;
    }

    public void setUnitsInDigit(Double[] unitsInDigit) {
        this.unitsInDigit = unitsInDigit;
    }

    public void setDigitalValues(short[][] digitalValues) {
        this.digitalValues = digitalValues;
    }

    public void setValuesInUnits(double[][] valuesInUnits) {
        this.valuesInUnits = valuesInUnits;
    }

}
