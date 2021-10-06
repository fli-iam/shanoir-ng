package org.shanoir.uploader.nominativeData;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;
import org.shanoir.dicom.importer.UploadJob;
import org.shanoir.dicom.importer.UploadJobManager;
import org.shanoir.dicom.importer.UploadState;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.action.DeleteDirectory;
import org.shanoir.uploader.gui.CurrentUploadsWindowTable;
import org.shanoir.util.ShanoirUtil;

public class CurrentNominativeDataController {

	private static Logger logger = Logger.getLogger(CurrentNominativeDataController.class);

	private CurrentNominativeDataModel currentNominativeDataModel = null;

	private NominativeDataUploadJobManager nominativeDataUploadJobManager;

	private UploadJobManager uploadJobManager;

	private CurrentUploadsWindowTable cuw;

	public CurrentNominativeDataController(final File workFolderFilePath, final CurrentUploadsWindowTable cuw) {
		super();
		this.currentNominativeDataModel = new CurrentNominativeDataModel();
		currentNominativeDataModel.addObserver(cuw);
		this.cuw = cuw;
		processWorkFolder(workFolderFilePath);

		cuw.table.addMouseListener(new MouseAdapter() {
			public DefaultTableModel model = (DefaultTableModel) cuw.table.getModel();

			public void mouseClicked(MouseEvent e) {
				int row = cuw.table.getSelectedRow();
				int col = cuw.table.getSelectedColumn();
				int rows = cuw.table.getRowCount();
				// Last row and last column: delete all
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
							if (uploadState.equals(cuw.finishedUploadState)
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
						processWorkFolder(workFolderFilePath);
					}
				// delete one import: ready (to gain disk space) or finished
				} else if (col == cuw.deleteColumn && row != -1) {
					String uploadState = (String) cuw.table.getModel().getValueAt(row, cuw.uploadStateColumn);
					if (uploadState.equals(cuw.finishedUploadState)
							|| uploadState.equals(cuw.readyUploadState)) {
						showDeleteConfirmationDialog(workFolderFilePath, cuw, row);					
					}
				// start the import
				} else if (col == cuw.importColumn && row != -1) {
					String uploadState = (String) cuw.table.getModel().getValueAt(row, cuw.uploadStateColumn);
					if (uploadState.equals(cuw.readyUploadState)) {
						String uploadJobFilePath = (String) cuw.table.getModel().getValueAt(row, 0) + File.separator + UploadJobManager.UPLOAD_JOB_XML;
						File uploadJobFile = new File(uploadJobFilePath);
						uploadJobManager = new UploadJobManager(uploadJobFile);
						UploadJob uploadJob = uploadJobManager.readUploadJob();
						cuw.frame.getImportDialogOpener().openImportDialog(uploadJob, uploadJobFile.getParentFile());
					}
				}
			}

			/**
			 * @param workFolderFilePath
			 * @param cuw
			 * @param row
			 */
			private void showDeleteConfirmationDialog(final File workFolderFilePath,
					final CurrentUploadsWindowTable cuw, int row) {
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
				if (cuw.selectedRow == cuw.rowsNb - 1 && col == cuw.importColumn) {
					cuw.table.getColumnModel().getColumn(col).setCellRenderer(new DeleteAllRenderer());
				} else if (col == cuw.importColumn) {
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
	 */
	private void processWorkFolder(File workFolder) {
		final List<File> folders = ShanoirUtil.listFolders(workFolder);
		logger.info("Display function: found " + folders.size() + " folders in work folder.");
		Map<String, NominativeDataUploadJob> currentUploads = new HashMap<String, NominativeDataUploadJob>();
		for (File f : folders) {
			NominativeDataUploadJob nominativeDataUploadJob = processFolder(f);
			currentUploads.put(f.getAbsolutePath(), nominativeDataUploadJob);
		}
		currentNominativeDataModel.setCurrentUploads(currentUploads);
	}

	/**
	 * Inspects the content of a folder. Copies the infos from one xml into the other xml, really bad.
	 * 
	 * @param folder
	 */
	private NominativeDataUploadJob processFolder(final File folder) {
		logger.info("Started processing folder " + folder.getName() + "...");
		initNominativeDataUploadJobManager(folder); // NOMINATIVE_DATA_JOB_XML
		initUploadJobManager(folder); // UPLOAD_JOB_XML
		if (nominativeDataUploadJobManager != null) {
			final NominativeDataUploadJob nominativeDataUploadJob = nominativeDataUploadJobManager.readUploadDataJob();
			if (uploadJobManager != null) {
				final UploadJob uploadJob = uploadJobManager.readUploadJob();
				final UploadState uploadState = uploadJob.getUploadState();
				nominativeDataUploadJob.setUploadState(uploadState);
				String uploadPercentage = nominativeDataUploadJob.getUploadPercentage();
				if (uploadPercentage == null || uploadPercentage.equals("")) {
					uploadPercentage = "0 %";
				}
				if (uploadState.toString().equals("FINISHED_UPLOAD")) {
					nominativeDataUploadJob.setUploadPercentage("FINISHED");
				} else if (uploadState.toString().equals("START")
						|| uploadState.toString().equals("START_AUTOIMPORT")) {
					nominativeDataUploadJob.setUploadPercentage(uploadPercentage);
				} else {
					nominativeDataUploadJob.setUploadPercentage((String) uploadState.toString());
				}
				return nominativeDataUploadJob;
			} else {
				logger.error("Folder found in workFolder without upload-job.xml.");
			}
		} else {
			logger.error("Folder found in workFolder without nominative-data-job.xml.");
		}

		logger.info("Ended processing folder " + folder.getName() + ".");
		return null;
	}

	/**
	 * Initializes an UploadJobManager and puts the other files in the list
	 * dicomFiles.
	 * 
	 * @param folder
	 * @param dicomFiles
	 */
	private void initNominativeDataUploadJobManager(final File folder) {
		final Collection<File> files = ShanoirUtil.listFiles(folder, null, false);
		for (Iterator filesIt = files.iterator(); filesIt.hasNext();) {
			final File file = (File) filesIt.next();
			if (file.getName().equals(NominativeDataUploadJobManager.NOMINATIVE_DATA_JOB_XML)) {
				nominativeDataUploadJobManager = new NominativeDataUploadJobManager(file);
			}
		}
	}

	private void initUploadJobManager(final File folder) {
		final Collection<File> files = ShanoirUtil.listFiles(folder, null, false);
		for (Iterator filesIt = files.iterator(); filesIt.hasNext();) {
			final File file = (File) filesIt.next();
			if (file.getName().equals(UploadJobManager.UPLOAD_JOB_XML)) {
				uploadJobManager = new UploadJobManager(file);
			}
		}
	}

	public void updateNominativeDataPercentage(File folder, String uploadPercentage) {
		if (uploadPercentage.equals("FINISHED_UPLOAD")) {
			uploadPercentage = "FINISHED";
		}
		currentNominativeDataModel.updateUploadPercentage(folder.getAbsolutePath(), uploadPercentage);
	}

	public void addNewNominativeData(File folder, NominativeDataUploadJob nominativeDataUploadJob) {
		currentNominativeDataModel.addUpload(folder.getAbsolutePath(), nominativeDataUploadJob);
	}

	public class DeleteAllRenderer extends DefaultTableCellRenderer {
		DeleteAllRenderer() {
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component tableCellRendererComponent = super.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);
			tableCellRendererComponent.setBackground(Color.LIGHT_GRAY);
			setHorizontalAlignment(SwingConstants.CENTER);
			tableCellRendererComponent.setFont(tableCellRendererComponent.getFont().deriveFont(Font.BOLD));
			if (row == cuw.rowsNb - 1) {
				if (value instanceof String) {
					String string = (String) value;
					setText(getHTML(string));
					setToolTipText(cuw.frame.resourceBundle
							.getString("shanoir.uploader.currentUploads.Action.deleteAll.tooltip"));
				}
			}
			return tableCellRendererComponent;
		}

		private String getHTML(String string) {
			StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			sb.append("<span style=\"color: red;\"><b>");
			sb.append(string);
			sb.append("</b></span>");
			sb.append("</html>");
			return sb.toString();
		}

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
			if (row == cuw.selectedRow) {
				if (value instanceof String) {
					String string = (String) value;
					setText(getHTML(string));
					setToolTipText(cuw.frame.resourceBundle
							.getString("shanoir.uploader.currentUploads.Action.delete.tooltip"));
				}
			}
			return tableCellRendererComponent;
		}

		private String getHTML(String string) {
			StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			sb.append("<span style=\"color: blue;\"><b>");
			sb.append(string);
			sb.append("</b></span>");
			sb.append("</html>");
			return sb.toString();
		}
	}
}
