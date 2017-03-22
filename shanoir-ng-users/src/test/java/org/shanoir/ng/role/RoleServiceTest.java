package org.shanoir.ng.role;

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
import org.shanoir.ng.role.Role;
import org.shanoir.ng.role.RoleRepository;
import org.shanoir.ng.role.RoleServiceImpl;
import org.shanoir.ng.utils.ModelsUtil;

/**
 * User detail service test.
 * 
 * @author msimon
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

	@Before
	public void setup() {
		given(roleRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createGuestRole()));
	}

    @Test
    public void findAllTest(){
        final List<Role> roles = roleService.findAll();
        Assert.assertNotNull(roles);
        Assert.assertTrue(roles.size() == 1);
       
        Mockito.verify(roleRepository,Mockito.times(1)).findAll();
    }

}
