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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class IdentifierCalculatorTest {

    private IdentifierCalculator identifierCalculator;

    private static final String FIRST_NAME_HASH1 = "7fae52662a241974bf38fa75a830ef0bb6e0e9a5401b64d640a1bf4250ed3852";

    private static final String BIRTH_NAME_HASH1 = "2eff19c7e49eeba688545585315ffabef9c8eac3828da0f5bb36c4f8a457ce0b";

    private static final String BIRTH_DATE_HASH = "472ec426afd816174c494ec568eb31081057a89f24ca9b57f03f9049bf8ed58b";

    private static final String FIRST_NAME = "firstName";

    private static final String LAST_NAME = "lastName";

    @BeforeEach
    public void setup() {
        identifierCalculator = new IdentifierCalculator();
    }

    @Test
    public void testCalculateIdentifierWithHashs() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String subjectIdentifier = identifierCalculator.calculateIdentifierWithHashs(FIRST_NAME_HASH1, BIRTH_NAME_HASH1,
                BIRTH_DATE_HASH);
        // Values have been acquired during tests with ShUp v5.2
        assertEquals("f618582aad29463cc1f4d4fc09dfdddd00584a654bcc2ae2457c338a21da5cd6", subjectIdentifier);
    }

    @Test
    public void testCalculateIdentifier() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String subjectIdentifier = identifierCalculator.calculateIdentifier(FIRST_NAME, LAST_NAME, "01/01/2020");
        // Values have been acquired during tests with master sh-old
        assertEquals("AB-859C-100131", subjectIdentifier);
    }

}
