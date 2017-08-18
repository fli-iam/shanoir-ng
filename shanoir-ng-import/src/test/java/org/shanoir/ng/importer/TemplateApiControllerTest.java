package org.shanoir.ng.importer;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.importer.Template;
import org.shanoir.ng.importer.ImporterApiController;
import org.shanoir.ng.importer.TemplateService;
import org.shanoir.ng.shared.exception.ShanoirImportException;
import org.shanoir.ng.utils.ModelsUtil;
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
 * Unit tests for template controller.
 *
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ImporterApiController.class)
@AutoConfigureMockMvc(secure = false)
public class TemplateApiControllerTest {

	private static final String REQUEST_PATH = "/template";
	private static final String REQUEST_PATH_FOR_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private TemplateService templateServiceMock;

	@Before
	public void setup() throws ShanoirImportException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(templateServiceMock).deleteById(1L);
		given(templateServiceMock.findAll()).willReturn(Arrays.asList(new Template()));
		given(templateServiceMock.findById(1L)).willReturn(new Template());
		given(templateServiceMock.save(Mockito.mock(Template.class))).willReturn(new Template());
	}

	@Test
	@WithMockUser
	public void updateFileTest() throws Exception {
		
	}

}
