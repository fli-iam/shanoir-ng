package org.shanoir.uploader.nominativeData;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.ng.importer.model.UploadState;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.action.DeleteDirectory;
import org.shanoir.uploader.gui.CurrentUploadsWindowTable;
import org.shanoir.uploader.utils.ImportUtils;
import org.shanoir.uploader.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@org.springframework.stereotype.Component
public class CurrentNominativeDataController {

	private static final Logger logger = LoggerFactory.getLogger(CurrentNominativeDataController.class);

	private CurrentNominativeDataModel currentNominativeDataModel = null;

	private NominativeDataImportJobManager importJobManager;

	private CurrentUploadsWindowTable cuw;

	@SuppressWarnings("deprecation")
	public void configure(final File workFolderFilePath, final CurrentUploadsWindowTable cuw) throws IOException {
		this.cuw = cuw;
		this.currentNominativeDataModel = new CurrentNominativeDataModel();
		currentNominativeDataModel.addObserver(cuw);
		processWorkFolder(workFolderFilePath);

		cuw.table.addMouseListener(new MouseAdapter() {
			public DefaultTableModel model = (DefaultTableModel) cuw.table.getModel();

            @Override
			public void mouseClicked(MouseEvent e) {
				int row = cuw.table.getSelectedRow();
				int col = cuw.table.getSelectedColumn();
				int rows = cuw.table.getRowCount();
				// Last row and last column: delete all imports whatever their status
				if (col == cuw.deleteColumn && row == rows - 1) {
					String message = cuw.frame.resourceBundle
							.getString("shanoir.uploader.currentUploads.Action.deleteAll.confirmation.message");
					UIManager.put("OptionPane.cancelButtonText", cuw.frame.resourceBundle
							.getString("shanoir.uploader.currentUploads.Action.deleteAll.confirmation.cancel"));
					UIManager.put("OptionPane.noButtonText", cuw.frame.resourceBundle
							.getString("shanoir.uploader.currentUploads.Action.deleteAll.confirmation.no"));
					UIManager.put("OptionPane.okButtonText", cuw.frame.resourceBundle
							.getString("shanoir.uploader.currentUploads.Action.deleteAll.confirmation.ok"));
					UIManager.put("OptionPane.yesButtonText", cuw.frame.resourceBundle
							.getString("shanoir.uploader.currentUploads.Action.deleteAll.confirmation.yes"));
					if (JOptionPane.showConfirmDialog(null, message,
							cuw.frame.resourceBundle.getString("shanoir.uploader.currentUploads.Action.deleteAll.confirmation.title"), 1)
							== JOptionPane.YES_OPTION) {
						boolean uploadsToDelete = false;
						for (int i = 0; i < rows; i++) {
							String uploadState = (String) cuw.table.getModel().getValueAt(i, cuw.uploadStateColumn);
							if (uploadState.equals(cuw.checkOKUploadState)
									|| uploadState.equals(cuw.errorUploadState)) {
								DeleteDirectory dt = new DeleteDirectory();
								dt.delete((String) model.getValueAt(i, 0));
								uploadsToDelete = true;
							}
						}
						if (uploadsToDelete) {
							String mess = cuw.frame.resourceBundle
									.getString("shanoir.uploader.currentUploads.Action.deleteAll.succeeded.message");
							JOptionPane.showMessageDialog(new JFrame(), mess,
									cuw.frame.resourceBundle.getString(
											"shanoir.uploader.currentUploads.Action.deleteAll.succeeded.title"),
									JOptionPane.INFORMATION_MESSAGE);
						}
						try {
							processWorkFolder(workFolderFilePath);
						} catch (IOException eIO) {
							logger.error(eIO.getMessage(), eIO);
						}
					}
				// delete one import: ready (to gain disk space) or finished
				} else if (col == cuw.deleteColumn && row != -1) {
					String uploadState = (String) cuw.table.getModel().getValueAt(row, cuw.uploadStateColumn);
					if (uploadState.equals(cuw.checkOKUploadState)
							|| uploadState.equals(cuw.readyUploadState)) {
						try {
							showDeleteConfirmationDialog(workFolderFilePath, cuw, row);
						} catch (IOException eIO) {
							logger.error(eIO.getMessage(), eIO);
						}				
					}
				// start the import or try reimporting an exam with status "ERROR"
				} else if (col == cuw.importColumn && row != -1) {
					String uploadState = (String) cuw.table.getModel().getValueAt(row, cuw.uploadStateColumn);
					if (uploadState.equals(cuw.readyUploadState) || uploadState.equals(cuw.errorUploadState)) {
						String importJobFilePath = (String) cuw.table.getModel().getValueAt(row, 0) + File.separator + ShUpConfig.IMPORT_JOB_JSON;
						File importJobFile = new File(importJobFilePath);
						importJobManager = new NominativeDataImportJobManager(importJobFile); // Or uploadJobManager ? or dedicated importJobManager
						ImportJob importJob = importJobManager.readImportJob();
						cuw.frame.getImportDialogOpener().openImportDialog(importJob, importJobFile.getParentFile());
					}
				}
			}

			/**
			 * @param workFolderFilePath
			 * @param cuw
			 * @param row
			 * @throws IOException 
			 */
			private void showDeleteConfirmationDialog(final File workFolderFilePath,
					final CurrentUploadsWindowTable cuw, int row) throws IOException {
				String message = cuw.frame.resourceBundle
						.getString("shanoir.uploader.currentUploads.Action.delete.confirmation.message")
						+ (String) cuw.table.getModel().getValueAt(row, cuw.patientNameColumn) + "?";
				UIManager.put("OptionPane.cancelButtonText", cuw.frame.resourceBundle
						.getString("shanoir.uploader.currentUploads.Action.delete.confirmation.cancel"));
				UIManager.put("OptionPane.noButtonText", cuw.frame.resourceBundle
						.getString("shanoir.uploader.currentUploads.Action.delete.confirmation.no"));
				UIManager.put("OptionPane.okButtonText", cuw.frame.resourceBundle
						.getString("shanoir.uploader.currentUploads.Action.delete.confirmation.ok"));
				UIManager.put("OptionPane.yesButtonText", cuw.frame.resourceBundle
						.getString("shanoir.uploader.currentUploads.Action.delete.confirmation.yes"));
				if (JOptionPane.showConfirmDialog(null, message,
						cuw.frame.resourceBundle.getString("shanoir.uploader.currentUploads.Action.delete.confirmation.title"), 1)
						== JOptionPane.YES_OPTION) {
					String uploadState = (String) cuw.table.getModel().getValueAt(row, cuw.uploadStateColumn);
					if (!uploadState.startsWith(cuw.startUploadState)
							&& !uploadState.startsWith(cuw.startAutoImportUploadState)) {
						DeleteDirectory dt = new DeleteDirectory();
						model.getValueAt(row, 0);
						dt.delete((String) model.getValueAt(row, 0));
					} else {
						String mess = cuw.frame.resourceBundle
								.getString("shanoir.uploader.currentUploads.Action.delete.notPossible.message");
						JOptionPane.showMessageDialog(new JFrame(), mess,
								cuw.frame.resourceBundle.getString(
										"shanoir.uploader.currentUploads.Action.delete.notPossible.title"),
								JOptionPane.WARNING_MESSAGE);
					}
				}
				processWorkFolder(workFolderFilePath);
			}

			public void mouseEntered(MouseEvent e) {
				cuw.selectedRow = cuw.table.rowAtPoint(e.getPoint());
				int col = cuw.table.columnAtPoint(e.getPoint());
				Rectangle bounds = cuw.table.getCellRect(cuw.selectedRow, col, false);
				int x = e.getX() - bounds.x;
				int y = e.getY() - bounds.y;
				cuw.rowsNb = cuw.table.getRowCount();
				if (col == cuw.deleteColumn) {
					try {
						cuw.table.getColumnModel().getColumn(col).setCellRenderer(new Delete_Renderer());
					} catch (Exception exp) {
						logger.error(exp.toString());
					}
				}
				cuw.table.repaint();
			}

			public void mouseExited(MouseEvent e) {
				// / SupportLabel.setText(supportMail);
			}
		});
	}

