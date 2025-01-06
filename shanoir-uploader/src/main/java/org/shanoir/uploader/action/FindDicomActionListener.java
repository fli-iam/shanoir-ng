package org.shanoir.uploader.action;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.ConnectException;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.shanoir.ng.importer.dicom.DicomDirGeneratorService;
import org.shanoir.ng.importer.dicom.DicomDirToModelService;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.query.Media;
import org.shanoir.uploader.dicom.query.PatientTreeNode;
import org.shanoir.uploader.dicom.query.SerieTreeNode;
import org.shanoir.uploader.dicom.query.StudyTreeNode;
import org.shanoir.uploader.gui.DicomTree;
import org.shanoir.uploader.gui.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the logic when the open file from CD/DVD menu or the
 * query button is clicked
 * 
 * @author mkain
 * @author yyao
 * 
 */
public class FindDicomActionListener extends JPanel implements ActionListener {

	private static final Logger logger = LoggerFactory.getLogger(FindDicomActionListener.class);

	private static final long serialVersionUID = 7126127792556196772L;

	private static final String DICOMDIR = "DICOMDIR";

	private MainWindow mainWindow;

	private JFileChooser fileChooser;

	private IDicomServerClient dicomServerClient;

	private String filePathDicomDir;
	
	private DicomDirGeneratorService dicomDirGeneratorService = new DicomDirGeneratorService();

	public FindDicomActionListener(final MainWindow mainWindow,
			final JFileChooser fileChooser,
			final IDicomServerClient dicomServerClient) {
		this.mainWindow = mainWindow;
		this.fileChooser = fileChooser;
		this.dicomServerClient = dicomServerClient;
	}

	/**
	 * This method contains all the logic which is performed when the open file
	 * from CD/DVD menu or the query button is clicked. The answer from the PACS
	 * and the content of the DICOMDIR are moved into the Media object for display
	 * in the GUI and the JTree.
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
					+ mainWindow.birthDate.toString() + " "
					+ mainWindow.studyDescriptionTF.getText() + " "
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
				// extract lastName, firstName1 and firstName2 from the field PatientName
				String patientName = mainWindow.patientNameTF.getText();
				String patientNameFinal = "";
				String lastName = "";
				String firstName1 = "";
				String firstName2 = "";
		
				// Use regular expression to split by either "," or ", "
				String[] nameParts = patientName.split(",\\s*");
				if (nameParts.length > 1) {
					lastName = nameParts[0];
					if (nameParts.length > 1) {
						firstName1 = nameParts[1];
						if (nameParts.length > 2) {
							firstName2 = nameParts[2];
						}
					}
				} else {
					lastName = patientName + "*";
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
					patientNameFinal = lastName.toUpperCase();

					String modality = null;
					if (!mainWindow.noRB.isSelected()) {
						if (mainWindow.mrRB.isSelected()) {
							modality = "MR";
						} else if (mainWindow.ctRB.isSelected()) {
							modality = "CT";
						} else if (mainWindow.ptRB.isSelected()) {
							modality = "PT";
						} else if (mainWindow.nmRB.isSelected()) {
							modality = "NM";
						}
					}
					boolean studyRootQuery = false;
					if (mainWindow.sRB.isSelected()) {
						studyRootQuery = true;
					}
					List<Patient> patients = dicomServerClient.queryDicomServer(
							studyRootQuery,
							modality, patientNameFinal, mainWindow.patientIDTF.getText(),
							mainWindow.studyDescriptionTF.getText(),
							mainWindow.birthDate, mainWindow.studyDate);
					fillMediaWithPatients(media, patients);

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
		// set values for display in the GUI tree
		mainWindow.dicomTree = new DicomTree(media);
		// expand entire JTree after creation
		for (int i = 0; i < mainWindow.dicomTree.getRowCount(); i++) {
			mainWindow.dicomTree.expandRow(i);
		}
		mainWindow.dicomTreeJScrollPane.setViewportView(mainWindow.dicomTree);
		mainWindow.dicomTree.addTreeSelectionListener(mainWindow.getSAL());
	}

	/**
	 * Fill media object to display from DicomDIR.
	 * @param media
	 * @param selectedRootDir
	 * @param dicomDir
	 */
	private void fillMediaWithPatients(Media media, final List<Patient> patients) {
		if (patients != null) {
			for (Iterator iterator = patients.iterator(); iterator.hasNext();) {
				Patient patient = (Patient) iterator.next();
				final PatientTreeNode patientTreeNode = media.initChildTreeNode(patient);
				logger.info("Patient info read: " + patient.toString());
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
	}

	public String getFilePathDicomDir() {
		return filePathDicomDir;
	}

}
