package org.shanoir.studycard.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.studycard.model.StudyCard;
import org.shanoir.studycard.service.StudyCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class StudyCardControllerTest {

	@Autowired
	private StudyCardService studyCardService;

	@Autowired
	private MockMvc mockMvc;

	private final StudyCard newStudyCard = new StudyCard(0, "test0", false, 1L, 1L, 1L);
	private final StudyCard studyCard = new StudyCard(1, "test1Bis", false, 1L, 1L, 1L);

	@Before
	public void setUp() throws Exception {
		StudyCard mockStudyCard = new StudyCard(0, "test1", false, 1L, 1L, 1L);
		if (studyCardService.findByName("test1") == null) {
			studyCardService.save(mockStudyCard);
		}
	}

	@Test
	public void shouldReturnStudyCardList() throws Exception {
		this.mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("test1")));
	}

	@Test
	public void shouldReturnStudyCard() throws Exception {
		this.mockMvc.perform(get("/1")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("id")));
	}

	@Test
	public void shouldNotReturnStudyCard() throws Exception {
		this.mockMvc.perform(get("/1000")).andDo(print()).andExpect(status().isOk()).andExpect(content().string(""));
	}

	@Test
	public void shouldCreateAndReturnStudyCard() throws Exception {
		this.mockMvc
				.perform(post("/").content(asJsonString(newStudyCard)).accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk()).andExpect(content().string(containsString("test0")));
	}

	@Test
	public void shouldUpdateAndReturnStudyCard() throws Exception {
		this.mockMvc
				.perform(put("/1").content(asJsonString(studyCard)).accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk()).andExpect(content().string(containsString("test1Bis")));
	}

	private static String asJsonString(final Object obj) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			final String jsonContent = mapper.writeValueAsString(obj);
			return jsonContent;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
