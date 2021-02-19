package org.shanoir.uploader.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.shanoir.dicom.importer.Serie;
import org.shanoir.dicom.model.DicomTreeNode;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.query.Media;
import org.shanoir.uploader.dicom.query.Patient;
import org.shanoir.uploader.dicom.query.Study;
import org.shanoir.uploader.gui.ImportFromCSVWindow;
import org.shanoir.uploader.model.CsvImport;
import org.shanoir.uploader.model.rest.IdList;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;
import org.shanoir.util.ShanoirUtil;

public class ImportFromCsvActionListener implements ActionListener {

	ImportFromCSVWindow importFromCSVWindow;
	IDicomServerClient dicomServerClient;
	File shanoirUploaderFolder;
	
	List<CsvImport> csvImports;
	ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG;
	
	private static Logger logger = Logger.getLogger(ImportFromCsvActionListener.class);
	
	public ImportFromCsvActionListener(ImportFromCSVWindow importFromCSVWindow, IDicomServerClient dicomServerClient, File shanoirUploaderFolder, ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG) {
		this.importFromCSVWindow = importFromCSVWindow;
		this.dicomServerClient = dicomServerClient;
		this.shanoirUploaderFolder = shanoirUploaderFolder;
		this.shanoirUploaderServiceClientNG = shanoirUploaderServiceClientNG;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Iterate over import to import them one by one
		Set<Long> idList = new HashSet<>();
		Map<String, ArrayList<String>> studyCardsByStudy = new HashMap<>();

		for (CsvImport importTodo : this.csvImports) {
			idList.add(Long.valueOf(importTodo.getStudyId()));
			studyCardsByStudy.put(importTodo.getStudyId(), new ArrayList<String>());
		}

		IdList idealist = new IdList();
		idealist.setIdList(new ArrayList<>(idList));
		List<StudyCard> studyCards = shanoirUploaderServiceClientNG.findStudyCardsByStudyIds(idealist);
		studyCards.stream().forEach( element -> {
			studyCardsByStudy.get(element.getStudyId().toString()).add(element.getName());
		});
		
		for (CsvImport importTodo : this.csvImports) {
			importData(importTodo, studyCardsByStudy);
		}
	}

	/**
	 * Loads data to shanoir NG
	 * @param csvImport the import
	 * @param studyCardsByStudy the list of study
	 * @return
	 */
	private void importData(CsvImport csvImport, Map<String, ArrayList<String>> studyCardsByStudy) {
		
		// Check existence of study / study card
		if (!studyCardsByStudy.get(csvImport.getStudyId()).contains(csvImport.getStudyCardId())) {
			// Set csv import in error and then return
			logger.error("We gat a prwobelm here, the study card is not contained in the study");
		}
		
		// Request PACS
		Media media;
		try {
			media = dicomServerClient.queryDicomServer("DIR OL", null, null, null, null, null);
			System.err.println(media);
		} catch (Exception e) {
			// Set import in error here and stop import for this particular import
			e.printStackTrace();
			return;
		}

		File uploadFolder = createUploadFolder(shanoirUploaderFolder, csvImport.getCommonName());

		Collection<Serie> selectedSeries = new ArrayList<Serie>();
		for (DicomTreeNode item : media.getTreeNodes().values()) {
			if (item instanceof Patient) {
				Patient patient = (Patient) item;

				Collection<DicomTreeNode> studies = patient.getTreeNodes().values();
				for (Iterator studiesIt = studies.iterator(); studiesIt.hasNext();) {
					Study study = (Study) studiesIt.next();
					Collection<DicomTreeNode> series = study.getTreeNodes().values();
					for (Iterator seriesIt = series.iterator(); seriesIt.hasNext();) {
						Serie serie = (Serie) seriesIt.next();
						selectedSeries.add(serie);
					}
				}
			} else {
				System.err.println("I CRY LIKE A BABY DOLL");
			}
		}
		System.err.println("I LAuHGH A LOT" + selectedSeries);
		// Copy data from PACS
		List<String> files = dicomServerClient.retrieveDicomFiles(selectedSeries , uploadFolder);
		
		System.err.println("I GOT DEM" + files);
		
		// Create subject and examination now that we now that we have the series
		
		// Create examination
		
		// pseudonymize data
		
		// Import data
		
		// Manage error
	}

	/**
	 * @return the csvImports
	 */
	public List<CsvImport> getCsvImports() {
		return csvImports;
	}

	/**
	 * @param csvImports the csvImports to set
	 */
	public void setCsvImports(List<CsvImport> csvImports) {
		this.csvImports = csvImports;
	}

	private File createUploadFolder(final File workFolder, final String identifier) {
		final String timeStamp = ShanoirUtil.getCurrentTimeStampForFS();
		final String folderName = workFolder.getAbsolutePath() + File.separator + identifier
				+ "_" + timeStamp;
		File uploadFolder = new File(folderName);
		uploadFolder.mkdirs();
		logger.info("UploadFolder created: " + uploadFolder.getAbsolutePath());
		return uploadFolder;
	}

}
