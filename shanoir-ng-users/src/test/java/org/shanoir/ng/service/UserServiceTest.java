package org.shanoir.ng.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.model.User;
import org.shanoir.ng.repository.UserRepository;
import org.shanoir.ng.service.impl.UserServiceImpl;

/**
 * User detail service test.
 * 
 * @author msimon
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void findAllTest(){
        final List<User> users = userService.findAll();
        Assert.assertNotNull(users);
        Assert.assertTrue(users.size() == 0);
    }

}
