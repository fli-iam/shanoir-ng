package org.shanoir.uploader.dicom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.media.DicomDirReader;
import org.dcm4che2.media.DirectoryRecordType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

/**
 * The Class ShanoirDicomDirReader.
 */
public class ShanoirDicomDirReader {

	/**
	 * The Class ImageComparer.
	 */
	class ImageComparer implements Comparator<DicomObject> {

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(final DicomObject obj1, final DicomObject obj2) {
			if (((DicomObject) obj1).getString(Tag.InstanceNumber) != null
					&& ((DicomObject) obj2).getString(Tag.InstanceNumber) != null) {
				int nr1 = Integer.parseInt(((DicomObject) obj1).getString(Tag.InstanceNumber));
				int nr2 = Integer.parseInt(((DicomObject) obj2).getString(Tag.InstanceNumber));
				return nr1 - nr2;
			}
			return 0;
		}
	}

	/** Logger. */
	@Logger
	private static Log log = Logging.getLog(ShanoirDicomDirReader.class);

	/** The dicom dir. */
	private DicomDirReader dicomDir;

	/**
	 * Instantiates a new shanoir dicom dir reader.
	 *
	 * @param file
	 *            the file
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public ShanoirDicomDirReader(final File file) throws IOException {
		dicomDir = new DicomDirReader(file);
		System.out.println("cons " +dicomDir.getMediaStorageSOPInstanceUID());
		System.out.println("cons " +dicomDir.findFirstRootRecord());
		dicomDir.setShowInactiveRecords(false);
	}

	/**
	 * Instantiates a new shanoir dicom dir reader.
	 *
	 * @param file
	 *            the file
	 *
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public ShanoirDicomDirReader(final String file) throws IOException {
		dicomDir = new DicomDirReader(new File(file));
		dicomDir.setShowInactiveRecords(false);
	}

	/**
	 * Gets the all studys.
	 *
	 * @return the all studys
	 */
	public List<DicomObject> getAllStudys() {
		List<DicomObject> studyList = new ArrayList<DicomObject>();
		log.info("getAllStudys : reading patient records:");
		try {
			DicomObject next = dicomDir.findFirstRootRecord();
			while (next != null) {
				if (DirectoryRecordType.PATIENT.equals(next.getString(Tag.DirectoryRecordType))) {
					try {
						DicomObject study = dicomDir.findFirstChildRecord(next);
						while (study != null) {
							if (DirectoryRecordType.STUDY.equals(next.getString(Tag.DirectoryRecordType))) {
								try {
									studyList.add(next);
								} catch (Exception e) {
									log.warn("getAllStudys : Error getting AllStudies: " + e.getLocalizedMessage());
								}
							}
							study = dicomDir.findNextSiblingRecord(study);
						}
					} catch (Exception e) {
						log.warn("getAllStudys : Error getting AllStudies: " + e.getLocalizedMessage());
					}
				}
				next = dicomDir.findNextSiblingRecord(next);
			}
		} catch (IOException e) {
			log.warn("getAllStudys : Error getting AllStudies: " + e.getLocalizedMessage());
		}
		return studyList;
	}

	/**
	 * Gets the image paths from series.
	 *
	 * @param seriesRecord
	 *            the series record
	 *
	 * @return the image paths from series
	 */
	public File[] getImagePathsFromSeries(final DicomObject seriesRecord) {

		Collection<DicomObject> imageVector = getImagesFromSeries(seriesRecord);
		File[] files = new File[imageVector.size()];
		int i = 0;
		for (Iterator<DicomObject> iter = imageVector.iterator(); iter.hasNext();) {
			DicomObject element = (DicomObject) iter.next();
			try {
				File f = dicomDir.toReferencedFile(element);
				files[i] = f.getAbsoluteFile();
				i++;
			} catch (Exception e) {
				log.warn("getImagePathsFromSeries : Error getting Imagepaths from Series: " + e.getLocalizedMessage());
			}
		}
		return files;
	}

