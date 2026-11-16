/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.dataset.schedule;

import com.opencsv.CSVWriter;
import jakarta.transaction.Transactional;
import org.apache.commons.collections4.ListUtils;
import org.hibernate.Hibernate;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * This class iterates over all examinations in the database of Shanoir and
 * applies multiple consistency checks on the DICOM data below in the tree. It
 * produces a .csv file with the result of his consistency check on the server.
 * It runs in an intervall and stores the latest analyzed exam in its database.
 * As ongoing imports can create temporarily empty examinations, we only check
 * on examinations older than yesterday, not from today.
 *
 * StudyInstanceUIDs of examinations should be unique by default, but are not
 * unique in the database to cover the feature "copy datasets", where a clone
 * examination, should still point to the same DICOMs and therefore has the
 * same StudyInstanceUID.
 *
 * The following is checked:
 *
 * "ExaminationID", "ExaminationDate", "Today?", "Empty?", "#Files",
 * "StudyInstanceUID", "Multiple?", "Unique?"
 *
 * Today - do not touch, in case ongoing import
 * #Files - number of dataset_files in pacs (only check dicoms)
 * Multiple - does the examination, after multiple imports contains multiple
 *                 StudyInstanceUIDs
 * Unique - is the StudyInstanceUID unique
 *
 * @author mkain
 *
 */
@Service
public class DatasetConsistencyChecker {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetConsistencyChecker.class);

    @Value("${logging.file.name}")
    private String loggingFileName;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private DatasetConsistencyChecker self;

    private static final String DCC_CSV = "dcc.csv";

    private static final Integer PARTITION_SIZE = 10000;

    @Scheduled(fixedDelay = 2 * 60 * 60 * 1000) // Run every 2 hours (in milliseconds)
    public void checkNewImports() {
        check(datasetRepository.findNewIds());
    }

    public void checkPastImports() {
        check(datasetRepository.findNotCheckedIds());
    }

    public void check(List<Long> datasetIdList) {
        try {
            LOG.info("---------------");
            LOG.info("---------------");
            LOG.info("START...");
            long startTime = System.currentTimeMillis();
            long datasetsCount = datasetIdList.size();
            LOG.info("Datasets count: " + datasetsCount);
            Integer inconsistentCount = 0;

            if (!datasetIdList.isEmpty()) {
                List<Dataset> datasets = datasetRepository.findByIdsWithDatasetFiles(datasetIdList);
                try {
                    inconsistentCount = checkDatasets(datasets);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            LOG.info("---------------");
            LOG.info("Summary: total datasets checked: " + datasetIdList.size());
            LOG.info("Summary: time required for entire check: " + duration + " milliseconds.");
            LOG.info("Summary: number of inconsistent datasets: " + inconsistentCount);
            LOG.info("STOP...");
            LOG.info("---------------");
            LOG.info("---------------");
        } catch (Exception e) {
            LOG.info("STOPPED with exception...");
            LOG.error(e.getMessage(), e);
        }
    }


    private Integer checkDatasets(List<Dataset> datasetsToCheck) throws IOException {
        if (!datasetsToCheck.isEmpty()) {
            File datasetsLogFile = new File(loggingFileName);
            if (datasetsLogFile.exists()) {
                File parent = datasetsLogFile.getParentFile();
                File csvFile = new File(parent.getAbsolutePath() + File.separator + DCC_CSV);
                boolean newCSVFileCreated = csvFile.createNewFile();
                try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile, true))) {
                    if (newCSVFileCreated) {
                        String[] header = {"DatasetId", "DatasetDate", "#Files", "InPacs", "#FilesNotInPacs"};
                        writer.writeNext(header);
                    }

                    Integer inconsistentCount = 0;

                    for (List<Dataset> partition : ListUtils.partition(datasetsToCheck,PARTITION_SIZE)) {
                        inconsistentCount += self.checkDatasetPartition(partition, writer);
                    }
                    return inconsistentCount;
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            } else {
                LOG.error("Log file of datasets not found.");
            }
        } else {
            LOG.info("No new datasets found.");
        }
        return 0;
    }

    @Transactional
    protected Integer checkDatasetPartition(List<Dataset> partition, CSVWriter writer) {
        Integer inconsistentCount = 0;
        for(Dataset dataset : partition) {
            Hibernate.initialize(dataset.getDatasetExpressions());
            checkDataset(dataset, writer);
            if(!dataset.isConsistent()) {
                inconsistentCount++;
            }
        }
        datasetRepository.saveAll(partition);
        return inconsistentCount;
    }

    private void checkDataset(Dataset dataset, CSVWriter writer) {
        LOG.debug("Processing dataset with ID: " + dataset.getId());
        long startTime = System.currentTimeMillis();
        String[] line = new String[8];
        line[0] = dataset.getId().toString();
        line[1] = dataset.getCreationDate().toString();
        List<String> filesInPACS = new ArrayList<String>();

        checkPACSDataset(dataset, filesInPACS);

        int sliceCount = dataset.getDatasetExpressions().stream().mapToInt(de -> de.getDatasetFiles().size()).sum();
        line[2] = Integer.toString(sliceCount);
        line[3] = Boolean.toString(dataset.isConsistent());
        line[4] = Integer.toString(sliceCount - filesInPACS.size());

        LOG.debug("Dataset {}: references {} files in PACS.", dataset.getId(), filesInPACS.size());

        writer.writeNext(line);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        LOG.debug("Time required for dataset check: " + duration + " milliseconds.");
    }

    private void checkPACSDataset(Dataset dataset, List<String> filesInPACS) {
        List<DatasetExpression> expressions = dataset.getDatasetExpressions();
        dataset.setConsistent(true);

        if (expressions != null && !expressions.isEmpty()) {
            expressions.forEach(e -> {
                if (DatasetExpressionFormat.DICOM.equals(e.getDatasetExpressionFormat())) {
                    if(!checkExpression(e, filesInPACS)) {
                        dataset.setConsistent(false);
                    }
                }
            });
        }
    }

    private boolean checkExpression(DatasetExpression expression, List<String> filesInPACS) {
        List<DatasetFile> files = expression.getDatasetFiles();
        boolean inPacs = true;

        if (files != null && !files.isEmpty()) {
            for (DatasetFile f : files) {
                if (f.isPacs()) {
                    synchronized (filesInPACS) {
                        filesInPACS.add(f.getPath());
                    }
                } else {
                    inPacs = false;
                }
            }
        }
        return inPacs;
    }
}
