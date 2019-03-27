package org.shanoir.ng.shared.common;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
 * Unit tests for study controller.
 *
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = CommonApiController.class)
@AutoConfigureMockMvc(secure = false)
public class CommonApiControllerTest {

	private static final String REQUEST_PATH = "/common";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private CommonService commonServiceMock;

	@Before
	public void setup() {
		gson = new GsonBuilder().create();

		given(commonServiceMock.findByIds(Mockito.any(CommonIdsDTO.class))).willReturn(new CommonIdNamesDTO());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void findStudySubjectCenterNamesByIdsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createCommonIdsDTO())))
				.andExpect(status().isOk());
	}

}
