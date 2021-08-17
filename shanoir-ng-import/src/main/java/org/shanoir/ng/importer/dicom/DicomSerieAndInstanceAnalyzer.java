package org.shanoir.ng.importer.dicom;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.dcmr.AcquisitionModality;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.utils.ImportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class is a helper class, that will in the future be used to detect
 * any kind of special DICOM series and instances within an import.
 * 
 * @author mkain
 *
 */
@Component
public class DicomSerieAndInstanceAnalyzer {
	
	private static final Logger LOG = LoggerFactory.getLogger(DicomSerieAndInstanceAnalyzer.class);

	private static final String DOUBLE_EQUAL = "==";

	private static final String SEMI_COLON = ";";
	
	@Value("${shanoir.import.series.isspectroscopy}")
	private String isSpectroscopy;
	
	/**
	 * By default raw data storage and sub-types are ignored.
	 * 
	 * @param attributes
	 * @return
	 */
	public boolean checkInstanceIsIgnored(Attributes attributes) {
		final String sopClassUID = attributes.getString(Tag.SOPClassUID);
		if (UID.RawDataStorage.equals(sopClassUID)
			|| UID.SpatialRegistrationStorage.equals(sopClassUID)
			|| UID.SpatialFiducialsStorage.equals(sopClassUID)
			|| UID.DeformableSpatialRegistrationStorage.equals(sopClassUID)
			|| UID.SegmentationStorage.equals(sopClassUID)
			|| UID.SurfaceSegmentationStorage.equals(sopClassUID)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Ignore all series, that are not medical imaging.
	 * @param serie
	 * @return
	 */
	public boolean checkSerieIsIgnored(Attributes attributes) {
		String modality = attributes.getString(Tag.Modality);
		if (AcquisitionModality.codeOf(modality) != null) {
			return false;
		}
		return true;
	}
	
	public void checkSerieIsSpectroscopy(Serie serie) {
		final String sopClassUID = serie.getSopClassUID();
		final String seriesDescription = serie.getSeriesDescription();	
		boolean isSpectroscopy = checkSerieIsSpectroscopy(sopClassUID, seriesDescription);
		serie.setIsSpectroscopy(isSpectroscopy);
	}

	public Serie checkSerieIsSpectroscopy(Serie serie, Attributes attributes) {
		final String sopClassUID = attributes.getString(Tag.SOPClassUID);
		final String seriesDescription = attributes.getString(Tag.SeriesDescription);		
		boolean isSpectroscopy = checkSerieIsSpectroscopy(sopClassUID, seriesDescription);
		serie.setIsSpectroscopy(isSpectroscopy);
		return serie;
	}
	
	/**
	 * This method uses the SOPClassUID and the properties string isspectroscopy to check if a serie
	 * contains spectroscopy. UID.MRSpectroscopyStorage is Enhanced MR already, Private Siemens CSA
	 * Non-Image Storage is Original MR, as in the first version of the MR Image IOD MR spectroscopy
	 * did not yet exist or was known during the creation of the standard.
	 */
	private boolean checkSerieIsSpectroscopy(final String sopClassUID, final String seriesDescription) {
		if (UID.MRSpectroscopyStorage.equals(sopClassUID)) {
			LOG.info("Serie found with MR Spectroscopy: {}", seriesDescription);
			return true;
		}
		if (UID.PrivateSiemensCSANonImageStorage.equals(sopClassUID)) { // before private attribute used by Siemens
			final String[] seriesDescriptionsToIdentifySpectroscopyInSerie = isSpectroscopy.split(SEMI_COLON);
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
	 * Checks if serie is Enhanced Dicom. SOPClassUID is normally NOT part of the DICOMDIR,
	 * but maybe part of the dicom query C-FIND and can be checked early.
	 * 
	 * @param serie
	 * @param attributes
	 */
	public void checkSerieIsEnhanced(Serie serie, Attributes attributes) {
		final String sopClassUID = attributes.getString(Tag.SOPClassUID);
		if (sopClassUID != null) {
			if (UID.EnhancedMRImageStorage.equals(sopClassUID)
				|| UID.MRSpectroscopyStorage.equals(sopClassUID) // enhanced by default
				|| UID.EnhancedMRColorImageStorage.equals(sopClassUID)
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
			LOG.warn("SOPClassUID not found to detect Enhanced DICOM.");
		}
	}
	
	/**
	 * Checks for multi-frame dicom serie. Requires Enhanced DICOM check done before.
	 * 
	 * @param serie
	 * @param attributes
	 */
	public void checkSerieIsMultiFrame(Serie serie, Attributes attributes) {
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
