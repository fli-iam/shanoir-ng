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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.acquisitionequipment.controler.AcquisitionEquipmentApiController;
import org.shanoir.ng.acquisitionequipment.dto.AcquisitionEquipmentDTO;
import org.shanoir.ng.acquisitionequipment.dto.mapper.AcquisitionEquipmentMapper;
import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.acquisitionequipment.service.AcquisitionEquipmentService;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.security.ControlerSecurityService;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Unit tests for acquisition equipment controller.
 *
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(AcquisitionEquipmentApiController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AcquisitionEquipmentApiControllerTest {

	private static final String REQUEST_PATH = "/acquisitionequipments";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private AcquisitionEquipmentMapper acquisitionEquipmentMapper;

	@MockBean
	private AcquisitionEquipmentService acquisitionEquipmentService;

	@MockBean
	private ShanoirEventService eventService;
	
	@MockBean(name = "controlerSecurityService")
	private ControlerSecurityService controlerSecurityService;

	@Before
	public void setup() throws EntityNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException  {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
		given(acquisitionEquipmentMapper
				.acquisitionEquipmentsToAcquisitionEquipmentDTOs(Mockito.anyListOf(AcquisitionEquipment.class)))
						.willReturn(Arrays.asList(new AcquisitionEquipmentDTO()));
		AcquisitionEquipmentDTO acqEq = new AcquisitionEquipmentDTO();
		acqEq.setId(Long.valueOf(123));
		given(acquisitionEquipmentMapper
				.acquisitionEquipmentToAcquisitionEquipmentDTO(Mockito.any(AcquisitionEquipment.class)))
						.willReturn(acqEq);
		doNothing().when(acquisitionEquipmentService).deleteById(1L);
		given(acquisitionEquipmentService.findAll()).willReturn(Arrays.asList(new AcquisitionEquipment()));
		given(acquisitionEquipmentService.findById(1L)).willReturn(Optional.of(new AcquisitionEquipment()));
		given(acquisitionEquipmentService.create(Mockito.any(AcquisitionEquipment.class))).willReturn(new AcquisitionEquipment());
		given(controlerSecurityService.idMatches(Mockito.anyLong(), Mockito.any(AcquisitionEquipment.class))).willReturn(true);
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void deleteAcquisitionEquipmentTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
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
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createAcquisitionEquipment())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void updateAcquisitionEquipmentTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createAcquisitionEquipment())))
				.andExpect(status().isNoContent());
	}

}
