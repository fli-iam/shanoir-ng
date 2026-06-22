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

package org.shanoir.ng.score;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * The Class Score.
 *
 * @author JCome
 *
 */
@Entity
@PrimaryKeyJoinColumn(name = "score_id")
@Table(name = "coded_score")
public class CodedScore extends Score {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 931538633792918610L;

    /** Ref Score code. */
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "scale_item_id")
    private ScaleItem scaleItem;

    /**
     * Gets the scale item.
     *
     * @return the scaleItem
     */
    public ScaleItem getScaleItem() {
        return scaleItem;
    }

    /**
     * Sets the ref score code.
     *
     * @param scaleItem
     *            the scaleItem to set
     */
    public void setScaleItem(ScaleItem scaleItem) {
        this.scaleItem = scaleItem;
    }

}
