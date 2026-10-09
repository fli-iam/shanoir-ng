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

package org.shanoir.ng.bids.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.shanoir.ng.storage.StorageService;
import org.shanoir.ng.study.dto.DatasetDescription;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class BIDSServiceImpl implements BIDSService {

    private static final Logger LOG = LoggerFactory.getLogger(BIDSServiceImpl.class);

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Value("${storage.file-system.bids-data}")
    private String bidsStorageDir;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String SUBJECT_IDENTIFIER = "subject_identifier";

    private static final String PARTICIPANT_ID = "participant_id";

    private static final String SUBJECT_AGE = "subject_age";

    private static final String SUBJECT_SEX = "subject_sex";

    private static final String CSV_SEPARATOR = "\t";

    private static final String CSV_SPLITTER = "\n";

    private static final String[] CSV_PARTICIPANTS_HEADER = {
            PARTICIPANT_ID,
            SUBJECT_IDENTIFIER,
            SUBJECT_AGE,
            SUBJECT_SEX
    };

    private static final String DATASET_DESCRIPTION_FILE = "dataset_description.json";

    private static final String README_FILE = "README";

    @Override
    public String generateParticipantsTsvFile(Long studyId) throws IOException {
        Study study = studyRepository.findById(studyId).orElse(null);
        File workFolder = getBidsFolderPath(studyId);
        File baseDir = createBaseBidsFolder(workFolder, study.getName());
        File csvFile = new File(baseDir.getAbsolutePath() + File.separator + "participants.tsv");

        if (csvFile.exists()) {
            // Recreate it everytime
            FileUtils.deleteQuietly(csvFile);
        }
        String participants = participantsTsv(studyId);

        try {
            Files.write(Path.of(csvFile.getAbsolutePath()), participants.getBytes());
        } catch (IOException e) {
            LOG.error("Error while creating particpants.tsv file: {}", e);
        }
        return participants;
    }

    @Override
    public String participantsTsv(Long studyId) {
        return participantsSerializer(subjectRepository.findByStudy_Id(studyId)).toString();
    }

    public StringBuilder participantsSerializer(List<Subject> subjects) {
        StringBuilder buffer =  new StringBuilder();
        // Headers. Columns are separated, not terminated, by tabs: a trailing tab would read as an empty column.
        buffer.append(String.join(CSV_SEPARATOR, CSV_PARTICIPANTS_HEADER)).append(CSV_SPLITTER);

        for (Subject subject : subjects) {
            String subjectAge = ageCalculation(subject);
            String subjectSex = subject.getSex() != null ? subject.getSex().name() : "O";
            // Write in the file the values
            buffer.append(String.join(CSV_SEPARATOR,
                    StorageService.SUBJECT + subject.getId(),
                    String.valueOf(subject.getId()),
                    subjectAge,
                    subjectSex))
                    .append(CSV_SPLITTER);
        }

        return buffer;
    }

    public File getBidsFolderPath(final Long studyId) {
        String tmpFilePath = bidsStorageDir + File.separator + StorageService.STUDY + studyId;
        return new File(tmpFilePath);
    }

    private File createBaseBidsFolder(File workFolder, String studyName) {
        workFolder.mkdirs();
        // 2. Create dataset_description.json and README
        DatasetDescription datasetDescription = new DatasetDescription();
        datasetDescription.setName(studyName);
        try {
            objectMapper.writeValue(new File(workFolder.getAbsolutePath() + File.separator + DATASET_DESCRIPTION_FILE), datasetDescription);
            objectMapper.writeValue(new File(workFolder.getAbsolutePath() + File.separator + README_FILE), studyName);
        } catch (JacksonException e) {
            LOG.error(e.getMessage());
        }
        return workFolder;
    }

    private String ageCalculation(Subject subject) {
        java.time.LocalDate bd = subject.getBirthDate();
        if (bd == null) {
            return "n/a";
        }
        LocalDate birthDate = new LocalDate(bd.getYear(), bd.getMonthValue(), bd.getDayOfMonth());
        LocalDate now = new LocalDate();
        Years age = Years.yearsBetween(birthDate, now);
        return String.valueOf(age.get(DurationFieldType.years()));
    }

}
