package org.shanoir.uploader.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.UploadState;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.nominativeData.CurrentNominativeDataModel;

@SuppressWarnings("deprecation")
public class CurrentUploadsWindowTable implements Observer {

	private static CurrentUploadsWindowTable instance;
	public final MainWindow frame;
	public final JTable table;
	Object[] columnNames;
	Object[] paths;
	public int importColumn = 7;
	public int deleteColumn = 8;
	public int patientNameColumn = 2;
	public int uploadStateColumn = 6;
	public String readyUploadState = UploadState.READY.toString();
	public String startUploadState = UploadState.START.toString();
	public String startAutoImportUploadState = UploadState.START_AUTOIMPORT.toString();
	public String finishedUploadState = UploadState.FINISHED.toString();
	public String errorUploadState = UploadState.ERROR.toString();
	public String checkOKUploadState = UploadState.CHECK_OK.toString();
	public String checkKOUploadState = UploadState.CHECK_KO.toString();
	public int selectedRow;
	public int rowsNb;

	// Formatter to display the dates in the table
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ShUpConfig.formatter.toPattern());

	private CurrentUploadsWindowTable(MainWindow frame) {
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
		// Create the non editable table to display the current uploads
		DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
    		@Override
    		public boolean isCellEditable(int row, int column) {
        		return false;
    		}
		};
		this.table = new JTable(model);

		// Activate sorting for comparable content
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
		sorter.setSortable(7, false);
		sorter.setSortable(8, false);
		table.setRowSorter(sorter);

		initTable();
		frame.scrollPaneUpload.getViewport().add(table);
	}

	// Method to create the singleton instance of the class
	public static synchronized CurrentUploadsWindowTable getInstance(MainWindow frame) {
        if (instance == null) {
            instance = new CurrentUploadsWindowTable(frame);
        }
        return instance;
    }

	private void initTable() {
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
    }

	public void fillTable(Map<String, ImportJob> initialUploads) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		for (Map.Entry<String, ImportJob> entry : initialUploads.entrySet()) {
			if (entry.getValue() != null) {
				String key = entry.getKey();
				ImportJob nominativeDataImportJob = (ImportJob) entry.getValue();
				addRow(model, key, nominativeDataImportJob);
			}
		}
	}

	private void addRow(DefaultTableModel model, String key, ImportJob nominativeDataImportJob) {
		Serie firstSelectedSerie = nominativeDataImportJob.getFirstSelectedSerie();
		String actionImport = (String) frame.resourceBundle.getString("shanoir.uploader.currentUploads.Action.import");
		String actionDelete = (String) frame.resourceBundle.getString("shanoir.uploader.currentUploads.Action.delete");
		Object[] row = switch (nominativeDataImportJob.getUploadState()) {
			case READY, ERROR -> new Object[] {
				key,
				nominativeDataImportJob.getSubject().getIdentifier(),
				nominativeDataImportJob.getPatient().getPatientFirstName() + " " + nominativeDataImportJob.getPatient().getPatientLastName(), // Was firstname and lastname from nominativeDataUploadJob, check if firstname still present
				nominativeDataImportJob.getPatient().getPatientID(),
				nominativeDataImportJob.getStudy().getStudyDate().format(formatter),
				firstSelectedSerie.getEquipment().getManufacturer() + " (" + firstSelectedSerie.getEquipment().getDeviceSerialNumber() + ")", // was e.g Philips (serial number)
				nominativeDataImportJob.getUploadState().toString(),
				actionImport,
				actionDelete
			};
			case FINISHED -> new Object[] {
				key,
				nominativeDataImportJob.getSubject().getIdentifier(),
				nominativeDataImportJob.getPatient().getPatientFirstName() + " " + nominativeDataImportJob.getPatient().getPatientLastName(),
				nominativeDataImportJob.getPatient().getPatientID(),
				nominativeDataImportJob.getStudy().getStudyDate().format(formatter),
				firstSelectedSerie.getEquipment().getManufacturer() + " (" + firstSelectedSerie.getEquipment().getDeviceSerialNumber() + ")",
				nominativeDataImportJob.getUploadPercentage().toString(),
				"",
				""
			};
			case CHECK_OK, CHECK_KO -> new Object[] {
				key,
				nominativeDataImportJob.getSubject().getIdentifier(),
				nominativeDataImportJob.getPatient().getPatientFirstName() + " " + nominativeDataImportJob.getPatient().getPatientLastName(),
				nominativeDataImportJob.getPatient().getPatientID(),
				nominativeDataImportJob.getStudy().getStudyDate().format(formatter),
				firstSelectedSerie.getEquipment().getManufacturer() + " (" + firstSelectedSerie.getEquipment().getDeviceSerialNumber() + ")",
				nominativeDataImportJob.getUploadPercentage().toString(),
				"",
				actionDelete
			};
			default -> new Object[] {
				key,
				nominativeDataImportJob.getSubject().getIdentifier(),
				nominativeDataImportJob.getPatient().getPatientFirstName() + " " + nominativeDataImportJob.getPatient().getPatientLastName(),
				nominativeDataImportJob.getPatient().getPatientID(),
				nominativeDataImportJob.getStudy().getStudyDate().format(formatter),
				firstSelectedSerie.getEquipment().getManufacturer() + " (" + firstSelectedSerie.getEquipment().getDeviceSerialNumber() + ")",
				nominativeDataImportJob.getUploadPercentage().toString(),
				"",
				""
			};
		};
		model.addRow(row);
	}

	public void addLineToTable(String absolutePath, ImportJob nominativeDataImportJob) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		addRow(model, absolutePath, nominativeDataImportJob);
	}

	public void updatePercent(String path, String percentage) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		int nbRow = model.getRowCount();
		for (int i = 0; i < nbRow; i++) {
			if (model.getValueAt(i, 0).equals(path)) {
				model.setValueAt(percentage, i, uploadStateColumn);
			}
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked
	 * from the event-dispatching thread.
	 */
	private void showGUI(MainWindow frame) {
		frame.scrollPaneUpload.getViewport().add(table);
	}

	public void showWindow(final MainWindow frame, final Object[] paths) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		this.paths = paths;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				showGUI(frame);
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
		for (Map.Entry<String, ImportJob> entry : currentNominativeDataModel.getCurrentUploads()
				.entrySet()) {
			if (entry.getValue() != null) {
				if (entry.getValue().getUploadPercentage() == null
					|| entry.getValue().getUploadPercentage().isEmpty()
					|| UploadState.READY.toString().compareTo(entry.getValue().getUploadPercentage()) == 0) {
					// Do Nothing
				} else {
					if (entry.getValue().getUploadPercentage().equals(finishedUploadState)
					|| entry.getValue().getUploadPercentage().equals(checkOKUploadState)
					|| entry.getValue().getUploadPercentage().equals(checkKOUploadState)) {
						totalUploadPercent += 100;
						nbFinishUpload++;
					} else if (entry.getValue().getUploadPercentage().equals(errorUploadState)) {
						nbErrorUpload++;
					} else {
						nbStartUpload++;
						int percent = Integer.parseInt(entry.getValue().getUploadPercentage().substring(0,
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
			frame.uploadErrorAlert.setText(frame.resourceBundle.getString("shanoir.uploader.failedUploadsMessagePart1")
					+ frame.resourceBundle.getString("shanoir.uploader.failedUploadsMessagePart2"));
		} else {
			frame.uploadErrorAlert.setText("");
		}
	}

    @Override
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
