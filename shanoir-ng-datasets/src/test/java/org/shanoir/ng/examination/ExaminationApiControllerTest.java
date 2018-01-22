package org.shanoir.ng.examination;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Unit tests for examination controller.
 *
 * @author ifakhfakh
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ExaminationApiController.class)
@AutoConfigureMockMvc(secure = false)
public class ExaminationApiControllerTest {

	private static final String REQUEST_PATH = "/examinations";
	private static final String REQUEST_PATH_COUNT = REQUEST_PATH + "/count";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ExaminationMapper examinationMapperMock;

	@MockBean
	private ExaminationService examinationServiceMock;

	@Before
	public void setup() throws ShanoirException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(examinationServiceMock).deleteById(1L);
		given(examinationServiceMock.countExaminationsByUserId()).willReturn(2L);
		given(examinationServiceMock.findAll(Mockito.any(Pageable.class))).willReturn(Arrays.asList(new Examination()));
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());
		given(examinationServiceMock.save(Mockito.mock(Examination.class))).willReturn(new Examination());
	}

	@Test
	public void countExaminationsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_COUNT).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteExaminationTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	public void findExaminationByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findExaminationsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void saveNewExaminationTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createExamination())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void updateExaminationTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createExamination())))
				.andExpect(status().isNoContent());
	}

}
