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

package org.shanoir.ng.preclinical.examination_extradata;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.preclinical.extra_data.bloodgas_data.BloodGasData;
import org.shanoir.ng.preclinical.extra_data.examination_extra_data.ExaminationExtraData;
import org.shanoir.ng.preclinical.extra_data.physiological_data.PhysiologicalData;
import org.shanoir.ng.utils.ExtraDataModelUtil;
import org.shanoir.ng.utils.KeycloakControllerTestIT;
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

/**
 * Integration tests for examination extradata controller.
 *
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class ExtraDataApiControllerTestIT extends KeycloakControllerTestIT {

	private static final String REQUEST_PATH_EXAMINATION = "/examination";
	private static final String EXAMINATION_ID = "/1";
	private static final String REQUEST_EXTRADATA = "/extradata";
	private static final String REQUEST_PHYSIOLOGICALDATA = "/physiologicaldata";
	private static final String REQUEST_BLOODGASDATA = "/bloodgasdata";
	private static final String REQUEST_UPLOAD = "/upload";
	private static final String REQUEST_PATH_EXTRADATA = REQUEST_PATH_EXAMINATION + EXAMINATION_ID + REQUEST_EXTRADATA;
	private static final String REQUEST_PATH_PHYSIOLOGICALDATA = REQUEST_PATH_EXAMINATION + EXAMINATION_ID
			+ REQUEST_PHYSIOLOGICALDATA;
	private static final String REQUEST_PATH_BLOODGASDATA = REQUEST_PATH_EXAMINATION + EXAMINATION_ID
			+ REQUEST_BLOODGASDATA;
	private static final String REQUEST_PATH_ALL = REQUEST_PATH_EXTRADATA + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH_EXTRADATA + "/1";
	private static final String REQUEST_PATH_PHYSIO_WITH_ID = REQUEST_PATH_PHYSIOLOGICALDATA + "/1";
	private static final String REQUEST_PATH_BLOODGAS_WITH_ID = REQUEST_PATH_BLOODGASDATA + "/1";
	private static final String REQUEST_PATH_UPLOAD = REQUEST_PATH_EXTRADATA + REQUEST_UPLOAD;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void findExtraDataByIdProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_WITH_ID, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findExtraDataByIdWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void findExtraDatasProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_ALL, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findExtraDatasWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_ALL, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void saveNewExtraDataProtected() {
		final ResponseEntity<String> response = restTemplate.postForEntity(REQUEST_PATH_EXTRADATA,
				new ExaminationExtraData(), String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void saveNewExtraDataWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<ExaminationExtraData> entity = new HttpEntity<ExaminationExtraData>(
				ExtraDataModelUtil.createExaminationExtraData(), getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_EXTRADATA, HttpMethod.POST, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void uploadNewExtraDataProtected() {
		final HttpEntity<ExaminationExtraData> entity = new HttpEntity<ExaminationExtraData>(
				ExtraDataModelUtil.createExaminationExtraData());

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_UPLOAD, HttpMethod.PUT, entity,
				String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void uploadNewExtraDataWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<ExaminationExtraData> entity = new HttpEntity<ExaminationExtraData>(
				ExtraDataModelUtil.createExaminationExtraData(), getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_UPLOAD, HttpMethod.PUT, entity,
				String.class);
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
	}

	@Test
	public void updatePhysiologicalDataProtected() {
		final HttpEntity<PhysiologicalData> entity = new HttpEntity<PhysiologicalData>(
				ExtraDataModelUtil.createExaminationPhysiologicalData());

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_PHYSIO_WITH_ID, HttpMethod.PUT,
				entity, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void updateBloodGasDataProtected() {
		final HttpEntity<BloodGasData> entity = new HttpEntity<BloodGasData>(
				ExtraDataModelUtil.createExaminationBloodGasData());

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_BLOODGAS_WITH_ID, HttpMethod.PUT,
				entity, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

}