	/**
	 * Gets the images from series.
	 *
	 * @param seriesRecord
	 *            the series record
	 *
	 * @return the images from series
	 */
	public List<DicomObject> getImagesFromSeries(final DicomObject seriesRecord) {
		final List<DicomObject> imageList = new ArrayList<DicomObject>();
		log.info("getImagesFromSeries : reading image records.");
		try {
			DicomObject next = dicomDir.findFirstChildRecord(seriesRecord);
			while (next != null) {
				try {
					imageList.add(next);
				} catch (Exception e) {
					log.warn("getImagesFromSeries : Error getting Images from Series: " + e.getLocalizedMessage());
				}
				next = dicomDir.findNextSiblingRecord(next);
			}
		} catch (IOException e) {
			log.warn("getImagesFromSeries : Error getting Images from Series: " + e.getLocalizedMessage());
		}
		Collections.sort(imageList, new ImageComparer());
		return imageList;
	}

	/**
	 * Gets the patients.
	 *
	 * @return the patients
	 */
	public List<DicomObject> getPatients() {
		final List<DicomObject> patientList = new ArrayList<DicomObject>();
		log.info("getPatients : reading patient records.");
		try {
			DicomObject next = dicomDir.findFirstRootRecord();
		
			System.out.println("isEmpty: "+dicomDir.isEmpty());
			System.out.println("isShowInactiveRecords: "+dicomDir.isShowInactiveRecords());
			System.out.println("isNoKnownInconsistencies: "+dicomDir.isNoKnownInconsistencies());
			
			
			while (next != null) {
				System.out.println("next: "+dicomDir.getFileSetInformation().toString());
				
				final String directoryRecordType = next.getString(Tag.DirectoryRecordType);
				System.out.println("directoryRecordType: "+directoryRecordType);
				if (DirectoryRecordType.PATIENT.equals(directoryRecordType)) {
					try {
						patientList.add(next);
						log.info("Patient = "+next.toString());
					} catch (Exception e) {
						log.error("getPatients : Error getting Patients: " + e.getLocalizedMessage());
					}
				}
				next = dicomDir.findNextSiblingRecord(next);
			}
		} catch (IOException e) {
			log.warn("getPatients : Error getting Patients: " + e.getLocalizedMessage());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR: "+e.toString()) ;
		}
		
		return patientList;
	}

	/**
	 * Gets the series from study.
	 *
	 * @param studyRecord
	 *            the study record
	 *
	 * @return the series from study
	 */
	public List<DicomObject> getSeriesFromStudy(final DicomObject studyRecord) {
		final List<DicomObject> seriesList = new ArrayList<DicomObject>();
		log.info("getSeriesFromStudy : reading series records.");
		try {
			DicomObject next = dicomDir.findFirstChildRecord(studyRecord);
			while (next != null) {
				if (DirectoryRecordType.SERIES.equals(next.getString(Tag.DirectoryRecordType))) {
					try {
						seriesList.add(next);
					} catch (Exception e) {
						log.warn("getSeriesFromStudy : Error getting Series from Studies: " + e.getLocalizedMessage());
					}
				}
				next = dicomDir.findNextSiblingRecord(next);
			}
		} catch (IOException e) {
			log.warn("Error getting Series from Studies: " + e.getLocalizedMessage());
		}
		return seriesList;
	}

	/**
	 * Gets the studies from patients.
	 *
	 * @param patientRecord
	 *            the patient record
	 *
	 * @return the studies from patients
	 */
	public List<DicomObject> getStudiesFromPatients(final DicomObject patientRecord) {
		final List<DicomObject> studyList = new ArrayList<DicomObject>();
		log.info("getStudiesFromPatients : reading study records.");
		try {
			DicomObject next = dicomDir.findFirstChildRecord(patientRecord);
			while (next != null) {
				if (DirectoryRecordType.STUDY.equals(next.getString(Tag.DirectoryRecordType))) {
					try {
						studyList.add(next);
					} catch (Exception e) {
						log.warn("getStudiesFromPatients : Error getting Studies: " + e.getLocalizedMessage());
					}
				}
				next = dicomDir.findNextSiblingRecord(next);
			}
		} catch (IOException e) {
			log.warn("getStudiesFromPatients : Error getting Studies: " + e.getLocalizedMessage());
		}
		return studyList;
	}

	/**
	 * Close.
	 */
	public void close() {
		try {
			dicomDir.close();
		} catch (IOException e) {
			log.error("close : Error: " + e.getLocalizedMessage());
		}
	}

}
