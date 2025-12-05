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

package org.shanoir.ng.study.controler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.dto.IdNameCenterStudyDTO;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.dto.StudyLightDTO;
import org.shanoir.ng.study.dto.StudyStatisticsDTO;
import org.shanoir.ng.study.dto.StudyStorageVolumeDTO;
import org.shanoir.ng.study.dto.mapper.StudyMapper;
import org.shanoir.ng.study.dua.DataUserAgreement;
import org.shanoir.ng.study.dua.DataUserAgreementService;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.security.StudyFieldEditionSecurityManager;
import org.shanoir.ng.study.service.RelatedDatasetService;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.study.service.StudyUniqueConstraintManager;
import org.shanoir.ng.study.service.StudyUserService;
import org.shanoir.ng.tag.model.Tag;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Controller
public class StudyApiController implements StudyApi {

    private static final String PDF_EXTENSION = ".pdf";

    @Value("${studies-data}")
    private String dataDir;

    @Autowired
    private StudyService studyService;

    @Autowired
    private StudyMapper studyMapper;

    @Autowired
    private StudyFieldEditionSecurityManager fieldEditionSecurityManager;

    @Autowired
    private StudyUniqueConstraintManager uniqueConstraintManager;

    @Autowired
    private StudyUserService studyUserService;

    @Autowired
    private DataUserAgreementService dataUserAgreementService;

    @Autowired
    private ShanoirEventService eventService;

    @Autowired
    private RelatedDatasetService relatedDatasetService;

    private static final Logger LOG = LoggerFactory.getLogger(StudyApiController.class);

    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public StudyApiController(final HttpServletRequest request) {
        this.request = request;
    }

