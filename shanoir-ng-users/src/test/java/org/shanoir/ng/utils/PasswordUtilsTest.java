package org.shanoir.ng.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.exception.ShanoirUsersException;

/**
 * @author msimon
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PasswordUtilsTest {

	private static final String PASSWORD = "testtest";
	
	private static final String HASHED_PASSWORD = "5181AB-85B9-71";
	
    @Test
	public void getHashTest() {
		final String hash = PasswordUtils.getHash(PASSWORD);
        Assert.assertNotNull(hash);
        Assert.assertTrue(HASHED_PASSWORD.equals(hash));
	}
	
    @Test
	public void generatePasswordTest() {
		final String password = PasswordUtils.generatePassword();
        Assert.assertNotNull(password);
        Assert.assertTrue(password.length() == 8);
	}
	
    @Test
	public void checkPasswordPolicyTest() throws ShanoirUsersException {
		PasswordUtils.checkPasswordPolicy("aa11@@22", null);
	}
	
    @Test
	public void checkGeneratedPasswordPolicyTest() throws ShanoirUsersException {
		PasswordUtils.checkPasswordPolicy(PasswordUtils.generatePassword(), null);
	}
	
    @Test(expected = ShanoirUsersException.class)
	public void checkPasswordPolicyBadPwdTest1() throws ShanoirUsersException {
		PasswordUtils.checkPasswordPolicy("aa11@", null);
	}
	
    @Test(expected = ShanoirUsersException.class)
	public void checkPasswordPolicyBadPwdTest2() throws ShanoirUsersException {
		PasswordUtils.checkPasswordPolicy("@@11@@22", null);
	}
	
    @Test(expected = ShanoirUsersException.class)
	public void checkPasswordPolicyBadPwdTest3() throws ShanoirUsersException {
		PasswordUtils.checkPasswordPolicy("aabb@@cc", null);
	}
	
    @Test(expected = ShanoirUsersException.class)
	public void checkPasswordPolicyBadPwdTest4() throws ShanoirUsersException {
		PasswordUtils.checkPasswordPolicy("aa11aa22", null);
	}
	
}
