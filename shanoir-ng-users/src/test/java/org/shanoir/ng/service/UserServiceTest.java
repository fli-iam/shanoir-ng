package org.shanoir.ng.service;

import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.exception.ShanoirUsersException;
import org.shanoir.ng.model.User;
import org.shanoir.ng.repository.UserRepository;
import org.shanoir.ng.service.impl.UserServiceImpl;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

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
	
    @Mock
    private RabbitTemplate rabbitTemplate;
    
    @InjectMocks
    private UserServiceImpl userService;

	@Before
	public void setup() {
		given(userRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createUser()));
		given(userRepository.findOne(USER_ID)).willReturn(ModelsUtil.createUser());
	}

    @Test
    public void confirmAccountRequestTest() throws ShanoirUsersException {
    	final User user = ModelsUtil.createUser();
        user.setAccountRequestDemand(true);
		given(userRepository.findOne(USER_ID)).willReturn(user);
		
		final User updatedUser = userService.confirmAccountRequest(USER_ID, createUser());
        Assert.assertNotNull(updatedUser);
        Assert.assertTrue(UPDATED_USER_FIRSTNAME.equals(updatedUser.getFirstName()));
        
        Mockito.verify(userRepository,Mockito.times(1)).findOne(Mockito.anyLong());
        user.setAccountRequestDemand(false);
        Mockito.verify(userRepository,Mockito.times(1)).save(user);
    }

    @Test(expected = ShanoirUsersException.class)
    public void confirmAccountRequestBadUserIdTest() throws ShanoirUsersException {
		given(userRepository.findOne(USER_ID)).willReturn(null);
		
        userService.confirmAccountRequest(USER_ID, new User());
        
        Mockito.verify(userRepository,Mockito.times(1)).findOne(Mockito.anyLong());
        Mockito.verify(userRepository,Mockito.times(0)).save(Mockito.any(User.class));
    }

    @Test(expected = ShanoirUsersException.class)
    public void confirmAccountRequestNoDemandTest() throws ShanoirUsersException {
		given(userRepository.findOne(USER_ID)).willReturn(ModelsUtil.createUser());
		
        userService.confirmAccountRequest(USER_ID, new User());
        
        Mockito.verify(userRepository,Mockito.times(1)).findOne(Mockito.anyLong());
        Mockito.verify(userRepository,Mockito.times(0)).save(Mockito.any(User.class));
    }

    //@Test
    public void denyAccountRequestTest() throws ShanoirUsersException {
    	final User user = ModelsUtil.createUser();
        user.setAccountRequestDemand(true);
		given(userRepository.findOne(USER_ID)).willReturn(user);
		
        userService.denyAccountRequest(USER_ID);
        // TODO
    }

    @Test(expected = ShanoirUsersException.class)
    public void denyAccountRequestBadUserIdTest() throws ShanoirUsersException {
		given(userRepository.findOne(USER_ID)).willReturn(null);
		
        userService.denyAccountRequest(USER_ID);
        
        Mockito.verify(userRepository,Mockito.times(1)).findOne(Mockito.anyLong());
        Mockito.verify(userRepository,Mockito.times(0)).save(Mockito.any(User.class));
    }

    @Test(expected = ShanoirUsersException.class)
    public void denyAccountRequestNoDemandTest() throws ShanoirUsersException {
		given(userRepository.findOne(USER_ID)).willReturn(ModelsUtil.createUser());
		
        userService.denyAccountRequest(USER_ID);
        
        Mockito.verify(userRepository,Mockito.times(1)).findOne(Mockito.anyLong());
        Mockito.verify(userRepository,Mockito.times(0)).save(Mockito.any(User.class));
    }

    @Test
    public void deleteByIdTest() throws ShanoirUsersException {
        userService.deleteById(USER_ID);
        
        Mockito.verify(userRepository,Mockito.times(1)).delete(Mockito.anyLong());
    }

    @Test
    public void findAllTest(){
        final List<User> users = userService.findAll();
        Assert.assertNotNull(users);
        Assert.assertTrue(users.size() == 1);
        
        Mockito.verify(userRepository,Mockito.times(1)).findAll();
    }

    @Test
    public void findByIdTest(){
        final User user = userService.findById(USER_ID);
        Assert.assertNotNull(user);
        Assert.assertTrue(ModelsUtil.USER_FIRSTNAME.equals(user.getFirstName()));
        
        Mockito.verify(userRepository,Mockito.times(1)).findOne(Mockito.anyLong());
    }

    @Test
    public void saveTest() throws ShanoirUsersException {
        userService.save(createUser());
        
        Mockito.verify(userRepository,Mockito.times(1)).save(Mockito.any(User.class));
    }

    @Test
    public void updateTest() throws ShanoirUsersException {
        final User updatedUser = userService.update(createUser());
        Assert.assertNotNull(updatedUser);
        Assert.assertTrue(UPDATED_USER_FIRSTNAME.equals(updatedUser.getFirstName()));
        
        Mockito.verify(userRepository,Mockito.times(1)).save(Mockito.any(User.class));
    }

    @Test
    public void updateFromShanoirOldTest() throws ShanoirUsersException {
        userService.updateFromShanoirOld(createUser());
        
        Mockito.verify(userRepository,Mockito.times(1)).findOne(Mockito.anyLong());
        Mockito.verify(userRepository,Mockito.times(1)).save(Mockito.any(User.class));
    }
    
    private User createUser() {
    	final User user = new User();
    	user.setId(USER_ID);
    	user.setFirstName(UPDATED_USER_FIRSTNAME);
    	return user;
    }

}
