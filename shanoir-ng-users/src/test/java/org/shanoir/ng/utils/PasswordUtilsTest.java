package org.shanoir.ng.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

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
	public void checkPasswordPolicyTest() {
    	Assert.assertTrue(PasswordUtils.checkPasswordPolicy("aA11@@22"));
	}
	
    @Test
	public void checkGeneratedPasswordPolicyTest() {
    	Assert.assertTrue(PasswordUtils.checkPasswordPolicy(PasswordUtils.generatePassword()));
	}
	
    @Test
	public void checkPasswordPolicyBadPwdTest1() {
    	Assert.assertFalse(PasswordUtils.checkPasswordPolicy("aa11@"));
	}
	
    @Test
	public void checkPasswordPolicyBadPwdTest2() {
    	Assert.assertFalse(PasswordUtils.checkPasswordPolicy("@@11@@22"));
	}
	
    @Test
	public void checkPasswordPolicyBadPwdTest3()  {
    	Assert.assertFalse(PasswordUtils.checkPasswordPolicy("aabb@@cc"));
	}
	
    @Test
	public void checkPasswordPolicyBadPwdTest4() {
    	Assert.assertFalse(PasswordUtils.checkPasswordPolicy("aa11aa22"));
	}
	
}
