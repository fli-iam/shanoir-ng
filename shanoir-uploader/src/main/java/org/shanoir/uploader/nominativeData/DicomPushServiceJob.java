package org.shanoir.uploader.nominativeData;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.action.DownloadOrCopyActionListener;
import org.shanoir.uploader.exception.PseudonymusException;
import org.shanoir.uploader.upload.UploadJob;
import org.shanoir.uploader.upload.UploadJobManager;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DicomPushServiceJob {

	private static final Logger logger = LoggerFactory.getLogger(CurrentNominativeDataController.class);
	
	@Autowired
	private CurrentNominativeDataController currentNominativeDataController;

	private DownloadOrCopyActionListener dOCAL;

	private Set<Serie> incomingSeries = new HashSet<>();

	
	public void setDownloadOrCopyActionListener(DownloadOrCopyActionListener dOCAL) {
		this.dOCAL = dOCAL;
	}


    @Scheduled(fixedRate = 30000)
	public void execute() {
		// We check if the username is set (= login was successful), if not we do nothing
		if (ShUpConfig.username != null) {
			logger.info("Monitoring of incoming 'DICOM PUSHED' examinations started...");
			// new File(ShUpConfig.shanoirUploaderFolder.getAbsolutePath() + File.separator + ShUpConfig.WORK_FOLDER);
			File workFolder = ShUpOnloadConfig.getWorkFolder();
			monitorPushedExaminations(workFolder);
			logger.info("Monitoring of incoming 'DICOM PUSHED' examinations ended...");
		}
	}

	/**
	 * Walk trough all folders within the work folder and check if there are complete exams incoming from a DICOM PUSH
	 * we then create the upload xml files for the upload to be loaded in the table.
	 */
	private void monitorPushedExaminations(File workFolder) {
		List<File> folders = Util.listFolders(workFolder);
		if (!folders.isEmpty()) {
			// We browse the content of the workfolder
			for (File dir : folders) {
				// If there is a directory then its a DICOM study, shanoiruploader xml upload files will be at that level
				if (dir.isDirectory()) {
					File[] xmlFiles = dir.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File folder, String name) {
							return name.equals(NominativeDataUploadJobManager.NOMINATIVE_DATA_JOB_XML) || name.equals(UploadJobManager.UPLOAD_JOB_XML);
						}
					});
					if (xmlFiles.length == 0) {
						if (isExamComplete(dir)) {
							logger.info("Complete exam found in folder {}.", dir.getName());
							//this.currentNominativeDataController.processFolder(dir);
							// if (nominativeDataUploadJobManager != null) {
							// 	final NominativeDataUploadJob nominativeDataUploadJob = nominativeDataUploadJobManager.readUploadDataJob();
							// 	if (nominativeDataUploadJob.getUploadState().equals(UploadState.START)) {
							// 		nominativeDataUploadJob.setUploadState(UploadState.READY);
							// 		nominativeDataUploadJobManager.writeUploadDataJob(nominativeDataUploadJob);
							// 	}
							// }
						}
						
					}
				}
			}
		// Map<String, NominativeDataUploadJob> currentUploads = new LinkedHashMap<String, NominativeDataUploadJob>();
		// for (File f : folders) {
		// 	NominativeDataUploadJob nominativeDataUploadJob = processFolder(f);
		// 	if (nominativeDataUploadJob != null){
		// 		currentUploads.put(f.getAbsolutePath(), nominativeDataUploadJob);
		// 	}	
		// }
		// currentNominativeDataModel.setCurrentUploads(currentUploads);
		}
	}

	private boolean isExamComplete(File folder) {
		List<Attributes> dicomAttributesList = new ArrayList<>();
		incomingSeries.clear();
		// We check for subdirectories in case DICOM series are stored in subdirectories
		File[] subdirectories = folder.listFiles(f -> f.isDirectory());
		if (subdirectories != null && subdirectories.length > 0) {
			for (File subdirectory : subdirectories) {
				if (!isExamComplete(subdirectory)) {
					return false;
				}
			}
			// TODO : copy all .dcm files from subdirectories into upload folder ?
			return true;
		}
		// We create a map to store the SeriesInstanceUID for a list of instance numbers as value
		Map<String, List<Integer>> seriesMap = new HashMap<>();

        File[] dicomFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".dcm"));

        if (dicomFiles == null || dicomFiles.length == 0) {
            logger.debug("No DICOM files found in {}", folder.getName());
            return false;
        }

        String studyUID = null;
		String seriesUID = null;
		Attributes dicomAttributes = null;
		
        for (File file : dicomFiles) {
            try (DicomInputStream dis = new DicomInputStream(file)) {
                dicomAttributes = dis.readDataset();

                String currentStudyUID = dicomAttributes.getString(Tag.StudyInstanceUID);
                String currentSeriesUID = dicomAttributes.getString(Tag.SeriesInstanceUID);
                Integer instanceNumber = dicomAttributes.getInt(Tag.InstanceNumber, 0);

                if (studyUID == null) {
					// We define the studyInstanceUID
                    studyUID = currentStudyUID;
                } else if (!studyUID.equals(currentStudyUID)) {
                    logger.debug("Warning: DICOM files from different studies found in the same folder.");
                    return false;
                }

				if (seriesUID == null) {
					seriesUID = currentSeriesUID;
					dicomAttributesList.add(dicomAttributes);
				// if we have multiple series in the same folder, 
				// we store dicomAttributes to create multiple series for the importJob
				} else if (!seriesUID.equals(currentSeriesUID)) {
					dicomAttributesList.add(dicomAttributes);
				}

                // We store the instance number for each serie
                seriesMap.computeIfAbsent(seriesUID, k -> new ArrayList<>()).add(instanceNumber);

            } catch (IOException e) {
                logger.error("Error reading DICOM file: {}", file.getName());
            }
        }

        // We check if each serie is complete
        for (Map.Entry<String, List<Integer>> entry : seriesMap.entrySet()) {
            List<Integer> instances = entry.getValue();
            Collections.sort(instances);
            int expectedSize = instances.get(instances.size() - 1);

            if (instances.size() < expectedSize) {
                logger.debug("DICOM serie {} is incomplete.", entry.getKey());
                return false;
            }

			// We create the series from the dicomAttributesList
			for (Attributes attributes : dicomAttributesList) {
				Serie serie = new Serie(attributes);
				incomingSeries.add(serie);
			}
        }

        logger.debug("DICOM study {} is complete.", studyUID); // not true in case we browse every subdirectories
		prepareUploadJob(dicomAttributes);
        return true;
	}

	// TODO : factorize this method with the one in DownloadOrCopyActionListener ?
	private void prepareUploadJob(Attributes dicomAttributes) {
		Patient patient = new Patient(dicomAttributes);
		Study study = new Study(dicomAttributes);
		ImportJob importJob = ImportUtils.createNewImportJob(patient, study);
		try {
			importJob.setSubject(dOCAL.createSubjectFromPatient(patient));
			} catch (PseudonymusException e) {
				logger.error(e.getMessage(), e);
				return;
			} catch (UnsupportedEncodingException e) {
				logger.error(e.getMessage(), e);
				return;
			} catch (NoSuchAlgorithmException e) {
				logger.error(e.getMessage(), e);
				return;
				}
		importJob.setSelectedSeries(incomingSeries);
		UploadJob uploadJob = new UploadJob();
		
		// TODO : setselectedSeries from Study ?
		// List<Serie> series = study.getSeries();
		// 				for (Serie serie : series) {
		// 					if (!serie.isIgnored() && !serie.isErroneous()) {
		// 						importJob.getSelectedSeries().add((Serie)serie.clone());
		// 					}
		// 				}
		ImportUtils.initUploadJob(importJob, uploadJob);
		ImportUtils.createUploadFolder(ShUpOnloadConfig.getWorkFolder(), importJob.getSubject().getIdentifier());
		//TODO : copy content of all DICOM files in the upload folder
		// UploadJobManager uploadJobManager = new UploadJobManager(uploadFolder.getAbsolutePath());
		// uploadJobManager.writeUploadJob(uploadJob);
	}
    
}
