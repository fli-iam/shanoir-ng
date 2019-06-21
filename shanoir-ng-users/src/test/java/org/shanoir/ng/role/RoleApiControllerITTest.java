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

//package org.shanoir.ng.role;
//
//import static org.junit.Assert.assertEquals;
//
//import java.security.GeneralSecurityException;
//import java.security.KeyManagementException;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.cert.X509Certificate;
//
//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.HttpsURLConnection;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLSession;
//import javax.net.ssl.TrustManager;
//
//import org.apache.http.HttpHost;
//import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
//import org.apache.http.conn.ssl.TrustStrategy;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.shanoir.ng.utils.KeycloakControllerTestIT;
//import org.shanoir.ng.utils.tests.TestTrustManager;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.boot.web.client.RestTemplateBuilder;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.web.client.RestTemplate;
//
///**
// * Integration tests for role controller.
// *
// * @author msimon
// *
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")
//
//public class RoleApiControllerITTest extends KeycloakControllerTestIT {
//
//	private static final String REQUEST_PATH = "/roles";
//	
//	@Autowired
//    private TestRestTemplate restTemplate;
//    
//
//    @Before
//	public void setup() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException  {
//    	
////    	SSLContext sslContext = SSLContext.getInstance("SSL");
////		sslContext.init(null, new TrustManager[] { new TestTrustManager() }, new java.security.SecureRandom());
////    	SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
////    	CloseableHttpClient httpClient = HttpClients.custom()
////    	        .setSSLSocketFactory(csf)
////    	        .build();
////    	HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
////    	
////    	restTemplate = new RestTemplate(requestFactory);
//    }
//
//
//	@Test
//	public void findRolesWithLogin() throws GeneralSecurityException {
//		HttpEntity<String> entity = new HttpEntity<String>(null, getHeadersWithToken(true));
//		
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.GET, entity, String.class);
//		//final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH, String.class);
//		assertEquals(HttpStatus.OK, response.getStatusCode());
//	}
//
//	@Test
//	public void findRolesWithBadRole() throws GeneralSecurityException {
//		HttpEntity<String> entity = new HttpEntity<String>(null, getHeadersWithToken(false));
//		
//		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.GET, entity, String.class);
//		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
//	}
//	
//}
