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

package org.shanoir.anonymization.anonymization;

import java.util.Map;

/**
 * SeriesInstanceUID/StudyInstanceUID/FrameOfReferenceUID are looked up by their
 * OLD (pre-anonymization) value, since the same shared map is reused across
 * files for consistency. SOPInstanceUID is different: it is regenerated
 * independently per file (never looked up in a shared map), so it can only be
 * correlated back to an Instance via the file it came from.
 */
public final class AnonymizationResult {

    private final Map<String, String> seriesInstanceUIDs;

    private final Map<String, String> studyInstanceUIDs;

    private final Map<String, String> frameOfReferenceUIDs;

    // File.getAbsolutePath() -> new SOPInstanceUID
    private final Map<String, String> sopInstanceUIDsByFilePath;

    AnonymizationResult(Map<String, String> seriesInstanceUIDs, Map<String, String> studyInstanceUIDs,
            Map<String, String> frameOfReferenceUIDs, Map<String, String> sopInstanceUIDsByFilePath) {
        this.seriesInstanceUIDs = seriesInstanceUIDs;
        this.studyInstanceUIDs = studyInstanceUIDs;
        this.frameOfReferenceUIDs = frameOfReferenceUIDs;
        this.sopInstanceUIDsByFilePath = sopInstanceUIDsByFilePath;
    }

    public Map<String, String> getSeriesInstanceUIDs() {
        return seriesInstanceUIDs;
    }

    public Map<String, String> getStudyInstanceUIDs() {
        return studyInstanceUIDs;
    }

    public Map<String, String> getFrameOfReferenceUIDs() {
        return frameOfReferenceUIDs;
    }

    public Map<String, String> getSopInstanceUIDsByFilePath() {
        return sopInstanceUIDsByFilePath;
    }

}
