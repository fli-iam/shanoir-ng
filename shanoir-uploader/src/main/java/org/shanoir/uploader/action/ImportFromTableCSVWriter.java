package org.shanoir.uploader.action;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.shanoir.uploader.ShUpOnloadConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVWriter;

public class ImportFromTableCSVWriter {

    private static final Logger LOG = LoggerFactory.getLogger(ImportFromTableCSVWriter.class);

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    private static final String MASS_IMPORT_EXAMINATIONS = "mass_import_examinations_";

    private static final String CSV = ".csv";

    private static final String[] HEADER = {"FirstName", "LastName", "BirthName", "BirthDate",
        "SubjectName", "ExaminationDate", "Error"};

    private File examinationsOK;

    private File examinationsKO;

    public ImportFromTableCSVWriter() {
        File workFolder = ShUpOnloadConfig.getWorkFolder();
        Date now = new Date();
        String timestamp = SDF.format(now);
        String csvFileName = MASS_IMPORT_EXAMINATIONS + timestamp + "_OK" + CSV;
        this.examinationsOK = createCSVFile(workFolder, csvFileName);
        csvFileName = MASS_IMPORT_EXAMINATIONS + timestamp + "_KO" + CSV;
        this.examinationsKO = createCSVFile(workFolder, csvFileName);
    }

    private File createCSVFile(File workFolder, String csvFileName) {
        File csvFile = new File(workFolder.getAbsolutePath() + File.separator + csvFileName);
        if (!csvFile.exists()) {
            try {
                csvFile.createNewFile();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        // Create header by default
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile))) {
            writer.writeNext(HEADER);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return csvFile;
    }

    public void addExaminationLine(boolean ok, String[] line) {
        File csvFile;
        if (ok) {
            csvFile = examinationsOK;
        } else {
            csvFile = examinationsKO;
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile, true))) {
            writer.writeNext(line);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
