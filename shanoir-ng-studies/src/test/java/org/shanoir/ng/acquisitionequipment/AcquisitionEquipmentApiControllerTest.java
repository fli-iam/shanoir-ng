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

package org.shanoir.ng.acquisitionequipment;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.acquisitionequipment.controler.AcquisitionEquipmentApiController;
import org.shanoir.ng.acquisitionequipment.dto.AcquisitionEquipmentDTO;
import org.shanoir.ng.acquisitionequipment.dto.mapper.AcquisitionEquipmentMapper;
import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.service.AcquisitionEquipmentService;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.jackson.JacksonUtils;
import org.shanoir.ng.shared.security.ControllerSecurityService;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Unit tests for acquisition equipment controller.
 *
 * @author msimon
 *
 */
@WebMvcTest(AcquisitionEquipmentApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class AcquisitionEquipmentApiControllerTest {

	private static final String REQUEST_PATH = "/acquisitionequipments";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	@Autowired
	private MockMvc mvc;

	@MockBean
	private AcquisitionEquipmentMapper acquisitionEquipmentMapper;

	@MockBean
	private AcquisitionEquipmentService acquisitionEquipmentService;

	@MockBean
	private ShanoirEventService eventService;
	
	@MockBean(name = "controllerSecurityService")
	private ControllerSecurityService controllerSecurityService;

	@BeforeEach
	public void setup() throws EntityNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException  {
		given(acquisitionEquipmentMapper
				.acquisitionEquipmentsToAcquisitionEquipmentDTOs(Mockito.anyList()))
						.willReturn(Arrays.asList(new AcquisitionEquipmentDTO()));
		AcquisitionEquipmentDTO acqEq = new AcquisitionEquipmentDTO();
		acqEq.setId(Long.valueOf(123));
		AcquisitionEquipment equip = new AcquisitionEquipment();
		equip.setId(1L);
		given(acquisitionEquipmentMapper
				.acquisitionEquipmentToAcquisitionEquipmentDTO(Mockito.any(AcquisitionEquipment.class)))
						.willReturn(acqEq);
		doNothing().when(acquisitionEquipmentService).deleteById(1L);
		given(acquisitionEquipmentService.findAll()).willReturn(Arrays.asList(equip));
		given(acquisitionEquipmentService.findById(1L)).willReturn(Optional.of(equip));
		given(acquisitionEquipmentService.create(Mockito.any(AcquisitionEquipment.class))).willReturn(equip);
		given(controllerSecurityService.idMatches(Mockito.anyLong(), Mockito.any(AcquisitionEquipment.class))).willReturn(true);
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void deleteAcquisitionEquipmentTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}
	
	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void deleteAcquisitionEquipmentUnknownTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH + "/0").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser
	public void findAcquisitionEquipmentByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void findAcquisitionEquipmentsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void saveNewAcquisitionEquipmentTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(ModelsUtil.createAcquisitionEquipment())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void updateAcquisitionEquipmentTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(ModelsUtil.createAcquisitionEquipment())))
				.andExpect(status().isNoContent());
	}

}
