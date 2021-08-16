package org.shanoir.ng.importer.dicom;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.shanoir.ng.utils.ImportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class is a helper class, that will in the future be used to detect
 * any kind of special DICOM series within an import.
 * 
 * @author mkain
 *
 */
@Component
public class DicomSerieAnalyzer {
	
	private static final Logger LOG = LoggerFactory.getLogger(DicomSerieAnalyzer.class);

	private static final String DOUBLE_EQUAL = "==";

	private static final String SEMI_COLON = ";";
	
	@Value("${shanoir.import.series.isspectroscopy}")
	private String isSpectroscopy;
	
	public boolean checkSerieIsSpectroscopy(Attributes attributes) {
		final String sopClassUID = attributes.getString(Tag.SOPClassUID);
		final String seriesDescription = attributes.getString(Tag.SeriesDescription);			
		return checkSerieIsSpectroscopy(sopClassUID, seriesDescription);
	}
	
	/**
	 * This method uses the properties string isspectroscopy to check if a serie
	 * contains spectroscopy.
	 */
	public boolean checkSerieIsSpectroscopy(final String sopClassUID, final String seriesDescription) {
		if (UID.MRSpectroscopyStorage.equals(sopClassUID) || UID.PrivateSiemensCSANonImageStorage.equals(sopClassUID)) {
			return true;
		}
		final String[] seriesDescriptionsToIdentifySpectroscopyInSerie = isSpectroscopy.split(SEMI_COLON);
		for (final String item : seriesDescriptionsToIdentifySpectroscopyInSerie) {
			final String tag = item.split(DOUBLE_EQUAL)[0];
			final String value = item.split(DOUBLE_EQUAL)[1];
			LOG.debug("checkIsSpectroscopy : tag={}, value={}", tag, value);
			String wildcard = ImportUtils.wildcardToRegex(value);
			if (seriesDescription != null && seriesDescription.matches(wildcard)) {
				return true;
			}
		}
		return false;
	}

}
