package org.shanoir.uploader.action;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.ConnectException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.shanoir.ng.importer.dicom.DicomDirGeneratorService;
import org.shanoir.ng.importer.dicom.DicomDirToModelService;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.uploader.dicom.DicomTreeNode;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.query.Media;
import org.shanoir.uploader.dicom.query.PatientTreeNode;
import org.shanoir.uploader.dicom.query.SerieTreeNode;
import org.shanoir.uploader.dicom.query.StudyTreeNode;
import org.shanoir.uploader.gui.DicomTree;
import org.shanoir.uploader.gui.MainWindow;

/**
 * This class implements the logic when the open file from CD/DVD menu or the
 * query button is clicked
 * 
 * @author mkain
 * @author yyao
 * 
 */
public class FindDicomActionListener extends JPanel implements ActionListener {

	private static final String DICOMDIR = "DICOMDIR";

	private static Logger logger = Logger
			.getLogger(FindDicomActionListener.class);

	private MainWindow mainWindow;

	private JFileChooser fileChooser;

	private IDicomServerClient dicomServerClient;

	private String filePathDicomDir;
	
	private DicomDirGeneratorService dicomDirGeneratorService = new DicomDirGeneratorService();
	
	private ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer;

	public FindDicomActionListener(final MainWindow mainWindow,
			final JFileChooser fileChooser,
			final IDicomServerClient dicomServerClient,
			final ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer) {
		this.mainWindow = mainWindow;
		this.fileChooser = fileChooser;
		this.dicomServerClient = dicomServerClient;
		this.dicomFileAnalyzer = dicomFileAnalyzer;
	}

