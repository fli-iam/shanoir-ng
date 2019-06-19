package org.shanoir.ng.accountrequest;

import static org.junit.Assert.fail;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.accountrequest.controller.AccountRequestApi;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.repository.UserRepository;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

/**
 * User security service test.
 * 
 * @author jlouis
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AccountRequestApiSecurityTest {

	private BindingResult mockBindingResult;

	@MockBean
	private UserRepository userRepository;

	@Autowired
	private AccountRequestApi accountRequestApi;
	
	private User mockAccountReqUser;

	@Before
	public void setup() {
		mockAccountReqUser = ModelsUtil.createUser(null);
			mockAccountReqUser.setAccountRequestDemand(true);
			mockAccountReqUser.setRole(null);
		mockBindingResult = new BeanPropertyBindingResult(mockAccountReqUser, "accountRequest");
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException {
		
		assertAccessAuthorized((t, u) -> {
			try {
				accountRequestApi.saveNewAccountRequest(t, u);
			} catch (RestServiceException e) {
				fail(e.toString());
			}
		}, mockAccountReqUser, mockBindingResult);
	}

}
