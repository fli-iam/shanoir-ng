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

package org.shanoir.ng.dua.controller;

import java.util.Optional;

import org.shanoir.ng.dua.dto.DuaDraftCreationWrapperDTO;
import org.shanoir.ng.dua.dto.DuaDraftDTO;
import org.shanoir.ng.dua.dto.mapper.DuaDraftMapper;
import org.shanoir.ng.dua.model.DuaDraft;
import org.shanoir.ng.dua.service.DuaDraftService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.email.DuaDraftWrapper;
import org.shanoir.ng.shared.exception.EntityFoundException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Parameter;

@Controller
public class DuaDraftAPIController implements DuaDraftAPI {

	@Autowired
	DuaDraftService duaDraftService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private DuaDraftMapper mapper;

	@Autowired
	private StudyService studyService;

	@Value("${front.server.url}")
	private String frontServerUrl;


	@Override
	public ResponseEntity<String> saveNew(
			@Parameter(description = "dua draft to create", required = true) @RequestBody DuaDraftCreationWrapperDTO dua, BindingResult result)
			throws RestServiceException {

	    DuaDraft duaEntity = mapper.DuaDraftCreationDTOToDuaDraft(dua.getDuaDraft());
		try {
			DuaDraft created = duaDraftService.create(duaEntity);
			if (dua.getEmail() != null) {
				sendDuaDraftCreationMail(created, dua.getEmail());
			}
			return new ResponseEntity<>(created.getId(), HttpStatus.OK);
		} catch (EntityFoundException ex) {
			return new ResponseEntity<>("This id is already taken", HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (JsonProcessingException ex) {
            return new ResponseEntity<>("The mail could not be sent, json serializing error", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<Void> update(
			@Parameter(description = "id of the draft", required = true) @PathVariable("duaId") String duaId,
			@Parameter(description = "study to update", required = true) @RequestBody DuaDraftDTO dua, BindingResult result)
			throws RestServiceException {

		DuaDraft duaEntity = mapper.DuaDraftDTOToDuaDraft(dua);
		duaEntity.setId(duaId);
		try {
			duaDraftService.update(duaEntity);
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (EntityNotFoundException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}

	@Override
	public ResponseEntity<DuaDraftDTO> findById(
			@Parameter(description = "id of the dua draft", required = true) @PathVariable("duaId") String duaId) {

		Optional<DuaDraft> dua = duaDraftService.findById(duaId);
		if (dua.isPresent()) {
			DuaDraftDTO dto = mapper.duaDraftToDuaDraftDTO(dua.get());
			return new ResponseEntity<>(dto, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

    private void sendDuaDraftCreationMail(DuaDraft duaEntity, String email) throws JsonProcessingException {
		DuaDraftWrapper data = new DuaDraftWrapper();
		String link = frontServerUrl + "/shanoir-ng/dua/view/" + duaEntity.getId();
		data.setDuaLink(link);
		data.setRecipienEmailAddress(email);
		data.setSenderUserId(KeycloakUtil.getTokenUserId());
		rabbitTemplate.convertAndSend(RabbitMQConfiguration.DUA_DRAFT_MAIL_QUEUE, objectMapper.writeValueAsString(data));
    }

}