	/**
	 * This method contains all the logic which is performed when the open file
	 * from CD/DVD menu or the query button is clicked.
	 */
	public void actionPerformed(ActionEvent event) {
		mainWindow.isDicomObjectSelected = false;
		// clean up editPanel when new file chosen
		mainWindow.lastNameTF.setText("");
		mainWindow.firstNameTF.setText("");
		mainWindow.birthNameTF.setText("");
		mainWindow.birthDateTF.setText("");
		mainWindow.mSexR.setSelected(true);
		mainWindow.fSexR.setSelected(false);

		Media media = new Media();
		// when the open file from CD/DVD menu is clicked
		if (event.getSource().getClass() == JMenuItem.class) {
			logger.info("Opening DICOM files from CD/DVD/local file system...");
			this.mainWindow.isFromPACS = false;

			int returnVal = fileChooser.showOpenDialog(FindDicomActionListener.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File selectedRootDir = fileChooser.getSelectedFile();
				if (selectedRootDir.isDirectory()) {
					try {
						boolean dicomDirGenerated = false;
						File dicomDirFile = new File(selectedRootDir, DICOMDIR);
						if (!dicomDirFile.exists()) {
							logger.info("No DICOMDIR found: generating one.");
							dicomDirGeneratorService.generateDicomDirFromDirectory(dicomDirFile, selectedRootDir);
							dicomDirGenerated = true;
							logger.info("DICOMDIR generated at path: " + dicomDirFile.getAbsolutePath());
						}
						final DicomDirToModelService dicomDirReader = new DicomDirToModelService();
						List<Patient> patients = dicomDirReader.readDicomDirToPatients(dicomDirFile);
						fillMediaWithPatients(media, patients);
						filePathDicomDir = selectedRootDir.toString();
						dicomFileAnalyzer.createImagesAndAnalyzeDicomFiles(patients, selectedRootDir.getAbsolutePath(), false, null);
						// clean up in case of dicomdir generated
						if (dicomDirGenerated) {
							dicomDirFile.delete();
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				} else {
					logger.error("Please choose a directory.");
				}
			}
		// when the query button is clicked
		} else if (event.getSource().getClass() == JButton.class) {
			logger.info("Querying DICOM server with query parameters: "
					+ mainWindow.patientNameTF.getText() + " "
					+ mainWindow.patientIDTF.getText() + " "
					+ mainWindow.studyDescriptionTF.getText() + " "
					+ mainWindow.seriesDescriptionTF.getText() + " "
					+ mainWindow.studyDate.toString()) ;
			this.mainWindow.isFromPACS = true;

			this.mainWindow.setCursor(Cursor
					.getPredefinedCursor(Cursor.WAIT_CURSOR));
			try {

				/*
				 * Indexing Patient Name attribute research is not case
				 * sensitive and request becomes in the form
				 * "lastName, firstName1, firstName2"
				 */

				// extract lastName, firstName1 and firstName2 from the field
				// PatientName
				String patientName = mainWindow.patientNameTF.getText();
				String patientNameFinal = "";
				String lastName = "";
				String firstName1 = "";
				String firstName2 = "";

				if (patientName.contains(",")) {
					lastName = patientName.substring(0,
							patientName.indexOf(","));
					patientName = patientName.substring(
							patientName.indexOf(",") + 2, patientName.length());
					if (patientName.contains(",")) {
						firstName1 = patientName.substring(0,
								patientName.indexOf(","));
						firstName2 = patientName.substring(
								patientName.indexOf(",") + 2,
								patientName.length());
					} else {
						firstName1 = patientName;
					}
				} else
					lastName = patientName;

				// Users can't use the wildcard "*" on the lastName unless they
				// introduce at least 4 characters
				boolean exitFlag = false;
				if (lastName.contains("*")) {
					String lastNamePartIntroduced = lastName;
					String regex = "\\*";
					String replacement = "";
					lastNamePartIntroduced = lastNamePartIntroduced.replaceAll(
							regex, replacement);
					if (lastNamePartIntroduced.length() < 4) {
						String message = "\"The wildcard \"*\" can not be used on the last Name unless introducing at least 4 characters\"\n";
						JOptionPane.showMessageDialog(new JFrame(), message,
								"ERROR", JOptionPane.ERROR_MESSAGE);
						mainWindow.patientNameTF.setText("");
						exitFlag = true;
					}
				}

				// for Request, the Patient Name must be of the form:
				// lastName^firstName1^firstName2

				if (!firstName2.equals(""))
					patientNameFinal = lastName.toUpperCase() + "^"
							+ firstName1.toUpperCase() + "^"
							+ firstName2.toUpperCase();
				else if (!firstName1.equals(""))
					patientNameFinal = lastName.toUpperCase() + "^"
							+ firstName1.toUpperCase();
				else
					patientNameFinal = patientName.toUpperCase();

				// exitFlag: verify that the request is well written
				// dicomServerClient.echoDicomServer(): verify that a connection
				// is established to the PACS
				// can not echo a GE PACS => control omitted
				if (!exitFlag /* && dicomServerClient.echoDicomServer() */) {
					// query Dicom Server
					List<Patient> patients = dicomServerClient.queryDicomServer(
							patientNameFinal, mainWindow.patientIDTF.getText(),
							mainWindow.studyDescriptionTF.getText(),
							mainWindow.seriesDescriptionTF.getText(),
							mainWindow.dateRS, mainWindow.studyDate);
					fillMediaWithPatients(media, patients);
				} else {
					media = null;
				}
				this.mainWindow.setCursor(Cursor.getDefaultCursor());
			} catch (ConnectException cE) {
				logger.error(cE.getMessage(), cE);
				this.mainWindow.setCursor(Cursor.getDefaultCursor());
				JOptionPane.showMessageDialog(mainWindow.frame,
						"Connection to DICOM server could not be established.",
						"Connection error", JOptionPane.ERROR_MESSAGE);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				this.mainWindow.setCursor(Cursor.getDefaultCursor());
				JOptionPane.showMessageDialog(mainWindow.frame,
						"Connection to DICOM server could not be established.",
						"Connection error", JOptionPane.ERROR_MESSAGE);
			}
		}
		media = sortTree(media);
		// set values for display in the GUI tree
		mainWindow.dicomTree = new DicomTree(media);
		// expand entire JTree after creation
		for (int i = 0; i < mainWindow.dicomTree.getRowCount(); i++) {
			mainWindow.dicomTree.expandRow(i);
		}
		mainWindow.dicomTreeJScrollPane.setViewportView(mainWindow.dicomTree);
		mainWindow.dicomTree.addTreeSelectionListener(mainWindow.getSAL());
		mainWindow.getSAL().setDicomData(null);
	}

	/**
	 * Fill media object to display from DicomDIR.
	 * @param media
	 * @param selectedRootDir
	 * @param dicomDir
	 */
	private void fillMediaWithPatients(Media media, final List<Patient> patients) {
		for (Iterator iterator = patients.iterator(); iterator.hasNext();) {
			Patient patient = (Patient) iterator.next();
			final PatientTreeNode patientTreeNode = media.initChildTreeNode(patient);
			logger.info("Patient info read from DICOMDIR: " + patient.toString());
			// add patients
			media.addTreeNode(patient.getPatientID(), patientTreeNode);
			List<Study> studies = patient.getStudies();
			for (Iterator iterator2 = studies.iterator(); iterator2.hasNext();) {
				Study study = (Study) iterator2.next();
				final StudyTreeNode studyTreeNode = patientTreeNode.initChildTreeNode(study);
				// add studies
				patientTreeNode.addTreeNode(studyTreeNode.getId(), studyTreeNode);
				List<Serie> series = study.getSeries();
				for (Iterator iterator3 = series.iterator(); iterator3.hasNext();) {
					Serie serie = (Serie) iterator3.next();
					if (!serie.isErroneous() && !serie.isIgnored()) {
						final SerieTreeNode serieTreeNode = studyTreeNode.initChildTreeNode(serie);
						// add series
						studyTreeNode.addTreeNode(serieTreeNode.getId(), serieTreeNode);
					}
				}
				
			}
		}
	}

	public String getFilePathDicomDir() {
		return filePathDicomDir;
	}

	/**
	 * @param Media
	 *            : root of tree
	 * @return sorted elements alphabetically
	 */
	public static Media sortTree(Media media) {
		logger.debug("Begin media sorting");
		try {		
			Iterator patientsIterator = media.getChildren();
			// Count nb results in Media
			int nbMediaElements = 0;
			while (patientsIterator.hasNext()) {
				Map.Entry pair = (Map.Entry) patientsIterator.next();
				nbMediaElements++;
			}
			logger.debug("Nb elements in media : " + nbMediaElements );

			// Create a table of patientNames and a table of patientNodes
			String[] patientNames = new String[nbMediaElements];
			DicomTreeNode[] patientNodes = new DicomTreeNode[nbMediaElements];
			int counter = 0;
			patientsIterator = media.getChildren();
			while (patientsIterator.hasNext()) {
				Map.Entry pair = (Map.Entry) patientsIterator.next();
				String patientKey = pair.getKey().toString();
				patientNames[counter] = patientKey.substring(patientKey.indexOf(" ") + 1);
				DicomTreeNode patient = (DicomTreeNode) pair.getValue();
				patientNodes[counter] = patient;
				logger.debug("Queried Subject Name " + counter + " : " + patientNames[counter].toString() );
				logger.debug("Queried Subject Node " + counter + " : " + patientNodes [counter].toString() );
				counter++;
			}

			// Sort elements in patientNames table and patientNodes table based on
			// patient's Names
			for (int i = 0; i < nbMediaElements; i++) {
				String name1 = patientNames[i];
				for (int j = 1; j < nbMediaElements-i; j++) {
					String name2 = patientNames[j-1];
					String name3 = patientNames[j];
					if (name2 != null && !"".equals(name2) && name3 != null
							&& !"".equals(name3)
							&& name2.compareToIgnoreCase(name3) > 0) {
						patientNames[j-1] = name3;
						patientNames[j] = name2;
						DicomTreeNode temp = patientNodes[j-1];
						patientNodes[j-1] = patientNodes[j];
						patientNodes[j] = temp;
					}
				}
			}
			
			for (int i = 0; i < nbMediaElements; i++) {
				logger.debug("Sorted Subject Name " + i + " : " + patientNames[i].toString() );
				logger.debug("Sorted Subject Node " + i + " : " + patientNodes[i].toString() );
			}
			
			// Create Sorted Media
			DicomTreeNode sortedMedia = new Media();
			for (int i = 0; i < nbMediaElements; i++) {
				sortedMedia.addTreeNode(patientNodes[i].getId(), patientNodes[i]);
			}
			
			logger.debug("Sorted Media : " + sortedMedia.toString());
			logger.debug("End media sorting");
			return (Media) sortedMedia;
		} catch (Exception e) {
			logger.error("Not able to sort tree ", e);
			return null;
		}
	}

}
