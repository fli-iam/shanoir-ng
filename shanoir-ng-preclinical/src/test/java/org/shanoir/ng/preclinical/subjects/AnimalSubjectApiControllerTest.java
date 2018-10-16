package org.shanoir.ng.preclinical.subjects;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathologyService;
import org.shanoir.ng.preclinical.references.RefsService;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapyService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.AnimalSubjectModelUtil;
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
 * Unit tests for subjects controller.
 *
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AnimalSubjectApiController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
@ActiveProfiles("test")
public class AnimalSubjectApiControllerTest {

	private static final String REQUEST_PATH = "/subject";
	private static final String REQUEST_PATH_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
	private static final String REQUEST_PATH_WITH_SUBJECT_ID = REQUEST_PATH + "/find/1";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private AnimalSubjectService subjectsServiceMock;

	@MockBean
	private RefsService refsServiceMock;

	@MockBean
	private SubjectPathologyService subjectPathologiesServiceMock;

	@MockBean
	private SubjectTherapyService subjectTherapiesServiceMock;

	@Before
	public void setup() throws ShanoirException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(subjectsServiceMock).deleteById(1L);
		given(subjectsServiceMock.findAll()).willReturn(Arrays.asList(new AnimalSubject()));
		given(subjectsServiceMock.findById(1L)).willReturn(new AnimalSubject());
		List<AnimalSubject> subjects = new ArrayList<AnimalSubject>();
		subjects.add(new AnimalSubject());
		given(subjectsServiceMock.findBy("subjectId", 1L)).willReturn(subjects);
		given(subjectsServiceMock.save(Mockito.mock(AnimalSubject.class))).willReturn(new AnimalSubject());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteSubjectTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findSubjectByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findSubjectsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_ALL).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void saveNewSubjectTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(AnimalSubjectModelUtil.createAnimalSubject()))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void updateSubjectTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(AnimalSubjectModelUtil.createAnimalSubject()))).andExpect(status().isOk());
	}

	@Test
	public void findSubjectBySubjectIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_SUBJECT_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

}
