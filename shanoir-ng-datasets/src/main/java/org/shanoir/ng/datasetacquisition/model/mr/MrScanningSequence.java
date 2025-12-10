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

package org.shanoir.ng.datasetacquisition.model.mr;

import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scanning Sequence.
 *
 * @author atouboul
 *
 */
public enum MrScanningSequence {

    // Spin Echo
    SE(1),

    // Inversion Recovery
    IR(2),

    // Gradient Recalled
    GR(3),

    // Echo Planar
    EP(4),

    // Research Mode
    RM(5),

    // Spectroscopy
    S(6),

    // Fast Field Echo (Philips)
    FFE(7),

    // Could not map correctly
    UNKNOWN(8);

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(MrScanningSequence.class);

    private int id;

    /**
     * Constructor.
     *
     * @param id
     *            id
     */
    private MrScanningSequence(final int id) {
        this.id = id;
    }

    /**
     * Get a Scanning Sequence by its id.
     *
     * @param id
     *            sequence id.
     * @return Scanning Sequence.
     */
    public static MrScanningSequence getScanningSequence(final Integer id) {
        if (id == null) {
            return null;
        }
        for (MrScanningSequence scanningSequence : MrScanningSequence.values()) {
            if (id.equals(scanningSequence.getId())) {
                return scanningSequence;
            }
        }
        return UNKNOWN;
    }

    /**
     * Get a Scanning Sequence by its name.
     *
     * @param type
     *            sequence id.
     * @return Scanning Sequence.
     */
    public static MrScanningSequence getIdByType(final String type) {
        if (type == null) {
            return null;
        }
        if (EnumUtils.isValidEnum(MrScanningSequence.class, type)) {
            return MrScanningSequence.valueOf(type);
        } else {
            LOG.warn("MrScanningSequence of type: " + type + " set to UNKNOWN.");
            return UNKNOWN;
        }
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

}