	/**
	 * Walk trough all folders within the work folder.
	 * 
	 * @param workFolder
	 * @throws IOException 
	 */
	private void processWorkFolder(File workFolder) throws IOException {
		List<File> folders = Util.listFolders(workFolder);
		logger.info("Found " + folders.size() + " folders in workFolder.");
		Map<String, ImportJob> currentUploads = new LinkedHashMap<String, ImportJob>();
		for (File f : folders) {
			ImportJob nominativeDataImportJob = processFolder(f);
			if (nominativeDataImportJob != null)
				currentUploads.put(f.getAbsolutePath(), nominativeDataImportJob);
		}
		currentNominativeDataModel.setCurrentUploads(currentUploads);
	}

	/**
	 * Inspects the content of a folder. Get the upload informations from import-job.json
	 * 
	 * @param folder
	 */
	private ImportJob processFolder(final File folder) throws IOException {
		logger.info("Started processing folder " + folder.getName());
		// Check if the folder contains an import-job.json file
		initNominativeDataImportJobManager(folder);
		if (importJobManager != null) {
			final ImportJob importJob = importJobManager.readImportJob();
			if (importJob != null) {
				// In case of previous ShUp version imports, Patient and Study data were stored in xml files
				if (importJob.getPatient() == null || importJob.getStudy() == null) {
					Patient patient = ImportUtils.getPatientFromNominativeDataJob(importJob.getWorkFolder());
					importJob.setPatient(patient);
					Study study = ImportUtils.getStudyFromNominativeDataJob(importJob.getWorkFolder());
					importJob.setStudy(study);
					// We write the json file so that we can open ImportDialog for this old import
					importJobManager.writeImportJob(importJob);
				}
				// In case of previous version importJobs 
				// (without uploadState) we look for uploadState value from upload-job.xml file
				if (importJob.getUploadState() == null) {
					String uploadJobState = ImportUtils.getUploadStateFromUploadJob(folder);
					importJob.setUploadState(UploadState.fromString(uploadJobState));
				}
				final UploadState uploadState = importJob.getUploadState();
				String uploadPercentage = importJob.getUploadPercentage();
				if (uploadPercentage == null || uploadPercentage.equals("")) {
					uploadPercentage = "0 %";
				}
				if (uploadState.toString().equals(UploadState.FINISHED.toString())) {
					importJob.setUploadPercentage(UploadState.FINISHED.toString());
				} else if (uploadState.toString().equals(UploadState.START.toString())
					|| uploadState.toString().equals(UploadState.START_AUTOIMPORT.toString())) {
						importJob.setUploadPercentage(uploadPercentage);
				} else {
					importJob.setUploadPercentage((String) uploadState.toString());
				}
				return importJob;
			} //else {
			// 	logger.error("Folder found in workFolder without upload-job.xml.");
			// }
		} else {
			logger.error("Folder " + folder.getName() + " found in workFolder without import-job.json.");
		}

		logger.info("Ended processing folder " + folder.getName() + ".");
		return null;
	}

