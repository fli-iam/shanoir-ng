package org.shanoir.ng.preclinical.anesthetics.examination_anesthetics;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticService;
import org.shanoir.ng.preclinical.references.RefsService;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.utils.AnestheticModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Unit tests for examination anesthetics controller.
 *
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ExaminationAnestheticApiController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
@ActiveProfiles("test")
public class ExaminationAnestheticApiControllerTest {

	private static final String REQUEST_PATH_EXAMINATION = "/examination";
	private static final String EXAMINATION_ID = "/1";
	private static final String REQUEST_PATH_ANESTHETIC = "/anesthetic";
	private static final String REQUEST_PATH = REQUEST_PATH_EXAMINATION + EXAMINATION_ID + REQUEST_PATH_ANESTHETIC;
	private static final String REQUEST_PATH_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ExaminationAnestheticService examAnestheticServiceMock;
	@MockBean
	private AnestheticService anestheticsServiceMock;

	@MockBean
	private RefsService referencesServiceMock;

	@Before
	public void setup() throws ShanoirPreclinicalException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(examAnestheticServiceMock).deleteById(1L);
		given(examAnestheticServiceMock.findAll()).willReturn(Arrays.asList(new ExaminationAnesthetic()));
		given(examAnestheticServiceMock.findByExaminationId(1L)).willReturn(Arrays.asList(new ExaminationAnesthetic()));
		given(examAnestheticServiceMock.findById(1L)).willReturn(new ExaminationAnesthetic());
		given(examAnestheticServiceMock.save(Mockito.mock(ExaminationAnesthetic.class)))
				.willReturn(new ExaminationAnesthetic());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteExaminationAnestheticTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findExaminationAnestheticByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findExaminationAnestheticsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_ALL).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void saveNewExaminationAnestheticTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(AnestheticModelUtil.createExaminationAnesthetic()))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void updateExaminationAnestheticTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(AnestheticModelUtil.createExaminationAnesthetic()))).andExpect(status().isOk());
	}

}
