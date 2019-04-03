package org.shanoir.ng.center;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.center.controler.CenterApiController;
import org.shanoir.ng.center.dto.CenterDTO;
import org.shanoir.ng.center.dto.mapper.CenterMapper;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.security.CenterFieldEditionSecurityManager;
import org.shanoir.ng.center.service.CenterService;
import org.shanoir.ng.center.service.CenterUniqueConstraintManager;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
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
 * Unit tests for center controller.
 *
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = CenterApiController.class)
@AutoConfigureMockMvc(secure = false)
public class CenterApiControllerTest {

	private static final String REQUEST_PATH = "/centers";
	private static final String REQUEST_PATH_FOR_NAMES = REQUEST_PATH + "/names";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private CenterMapper centerMapperMock;

	@MockBean
	private CenterService centerServiceMock;
	
	@MockBean
	private CenterFieldEditionSecurityManager fieldEditionSecurityManager;
	
	@MockBean
	private CenterUniqueConstraintManager uniqueConstraintManager;

	@Before
	public void setup() throws EntityNotFoundException  {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		given(centerMapperMock.centersToCenterDTOs(Mockito.anyListOf(Center.class)))
				.willReturn(Arrays.asList(new CenterDTO()));
		given(centerMapperMock.centerToCenterDTO(Mockito.any(Center.class))).willReturn(new CenterDTO());

		doNothing().when(centerServiceMock).deleteById(1L);
		given(centerServiceMock.findAll()).willReturn(Arrays.asList(new Center()));
		given(centerServiceMock.findById(1L)).willReturn(new Center());
		given(centerServiceMock.findIdsAndNames()).willReturn(Arrays.asList(new IdNameDTO()));
		given(centerServiceMock.create(Mockito.mock(Center.class))).willReturn(new Center());
		given(fieldEditionSecurityManager.validate(Mockito.any(Center.class))).willReturn(new FieldErrorMap());
		given(uniqueConstraintManager.validate(Mockito.any(Center.class))).willReturn(new FieldErrorMap());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteCenterTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	public void findCenterByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findCentersTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findCentersNamesTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_FOR_NAMES).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void saveNewCenterTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createCenter())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void updateCenterTest() throws Exception {
		Center existingCenter = ModelsUtil.createCenter();
		existingCenter.setId(1L);
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(existingCenter)))
				.andExpect(status().isNoContent());
	}

}
