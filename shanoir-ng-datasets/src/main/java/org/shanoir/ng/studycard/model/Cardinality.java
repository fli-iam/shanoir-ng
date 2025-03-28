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

package org.shanoir.ng.studycard.model;

/**
 * DICOM VM (Value Multiplicity)
 *
 * distinct existing values :
 * 1, 1-n, 1-8, 2-n, 3-3n, 2, 4, 1-2, 1-3, 2-2n, 3, 6, 16, 1-n1, 1-32, 1-99, 3-n
 */
public class Cardinality {

    int number;

    boolean isMultiplier;

    public static Cardinality ONE = new Cardinality(1);

    Cardinality(String str) {
        String strCopy = new String(str);
        if ("n".equals(str)) {
            isMultiplier = true;
            number = 1;
        } else {
            if (strCopy.contains("n")) {
                isMultiplier = true;
                strCopy = strCopy.replace("n", "");
            } else {
                isMultiplier = false;
            }
            number = Integer.parseInt(strCopy);
        }
    }

    Cardinality(int singleValue) {
        number = singleValue;
        isMultiplier = false;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isMultiplier() {
        return isMultiplier;
    }

    public void setMultiplier(boolean isMultiplier) {
        this.isMultiplier = isMultiplier;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Cardinality
            && ((Cardinality) obj).isMultiplier() == this.isMultiplier()
            && ((Cardinality) obj).getNumber() == this.getNumber();
    }

    @Override
    public String toString() {
        return getNumber() + (isMultiplier() ? "N" : "");
    }

}


