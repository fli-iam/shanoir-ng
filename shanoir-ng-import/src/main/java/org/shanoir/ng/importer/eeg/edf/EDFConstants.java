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

import java.nio.charset.Charset;

/**
 * This class contains constants for the EDF/EDF+ specification.
 */
public final class EDFConstants {

    private EDFConstants() { }

    static final Charset CHARSET = Charset.forName("ASCII");

    static final int IDENTIFICATION_CODE_SIZE = 8;
    static final int LOCAL_SUBJECT_IDENTIFICATION_SIZE = 80;
    static final int LOCAL_REOCRDING_IDENTIFICATION_SIZE = 80;
    static final int START_DATE_SIZE = 8;
    static final int START_TIME_SIZE = 8;
    static final int HEADER_SIZE = 8;
    static final int DATA_FORMAT_VERSION_SIZE = 44;
    static final int DURATION_DATA_RECORDS_SIZE = 8;
    static final int NUMBER_OF_DATA_RECORDS_SIZE = 8;
    static final int NUMBER_OF_CHANELS_SIZE = 4;

    static final int LABEL_OF_CHANNEL_SIZE = 16;
    static final int TRANSDUCER_TYPE_SIZE = 80;
    static final int PHYSICAL_DIMENSION_OF_CHANNEL_SIZE = 8;
    static final int PHYSICAL_MIN_IN_UNITS_SIZE = 8;
    static final int PHYSICAL_MAX_IN_UNITS_SIZE = 8;
    static final int DIGITAL_MIN_SIZE = 8;
    static final int DIGITAL_MAX_SIZE = 8;
    static final int PREFILTERING_SIZE = 80;
    static final int NUMBER_OF_SAMPLES_SIZE = 8;
    static final int RESERVED_SIZE = 32;

    /**
     * The size of the EDF-Header-Record containing information about the
     * recording
     */
    static final int HEADER_SIZE_RECORDING_INFO
            = IDENTIFICATION_CODE_SIZE + LOCAL_SUBJECT_IDENTIFICATION_SIZE + LOCAL_REOCRDING_IDENTIFICATION_SIZE
            + START_DATE_SIZE + START_TIME_SIZE + HEADER_SIZE + DATA_FORMAT_VERSION_SIZE + DURATION_DATA_RECORDS_SIZE
            + NUMBER_OF_DATA_RECORDS_SIZE + NUMBER_OF_CHANELS_SIZE;

    /**
     * The size per channel of the EDF-Header-Record containing information a
     * channel of the recording
     */
    static final int HEADER_SIZE_PER_CHANNEL
            = LABEL_OF_CHANNEL_SIZE + TRANSDUCER_TYPE_SIZE + PHYSICAL_DIMENSION_OF_CHANNEL_SIZE
            + PHYSICAL_MIN_IN_UNITS_SIZE + PHYSICAL_MAX_IN_UNITS_SIZE + DIGITAL_MIN_SIZE + DIGITAL_MAX_SIZE
            + PREFILTERING_SIZE + NUMBER_OF_SAMPLES_SIZE + RESERVED_SIZE;
}
