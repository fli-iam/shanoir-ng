//package org.shanoir.ng.importer;
//
//import org.junit.Before;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
//import org.springframework.test.context.junit4.SpringRunner;
//
///**
// * Unit tests for study controller.
// *
// * @author atouboul
// *
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc(secure = false)
//public class ImporterApiControllerTest {
//
//	private static final String REQUEST_PATH = "/importer/upload_dicom/";
//
//	@Autowired
//	private ImporterApiController iac;
//
//	@Before
//	public void setup() {
//		// DO NOTHING FOR NOW
//	}
//
//	/**
//	 * Test that rest services returns 200 when running (assume that code in
//	 * rest service is fine) If file doesn't exists, then return an message in
//	 * stdout saying file is missing
//	 * 
//	 * @throws Exception
//	 */
//
////	@Test
//	public void uploadFileTest() throws Exception {
//		
//		System.out.println(formatTime("190526.44500"));
//		System.out.println(formatTime(formatNotDot("190526.44500")));
////190927.625000
////		String filePath = "/media/extra/shanoir/sample.zip";
////		File f = new File(filePath);
////		if (f.exists() && !f.isDirectory()) {
////
////			MockMultipartFile multipartFile = new MockMultipartFile("file", "DCM_IMPORT_SAMPLE.zip", "application/zip",
////					new FileInputStream(new File(filePath)));
////
////			MockMvc mockMvc = MockMvcBuilders.standaloneSetup(iac).build();
////			mockMvc.perform(MockMvcRequestBuilders.fileUpload(REQUEST_PATH).file(multipartFile)).andDo(print())
////					.andExpect(status().isOk());
////
////		} else {
////
////			System.out.println(
////					"[TEST CASE ERROR] UNABLE TO RETRIEVE FILE FOR TESTCASE ImporterApiControllerTest.uploadFileTest() at location : "
////							+ filePath);
////
////		}
//
//	}
//	
//
//
//	public static String formatNotDot(String num) {
//	num = num.trim().replaceAll("", "");
//	if (num.matches("^0*$"))
//	num = "";
//	return num;
//	}
//	
//	static public String formatTime(String Numero) {
//
//		if (Numero.matches("^0-9*$")) {
//		StringBuffer r = new StringBuffer();
//		for (int i = 0, j = 6; i < j; i++) {
//		r.append(Numero.charAt(i));
//		if ((i % 2 == 1) && (i < (j - 1)))
//		r.append(':');
//		}
//		return r.toString();
//		}
//		return Numero;
//		}
//
//}
