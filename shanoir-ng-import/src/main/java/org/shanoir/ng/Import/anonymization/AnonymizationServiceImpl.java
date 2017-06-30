package org.shanoir.ng.Import.anonymization;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.shanoir.ng.Import.Serie;
import org.shanoir.ng.Import.dto.ImportSubjectDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AnonymizationServiceImpl implements AnonymizationServcie{

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AnonymizationServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	
	private static final String ANONYMIZATION_FILE_PATH = "anonymizationOld.properties";
	
	@Override
	public void anonymize(List<Serie> serieList, int startCount, String folderPath, ImportSubjectDTO subject) {
		final int totalAmount = calculateTotalAmount(serieList);
		LOG.debug("anonymize : totalAmount=" + totalAmount);
		int current = 0;
	
			for (final Serie serie : serieList) {
				LOG.debug("anonymize : serie " + serie.getId() + " selected = " + serie.isSelected());
				if (serie.isSelected()) {
					// iterate over all images and non-image objects of the serie
					final List<String> allObjectList = new ArrayList<String>();
					allObjectList.addAll(serie.getImagesPathList());
					allObjectList.addAll(serie.getNonImagesPathList());
					for (final String path : allObjectList) {
						final String imagePath = convertFilePath(folderPath + path);

						// Perform the anonymization
						performAnonymization(imagePath, subject);

						current++;
						final int currentPercent = startCount + (int) (current * (100 - startCount) / totalAmount);
						LOG.debug("anonymize : anonymization current percent= " + currentPercent + " %");

					}
				}
			}
		
		
	}
	
	/**
	 * Calculate the total amount of images in the given serie list.
	 *
	 * @param serieList
	 *            the serie list
	 *
	 * @return the int
	 */
	private int calculateTotalAmount(final List<Serie> serieList) {
		int amount = 0;
		for (final Serie serie : serieList) {
			amount += serie.getImagesPathList().size();
			amount += serie.getNonImagesPathList().size();
		}
		return amount;
	}
	
	/**
	 * Replace the file separators to make it work under windows or unix system.
	 *
	 * @param firstImagePath
	 *            the first image path
	 *
	 * @return the string
	 */
	private String convertFilePath(final String firstImagePath) {
		return firstImagePath.replaceAll("\\\\", "/");
	}
	
	
	/**
	 * Perform the anonymization for the given image and given subject.
	 *
	 * @param imagePath
	 *            the image path
	 * @param subject
	 *            the subject
	 */
	private void performAnonymization(final String imagePath, final ImportSubjectDTO subject) {
		DicomObject dcmObj;
		DicomInputStream din = null;

		try {	
			din = new DicomInputStream(new File(imagePath));
			dcmObj = din.readDicomObject();
			din.close();
			final String sourceTransferSyntaxUID = dcmObj.getString(Tag.TransferSyntaxUID);
			/*if (sourceTransferSyntaxUID != null && sourceTransferSyntaxUID.startsWith("1.2.840.10008.1.2.4")) {
				// uncompress dicom image
				ShanoirExec.dcmdjpeg(ShanoirUtil.getDcmdjpegPath(), imagePath, imagePath);
				din = new DicomInputStream(new File(imagePath));
				dcmObj = din.readDicomObject();
				din.close();
			}*/

			for (final int tag : getAnonymizationTags().keySet()) {
				final String value = getFinalValueForTag(subject, tag);
				dcmObj.putString(tag, null, value);
				DicomElement el = dcmObj.get(tag);
			}

			// set the Patient ID equal to the subject common name
			if (subject != null) {
				final String commonName = subject.getName();
				dcmObj.putString(Tag.PatientID, null, commonName);
			}

			// change the study description if needed
			/*final IDicomImporter dicomImporter = (IDicomImporter) Component.getInstance("dicomImporter");
			if (dicomImporter.getSelectedSeries() != null && !dicomImporter.getSelectedSeries().isEmpty()
					&& dicomImporter.getSelectedSeries().get(0).getParent() != null) {
				final String studyDescription = ((org.shanoir.dicom.model.Study) dicomImporter.getSelectedSeries().get(
						0).getParent()).getStudyDescriptionOverwrite();
				dcmObj.putString(Tag.StudyDescription, null, studyDescription);
			}*/

			final DicomOutputStream dos = new DicomOutputStream(new File(imagePath));
			dos.writeDicomFile(dcmObj);
			dos.close();
		} catch (final IOException exc) {
			LOG.error("performAnonymization : ", exc);
			exc.printStackTrace();
		}
	}

	
	/**
	 * Get the dicom tags that must be anonymized for the config properties
	 * file.
	 *
	 * @return the anonymization tags
	 */
	private  HashMap<Integer, String> getAnonymizationTags() {

		/** The anonymization tags. */
		HashMap<Integer, String> anonymizationTags;	
		
		anonymizationTags = new HashMap<Integer, String>();
			final HashMap<String, String> tags = getAnonymizationProperties();
			LOG.debug("getAnonymizationTags : tags=" + tags);
			for (final String key : tags.keySet()) {
				final Integer tagIntValue = Integer.decode(key);
				LOG.debug("getAnonymizationTags : tagIntValue=" + tagIntValue);
				anonymizationTags.put(tagIntValue, tags.get(key));
			}
			return anonymizationTags;
		}
	
	
	/**
	 * return a HashMap<String, String> of all the properties which name starts
	 * with the given String.
	 *
	 * @param startsWith
	 *            the starts with
	 *
	 * @return the properties start with
	 */
	private HashMap<String, String> getAnonymizationProperties() {
		LOG.debug("getAnonymizationProperties : Begin" );
		/** anonymization file. */
		Properties props = null;
		props = loadFromResource(ANONYMIZATION_FILE_PATH);
	
		final HashMap<String, String> hashMapProps = new HashMap<String, String>();
		for (final Enumeration enumeration = props.keys(); enumeration.hasMoreElements();) {
			final String key = (String) enumeration.nextElement();
			if (key != null) {
				hashMapProps.put(key, props.getProperty(key));
			}
		}
		LOG.debug("getAnonymizationProperties : return hashMapProps=" + hashMapProps);
		LOG.debug("getAnonymizationProperties : End");
		return hashMapProps;
	}
	
	/**
	 * Load from resource.
	 *
	 * @param resource
	 *            resource
	 *
	 * @return the properties
	 */
	private static Properties loadFromResource(final String resource) {
		/** configuration file. */
		 Properties props = null;
		
		props = new Properties();
		final InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);

		if (stream != null) {
			try {
				LOG.debug("loadFromResource : reading properties from: " + resource);

				try {
					props.load(stream);
				} catch (final IOException ioe) {
					LOG.error("loadFromResource : could not read " + resource, ioe);
					props = null;
				}
			} finally {
				try {
					stream.close();
				} catch (final IOException ex) {
					props = null;
				} // swallow
			}
		} else {
			LOG.debug("loadFromResource : not found: " + resource);
			props = null;
		}

		return props;
	}
	
	
	/**
	 * Return the final value for the given tag.
	 *
	 * @param subject
	 *            the subject
	 * @param tag
	 *            the tag
	 *
	 * @return the final value for tag
	 */
	private String getFinalValueForTag(final ImportSubjectDTO subject, final int tag) {
		String result = "";
		if (subject != null) {
			// patient name : return subject name or subject identifier
			if (tag == Tag.PatientName) {
				if (subject.getName() != null) {
					return subject.getName();
				} else {
					result = subject.getIdentifier();
				}
			}
			// patient birth date : return the 01/01 of the same year
			/*else if (tag == Tag.PatientBirthDate) {
				final GregorianCalendar birthDate = new GregorianCalendar();
				birthDate.setTime(subject.getBirthDate());
				// set day and month to 01/01
				birthDate.set(Calendar.MONTH, Calendar.JANUARY);
				birthDate.set(Calendar.DAY_OF_MONTH, 1);
				birthDate.set(Calendar.HOUR, 1);
				result = ShanoirUtil.convertDicomDateToString(birthDate.getTime());
			}*/
			// else : return value from the configuration file
			else {
				result = getAnonymizationTags().get(tag);
			}
		}
		return result;
	}
	

}
