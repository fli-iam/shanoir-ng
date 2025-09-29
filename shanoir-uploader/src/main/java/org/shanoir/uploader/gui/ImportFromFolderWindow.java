package org.shanoir.uploader.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.uploader.action.ImportFromFolderActionListener;
import org.shanoir.uploader.action.UpdateFolderImportStudyListener;
import org.shanoir.uploader.action.UploadFromFolderActionListener;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.gui.customcomponent.JComboBoxMandatory;
import org.shanoir.uploader.model.ExaminationImport;
import org.shanoir.uploader.model.FolderImport;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportFromFolderWindow extends JFrame {

    public JButton uploadButton;
    public JButton openButton;

    private static Logger logger = LoggerFactory.getLogger(ImportFromFolderWindow.class);

    FolderImport folderImport;

    public File shanoirUploaderFolder;
    public ResourceBundle resourceBundle;
    public JFrame frame;
    public JLabel error = new JLabel();
    public JLabel folderDetail = new JLabel();

    // Study / StudyCard
    public JLabel studyLabel;
    public JComboBoxMandatory studyCB;
    public JLabel studyCardLabel;
    public JComboBoxMandatory studyCardCB;
    public JLabel studyCardFilterLabel;
    public JTextField studyCardFilterTextField;

    final JPanel masterPanel;

    public JTable table;
    public JScrollPane scrollPaneUpload;
    public JSeparator separatorStudyStudyCard;
    public JLabel separatorSubjectStudyCardLabel;

    UploadFromFolderActionListener uploadListener;
    ImportFromFolderActionListener importListener;
    IDicomServerClient dicomServerClient;
    ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer;
    ShanoirUploaderServiceClient shanoirUploaderServiceClient;
    UpdateFolderImportStudyListener updateFolderImportStudyListener;

    public ImportFromFolderWindow(File shanoirUploaderFolder, ResourceBundle resourceBundle, JScrollPane scrollPaneUpload, IDicomServerClient dicomServerClient, ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer, ShanoirUploaderServiceClient shanoirUploaderServiceClientNG) {
        this.updateFolderImportStudyListener = new UpdateFolderImportStudyListener(shanoirUploaderServiceClientNG, this);
        this.shanoirUploaderFolder = shanoirUploaderFolder;
        this.resourceBundle = resourceBundle;
        this.dicomServerClient = dicomServerClient;
        this.dicomFileAnalyzer = dicomFileAnalyzer;
        this.shanoirUploaderServiceClient = shanoirUploaderServiceClientNG;
        this.scrollPaneUpload = scrollPaneUpload;

        // Create the frame.
        frame = new JFrame(resourceBundle.getString("shanoir.uploader.import.folder.title"));
        frame.setSize(1600, 700);
        this.setSize(1600, 700);

        // What happens when the frame closes?
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        // Panel content
        masterPanel = new JPanel(new GridBagLayout());
        masterPanel.setLayout(new GridBagLayout());
        masterPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        frame.setContentPane(masterPanel);

        // Folder description here
        this.folderDetail.setText(resourceBundle.getString("shanoir.uploader.import.folder.detail"));
        GridBagConstraints gBCcsvDetail = new GridBagConstraints();
        gBCcsvDetail.anchor = GridBagConstraints.NORTHWEST;
        gBCcsvDetail.gridx = 0;
        gBCcsvDetail.gridy = 0;
        masterPanel.add(this.folderDetail, gBCcsvDetail);

        // Potential error here
        GridBagConstraints gBCError = new GridBagConstraints();
        gBCError.anchor = GridBagConstraints.NORTHWEST;
        gBCError.gridx = 0;
        gBCError.gridy = 1;
        this.error.setForeground(Color.RED);
        masterPanel.add(this.error, gBCError);

        // SELECT STUDY here
        GridBagConstraints studyLabelGBC = new GridBagConstraints();

        studyLabel = new JLabel(resourceBundle.getString("shanoir.uploader.studyLabel") + " *");
        studyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        studyLabelGBC.anchor = GridBagConstraints.NORTHWEST;
        studyLabelGBC.gridx = 0;
        studyLabelGBC.gridy = 2;
        masterPanel.add(studyLabel, studyLabelGBC);
        studyLabel.setVisible(true);

        GridBagConstraints studyGBC = new GridBagConstraints();
        studyCB = new JComboBoxMandatory();
        studyCB.setBackground(Color.WHITE);
        studyGBC.anchor = GridBagConstraints.NORTHWEST;
        studyGBC.gridx = 0;
        studyGBC.gridy = 3;
        masterPanel.add(studyCB, studyGBC);
        studyCB.addItemListener(updateFolderImportStudyListener);
        studyCB.setVisible(true);

        // SELECT STUDY CARD here
        GridBagConstraints studyCardLabelGBC = new GridBagConstraints();
        studyCardLabel = new JLabel(resourceBundle.getString("shanoir.uploader.studycardLabel") + " *");
        studyCardLabelGBC.anchor = GridBagConstraints.NORTHWEST;
        studyCardLabelGBC.gridx = 0;
        studyCardLabelGBC.gridy = 4;
        studyCardLabel.setVisible(true);
        masterPanel.add(studyCardLabel, studyCardLabelGBC);

        GridBagConstraints studyCardGBC = new GridBagConstraints();

        studyCardCB = new JComboBoxMandatory();
        studyCardCB.setBackground(Color.WHITE);
        studyCardGBC.anchor = GridBagConstraints.NORTHWEST;
        studyCardGBC.gridx = 0;
        studyCardGBC.gridy = 5;
        studyCardCB.setVisible(true);
        studyCardCB.addItemListener(updateFolderImportStudyListener);
        masterPanel.add(studyCardCB, studyCardGBC);

        // OPEN FILE button here
        openButton = new JButton(resourceBundle.getString("shanoir.uploader.import.folder.button.open"));
        GridBagConstraints gBCOpenButton = new GridBagConstraints();
        gBCOpenButton.anchor = GridBagConstraints.NORTHWEST;
        gBCOpenButton.gridx = 0;
        gBCOpenButton.gridy = 6;
        openButton.setEnabled(false);
        masterPanel.add(openButton, gBCOpenButton);

        uploadListener = new UploadFromFolderActionListener(this);
        openButton.addActionListener(uploadListener);

        // initialize others
        this.updateFolderImportStudyListener.updateFolderImportForStudyAndStudyCard();

        // list of import display here
        //headers for the table
        String[] columns = new String[] {
                resourceBundle.getString("shanoir.uploader.import.folder.column.study.id"),
                resourceBundle.getString("shanoir.uploader.import.folder.column.studycard"),
                resourceBundle.getString("shanoir.uploader.import.folder.column.common.name"),
                resourceBundle.getString("shanoir.uploader.import.folder.column.comment"),
                resourceBundle.getString("shanoir.uploader.import.folder.column.error")
        };

        // Create table with data
        table = new JTable();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setColumnIdentifiers(columns);
        table.getColumnModel().getColumn(0).setMinWidth(30);
        table.getColumnModel().getColumn(1).setMinWidth(90);
        table.getColumnModel().getColumn(2).setMinWidth(90);
        table.getColumnModel().getColumn(3).setMinWidth(90);
        table.getColumnModel().getColumn(4).setMinWidth(500);

        // Add the table to the frame
        JPanel tablePanel = new JPanel(new BorderLayout());
        GridBagConstraints gBCTableanchor = new GridBagConstraints();
        gBCTableanchor.anchor = GridBagConstraints.NORTHWEST;
        gBCTableanchor.gridx = 0;
        gBCTableanchor.gridy = 7;
        tablePanel.setSize(1500, 500);
        masterPanel.add(tablePanel , gBCTableanchor);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setSize(1500, 500);
        scrollPane.setPreferredSize(new Dimension(1500, 500));
        table.getParent().setVisible(false);
        table.setSize(1500, 500);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablePanel.add(scrollPane);

        // IMPORT button here when necessary
        uploadButton = new JButton(resourceBundle.getString("shanoir.uploader.import.folder.button.import"));
        GridBagConstraints gBCuploadButton = new GridBagConstraints();
        gBCuploadButton.anchor = GridBagConstraints.NORTHWEST;
        gBCuploadButton.gridx = 0;
        gBCuploadButton.gridy = 8;
        uploadButton.setVisible(false);
        uploadButton.setEnabled(false);
        masterPanel.add(uploadButton, gBCuploadButton);

        importListener = new ImportFromFolderActionListener(this, resourceBundle, dicomServerClient, dicomFileAnalyzer, shanoirUploaderFolder, shanoirUploaderServiceClientNG);

        uploadButton.addActionListener(importListener);

        // center the frame
        // frame.setLocationRelativeTo(null );
        Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        int windowWidth = 1600;
        int windowHeight = 700;
        // set position and size
        frame.setBounds(center.x - windowWidth / 2, center.y - windowHeight / 2, windowWidth, windowHeight);

        // Show it.
        frame.setVisible(true);
    }

    /**
     * Displays an error in the  located error field
     * @param string the generated error to display
     */
    public void displayError(String string) {
        this.error.setText(string);
        this.error.setVisible(true);
    }

    public void displayImports(FolderImport imports) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.getDataVector().removeAllElements();

        boolean inError = false;
        for (ExaminationImport examinationImport : imports.getExaminationImports()) {
            model.addRow(examinationImport.getRawData());
        }
        this.error.setVisible(inError);

        model.fireTableDataChanged();
        table.getParent().setVisible(true);

        uploadButton.setEnabled(!inError);
    }

    public FolderImport getFolderImport() {
        return folderImport;
    }

    public void setFolderImport(FolderImport folderImport) {
        this.folderImport = folderImport;
    }
}