	/**
	 * Initializes an ImportJobManager and puts the other files in the list
	 * dicomFiles.
	 * 
	 * @param folder
	 * @param dicomFiles
	 */
	private void initNominativeDataImportJobManager(final File folder) {
		final Collection<File> files = Util.listFiles(folder, null, false);
		for (Iterator filesIt = files.iterator(); filesIt.hasNext();) {
			final File file = (File) filesIt.next();
			if (file.getName().equals(ShUpConfig.IMPORT_JOB_JSON)) {
				importJobManager = new NominativeDataImportJobManager(file);
			}
		}
	}

	// TODO : delete this method
	public void updateNominativeDataPercentage(File folder, String uploadPercentage) {
		if (uploadPercentage.equals(UploadState.FINISHED.toString())) {
			uploadPercentage = UploadState.FINISHED.toString();
		}
		currentNominativeDataModel.updateUploadPercentage(folder.getAbsolutePath(), uploadPercentage);
	}

	public void addNewNominativeData(File folder, ImportJob nominativeDataImportJob) {
		currentNominativeDataModel.addUpload(folder.getAbsolutePath(), nominativeDataImportJob);
	}

	public class Delete_Renderer extends DefaultTableCellRenderer {
 		Delete_Renderer() {
 		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component tableCellRendererComponent = super.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);
			tableCellRendererComponent.setBackground(Color.LIGHT_GRAY);
			setHorizontalAlignment(SwingConstants.CENTER);
			tableCellRendererComponent.setFont(tableCellRendererComponent.getFont().deriveFont(Font.BOLD));

			if (value instanceof String) {
 				String string = (String) value;
 				if (row != cuw.rowsNb - 1) {
 					setText(getDeleteHTML(string));
 					setToolTipText(cuw.frame.resourceBundle
 							.getString("shanoir.uploader.currentUploads.Action.delete.tooltip"));
 				} else {
 					setText(getDeleteAllHTML(string));
					setToolTipText(cuw.frame.resourceBundle
							.getString("shanoir.uploader.currentUploads.Action.deleteAll.tooltip"));
				}
			}
			return tableCellRendererComponent;
		}

		private String getDeleteHTML(String string) {
			StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			sb.append("<span style=\"color: blue;\"><b>");
			sb.append(string);
			sb.append("</b></span>");
			sb.append("</html>");
			return sb.toString();
		}

		private String getDeleteAllHTML(String string) {
			StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			sb.append("<span style=\"color: purple;\"><b>");
			sb.append(string);
			sb.append("</b></span>");
			sb.append("</html>");
			return sb.toString();
		}
	}
}
