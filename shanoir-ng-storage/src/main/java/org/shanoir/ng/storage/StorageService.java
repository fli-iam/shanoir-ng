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

package org.shanoir.ng.storage;

import java.io.InputStream;

import org.springframework.core.io.Resource;

public interface StorageService {

    public static final String STUDY = "study-";

    public static final String SUBJECT = "sub-";

    public static final String EXAMINATION = "examination-";

    public static final String SESSION = "ses-";

    public static final String PATHOLOGY_MODEL = "pathology-model-";

    public static final String UNUSED = "UNUSED";

    public static final String SLASH = "/";

    String storeStudyData(Long studyId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException;

    String storeExtraData(Long examinationId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException;

    String storeBIDSData(Long studyId, String subjectName, Long examinationId, String fileName,
            String dataTypeBIDS, InputStream inputStream, String contentType, long size)
            throws StorageException;

    public String storePreclinicalExtraData(Long examinationId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException;

    String storePathologyModelData(Long pathologyModelId, String fileName,
            InputStream inputStream, String contentType, long size)
            throws StorageException;

    Resource loadStudyData(Long studyId, String fileName) throws StorageException;

    Resource loadExtraData(Long examinationId, String fileName) throws StorageException;

    Resource loadPreclinicalExtraData(Long examinationId, String fileName) throws StorageException;

    Resource loadPathologyModelData(Long pathologyModelId, String fileName) throws StorageException;

    long getFileSizeExtraData(Long examinationId, String fileName) throws StorageException;

    String getPublicLocationStudies(String directory, String fileName) throws StorageException;

    String getPublicLocationDatasets(String directory, String fileName) throws StorageException;

    String getPublicLocationPreclinical(String directory, String fileName) throws StorageException;

    void deleteStudyData(Long studyId, String fileName) throws StorageException;

    void deleteExtraData(Long examinationId, String fileName) throws StorageException;

    void deletePreclinicalExtraData(Long examinationId, String fileName) throws StorageException;

    void deletePathologyModelData(Long pathologyModelId, String fileName) throws StorageException;

    void deleteDirectoryStudyData(Long studyId) throws StorageException;

    void deleteDirectoryExtraData(Long examinationId) throws StorageException;

    void deleteDirectoryPreclinicalExtraData(Long examinationId) throws StorageException;

    void deleteDirectoryDatasets(String directory) throws StorageException;

    void deleteDirectoryPreclinical(String directory) throws StorageException;

    void moveDatasets(String directory, String sourceFileName, String targetFileName) throws StorageException;

    void moveStudyData(Long studyId, String sourceFileName, String targetFileName) throws StorageException;

}
