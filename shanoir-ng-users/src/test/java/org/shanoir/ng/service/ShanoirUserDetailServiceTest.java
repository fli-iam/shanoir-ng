package org.shanoir.ng.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.model.User;
import org.shanoir.ng.repository.UserRepository;
import org.shanoir.ng.service.impl.ShanoirUserDetailsServiceImpl;
import org.shanoir.ng.utils.LoginUtil;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * User detail service test.
 * 
 * @author msimon
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ShanoirUserDetailServiceTest {

    @InjectMocks
    private ShanoirUserDetailsServiceImpl userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Test
    public void loadByUserNameTest(){
    	Mockito.when(userRepository.findByUsername(LoginUtil.NEW_USER_LOGIN)).thenReturn(ModelsUtil.createUser());

        final String username = LoginUtil.NEW_USER_LOGIN;
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Assert.assertNotNull(userDetails);
        Assert.assertTrue(userDetails.getClass().isAssignableFrom(User.class));
        Assert.assertTrue(username.equals(userDetails.getUsername()));
        Assert.assertTrue(userDetails.getAuthorities().size() == 1);
    }

}
