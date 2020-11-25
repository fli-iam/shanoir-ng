package org.shanoir.ng.exchange.imports.subject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IdentifierCalculatorTest {

	private IdentifierCalculator identifierCalculator;

	private static final String FIRST_NAME_HASH1 = "7fae52662a241974bf38fa75a830ef0bb6e0e9a5401b64d640a1bf4250ed3852";

	private static final String BIRTH_NAME_HASH1 = "2eff19c7e49eeba688545585315ffabef9c8eac3828da0f5bb36c4f8a457ce0b";

	private static final String BIRTH_DATE_HASH = "472ec426afd816174c494ec568eb31081057a89f24ca9b57f03f9049bf8ed58b";
	
	private static final String FIRST_NAME = "firstName";
	
	private static final String LAST_NAME = "lastName";
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	
	private Date birthDate;

	@SuppressWarnings("deprecation")
	@Before
	public void setup() {
		identifierCalculator = new IdentifierCalculator();
		try {
			birthDate = sdf.parse("01/01/2020");
			/**
			 * This behaviour is strange and is there to produce the same result
			 * as when using the Web GUI of sh-old, where 01:00:00 is set programmatically
			 * in the backend, even if not entered by the user, maybe because of time zone problems.
			 */
			birthDate.setHours(1);
		} catch (ParseException e) {
		}
	}

	@Test
	public void testCalculateIdentifierWithHashs() throws UnsupportedEncodingException, NoSuchAlgorithmException {
		String subjectIdentifier = identifierCalculator.calculateIdentifierWithHashs(FIRST_NAME_HASH1, BIRTH_NAME_HASH1,
				BIRTH_DATE_HASH);
		// Values have been acquired during tests with ShUp v5.2
		Assert.assertEquals("f618582aad29463cc1f4d4fc09dfdddd00584a654bcc2ae2457c338a21da5cd6", subjectIdentifier);
	}
	
	@Test
	public void testCalculateIdentifier() throws UnsupportedEncodingException, NoSuchAlgorithmException {
		String subjectIdentifier = identifierCalculator.calculateIdentifier(FIRST_NAME, LAST_NAME, birthDate);
		// Values have been acquired during tests with master sh-old
		// Assert.assertEquals("AC-844A74B6-74", subjectIdentifier);
	}

}
