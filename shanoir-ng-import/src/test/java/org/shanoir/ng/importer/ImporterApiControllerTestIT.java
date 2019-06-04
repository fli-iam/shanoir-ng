//package org.shanoir.ng.importer;
//
// import static org.junit.Assert.assertEquals;
//
// import java.io.IOException;
// import java.util.Arrays;
//
// import org.apache.http.client.ClientProtocolException;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.shanoir.ng.center.Center;
// import org.shanoir.ng.studycenter.StudyCenter;
// import org.shanoir.ng.utils.KeycloakControllerTestIT;
// import org.shanoir.ng.utils.ModelsUtil;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
// import org.springframework.boot.test.web.client.TestRestTemplate;
// import org.springframework.http.HttpEntity;
// import org.springframework.http.HttpMethod;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.context.junit4.SpringRunner;
//
// /**
//  * Integration tests for study controller.
//  *
//  * @author msimon
//  *
//  */
// @RunWith(SpringRunner.class)
// @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
// @ActiveProfiles("dev")
// public class ImporterApiControllerTestIT {
// }
//
// 	private static final String REQUEST_PATH = "/studies";
// 	private static final String REQUEST_PATH_FOR_NAMES = REQUEST_PATH + "/names";
// 	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
//
// 	@Autowired
// 	private TestRestTemplate restTemplate;
//
//   @Test
//   public void UploadFile() {
// 		File f = new File("/media/extra/shanoir/DCM_IMPORT_SAMPLE.zip");
// 		System.out.println(f.isFile()+"  "+f.getName()+f.exists());
// 		FileInputStream fi1 = new FileInputStream(f);
// 		// FileInputStream fi2 = new FileInputStream(new File("C:\\Users\\Public\\Pictures\\Sample Pictures\\Tulips.jpg"));
// 		MockMultipartFile fstmp = new MockMultipartFile("upload", f.getName(), "multipart/form-data",fi1);
// 		// MockMultipartFile secmp = new MockMultipartFile("upload", "Tulips.jpg","multipart/form-data",fi2);
// 		MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
// 		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/AddContacts")
// 						.file(fstmp)
// 						.file(secmp)
// 						.param("name","abc").param("email","abc@gmail.com").param("phone", "1234567890"))
// 						.andExpect(status().isOk());
//   }
// 	
// 	 @Test
// 	 public void findStudiesProtected() {
// 	 	final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH, String.class);
// 	 	assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
// 	 }
// 	
// 	 @Test
// 	 public void findStudiesWithLogin() throws ClientProtocolException, IOException {
// 	 	final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(false));
// 	
// 	 	final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.GET, entity,
// 	 			String.class);
// 	 	assertEquals(HttpStatus.OK, response.getStatusCode());
// 	 }
// 	
// 	 @Test
// 	 public void findStudiesNamesProtected() {
// 	 	final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_FOR_NAMES, String.class);
// 	 	assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
// 	 }
// 	
// 	 @Test
// 	 public void findStudiesNamesWithLogin() throws ClientProtocolException, IOException {
// 	 	final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));
// 	
// 	 	final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_FOR_NAMES, HttpMethod.GET, entity,
// 	 			String.class);
// 	 	assertEquals(HttpStatus.OK, response.getStatusCode());
// 	 }
// 	
// 	 @Test
// 	 public void findStudyByIdProtected() {
// 	 	final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_WITH_ID, String.class);
// 	 	assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
// 	 }
// 	
// 	 @Test
// 	 public void findStudyByIdWithLogin() throws ClientProtocolException, IOException {
// 	 	final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));
// 	
// 	 	final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.GET, entity,
// 	 			String.class);
// 	 	assertEquals(HttpStatus.OK, response.getStatusCode());
// 	 }
// 	
// 	 @Test
// 	 public void saveNewStudyProtected() {
// 	 	final ResponseEntity<String> response = restTemplate.postForEntity(REQUEST_PATH, new Study(), String.class);
// 	 	assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
// 	 }
// 	
// 	 @Test
// 	 public void saveNewStudyWithLogin() throws ClientProtocolException, IOException {
// 	
// 	 	final Study study = createStudy();
// 	 	study.setName("test2");
// 	 	final HttpEntity<Study> entity = new HttpEntity<Study>(study, getHeadersWithToken(true));
// 	
// 	 	final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH, HttpMethod.POST, entity,
// 	 			String.class);
// 	 	assertEquals(HttpStatus.OK, response.getStatusCode());
// 	
// 	 	// Get study id
// 	 	String studyId = response.getBody().split("\"id\":")[1].split(",")[0];
// 	
// 	 	// Delete study
// 	 	final ResponseEntity<String> responseDelete = restTemplate.exchange(REQUEST_PATH + "/" + studyId,
// 	 			HttpMethod.DELETE, entity, String.class);
// 	 	assertEquals(HttpStatus.NO_CONTENT, responseDelete.getStatusCode());
// 	 }
// 	
// 	 @Test
// 	 public void updateNewStudyProtected() {
// 	 	final HttpEntity<Study> entity = new HttpEntity<Study>(ModelsUtil.createStudy());
// 	
// 	 	final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
// 	 			String.class);
// 	 	assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
// 	 }
// 	
// 	 @Test
// 	 public void updateNewStudyWithLogin() throws ClientProtocolException, IOException {
// 	 	final HttpEntity<Study> entity = new HttpEntity<Study>(createStudy(), getHeadersWithToken(true));
// 	
// 	 	final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.PUT, entity,
// 	 			String.class);
// 	 	assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
// 	 }
// 	
// 	 private Study createStudy() {
// 	 	final Study study = new Study();
// 	 	study.setId(1L);
// 	 	study.setName("test");
// 	 	study.setStudyStatus(StudyStatus.FINISHED);
// 	 	final StudyCenter studyCenter = new StudyCenter();
// 	 	final Center center = new Center();
// 	 	center.setId(1L);
// 	 	studyCenter.setCenter(center);
// 	 	study.setStudyCenterList(Arrays.asList(studyCenter));
// 	 	return study;
// 	 }
//
// }