    @Override
    @Transactional
    public ResponseEntity<Void> deleteStudy(@PathVariable("studyId") Long studyId) {
        try {
            Study study = studyService.findById(studyId);
            if (study == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            if (study.getExaminations() != null && !study.getExaminations().isEmpty()) {
                // Error => should not be able to do this see #793
                return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
            }

            List<DataUserAgreement> duas = dataUserAgreementService.findDUAByStudyId(studyId);
            if (!CollectionUtils.isEmpty(duas)) {
                this.dataUserAgreementService.deleteAll(duas);
            }

            // Delete all linked files and DUA
            File studyFolder = new File(studyService.getStudyFilePath(studyId, ""));
            if (studyFolder.exists()) {
                FileUtils.deleteDirectory(studyFolder);
            }
            studyService.deleteById(studyId);
            eventService.publishEvent(new ShanoirEvent(ShanoirEventType.DELETE_STUDY_EVENT, studyId.toString(),
                    KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS, studyId));
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            LOG.error("Error while deleting protocol file {}", e);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<List<StudyDTO>> findStudies() {
        List<Study> studies = studyService.findAll();
        if (studies.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(studyMapper.studiesToStudyDTOs(studies), HttpStatus.OK);
    }

    public ResponseEntity<List<StudyLightDTO>> findStudiesLight() {
        List<Study> studies = studyService.findAll();
        if (studies.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(studyMapper.studiesToStudyLightDTOs(studies), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<IdName>> findStudiesNames() throws RestServiceException {
        List<IdName> studiesDTO = studyService.findAllNames();
        if (studiesDTO.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(studiesDTO, HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<List<IdNameCenterStudyDTO>> findStudiesNamesAndCenters() throws RestServiceException {
        List<IdNameCenterStudyDTO> studiesDTO = new ArrayList<>();
        final List<Study> studies = studyService.findAll();
        if (studies.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        for (Study study : studies) {
            studiesDTO.add(studyMapper.studyToExtendedIdNameDTO(study));
        }
        return new ResponseEntity<>(studiesDTO, HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<StudyDTO> findStudyById(@PathVariable("studyId") final Long studyId,
            boolean withStorageVolume) {
        Study study = studyService.findById(studyId);
        if (study == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        StudyDTO dto = studyMapper.studyToStudyDTODetailed(study);
        if (withStorageVolume) {
            dto.setStorageVolume(studyService.getDetailedStorageVolume(dto.getId()));
        }
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<StudyDTO> saveNewStudy(@RequestBody final Study study, final BindingResult result)
            throws RestServiceException {

        validate(study, result);

        Study createdStudy;
        try {
            addCurrentUserAsStudyUserIfEmptyStudyUsers(study);
            createdStudy = studyService.create(study);
            eventService.publishEvent(new ShanoirEvent(ShanoirEventType.CREATE_STUDY_EVENT,
                    createdStudy.getId().toString(), KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS));
        } catch (MicroServiceCommunicationException e) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Microservice communication error", e));
        }
        return new ResponseEntity<>(studyMapper.studyToStudyDTO(createdStudy), HttpStatus.OK);
    }

    private void addCurrentUserAsStudyUserIfEmptyStudyUsers(final Study study) {
        if (study.getStudyUserList() == null) {
            List<StudyUser> studyUserList = new ArrayList<StudyUser>();
            StudyUser studyUser = new StudyUser();
            studyUser.setStudy(study);
            studyUser.setUserId(KeycloakUtil.getTokenUserId());
            studyUser.setUserName(KeycloakUtil.getTokenUserName());
            studyUser.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_DOWNLOAD,
                    StudyUserRight.CAN_IMPORT, StudyUserRight.CAN_ADMINISTRATE));
            studyUserList.add(studyUser);
            study.setStudyUserList(studyUserList);
        }
    }

    @Override
    public ResponseEntity<String> copyDatasetsToStudy(
            @Parameter(description = "Dataset ids to copy", required = true) @RequestParam(value = "datasetIds", required = true) List<Long> datasetIds,
            @Parameter(description = "Study id to copy in", required = true) @RequestParam(value = "studyId", required = true) String studyIdAsStr,
            @Parameter(description = "center id of datasets", required = true) @RequestParam(value = "centerIds", required = true) List<Long> centerIds,
            @Parameter(description = "subject id of datasets", required = true) @RequestParam(value = "subjectIdStudyId", required = true) List<String> subjectIdStudyId) {
        String res = null;
        try {
            Long studyId = Long.valueOf(studyIdAsStr);
            relatedDatasetService.createSubjectsInTargetStudy(subjectIdStudyId, studyId);
            res = relatedDatasetService.addCenterAndCopyDatasetToStudy(datasetIds, studyId, centerIds);
        } catch (Exception e) {
            LOG.error("Error during copy for datasetsIds : " + datasetIds + ", studyId : " + studyIdAsStr
                    + ", centersId : " + centerIds + ". Error : ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<StudyStorageVolumeDTO> getDetailedStorageVolume(@PathVariable("studyId") final Long studyId)
            throws RestServiceException {
        StudyStorageVolumeDTO dto = studyService.getDetailedStorageVolume(studyId);
        if (dto == null) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Error while fetching study datasets storage volume details.", null));
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Map<Long, StudyStorageVolumeDTO>> getDetailedStorageVolumeByStudy(List<Long> studyIds) {
        return new ResponseEntity<>(studyService.getDetailedStorageVolumeByStudy(studyIds), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateStudy(@PathVariable("studyId") final Long studyId, @RequestBody final Study study,
            final BindingResult result) throws RestServiceException {

        validate(study, result);

        try {
            studyService.update(study);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (ShanoirException e) {
            throw new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage(), e));
        }

        eventService.publishEvent(new ShanoirEvent(ShanoirEventType.UPDATE_STUDY_EVENT, studyId.toString(),
                KeycloakUtil.getTokenUserId(), "", ShanoirEvent.SUCCESS, studyId));

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<List<StudyUserRight>> rights(@PathVariable("studyId") final Long studyId)
            throws RestServiceException {
        List<StudyUserRight> rights = this.studyUserService.getRightsForStudy(studyId);
        if (!rights.isEmpty()) {
            return new ResponseEntity<>(rights, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @Override
    public ResponseEntity<List<Tag>> tags(@PathVariable("studyId") final Long studyId)
            throws RestServiceException {
        if (studyId != null) {
            List<Tag> tags = this.studyService.getTagsFromStudy(studyId);
            if (!tags.isEmpty()) {
                return new ResponseEntity<>(tags, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @Override
    public ResponseEntity<Map<Long, List<StudyUserRight>>> rights() throws RestServiceException {
        Map<Long, List<StudyUserRight>> rights = this.studyUserService.getRights();
        if (!rights.isEmpty()) {
            return new ResponseEntity<>(rights, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @Override
    public ResponseEntity<Boolean> hasOneStudyToImport() throws RestServiceException {
        boolean hasOneStudy = this.studyUserService.hasOneStudyToImport();
        return new ResponseEntity<>(hasOneStudy, HttpStatus.OK);
    }

    @Override
    public void downloadProtocolFile(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
            @Parameter(description = "file to download", required = true) @PathVariable("fileName") String fileName,
            HttpServletResponse response) throws RestServiceException, IOException {
        String filePath = studyService.getStudyFilePath(studyId, fileName);
        LOG.info("Retrieving file : {}", filePath);
        File fileToDownLoad = new File(filePath);
        if (!fileToDownLoad.exists()) {
            response.sendError(HttpStatus.NO_CONTENT.value());
            return;
        }
        try (InputStream is = new FileInputStream(fileToDownLoad);) {
            response.setHeader("Content-Disposition", "attachment;filename = " + fileToDownLoad.getName());
            response.setContentType(request.getServletContext().getMimeType(fileToDownLoad.getAbsolutePath()));
            response.setContentLengthLong(fileToDownLoad.length());
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        }
    }

    @Override
    public ResponseEntity<Void> uploadProtocolFile(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
            @Parameter(description = "file to upload", required = true) @Valid @RequestBody MultipartFile file)
            throws RestServiceException {
        try {
            String parentDir = dataDir + "/study-" + studyId;
            Path path = Paths.get(parentDir);
            Files.createDirectories(path);
            LOG.info("path: {}", path.getFileName());
            Path newFilePath = Paths.get(parentDir + "/" + file.getOriginalFilename());
            Files.createFile(newFilePath);
            LOG.info("newFilePath: {}", newFilePath.getFileName());
            file.transferTo(newFilePath);
        } catch (Exception e) {
            LOG.error("Error while loading files on examination: {}. File not uploaded. {}", studyId, e);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private void validate(Study study, BindingResult result) throws RestServiceException {
        final FieldErrorMap errors = new FieldErrorMap().add(fieldEditionSecurityManager.validate(study))
                .add(new FieldErrorMap(result)).add(uniqueConstraintManager.validate(study));
        if (!errors.isEmpty()) {
            ErrorModel error = new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments",
                    new ErrorDetails(errors));
            throw new RestServiceException(error);
        }
    }

    public static File getUserDir(String importDir) {
        final Long userId = KeycloakUtil.getTokenUserId();
        final String userImportDirFilePath = importDir + File.separator + Long.toString(userId);
        final File userImportDir = new File(userImportDirFilePath);
        if (!userImportDir.exists()) {
            userImportDir.mkdirs(); // create if not yet existing
        } // else is wanted case, user has already its import directory
        return userImportDir;
    }

    @Override
    public ResponseEntity<List<DataUserAgreement>> getDataUserAgreements() throws RestServiceException, IOException {
        Long userId = KeycloakUtil.getTokenUserId();
        List<DataUserAgreement> dataUserAgreements = this.dataUserAgreementService
                .getDataUserAgreementsByUserId(userId);
        if (!dataUserAgreements.isEmpty()) {
            return new ResponseEntity<>(dataUserAgreements, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @Override
    public ResponseEntity<Boolean> hasDUAByStudyId(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId)
            throws ShanoirException {

        DataUserAgreement dua = this.dataUserAgreementService.findDUAByUserIdAndStudyId(KeycloakUtil.getTokenUserId(),
                studyId);

        return new ResponseEntity<>(dua != null, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> acceptDataUserAgreement(
            @Parameter(description = "id of the dua", required = true) @PathVariable("duaId") Long duaId)
            throws RestServiceException, MicroServiceCommunicationException {
        try {
            this.dataUserAgreementService.acceptDataUserAgreement(duaId);
        } catch (ShanoirException e) {
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null));
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> uploadDataUserAgreement(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
            @Parameter(description = "dua to upload", required = true) @Valid @RequestBody MultipartFile file)
            throws RestServiceException {
        try {
            if (!file.getOriginalFilename().endsWith(PDF_EXTENSION) || file.getSize() > 50000000) {
                LOG.error("Could not upload the file: {}", file.getOriginalFilename());
                // Clean up: delete from study in case upload not allowed
                Study study = studyService.findById(studyId);
                if (!CollectionUtils.isEmpty(study.getDataUserAgreementPaths())) {
                    study.getDataUserAgreementPaths().remove(file.getName());
                }
                studyService.update(study);
                return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
            }
            String duaFilePath = this.studyService.getStudyFilePath(studyId, file.getOriginalFilename());

            Path duaPath = Paths.get(duaFilePath);
            Files.createDirectories(duaPath.getParent());
            Files.createFile(duaPath);
            file.transferTo(duaPath);
        } catch (Exception e) {
            LOG.error("Error while loading files on study: {}. File not uploaded.", studyId, e);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public void downloadDataUserAgreement(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
            @Parameter(description = "file to download", required = true) @PathVariable("fileName") String fileName,
            HttpServletResponse response) throws RestServiceException, IOException {
        String filePath = studyService.getStudyFilePath(studyId, fileName);
        LOG.info("Retrieving file : {}", filePath);
        File fileToDownLoad = new File(filePath);
        if (!fileToDownLoad.exists()) {
            response.sendError(HttpStatus.NO_CONTENT.value());
            return;
        }
        try (InputStream is = new FileInputStream(fileToDownLoad);) {
            response.setHeader("Content-Disposition", "attachment;filename = " + fileToDownLoad.getName());
            response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        }
    }

    @Override
    public ResponseEntity<Void> deleteStudyUser(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId,
            @Parameter(description = "id of the userId", required = true) @PathVariable("userId") Long userId)
            throws IOException {
        studyService.removeStudyUserFromStudy(studyId, userId);
        List<StudyUserRight> surList = studyUserService.getRightsForStudy(studyId);

        if (surList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<List<IdName>> findPublicStudiesConnected() {
        List<IdName> studiesDTO = new ArrayList<>();

        List<Study> studies = studyService.findPublicStudies();
        if (studies.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        studies = filterStudies(studies, KeycloakUtil.getTokenUserId());

        for (Study study : studies) {
            studiesDTO.add(studyMapper.studyToIdNameDTO(study));
        }
        return new ResponseEntity<>(studiesDTO, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<StudyUser>> getStudyUserByStudyId(Long studyId) {
        List<StudyUser> studyUserList = this.studyUserService.findStudyUsersByStudyId(studyId);
        if (!studyUserList.isEmpty()) {
            return new ResponseEntity<>(studyUserList, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<List<StudyLightDTO>> findPublicStudiesData() {
        List<StudyLightDTO> studiesDTO = new ArrayList<>();
        List<Study> studies = studyService.findPublicStudies();
        if (studies.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        for (Study study : studies) {
            studiesDTO.add(studyMapper.studyToStudyLightDTONoFilePaths(study));
        }
        return new ResponseEntity<>(studiesDTO, HttpStatus.OK);
    }

    /**
     * This method allows to filter studies by on the one the given user is not part
     * in
     *
     * @param studies     the list of studies to filter
     * @param tokenUserId the user to filter with
     * @return the list of filtered studies
     */
    private List<Study> filterStudies(List<Study> studies, Long tokenUserId) {
        List<Study> filteredStudies = new ArrayList<Study>();
        for (Study study : studies) {
            boolean toFilter = false;
            for (StudyUser su : study.getStudyUserList()) {
                if (tokenUserId.equals(su.getUserId())) {
                    toFilter = true;
                }
            }
            if (!toFilter) {
                filteredStudies.add(study);
            }
        }
        return filteredStudies;
    }

    @Override
    public ResponseEntity<List<StudyStatisticsDTO>> getStudyStatistics(
            @Parameter(description = "id of the study", required = true) @PathVariable("studyId") Long studyId)
            throws RestServiceException, IOException {
        try {
            List<StudyStatisticsDTO> statistics = studyService.queryStudyStatistics(studyId);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(statistics);

        } catch (jakarta.persistence.NoResultException e) {
            throw new RestServiceException(new ErrorModel(HttpStatus.NOT_FOUND.value(), "No result found.", e));
        } catch (Exception e) {
            LOG.error("Error while executing study statistics stored procedure.", e);
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while querying the database.", e));
        }
    }

    @Override
    public ResponseEntity<List<Long>> getStudiesByRightForCurrentUser(
            @Parameter(description = "right requested", required = true) @PathVariable("right") StudyUserRight right)
            throws RestServiceException {
        try {
            List<Long> studies = studyService.queryStudiesByRight(right);

            return new ResponseEntity<>(studies, HttpStatus.OK);

        } catch (jakarta.persistence.NoResultException e) {
            throw new RestServiceException(new ErrorModel(HttpStatus.NOT_FOUND.value(), "No result found.", e));
        } catch (Exception e) {
            LOG.error("Error while executing the request.", e);
            throw new RestServiceException(
                    new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while querying the database.", e));
        }
    }
}
