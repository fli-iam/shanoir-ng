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

package org.shanoir.ng.examination.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/**
 * The Class NumericalVariable.
 * @author JCome
 */
@Entity
@PrimaryKeyJoinColumn(name = "instrument_variable_id")
@Table(name = "numerical_variable")
public class NumericalVariable extends InstrumentVariable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -4974109889862891260L;

    /** Maximum score value of this variable. */
    private Float maxScoreValue;

    /** Minimum score value of this variable. */
    private Float minScoreValue;

    /**
     * Gets the max score value.
     *
     * @return the maxScoreValue
     */
    public Float getMaxScoreValue() {
        return maxScoreValue;
    }

    /**
     * Gets the min score value.
     *
     * @return the minScoreValue
     */
    public Float getMinScoreValue() {
        return minScoreValue;
    }

    /**
     * Sets the max score value.
     *
     * @param maxScoreValue
     *            the maxScoreValue to set
     */
    public void setMaxScoreValue(final Float maxScoreValue) {
        this.maxScoreValue = maxScoreValue;
    }

    /**
     * Sets the min score value.
     *
     * @param minScoreValue
     *            the minScoreValue to set
     */
    public void setMinScoreValue(final Float minScoreValue) {
        this.minScoreValue = minScoreValue;
    }

}
