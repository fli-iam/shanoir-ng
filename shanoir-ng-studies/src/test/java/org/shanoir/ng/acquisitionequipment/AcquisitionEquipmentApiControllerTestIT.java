<<<<<<< HEAD
=======
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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.center.Center;
import org.shanoir.ng.manufacturermodel.ManufacturerModel;
import org.shanoir.ng.utils.KeycloakControllerTestIT;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

>>>>>>> upstream/develop
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

//package org.shanoir.ng.acquisitionequipment;
//
//import static org.junit.Assert.assertEquals;
//
//import java.io.IOException;
//
//import org.apache.http.client.ClientProtocolException;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
//import org.shanoir.ng.center.model.Center;
//import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
//import org.shanoir.ng.utils.KeycloakControllerTestIT;
//import org.shanoir.ng.utils.ModelsUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit4.SpringRunner;
//
///**
// * Integration tests for acquisition equipment controller.
// *
// * @author msimon
// *
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("dev")
//public class AcquisitionEquipmentApiControllerTestIT extends KeycloakControllerTestIT {
//	
//	private static final String REQUEST_PATH = "/acquisitionequipments";
//	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
//
//	@Autowired
//	private TestRestTemplate restTemplate;
//
//	@Test
//	public void findAcquisitionEquipmentByIdProtected() {
//		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_WITH_ID, String.class);
//		assertEquals(HttpStatus.FOUND, response.getStatusCode());
//	}
//
//	@Test
//	public void findAcquisitionEquipmentByIdWithLogin() throws ClientProtocolException, IOException {
//		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.GET, entity,
//				String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//	}
//
//	@Test
//	public void findAcquisitionEquipmentsProtected() {
//		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH, String.class);
//		assertEquals(HttpStatus.FOUND, response.getStatusCode());
//	}
//
//	@Test
//	public void findAcquisitionEquipmentsWithLogin() throws ClientProtocolException, IOException {
//		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.GET, entity,
//				String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//	}
//	
//	@Test
//	public void saveNewAcquisitionEquipmentProtected() {
//		final ResponseEntity<String> response = restTemplate.postForEntity(REQUEST_PATH, new AcquisitionEquipment(), String.class);
//		assertEquals(HttpStatus.FOUND, response.getStatusCode());
//	}
//
//	@Test
//	public void saveNewAcquisitionEquipmentWithLogin() throws ClientProtocolException, IOException {
//		
//		final AcquisitionEquipment equipment = createAcquisitionEquipment();
//		equipment.setSerialNumber("test2");
//		final HttpEntity<AcquisitionEquipment> entity = new HttpEntity<AcquisitionEquipment>(equipment, getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.POST, entity,
//				String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//		
//		// Get acquisition equipment id
//		String equipmentId = response.getBody().split("\"id\":")[1].split(",")[0];
//
//		// Delete acquisition equipment
//		final ResponseEntity<String> responseDelete = restTemplate
//				.exchange(REQUEST_PATH + "/" + equipmentId, HttpMethod.DELETE, entity, String.class);
//		assertEquals(HttpStatus.OK, responseDelete.getStatusCode());
//	}
//
//	@Test
//	public void updateNewAcquisitionEquipmentProtected() {
//		final HttpEntity<AcquisitionEquipment> entity = new HttpEntity<AcquisitionEquipment>(ModelsUtil.createAcquisitionEquipment());
//		
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
//				String.class);
//		assertEquals(HttpStatus.FOUND, response.getStatusCode());
//	}
//
//	@Test
//	public void updateNewAcquisitionEquipmentWithLogin() throws ClientProtocolException, IOException {
//		final HttpEntity<AcquisitionEquipment> entity = new HttpEntity<AcquisitionEquipment>(createAcquisitionEquipment(), getHeadersWithToken(true));
//
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
//				String.class);
//		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
//	}
//
//	private AcquisitionEquipment createAcquisitionEquipment() {
//		final Center center = new Center();
//		center.setId(1L);
//		final ManufacturerModel manufacturerModel = new ManufacturerModel();
//		manufacturerModel.setId(1L);
//		final AcquisitionEquipment equipment = new AcquisitionEquipment();
//		equipment.setId(1L);
//		equipment.setCenter(center);
//		equipment.setManufacturerModel(manufacturerModel);
//		equipment.setSerialNumber("test");
//		return equipment;
//	}
//
//}
