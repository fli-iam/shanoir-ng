package org.shanoir.ng.acquisitionequipment;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
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
 * Unit tests for acquisition equipment controller.
 *
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AcquisitionEquipmentApiController.class)
@AutoConfigureMockMvc(secure = false)
public class AcquisitionEquipmentApiControllerTest {

	private static final String REQUEST_PATH = "/acquisitionequipment";
	private static final String REQUEST_PATH_FOR_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private AcquisitionEquipmentService acquisitionEquipmentServiceMock;

	@Before
	public void setup() throws ShanoirStudiesException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(acquisitionEquipmentServiceMock).deleteById(1L);
		given(acquisitionEquipmentServiceMock.findAll()).willReturn(Arrays.asList(new AcquisitionEquipment()));
		given(acquisitionEquipmentServiceMock.findById(1L)).willReturn(new AcquisitionEquipment());
		given(acquisitionEquipmentServiceMock.save(Mockito.mock(AcquisitionEquipment.class)))
				.willReturn(new AcquisitionEquipment());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void deleteAcquisitionEquipmentTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findAcquisitionEquipmentByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findAcquisitionEquipmentsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_FOR_ALL).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void saveNewAcquisitionEquipmentTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createAcquisitionEquipment())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void updateAcquisitionEquipmentTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createAcquisitionEquipment())))
				.andExpect(status().isNoContent());
	}

}
