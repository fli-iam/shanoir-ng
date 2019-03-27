package org.shanoir.ng.coil;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.coil.controler.CoilApiController;
import org.shanoir.ng.coil.dto.CoilDTO;
import org.shanoir.ng.coil.dto.mapper.CoilMapper;
import org.shanoir.ng.coil.model.Coil;
import org.shanoir.ng.coil.service.CoilService;
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
 * Unit tests for coil controller.
 *
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = CoilApiController.class)
@AutoConfigureMockMvc(secure = false)
public class CoilApiControllerTest {

	private static final String REQUEST_PATH = "/coils";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private CoilMapper coilMapperMock;

	@MockBean
	private CoilService coilServiceMock;

	@Before
	public void setup() throws EntityNotFoundException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		given(coilMapperMock.coilsToCoilDTOs(Mockito.anyListOf(Coil.class)))
				.willReturn(Arrays.asList(new CoilDTO()));
		given(coilMapperMock.coilToCoilDTO(Mockito.any(Coil.class))).willReturn(new CoilDTO());

		doNothing().when(coilServiceMock).deleteById(1L);
		given(coilServiceMock.findAll()).willReturn(Arrays.asList(new Coil()));
		given(coilServiceMock.findById(1L)).willReturn(new Coil());
		given(coilServiceMock.create(Mockito.mock(Coil.class))).willReturn(new Coil());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteCoilTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	public void findCoilByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findCoilsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void saveNewCoilTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createCoil())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void updateCoilTest() throws Exception {
		Coil coil = ModelsUtil.createCoil();
		coil.setId(1L);
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(coil)))
				.andExpect(status().isNoContent());
	}

}
