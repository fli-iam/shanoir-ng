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

import jakarta.persistence.*;
import org.shanoir.ng.score.ScaleItem;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class NumericalVariable.
 *
 * @author JCome
 */
@Entity
@PrimaryKeyJoinColumn(name = "instrument_variable_id")
@Table(name = "coded_variable")
public class CodedVariable extends InstrumentVariable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -28168171333546302L;

    /** Minimum value. */
    @OneToOne
    @JoinColumn(name = "max_scale_item_id")
    private ScaleItem maxScaleItem;

    /** Minimum value. */
    @OneToOne
    @JoinColumn(name = "min_scale_item_id")
    private ScaleItem minScaleItem;

    /** The ref score code list. */
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "coded_variable_id")
    private List<ScaleItem> scaleItemList = new ArrayList<>(0);

    /**
     * Gets the max scale item.
     *
     * @return the maxScaleItem
     */
    public ScaleItem getMaxScaleItem() {
        return maxScaleItem;
    }

    /**
     * Gets the min scale item.
     *
     * @return the minScaleItem
     */
    public ScaleItem getMinScaleItem() {
        return minScaleItem;
    }

    /**
     * Gets the scale item list.
     *
     * @return the scaleItemList
     */
    public List<ScaleItem> getScaleItemList() {
        return scaleItemList;
    }

    /**
     * Sets the max scale item.
     *
     * @param maxScaleItem
     *            the maxScaleItem to set
     */
    public void setMaxScaleItem(final ScaleItem maxScaleItem) {
        this.maxScaleItem = maxScaleItem;
    }

    /**
     * Sets the min scale item.
     *
     * @param minScaleItem
     *            the minScaleItem to set
     */
    public void setMinScaleItem(final ScaleItem minScaleItem) {
        this.minScaleItem = minScaleItem;
    }

    /**
     * Sets the scale Item List.
     *
     * @param refScaleItemList
     *            the refScaleItemList to set
     */
    public void setScaleItemList(final List<ScaleItem> refScaleItemList) {
        this.scaleItemList = refScaleItemList;
    }
}
