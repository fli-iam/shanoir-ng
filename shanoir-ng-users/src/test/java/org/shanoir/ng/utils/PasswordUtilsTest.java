/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link PasswordUtils}: passwords are at least 12 characters long and contain a
 * lower-case letter, an upper-case letter, a digit and one of the special characters %$#@.
 *
 * @author msimon
 */
public class PasswordUtilsTest {

    private static final int PASSWORD_LENGTH = 12;

    /** Generated passwords are random, so check several of them. */
    @RepeatedTest(50)
    public void generatePasswordTest() {
        final String password = PasswordUtils.generatePassword();
        assertNotNull(password);
        assertEquals(PASSWORD_LENGTH, password.length());
        assertTrue(password.matches("[a-zA-Z0-9%$#@]+"), "Unexpected character in " + password);
        assertTrue(PasswordUtils.checkPasswordPolicy(password), "Policy not met by " + password);
    }

    @ParameterizedTest
    @ValueSource(strings = {"aA11@@22bbCC", "aA1%bB2$cC3#dD4@", "Zz9#Zz9#Zz9#"})
    public void checkPasswordPolicyValidTest(final String password) {
        assertTrue(PasswordUtils.checkPasswordPolicy(password));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "aA1@aA1@aA1",  // 11 characters
        "aa11@@22bbcc", // no upper-case letter
        "AA11@@22BBCC", // no lower-case letter
        "aA@@aA@@aAbB", // no digit
        "aA11aA22bB33", // no special character
        "aA11!!22bbCC"  // '!' is not an allowed special character
    })
    public void checkPasswordPolicyInvalidTest(final String password) {
        assertFalse(PasswordUtils.checkPasswordPolicy(password));
    }

}
