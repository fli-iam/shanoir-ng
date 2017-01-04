package org.shanoir.ng.service;

import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.model.User;
import org.shanoir.ng.model.exception.ShanoirUsersException;
import org.shanoir.ng.repository.UserRepository;
import org.shanoir.ng.service.impl.UserServiceImpl;
import org.shanoir.ng.utils.ModelsUtil;

/**
 * User detail service test.
 * 
 * @author msimon
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

	private static final Long USER_ID = 1L;
	private static final String UPDATED_USER_FIRSTNAME = "test";
	
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

	@Before
	public void setup() {
		given(userRepository.findOne(USER_ID)).willReturn(ModelsUtil.createUser());
	}

    @Test
    public void findAllTest(){
        final List<User> users = userService.findAll();
        Assert.assertNotNull(users);
        Assert.assertTrue(users.size() == 0);
    }

    @Test
    public void updateTest() throws ShanoirUsersException{
    	final User user = new User();
    	user.setId(USER_ID);
    	user.setFirstName(UPDATED_USER_FIRSTNAME);
    	
        final User updatedUser = userService.update(user);
        Assert.assertNotNull(updatedUser);
        Assert.assertTrue(UPDATED_USER_FIRSTNAME.equals(updatedUser.getFirstName()));
        
        Mockito.verify(userRepository,Mockito.times(1)).save(Mockito.any(User.class));
    }

}
