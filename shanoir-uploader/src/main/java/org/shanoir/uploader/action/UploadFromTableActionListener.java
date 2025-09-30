package org.shanoir.uploader.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.shanoir.ng.importer.dicom.query.DicomQuery;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.PatientVerification;
import org.shanoir.uploader.gui.ImportFromTableWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UploadFromTableActionListener implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(UploadFromTableActionListener.class);

    private static SimpleDateFormat dicomStudyDateFormat = new SimpleDateFormat("yyyymmdd");

    private static SimpleDateFormat birthDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private JFileChooser fileChooser;
    private ImportFromTableWindow importFromTableWindow;
    private ResourceBundle resourceBundle;

    public UploadFromTableActionListener(ImportFromTableWindow importFromTableWindow, ResourceBundle resourceBundle) {
        this.importFromTableWindow = importFromTableWindow;
        this.fileChooser = new JFileChooser();
        // Create a file filter for .xlsx files
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel files", "xlsx", "xls");
        fileChooser.setFileFilter(filter);
        this.resourceBundle = resourceBundle;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int result = fileChooser.showOpenDialog(importFromTableWindow);
        if (result == JFileChooser.APPROVE_OPTION) {
            readImportJobsFromFile(fileChooser.getSelectedFile());
        }
    }

    /**
     * Displays a table file, and checks its integrity.
     *
     * @param selectedFile the selected table file
     */
    private void readImportJobsFromFile(File selectedFile) {
        Map<String, ImportJob> importJobs = new LinkedHashMap<String, ImportJob>(10000);
        try (XSSFWorkbook myWorkBook = new XSSFWorkbook(selectedFile)) {
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);
            Iterator<Row> rowIterator = mySheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                int rowNumber = row.getRowNum();
                // rowNumber == 0 is the header line in bold
                if (rowNumber == 0) {
                    // do nothing with header
                } else {
                    addImportJob(importJobs, row);
                }
            }
        } catch (InvalidFormatException | IOException | IllegalStateException e) {
            logger.error("Error while parsing the input file: ", e);
            this.importFromTableWindow.displayError(resourceBundle.getString("shanoir.uploader.import.table.error.csv"));
            return;
        }
        logger.info(importJobs.entrySet().size() + " import jobs (== DICOM studies/examinations) read from table.");
        this.importFromTableWindow.displayImportJobs(importJobs);
    }

    private void addImportJob(Map<String, ImportJob> importJobs, Row row) {
        int rowNumber = row.getRowNum();
        ImportJob importJob = new ImportJob();
        importJob.setFromShanoirUploader(true);
        importJobs.put(String.valueOf(rowNumber), importJob);
        readDicomQuery(row, importJob);
        readPatientVerification(row, importJob);
        readImportJob(row, importJob);
    }

    private void readPatientVerification(Row row, ImportJob importJob) {
        PatientVerification patientVerification = new PatientVerification();
        Cell firstName = row.getCell(10);
        String value = handleCell(firstName);
        patientVerification.setFirstName(value);
        Cell lastName = row.getCell(11);
        value = handleCell(lastName);
        patientVerification.setLastName(value);
        Cell birthName = row.getCell(12);
        value = handleCell(birthName);
        patientVerification.setBirthName(value);
        Cell birthDate = row.getCell(13);
        value = handleCell(birthDate, true);
        patientVerification.setBirthDate(value);
        importJob.setPatientVerification(patientVerification);
    }

    private void readImportJob(Row row, ImportJob importJob) {
        Cell studyCardName = row.getCell(14);
        String value = handleCell(studyCardName);
        importJob.setStudyCardName(value);
        Cell subjectName = row.getCell(15);
        value = handleCell(subjectName);
        importJob.setSubjectName(value);
        Cell examComment = row.getCell(16);
        value = handleCell(examComment);
        importJob.setExaminationComment(value);
    }

    private void readDicomQuery(Row row, ImportJob importJob) {
        DicomQuery dicomQuery = new DicomQuery();
        Cell dicomQueryLevel = row.getCell(0);
        String value = handleCell(dicomQueryLevel);
        if ("STUDY".equals(value)) {
            dicomQuery.setStudyRootQuery(true);
        }
        Cell dicomPatientName = row.getCell(1);
        value = handleCell(dicomPatientName);
        dicomQuery.setPatientName(value);
        Cell dicomPatientID = row.getCell(2);
        value = handleCell(dicomPatientID);
        dicomQuery.setPatientID(value);
        Cell dicomPatientBirthDate = row.getCell(3);
        value = handleCell(dicomPatientBirthDate);
        dicomQuery.setPatientBirthDate(value);
        Cell dicomStudyDescription = row.getCell(4);
        value = handleCell(dicomStudyDescription);
        dicomQuery.setStudyDescription(value);
        Cell dicomStudyDate = row.getCell(5);
        value = handleCell(dicomStudyDate);
        dicomQuery.setStudyDate(value);
        Cell dicomModality = row.getCell(6);
        value = handleCell(dicomModality);
        dicomQuery.setModality(value);
        Cell dicomStudyFilter = row.getCell(7);
        value = handleCell(dicomStudyFilter);
        dicomQuery.setStudyFilter(value);
        Cell dicomMinStudyDateFilter = row.getCell(8);
        value = handleCell(dicomMinStudyDateFilter);
        dicomQuery.setMinStudyDateFilter(value);
        Cell dicomSerieFilter = row.getCell(9);
        value = handleCell(dicomSerieFilter);
        dicomQuery.setSerieFilter(value);
        importJob.setDicomQuery(dicomQuery);
    }

    private String handleCell(Cell cell) {
        return handleCell(cell, false);
    }

    private String handleCell(Cell cell, boolean specialHandling) {
        if (cell != null) {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        Date date = cell.getDateCellValue();
                        if (specialHandling) {
                            return birthDateFormat.format(date);
                        } else {
                            return dicomStudyDateFormat.format(date);
                        }
                    } else {
                        double numericValue = cell.getNumericCellValue();
                        if (numericValue == Math.floor(numericValue)) {
                            return String.valueOf((long) numericValue);
                        } else {
                            return BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
                        }
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    break;
                case BLANK:
                    break;
                case _NONE:
                    break;
                case ERROR:
                    break;
                default:
                    break;
            }
        }
        return "";
    }

}
