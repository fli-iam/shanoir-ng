package org.shanoir.uploader.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.shanoir.dicom.importer.UploadState;
import org.shanoir.uploader.nominativeData.CurrentNominativeDataModel;
import org.shanoir.uploader.nominativeData.NominativeDataUploadJob;

public class CurrentUploadsWindowTable implements Observer {

	public MainWindow frame;
	public static JTable table;
	Object[] columnNames;
	Object[] paths;
	public int importColumn = 7;
	public int deleteColumn = 8;
	public int patientNameColumn = 2;
	public int uploadStateColumn = 6;
	public String readyUploadState = "READY";
	public String startUploadState = "START";
	public String startAutoImportUploadState = "START_AUTOIMPORT";
	public String finishedUploadState = "FINISHED";
	public String errorUploadState = "ERROR";
	public int selectedRow;
	public int rowsNb;

	public CurrentUploadsWindowTable(final MainWindow frame) {
		this.frame = frame;
		final Object[] columnNames = {
			"id",
			frame.resourceBundle.getString("shanoir.uploader.currentUploads.ID"),
			frame.resourceBundle.getString("shanoir.uploader.currentUploads.patientName"),
			frame.resourceBundle.getString("shanoir.uploader.currentUploads.IPP"),
			frame.resourceBundle.getString("shanoir.uploader.currentUploads.studyDate"),
			frame.resourceBundle.getString("shanoir.uploader.currentUploads.mri"),
			frame.resourceBundle.getString("shanoir.uploader.currentUploads.importState"),
			frame.resourceBundle.getString("shanoir.uploader.currentUploads.Action.import"),
			frame.resourceBundle.getString("shanoir.uploader.currentUploads.Action.delete")
		};
		this.columnNames = columnNames;
		table = new JTable(new DefaultTableModel(columnNames, 0));
		table.setPreferredScrollableViewportSize(new Dimension(800, 100));
		table.setFillsViewportHeight(true);

		table.getColumnModel().getColumn(0).setPreferredWidth(0);
		table.getColumnModel().getColumn(0).setMinWidth(0);
		table.getColumnModel().getColumn(0).setWidth(0);
		table.getColumnModel().getColumn(0).setMaxWidth(0);
		table.getColumnModel().getColumn(1).setPreferredWidth(150);
		table.getColumnModel().getColumn(2).setPreferredWidth(150);
		table.getColumnModel().getColumn(5).setPreferredWidth(100);
		table.getColumnModel().getColumn(6).setPreferredWidth(40);
		table.getColumnModel().getColumn(7).setPreferredWidth(40);
		table.getColumnModel().getColumn(8).setPreferredWidth(50);

		// Resize and center the JTable header
		table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
		DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) table.getTableHeader()
				.getDefaultRenderer();
		headerRenderer.setHorizontalAlignment(JLabel.CENTER);

