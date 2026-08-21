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

package org.shanoir.ng.shared.dicom;

import java.io.File;
import java.io.FileNotFoundException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.dcmr.AcquisitionModality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DicomUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DicomUtils.class);

    private static final String RTPLAN = "RTPLAN";

    private static final String RTDOSE = "RTDOSE";

    private static final String RTSTRUCT = "RTSTRUCT";

    private DicomUtils() { }

    /**
     * Ignore all series, that are not medical imaging.
     *
     * @param serie
     * @return
     */
    public static boolean checkSerieIsIgnored(Attributes attributes) {
        String modality = attributes.getString(Tag.Modality);
        return AcquisitionModality.codeOf(modality) == null && !RTSTRUCT.equals(modality) && !RTDOSE.equals(modality) && !RTPLAN.equals(modality);
    }

    public static boolean checkDicomIsEnhanced(Attributes attributes) {
        final String sopClassUID = attributes.getString(Tag.SOPClassUID);
        if (sopClassUID != null) {
            if (UID.EnhancedMRImageStorage.equals(sopClassUID)
                    || UID.EnhancedMRColorImageStorage.equals(sopClassUID)
                    || UID.MRSpectroscopyStorage.equals(sopClassUID) // enhanced by default
                    || UID.LegacyConvertedEnhancedMRImageStorage.equals(sopClassUID)
                    || UID.EnhancedCTImageStorage.equals(sopClassUID)
                    || UID.EnhancedPETImageStorage.equals(sopClassUID)
                    || UID.EnhancedXAImageStorage.equals(sopClassUID)
                    || UID.EnhancedXRFImageStorage.equals(sopClassUID)) {
                return true;
            }
        } else {
            LOG.debug("SOPClassUID not found to detect Enhanced DICOM.");
        }
        return false;
    }

    public static int getDicomMultiFrameCount(Attributes attributes, boolean isEnhanced) {
        int multiFrameCount = 0;
        if (isEnhanced) {
            Attributes pFFGS = attributes.getNestedDataset(Tag.PerFrameFunctionalGroupsSequence);
            if (pFFGS != null) {
                multiFrameCount = pFFGS.size();
            }
        }
        return multiFrameCount;
    }

    public static String getDicomSequenceName(Attributes attributes, boolean isEnhanced) {
        if (isEnhanced) {
            return attributes.getString(Tag.PulseSequenceName);
        } else {
            return attributes.getString(Tag.SequenceName);
        }
    }

    public static String referencedFileIDToPath(String rootFilePath, String[] referencedFileIDArray)
            throws FileNotFoundException {
        StringBuilder stringBuilder = new StringBuilder();
        if (referencedFileIDArray != null) {
            stringBuilder.append(rootFilePath).append(File.separator);
            for (int count = 0; count < referencedFileIDArray.length; count++) {
                stringBuilder.append(referencedFileIDArray[count]);
                if (count != referencedFileIDArray.length - 1) {
                    stringBuilder.append(File.separator);
                }
            }
            return stringBuilder.toString();
        } else {
            throw new FileNotFoundException(
                    "instancePathArray in DicomDir: missing file: " + referencedFileIDArray);
        }
    }

}
