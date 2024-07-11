package org.shanoir.uploader.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JTabbedPane;

import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.gui.ImportFromTableWindow;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;

public class ImportFromTableActionListener implements ActionListener {

	private ImportFromTableWindow importFromTableWindow;
	private IDicomServerClient dicomServerClient;
	private ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer;
	private File shanoirUploaderFolder;

	private Map<String, ImportJob> importJobs;

	private ShanoirUploaderServiceClient shanoirUploaderServiceClientNG;
	private ResourceBundle resourceBundle;

	public ImportFromTableActionListener(ImportFromTableWindow importFromTableWindow, ResourceBundle resourceBundle, IDicomServerClient dicomServerClient, ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer, File shanoirUploaderFolder, ShanoirUploaderServiceClient shanoirUploaderServiceClientNG) {
		this.importFromTableWindow = importFromTableWindow;
		this.dicomServerClient = dicomServerClient;
		this.dicomFileAnalyzer = dicomFileAnalyzer;
		this.shanoirUploaderFolder = shanoirUploaderFolder;
		this.shanoirUploaderServiceClientNG = shanoirUploaderServiceClientNG;
		this.resourceBundle = resourceBundle;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		importFromTableWindow.openButton.setEnabled(false);
		importFromTableWindow.uploadButton.setEnabled(false);

		importFromTableWindow.progressBar.setStringPainted(true);
		importFromTableWindow.progressBar.setString("Preparing import...");
		importFromTableWindow.progressBar.setVisible(true);

		for (ImportJob importJob : importJobs.values()) {
			// query pacs

			// for disk volume reasons: only download one STUDY at a time
			Map<String, ImportJob> oneImportJob = new HashMap<>();
			oneImportJob.put("x", importJob);
			Runnable runnable = new DownloadOrCopyRunnable(true, dicomServerClient, dicomFileAnalyzer,  null, oneImportJob);
			Thread thread = new Thread(runnable);
			thread.start();
		}

//		ImportFromCsvRunner importer = new ImportFromCsvRunner(csvImports, resourceBundle, importFromCSVWindow, dicomServerClient, dicomFileAnalyzer, shanoirUploaderServiceClientNG);
//		importer.execute();

		boolean success = true;
		if (success) {
			importFromTableWindow.progressBar.setString("Success !");
			importFromTableWindow.progressBar.setValue(100);
			// Open current import tab and close table import panel
			((JTabbedPane) this.importFromTableWindow.scrollPaneUpload.getParent().getParent()).setSelectedComponent(this.importFromTableWindow.scrollPaneUpload.getParent());
			this.importFromTableWindow.frame.setVisible(false);
			this.importFromTableWindow.frame.dispose();
		} else {
			importFromTableWindow.openButton.setEnabled(true);
			importFromTableWindow.uploadButton.setEnabled(false);
		}
	}

	public Map<String, ImportJob> getImportJobs() {
		return importJobs;
	}

	public void setImportJobs(Map<String, ImportJob> importJobs) {
		this.importJobs = importJobs;
	}

}
