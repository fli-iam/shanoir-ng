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

package org.shanoir.ng.importer.dicom;

import java.util.Set;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.dcmr.AcquisitionModality;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.utils.ImportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a helper class, that will in the future be used to detect any
 * kind of special DICOM series and instances within an import.
 *
 * @author mkain
 *
 */
public final class DicomSerieAndInstanceAnalyzer {

    private DicomSerieAndInstanceAnalyzer() { }

    private static final Logger LOG = LoggerFactory.getLogger(DicomSerieAndInstanceAnalyzer.class);

    private static final String DICOMDIR_BASIC_DIRECTORY_IOD = "1.2.840.10008.1.3.10";

    private static final String RTPLAN = "RTPLAN";

    private static final String RTDOSE = "RTDOSE";

    private static final String RTSTRUCT = "RTSTRUCT";

    private static final String DICOM_VR_CODE_STRING_YES = "YES";

    private static final String DOUBLE_EQUAL = "==";

    private static final String SEMI_COLON = ";";

    private static final String IS_SPECTROSCOPY = "seriesDescription==*CSI*;seriesDescription==*csi*;seriesDescription==*SPECTRO*;seriesDescription==*spectro*;";

    private static final Set<String> SOP_CLASS_UIDS_IGNORED = Set.of(
            UID.RawDataStorage,
            UID.SpatialRegistrationStorage,
            UID.SpatialFiducialsStorage,
            UID.DeformableSpatialRegistrationStorage,
            UID.SegmentationStorage,
            UID.SurfaceSegmentationStorage,
            DICOMDIR_BASIC_DIRECTORY_IOD
    );

    /**
     * By default raw data storage and sub-types are ignored.
     * 
     * We ignore (even if very rare) DICOMDIR instances, as they
     * are not images.
     *
     * @param attributes
     * @return
     */
    public static boolean checkInstanceIsIgnored(Attributes attributes) {
        if (isIgnoredUID(attributes.getString(Tag.SOPClassUID), "SOPClassUID")) {
            return true;
        }
        if (isIgnoredUID(attributes.getString(Tag.ReferencedSOPClassUIDInFile), "ReferencedSOPClassUIDInFile")) {
            return true;
        }
        final String burnedInAnnotation = attributes.getString(Tag.BurnedInAnnotation);
        if (DICOM_VR_CODE_STRING_YES.equals(burnedInAnnotation)) {
            LOG.warn("Instance (image) ignored, because of burnedInAnnotation: {}", burnedInAnnotation);
            return true;
        }
        return false;
    }

    private static boolean isIgnoredUID(String sopClassUID, String source) {
        if (sopClassUID != null && SOP_CLASS_UIDS_IGNORED.contains(sopClassUID)) {
            LOG.warn("Instance (image) ignored, because of {}: {}", source, sopClassUID);
            return true;
        }
        return false;
    }

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

    public static void checkSerieIsSpectroscopy(Serie serie) {
        final String sopClassUID = serie.getSopClassUID();
        final String seriesDescription = serie.getSeriesDescription();
        boolean isSpectroscopy = checkSerieIsSpectroscopy(sopClassUID, seriesDescription);
        serie.setIsSpectroscopy(isSpectroscopy);
    }

    public static Serie checkSerieIsSpectroscopy(Serie serie, Attributes attributes) {
        final String sopClassUID = attributes.getString(Tag.SOPClassUID);
        final String seriesDescription = attributes.getString(Tag.SeriesDescription);
        boolean isSpectroscopy = checkSerieIsSpectroscopy(sopClassUID, seriesDescription);
        serie.setIsSpectroscopy(isSpectroscopy);
        return serie;
    }

    /**
     * This method uses the SOPClassUID and the properties string isspectroscopy
     * to check if a serie contains spectroscopy. UID.MRSpectroscopyStorage is
     * Enhanced MR already, Private Siemens CSA Non-Image Storage is Original
     * MR, as in the first version of the MR Image IOD MR spectroscopy did not
     * yet exist or was known during the creation of the standard.
     */
    private static boolean checkSerieIsSpectroscopy(final String sopClassUID, final String seriesDescription) {
        if (UID.MRSpectroscopyStorage.equals(sopClassUID)) {
            LOG.info("Serie found with MR Spectroscopy: {}", seriesDescription);
            return true;
        }
        if (UID.PrivateSiemensCSANonImageStorage.equals(sopClassUID)) { // before private attribute used by Siemens
            final String[] seriesDescriptionsToIdentifySpectroscopyInSerie = IS_SPECTROSCOPY.split(SEMI_COLON);
            for (final String item : seriesDescriptionsToIdentifySpectroscopyInSerie) {
                final String tag = item.split(DOUBLE_EQUAL)[0];
                final String value = item.split(DOUBLE_EQUAL)[1];
                LOG.debug("checkIsSpectroscopy : tag={}, value={}", tag, value);
                String wildcard = ImportUtils.wildcardToRegex(value);
                if (seriesDescription != null && seriesDescription.matches(wildcard)) {
                    LOG.info("Serie found with Spectroscopy (CSANonImageStorage): {}", seriesDescription);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if serie is Enhanced Dicom. SOPClassUID is normally NOT part of
     * the DICOMDIR, but maybe part of the dicom query C-FIND and can be checked
     * early.
     *
     * @param serie
     * @param attributes
     */
    public static void checkSerieIsEnhanced(Serie serie, Attributes attributes) {
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
                serie.setIsEnhanced(true);
            } else {
                serie.setIsEnhanced(false);
            }
        } else {
            LOG.debug("SOPClassUID not found to detect Enhanced DICOM.");
        }
    }

    /**
     * Checks for multi-frame dicom serie. Requires Enhanced DICOM check done
     * before.
     *
     * @param serie
     * @param attributes
     */
    public static void checkSerieIsMultiFrame(Serie serie, Attributes attributes) {
        int frameCount = 0;
        if (serie.getIsEnhanced()) {
            Attributes pFFGS = attributes.getNestedDataset(Tag.PerFrameFunctionalGroupsSequence);
            if (pFFGS != null) {
                frameCount = pFFGS.size();
            }
            serie.setSequenceName(attributes.getString(Tag.PulseSequenceName));
        } else {
            serie.setSequenceName(attributes.getString(Tag.SequenceName));
        }
        serie.setMultiFrameCount(frameCount);
        if (frameCount > 1) {
            serie.setIsMultiFrame(true);
        } else {
            serie.setIsMultiFrame(false); // an Enhanced Dicom can have only one frame
        }
    }

}
