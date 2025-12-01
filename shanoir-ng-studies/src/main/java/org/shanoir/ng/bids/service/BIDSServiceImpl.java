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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.shanoir.ng.study.dto.DatasetDescription;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
@Service
public class BIDSServiceImpl implements BIDSService {

    private static final Logger LOG = LoggerFactory.getLogger(BIDSServiceImpl.class);
    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Value("${bids-data-folder}")
    private String bidsStorageDir;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String STUDY_PREFIX = "stud-";

    private static final String SUBJECT_PREFIX = "sub-";

    private static final String SUBJECT_IDENTIFIER = "subject_identifier";

    private static final String PARTICIPANT_ID = "participant_id";

    private static final String SUBJECT_AGE = "subject_age";

    private static final String CSV_SEPARATOR = "\t";

    private static final String CSV_SPLITTER = "\n";

    private static final String[] CSV_PARTICIPANTS_HEADER = {
            PARTICIPANT_ID,
            SUBJECT_IDENTIFIER,
            SUBJECT_AGE
    };

    private static final String DATASET_DESCRIPTION_FILE = "dataset_description.json";

    private static final String README_FILE = "README";


    public ResponseEntity<ByteArrayResource> generateParticipantsTsv(final Long studyId) throws IOException {
        List<Subject> subjs = getSubjectsForStudy(studyId);
        StringBuilder data = participantsSerializer(subjs);

        ByteArrayResource resource = new ByteArrayResource(data.toString().getBytes());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename" + "participants.tsv")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .contentLength(data.length())
                .body(resource);
    }


    @Override
    public void generateParticipantsTsvFile(Long studyId) throws IOException {
        Study study = studyRepository.findById(studyId).orElse(null);
        File workFolder = getBidsFolderpath(studyId, study.getName());
        File baseDir = createBaseBidsFolder(workFolder, study.getName());
        File csvFile = new File(baseDir.getAbsolutePath() + File.separator + "participants.tsv");

        List<Subject> subjs = getSubjectsForStudy(studyId);

        if (csvFile.exists()) {
            // Recreate it everytime
            FileUtils.deleteQuietly(csvFile);
        }
        StringBuilder buffer = participantsSerializer(subjs);

        try {
            Files.write(Paths.get(csvFile.getAbsolutePath()), buffer.toString().getBytes());
        } catch (IOException e) {
            LOG.error("Error while creating particpants.tsv file: {}", e);
        }
    }

    public StringBuilder participantsSerializer(List<Subject> subjs) {
        int index = 1;
        StringBuilder buffer =  new StringBuilder();
        // Headers
        for (String columnHeader : CSV_PARTICIPANTS_HEADER) {
            buffer.append(columnHeader).append(CSV_SEPARATOR);
        }
        buffer.append(CSV_SPLITTER);

        for (Subject subject : subjs) {
            String subjectName = subject.getName();
            String subjectAge = ageCalculation(subject);
            subjectName = this.formatLabel(subjectName);
            // Write in the file the values
            buffer.append(SUBJECT_PREFIX).append(index++).append("_").append(subjectName).append(CSV_SEPARATOR)
                    .append(subject.getId()).append(CSV_SEPARATOR)
                    .append(subjectAge).append(CSV_SEPARATOR)
                    .append(CSV_SPLITTER);
        }

        return buffer;
    }

    private String formatLabel(String label) {
        return label.replaceAll("[^a-zA-Z0-9]+", "");
    }

    private List<Subject> getSubjectsForStudy(final Long studyId) throws JsonParseException, JsonMappingException, IOException {
        // Get the list of subjects
        List<Subject> subjects = subjectRepository.findByStudy_Id(studyId);
        return subjects;
    }

    public File getBidsFolderpath(final Long studyId, String studyName) {
        studyName = this.formatLabel(studyName);
        String tmpFilePath = bidsStorageDir + File.separator + STUDY_PREFIX + studyId + studyName;
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
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

        return workFolder;
    }

    private String ageCalculation(Subject subject) {
        java.time.LocalDate bd = subject.getBirthDate();
        LocalDate birthDate = new LocalDate(bd.getYear(), bd.getMonthValue(), bd.getDayOfMonth());
        LocalDate now = new LocalDate();
        Years age = Years.yearsBetween(birthDate, now);
        System.out.println("age = " + age.get(DurationFieldType.years()));
        return String.valueOf(age.get(DurationFieldType.years()));
    }

}
