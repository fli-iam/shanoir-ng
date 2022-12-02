package org.shanoir.ng.accessrequest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.shanoir.ng.accessrequest.controller.AccessRequestApiController;
import org.shanoir.ng.accessrequest.controller.AccessRequestService;
import org.shanoir.ng.accessrequest.model.AccessRequest;
import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.email.StudyInvitationEmail;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.jackson.JacksonUtils;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.service.UserService;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AccessRequestApiController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class AccessRequestApiControllerTest {

	private static final String REQUEST_PATH = "/accessrequest";

	@Autowired
	private MockMvc mvc;

	@MockBean
	ShanoirEventService eventService;

	@MockBean
	AccessRequestService accessRequestService;

	@MockBean
	EmailService emailService;

	@MockBean
	UserService userService;

	@MockBean
	RabbitTemplate rabbitTemplate;

	@Autowired
	ObjectMapper mapper;

	User user = new User();

	@Before
	public void setup() throws SecurityException {
		user.setId(1L);
		given(userService.findById(Mockito.any(Long.class))).willReturn(user);
	}

	@Test
	@WithMockKeycloakUser(id = 1)
	public void saveNewAccessRequestWithStudyNameTest() throws JsonProcessingException, Exception {
		AccessRequest request = createAccessRequest();
		
		// Access request is saved
		Mockito.when(accessRequestService.createAllowed(Mockito.any(AccessRequest.class))).thenReturn(request);
		
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(request)))
				.andExpect(status().isOk());
		
		// Do not call to get the name
		Mockito.verifyNoInteractions(this.rabbitTemplate);
		
		// Event is sent
		Mockito.verify(eventService).publishEvent(Mockito.any(ShanoirEvent.class));
		
		// Email is sent
		Mockito.verify(emailService).notifyStudyManagerAccessRequest(Mockito.any(AccessRequest.class));
	}

	@Test
	@WithMockKeycloakUser(id = 1)
	public void saveNewAccessRequestWithNoStudyNameTest() throws JsonProcessingException, Exception {
		AccessRequest request = createAccessRequest();
		// Only change: the study name is null, we have to load it.
		request.setStudyName(null);

		// Call to get the study name
		Mockito.when(this.rabbitTemplate.convertSendAndReceive(
				RabbitMQConfiguration.STUDY_NAME_QUEUE,
				request.getStudyId())).thenReturn("testStudyName");

		// Access request is saved
		ArgumentCaptor<AccessRequest> requestCaptor = ArgumentCaptor.forClass(AccessRequest.class);
		Mockito.when(accessRequestService.createAllowed(requestCaptor.capture())).thenReturn(request);
		
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(request)))
				.andExpect(status().isOk());
		
		// Check study name was updated
		assertEquals("testStudyName", requestCaptor.getValue().getStudyName());
	
		// Event is sent
		Mockito.verify(eventService).publishEvent(Mockito.any(ShanoirEvent.class));
		
		// Email is sent
		Mockito.verify(emailService).notifyStudyManagerAccessRequest(Mockito.any(AccessRequest.class));
	}

	@Test
	@WithMockKeycloakUser(id = 1)
	public void findAllByUserIdTest() throws Exception {
		// I get data, but only the one in demand
		Mockito.when(rabbitTemplate.
				convertSendAndReceive(RabbitMQConfiguration.STUDY_I_CAN_ADMIN_QUEUE, 1L))
		.thenReturn(Collections.singletonList(1L));
	
		List<AccessRequest> listOfRequests = new ArrayList<AccessRequest>();
		listOfRequests.add(createAccessRequest());
		listOfRequests.add(createAccessRequest());
		
		// One is already approved, studyName should not appear
		listOfRequests.get(0).setStatus(AccessRequest.APPROVED);
		listOfRequests.get(0).setStudyName("shouldNotBeNhere");
		
		Mockito.when(this.accessRequestService.findByStudyId(Mockito.any(List.class)))
		.thenReturn(listOfRequests);
		

		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH + "/byUser").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().string(
						Matchers.allOf(
								Matchers.not(Matchers.containsString("shouldNotBeNhere")),
								Matchers.containsString("name")
								)
						)
					);
	}

	@Test
	@WithMockKeycloakUser(id = 1)
	public void findAllByUserIdNoStudyTest() throws Exception {
		Mockito.when(rabbitTemplate.
				convertSendAndReceive(RabbitMQConfiguration.STUDY_I_CAN_ADMIN_QUEUE, 1L))
		.thenReturn(null);


		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH + "/byUser").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());

	}

	@Test
	@WithMockKeycloakUser(id = 1)
	public void findAllByUserIdNoDemand() throws Exception {
		// NO access request on demand -> NO CONTENT
		Mockito.when(rabbitTemplate.
				convertSendAndReceive(RabbitMQConfiguration.STUDY_I_CAN_ADMIN_QUEUE, 1L))
		.thenReturn(Collections.singletonList(1L));
	
		List<AccessRequest> listOfRequests = new ArrayList<AccessRequest>();
		listOfRequests.add(createAccessRequest());
		listOfRequests.add(createAccessRequest());
		
		// One is already approved, studyName should not appear
		listOfRequests.get(0).setStatus(AccessRequest.APPROVED);
		listOfRequests.get(1).setStatus(AccessRequest.APPROVED);
		
		Mockito.when(this.accessRequestService.findByStudyId(Mockito.any(List.class)))
		.thenReturn(listOfRequests);
		

		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH + "/byUser").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	@WithMockKeycloakUser(id = 1)
	public void resolveNewAccessRequestAcceptTest() throws Exception {
		AccessRequest request = createAccessRequest();
		user.setAccountRequestDemand(Boolean.TRUE);
		request.setUser(user);

		Mockito.when(accessRequestService.findById(1L)).thenReturn(Optional.of(request));
		
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH + "/resolve/1").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(true)))
				.andExpect(status().isOk());
		
		Mockito.verify(accessRequestService).update(request);
		Mockito.verify(this.userService).confirmAccountRequest(user);
		Mockito.verify(this.rabbitTemplate).convertSendAndReceive(
				Mockito.eq(RabbitMQConfiguration.STUDY_SUBSCRIPTION_QUEUE), Mockito.anyString());
		Mockito.verify(emailService).notifyUserAddedToStudy(request);
	}

	@Test
	@WithMockKeycloakUser(id = 1)
	public void resolveNewAccessRequestAcceptNotAccountTest() throws Exception {
		AccessRequest request = createAccessRequest();
		// No account request
		user.setAccountRequestDemand(Boolean.FALSE);
		request.setUser(user);

		Mockito.when(accessRequestService.findById(1L)).thenReturn(Optional.of(request));
		
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH + "/resolve/1").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(true)))
				.andExpect(status().isOk());
		
		Mockito.verify(accessRequestService).update(request);
		Mockito.verifyNoInteractions(this.userService);
		Mockito.verify(this.rabbitTemplate).convertSendAndReceive(
				Mockito.eq(RabbitMQConfiguration.STUDY_SUBSCRIPTION_QUEUE), Mockito.anyString());
		Mockito.verify(emailService).notifyUserAddedToStudy(request);
	}

	@Test
	@WithMockKeycloakUser(id = 1)
	public void resolveNewAccessRequestRefuse() throws JsonProcessingException, Exception {
		AccessRequest request = createAccessRequest();
		user.setAccountRequestDemand(Boolean.TRUE);
		request.setUser(user);

		Mockito.when(accessRequestService.findById(1L)).thenReturn(Optional.of(request));
		
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH + "/resolve/1").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(false)))
				.andExpect(status().isOk());
		
		Mockito.verify(accessRequestService).update(request);
		Mockito.verifyNoInteractions(this.rabbitTemplate);
		Mockito.verify(this.userService).denyAccountRequest(1L);
		Mockito.verify(emailService).notifyUserRefusedFromStudy(request);
	}

	@Test
	@WithMockKeycloakUser(id = 1)
	public void resolveNewAccessRequestRefuseNoDemand() throws JsonProcessingException, Exception {
		AccessRequest request = createAccessRequest();
		// No demand
		user.setAccountRequestDemand(Boolean.FALSE);
		request.setUser(user);

		Mockito.when(accessRequestService.findById(1L)).thenReturn(Optional.of(request));
		
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH + "/resolve/1").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(false)))
				.andExpect(status().isOk());
		
		Mockito.verify(accessRequestService).update(request);
		Mockito.verifyNoInteractions(this.userService);
		Mockito.verifyNoInteractions(this.rabbitTemplate);
		Mockito.verify(emailService).notifyUserRefusedFromStudy(request);
	}
	
	@Test
	@WithMockKeycloakUser(id = 1)
	public void resolveNewAccessRequestNoRequestTest() throws JsonProcessingException, Exception {
		// no ogoing request
		Mockito.when(accessRequestService.findById(1L)).thenReturn(Optional.empty());
		
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH + "/resolve/1").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(true)))
				.andExpect(status().isNoContent());
		
		Mockito.verifyNoInteractions(this.userService);
		Mockito.verifyNoInteractions(this.rabbitTemplate);
		Mockito.verifyNoInteractions(emailService);
	}

	@Test
	@WithMockKeycloakUser(id = 1)
	public void inviteExistingUserTest() throws Exception {
		// We invite an user that does not exists
		Mockito.when(this.userService.findByEmail("mail")).thenReturn(Optional.of(user));
		
		Map<String, Object> theMap = new LinkedHashMap<>();
		theMap.put("studyId", 1l);
		theMap.put("studyName", "name");
		theMap.put("email", "mail");
	
		Mockito.when(rabbitTemplate.convertSendAndReceive(
				Mockito.eq(RabbitMQConfiguration.STUDY_SUBSCRIPTION_QUEUE), Mockito.anyString())).thenReturn(true);
		
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH + "/invitation/").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.param("studyId", "" + 1l)
				.param("studyName", "name")
				.param("email", "mail"))
				.andExpect(status().isOk()).andExpect(content().string("User " + user.getUsername() + " was added to the study with success"));
		
		ArgumentCaptor<AccessRequest> requestCaptor = ArgumentCaptor.forClass(AccessRequest.class);
		
		Mockito.verify(this.accessRequestService).createAllowed(requestCaptor.capture());
		Mockito.verify(this.emailService).notifyUserAddedToStudy(Mockito.any(AccessRequest.class));
		
		AccessRequest request = requestCaptor.getValue();
		
		assertEquals(user, request.getUser());
		assertEquals("1", ""+request.getStudyId());
		assertEquals("name", request.getStudyName());
		assertEquals("From study manager", request.getMotivation());
		assertEquals(AccessRequest.APPROVED, request.getStatus());
	}

	@Test
	@WithMockKeycloakUser(id = 1)
	public void inviteExistingUserButFailedTest() throws Exception {
		// We invite an user that does not exists
		Mockito.when(this.userService.findByEmail("mail")).thenReturn(Optional.of(user));
		
		Map<String, Object> theMap = new LinkedHashMap<>();
		theMap.put("studyId", 1l);
		theMap.put("studyName", "name");
		theMap.put("email", "mail");
	
		// ATTENTION: the subscription failed
		Mockito.when(rabbitTemplate.convertSendAndReceive(
				Mockito.eq(RabbitMQConfiguration.STUDY_SUBSCRIPTION_QUEUE), Mockito.anyString())).thenReturn(false);
		
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH + "/invitation/").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.param("studyId", "" + 1l)
				.param("studyName", "name")
				.param("email", "mail"))
				.andExpect(status().isOk()).andExpect(content().string("User " + user.getUsername() + " exists but could not be added to this study. Please contact an administrator"));
		
		Mockito.verifyNoInteractions(this.accessRequestService);
		Mockito.verifyNoInteractions(this.emailService);
	}

	@Test
	@WithMockKeycloakUser(id = 1)
	public void inviteNotExistingUserTest() throws JsonProcessingException, Exception {
		// We invite an user that does not exists
		Mockito.when(this.userService.findByEmail("mail")).thenReturn(Optional.empty());
		
		Map<String, Object> theMap = new LinkedHashMap<>();
		theMap.put("studyId", 1l);
		theMap.put("studyName", "name");
		theMap.put("email", "mail");
		
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH + "/invitation/").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.param("studyId", "" + 1l)
				.param("studyName", "name")
				.param("email", "mail"))
				.andExpect(status().isOk());
		
		ArgumentCaptor<StudyInvitationEmail> emailCaptor = ArgumentCaptor.forClass(StudyInvitationEmail.class);
		Mockito.verify(this.emailService).inviteToStudy(emailCaptor.capture());
		
		assertEquals("mail", emailCaptor.getValue().getInvitedMail());
		assertEquals("1", emailCaptor.getValue().getStudyId());
		assertEquals("name", emailCaptor.getValue().getStudyName());
	}

	@Test
	@WithMockKeycloakUser(id = 1)
	public void testGetById() throws Exception {
		AccessRequest request = createAccessRequest();

		Mockito.when(this.accessRequestService.findById(1l)).thenReturn(Optional.of(request));
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH + "/1").accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().string(JacksonUtils.serialize(request)));
	}

	private AccessRequest createAccessRequest() {
		AccessRequest req = new AccessRequest();
		req.setMotivation("motivation");
		req.setStatus(AccessRequest.ON_DEMAND);
		req.setStudyId(1L);
		req.setStudyName("name");
		req.setUser(user);
		return req;
	}
	
}
