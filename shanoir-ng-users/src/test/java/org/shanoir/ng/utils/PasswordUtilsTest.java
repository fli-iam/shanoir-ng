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
	
}
