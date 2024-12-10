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

package org.shanoir.ng.tag.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.dataset.dto.DatasetDTO;
import org.shanoir.ng.dataset.dto.DatasetWithDependenciesDTOInterface;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.modality.EegDatasetMapper;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.dataset.service.CreateStatisticsService;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.DatasetDownloadError;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.dto.ProcessedDatasetImportJob;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.tag.model.StudyTag;
import org.shanoir.ng.tag.service.StudyTagService;
import org.shanoir.ng.utils.DatasetFileUtils;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Controller
public class StudyTagApiController implements StudyTagApi {

	private static final Logger LOG = LoggerFactory.getLogger(StudyTagApiController.class);

	@Autowired
	private DatasetService datasetService;

	@Autowired
	private StudyTagService studyTagService;

	@Override
	public ResponseEntity<Void> addStudyTagsToDataset(Long datasetId, List<Long> studyTagIds) throws EntityNotFoundException, SolrServerException, IOException {
		Dataset ds = datasetService.findById(datasetId);
		if (Objects.isNull(ds)) {throw new EntityNotFoundException(Dataset.class, datasetId);}

		studyTagService.addStudyTagsToDataset(ds, studyTagIds);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Void> removeStudyTagsFromDataset(Long datasetId, List<Long> studyTagIds) throws EntityNotFoundException, SolrServerException, IOException {
		Dataset ds = datasetService.findById(datasetId);
		if (Objects.isNull(ds)) {throw new EntityNotFoundException(Dataset.class, datasetId);}

		studyTagService.removeStudyTagsFromDataset(ds, studyTagIds);

		return new ResponseEntity<>(HttpStatus.OK);
	}
}