		// Center the JTable's cells
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		for (int j = 0; j < table.getColumnCount(); j++) {
			table.getColumnModel().getColumn(j).setCellRenderer(centerRenderer);
		}
		// Change Background color of action column
		table.getColumnModel().getColumn(importColumn).setCellRenderer(new Background_Renderer());
		table.getColumnModel().getColumn(deleteColumn).setCellRenderer(new Background_Renderer());
		frame.scrollPaneUpload.getViewport().add(table);
	}

	public void fillTable(Map<String, NominativeDataUploadJob> initialUploads) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		for (Map.Entry<String, NominativeDataUploadJob> entry : initialUploads.entrySet()) {
			if (entry.getValue() != null) {
				String key = entry.getKey();
				NominativeDataUploadJob nominativeDataUploadJob = (NominativeDataUploadJob) entry.getValue();
				addRow(model, key, nominativeDataUploadJob);
			}
		}
		addLastRow(model);
	}

	private void addLastRow(DefaultTableModel model) {
		model.addRow(new Object[] { "", "", "", "", "", "", "", "",
				(String) frame.resourceBundle.getString("shanoir.uploader.currentUploads.Action.deleteAll") });
	}

	private void addRow(DefaultTableModel model, String key, NominativeDataUploadJob nominativeDataUploadJob) {
		if (UploadState.READY.equals(nominativeDataUploadJob.getUploadState())) {
			model.addRow(new Object[] { key, nominativeDataUploadJob.getPatientPseudonymusHash(),
				nominativeDataUploadJob.getPatientName(), nominativeDataUploadJob.getIPP(),
				nominativeDataUploadJob.getStudyDate(), nominativeDataUploadJob.getMriSerialNumber(),
				nominativeDataUploadJob.getUploadState().toString(),
				(String) frame.resourceBundle.getString("shanoir.uploader.currentUploads.Action.import"),
				(String) frame.resourceBundle.getString("shanoir.uploader.currentUploads.Action.delete")
			});
		} else if (UploadState.FINISHED_UPLOAD.equals(nominativeDataUploadJob.getUploadState())) {
			model.addRow(new Object[] { key, nominativeDataUploadJob.getPatientPseudonymusHash(),
				nominativeDataUploadJob.getPatientName(), nominativeDataUploadJob.getIPP(),
				nominativeDataUploadJob.getStudyDate(), nominativeDataUploadJob.getMriSerialNumber(),
				nominativeDataUploadJob.getUploadPercentage(), "",
				(String) frame.resourceBundle.getString("shanoir.uploader.currentUploads.Action.delete") });
		} else if (UploadState.ERROR.equals(nominativeDataUploadJob.getUploadState())) {
			model.addRow(new Object[] { key, nominativeDataUploadJob.getPatientPseudonymusHash(),
				nominativeDataUploadJob.getPatientName(), nominativeDataUploadJob.getIPP(),
				nominativeDataUploadJob.getStudyDate(), nominativeDataUploadJob.getMriSerialNumber(),
				nominativeDataUploadJob.getUploadState().toString(), "",
				(String) frame.resourceBundle.getString("shanoir.uploader.currentUploads.Action.delete") });
		} else {
			model.addRow(new Object[] { key, nominativeDataUploadJob.getPatientPseudonymusHash(),
				nominativeDataUploadJob.getPatientName(), nominativeDataUploadJob.getIPP(),
				nominativeDataUploadJob.getStudyDate(), nominativeDataUploadJob.getMriSerialNumber(),
				nominativeDataUploadJob.getUploadPercentage(), "", "" });
		}
	}

	public void addLineToTable(String absolutePath, NominativeDataUploadJob nominativeDataUploadJob) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		int nbRow = model.getRowCount();
		model.removeRow(nbRow - 1);
		addRow(model, absolutePath, nominativeDataUploadJob);
		addLastRow(model);
	}

	public void updatePercent(String path, String percentage) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		int nbRow = model.getRowCount();
		for (int i = 0; i < nbRow - 1; i++) {
			if (model.getValueAt(i, 0).equals(path)) {
				model.setValueAt(percentage, i, uploadStateColumn);
			}
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked
	 * from the event-dispatching thread.
	 */
	private void showGUI(MainWindow frame, final Object[][] currentUploadsTable) {
		frame.scrollPaneUpload.getViewport().add(table);
	}

	public void showWindow(final MainWindow frame, final Object[][] currentUploadsTable, final Object[] paths) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		this.paths = paths;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showGUI(frame, currentUploadsTable);
			}
		});
	}

	class Background_Renderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = -7514583714509447137L;

		Background_Renderer() {
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component tableCellRendererComponent = super.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);
			tableCellRendererComponent.setBackground(Color.LIGHT_GRAY);
			setHorizontalAlignment(SwingConstants.CENTER);
			tableCellRendererComponent.setFont(tableCellRendererComponent.getFont().deriveFont(Font.BOLD));
			return tableCellRendererComponent;
		}
	}

	public void udpateMainWindowUploadStatistics(CurrentNominativeDataModel currentNominativeDataModel) {
		int nbFinishUpload = 0;
		int nbStartUpload = 0;
		int nbErrorUpload = 0;
		int totalUploadPercent = 0;
		for (Map.Entry<String, NominativeDataUploadJob> entry : currentNominativeDataModel.getCurrentUploads()
				.entrySet()) {
			if (entry.getValue() != null) {
				if (entry.getValue().getUploadPercentage() == null
					|| entry.getValue().getUploadPercentage().isEmpty()
					|| "READY".compareTo(entry.getValue().getUploadPercentage()) == 0) {
					// Do Nothing
				} else {
					if (entry.getValue().getUploadPercentage().equals("FINISHED")) {
						totalUploadPercent += 100;
						nbFinishUpload++;
					} else if (entry.getValue().getUploadPercentage().equals("ERROR")) {
						nbErrorUpload++;
					} else {
						nbStartUpload++;
						int percent = Integer.valueOf(entry.getValue().getUploadPercentage().substring(0,
								entry.getValue().getUploadPercentage().length() - 2));
						totalUploadPercent += percent;
					}
				}
			}
		}
		if (nbStartUpload != 0) {
			totalUploadPercent = Math.round(totalUploadPercent / (nbFinishUpload + nbStartUpload));
			frame.uploadProgressBar.setValue(totalUploadPercent);
		} else {
			frame.uploadProgressBar.setValue(0);
		}
		frame.startedUploadsLB
				.setText(frame.resourceBundle.getString("shanoir.uploader.startedUploadsSummary") + nbStartUpload);
		frame.finishedUploadsLB
				.setText(frame.resourceBundle.getString("shanoir.uploader.finishedUploadsSummary") + nbFinishUpload);
		frame.errorUploadsLB
				.setText(frame.resourceBundle.getString("shanoir.uploader.failedUploadsSummary") + nbErrorUpload);
		if (nbErrorUpload != 0) {
			frame.errorAlert.setText(frame.resourceBundle.getString("shanoir.uploader.failedUploadsMessagePart1")
					+ frame.resourceBundle.getString("shanoir.uploader.failedUploadsMessagePart2"));
		} else {
			frame.errorAlert.setText("");
		}
	}

	public void update(Observable o, Object arg) {
		CurrentNominativeDataModel currentNominativeDataModel = (CurrentNominativeDataModel) o;
		String[] msg = (String[]) arg;
		udpateMainWindowUploadStatistics(currentNominativeDataModel);

		if (msg.length > 1) {
			if (msg[0].equals("UpdatePercent")) {
				updatePercent(msg[1], msg[2]);
			}

			if (msg[0].equals("add")) {
				addLineToTable(msg[1], currentNominativeDataModel.getCurrentUploads().get(msg[1]));
			}

		} else if (msg[0].equals("fill")) {
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			int nbRow = model.getRowCount();
			for (int i = nbRow; i > 0; i--) {
				model.removeRow(i - 1);
			}
			rowsNb = 0;
			fillTable(currentNominativeDataModel.getCurrentUploads());
		}
	}

}
