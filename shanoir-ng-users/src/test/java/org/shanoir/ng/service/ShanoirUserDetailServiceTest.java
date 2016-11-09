package org.shanoir.ng.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.service.impl.ShanoirUserDetailsServiceImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * HmacUserDetailService test
 * Created by Michael DESIGAUD on 15/02/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShanoirUserDetailServiceTest {

    @InjectMocks
    private ShanoirUserDetailsServiceImpl userDetailsService;

    @Test
    public void loadByUserName(){
        final String username = "user";
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Assert.assertNotNull(userDetails);
        Assert.assertTrue(userDetails.getClass().isAssignableFrom(User.class));
        Assert.assertTrue(username.equals(userDetails.getUsername()));
        Assert.assertTrue(userDetails.getAuthorities().size() == 1);
    }

}
