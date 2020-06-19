package org.shanoir.ng.tasks;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.given;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.events.ShanoirEvent;
import org.shanoir.ng.events.ShanoirEventsService;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Test class for AsyncTaskApiController
 * @author fli
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {AsyncTaskApiController.class})
@AutoConfigureMockMvc(secure = false)
public class AsyncTaskApiControllerTest {

	private static final String REQUEST_PATH = "/tasks";
	private static final String REQUEST_PATH_SEARCH = REQUEST_PATH + "/types";

	@Autowired
	private MockMvc mvc;

	@MockBean
	ShanoirEventsService taskService;

	@Test
	@WithMockKeycloakUser(id = 1)
	public void testFindTasksByType() throws Exception {
		
		ShanoirEvent event = new ShanoirEvent();
		event.setLastUpdate(new Date());
		event.setId(2L);
		List<ShanoirEvent> events = Collections.singletonList(event );
		given(taskService.getEventsByUserAndType(1L, new String[]{"import-dataset-event"})).willReturn(events);
		
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_SEARCH)
				.accept(MediaType.APPLICATION_JSON)
				.param("types", "import-dataset-event"))
		.andExpect(status().isOk());
	}

	@Test
	@WithMockKeycloakUser(id = 1)
	public void testFindTasksByTypeNoContent() throws Exception {
		given(taskService.getEventsByUserAndType(1L, new String[]{"import-dataset-event"})).willReturn(null);
		
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_SEARCH)
				.accept(MediaType.APPLICATION_JSON)
				.param("types", "import-dataset-event"))
		.andExpect(status().isNoContent());
	}
}
