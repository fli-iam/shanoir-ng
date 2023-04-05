package org.shanoir.uploader.action;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.ConnectException;
import java.util.ArrayList;
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
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.shanoir.ng.exchange.imports.dicom.DicomDirGeneratorService;
import org.shanoir.uploader.dicom.DicomTreeNode;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.dicom.Serie;
import org.shanoir.uploader.dicom.ShanoirDicomDirReader;
import org.shanoir.uploader.dicom.query.Media;
import org.shanoir.uploader.dicom.query.Patient;
import org.shanoir.uploader.gui.DicomTree;
import org.shanoir.uploader.gui.MainWindow;
import org.shanoir.uploader.utils.Util;

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

	public FindDicomActionListener(final MainWindow mainWindow,
			final JFileChooser fileChooser,
			final IDicomServerClient dicomServerClient) {
		this.mainWindow = mainWindow;
		this.fileChooser = fileChooser;
		this.dicomServerClient = dicomServerClient;
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
		mainWindow.newPatientIDTF.setText("");
		mainWindow.birthDateTF.setText("");
		mainWindow.mSexR.setSelected(true);
		mainWindow.fSexR.setSelected(false);

		Media media = new Media();
		// when the open file from CD/DVD menu is clicked
		if (event.getSource().getClass() == JMenuItem.class) {
			logger.info("Opening Dicom files from CD/DVD...");
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
						final ShanoirDicomDirReader dicomDir = new ShanoirDicomDirReader(dicomDirFile);
						fillMediaFromCD(media, selectedRootDir, dicomDir);
						dicomDir.close();
						// clean up in case of dicomdir generated
						if (dicomDirGenerated) {
							dicomDirFile.delete();
						}
						logger.debug("populate : Media populated : " + media);
						logger.debug("populate : End");
					} catch (Exception e) {
						logger.error(e.getMessage());
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
				// dicomServerClient.echoDicomServer(): verify that a connexion
				// is established to the PACS
				// can not echo a GE PACS => control omitted
				if (!exitFlag /* && dicomServerClient.echoDicomServer() */) {
					// query Dicom Server
					media = dicomServerClient.queryDicomServer(
							patientNameFinal, mainWindow.patientIDTF.getText(),
							mainWindow.studyDescriptionTF.getText(),
							mainWindow.seriesDescriptionTF.getText(),
							mainWindow.dateRS, mainWindow.studyDate);
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
	private void fillMediaFromCD(Media media, File selectedRootDir,
			final ShanoirDicomDirReader dicomDir) {
		// Run through all patients
		for (final Iterator<DicomObject> itePatient = dicomDir.getPatients().iterator(); itePatient.hasNext();) {
			final DicomObject patientDicomObject = itePatient.next();
			final DicomTreeNode patient = media.initChildTreeNode(patientDicomObject);
			logger.info("Patient info read from DICOMDIR: " + ((Patient) patient).toString());
			// add patients
			media.addTreeNode(patient.getId(), patient);
			// Run through all studies
			for (final Iterator<DicomObject> iteStudy = dicomDir.getStudiesFromPatients(patientDicomObject).iterator(); iteStudy.hasNext();) {
				final DicomObject studyDicomObject = iteStudy.next();
				final DicomTreeNode study = patient.initChildTreeNode(studyDicomObject);
				// add studies
				patient.addTreeNode(study.getId(), study);
				// Run through all series
				for (final Iterator<DicomObject> iteSerie = dicomDir.getSeriesFromStudy(studyDicomObject).iterator(); iteSerie.hasNext();) {
					final DicomObject seriesDicomObject = iteSerie.next();
					try {
						DicomTreeNode serie = study.initChildTreeNode(seriesDicomObject);
						processSerie(selectedRootDir, dicomDir, study, seriesDicomObject, serie);
						// permits to display MRI info for CD
						Util.processSerieMriInfo(selectedRootDir, serie);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
			logger.debug("populate : getting next patient");
		}
	}

	/**
	 * Process images for each serie.
	 * @param selectedRootDir
	 * @param dicomDir
	 * @param study
	 * @param seriesDicomObject
	 * @param serie
	 */
	private void processSerie(File selectedRootDir, final ShanoirDicomDirReader dicomDir,
			final DicomTreeNode study, final DicomObject seriesDicomObject,
			DicomTreeNode serie) {
		final String modality = serie.getDescriptionMap().get("modality");
		if (modality != null) {
			List<String> imageFileNames = new ArrayList<String>();
			// Add all images path to the serie
			for (final Iterator<DicomObject> iteImages =
					dicomDir.getImagesFromSeries(seriesDicomObject).iterator(); iteImages.hasNext();) {
				final DicomObject imageDicomObject = iteImages.next();
				if (!"PR".equals(modality)) {
					final String[] imagesPathArray = imageDicomObject.getStrings(Tag.ReferencedFileID);
					if (imagesPathArray != null) {
						filePathDicomDir = selectedRootDir.toString();
						StringBuffer fileName = new StringBuffer();
						for (int i = 0; i < imagesPathArray.length; i++) {
							// do not append File.separator in case of last segment
							if (i == imagesPathArray.length - 1) {
								fileName.append(imagesPathArray[i]);
							} else {
								fileName.append(imagesPathArray[i] + File.separator);
							}
						}
						imageFileNames.add(fileName.toString());
					}
				}
			}
			((Serie)serie).setFileNames(imageFileNames);
			((Serie)serie).setImagesCount(imageFileNames.size());
			if (!"PR".equals(modality) && !"SR".equals(modality)) {
				if (logger.isDebugEnabled()) {
					logger.debug("populate : adding serie " + serie);
				}
				study.addTreeNode(serie.getId(), serie);
			} else {
				logger.debug("populate : not adding serie " + serie + " because it is of modality " + modality);
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
				patientNames[counter] = patientKey.substring(patientKey
						.indexOf(" ") + 1);
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
