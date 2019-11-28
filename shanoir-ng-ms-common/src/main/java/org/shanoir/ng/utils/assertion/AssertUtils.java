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

package org.shanoir.ng.utils.assertion;

import static org.junit.Assert.fail;

import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.security.access.AccessDeniedException;

public abstract class AssertUtils {
	
	
	public static void assertAccessDenied(AccessCheckedFunction0Arg function) throws ShanoirException {
		try {
			try { 
				function.apply();
				fail("This should return an AccessDeniedException.");
			} catch (AccessDeniedException e) {}			
		} catch (Exception e) {
			fail(buildFailMsg(e));
		}
	}
	
	public static <T> void assertAccessDenied(AccessCheckedFunction1Arg<T> function, T arg) throws ShanoirException {
		try {
			try { 
				function.apply(arg);
				fail("This should return an AccessDeniedException.");
			} catch (AccessDeniedException e) {}			
		} catch (Exception e) {
			fail(buildFailMsg(e));
		}
	}

	public static <T, U> void assertAccessDenied(AccessCheckedFunction2Arg<T, U> function, T arg1, U arg2) throws ShanoirException {
		try {
			try { 
				function.apply(arg1, arg2);
				fail("This should return an AccessDeniedException.");
			} catch (AccessDeniedException e) {}			
		} catch (Exception e) {
			fail(buildFailMsg(e));
		}
	}
	
	public static <T, U, V> void assertAccessDenied(AccessCheckedFunction3Arg<T, U, V> function, T arg1, U arg2, V arg3) throws ShanoirException {
		try {
			try { 
				function.apply(arg1, arg2, arg3);
				fail("This should return an AccessDeniedException.");
			} catch (AccessDeniedException e) {}			
		} catch (Exception e) {
			fail(buildFailMsg(e));
		}
	}
	
	
	public static void assertAccessAuthorized(AccessCheckedFunction0Arg function) throws ShanoirException {
		try {
			try { 
				function.apply();
			} catch (AccessDeniedException e) {
				fail("This should not return an AccessDeniedException.");
			}			
		} catch (Exception e) {}
	}
	
	public static <T> void assertAccessAuthorized(AccessCheckedFunction1Arg<T> function, T arg) throws ShanoirException {
		try {
			try { 
				function.apply(arg);
			} catch (AccessDeniedException e) {
				fail("This should not return an AccessDeniedException.");
			}			
		} catch (Exception e) {}
	}
	
	public static <T, U> void assertAccessAuthorized(AccessCheckedFunction2Arg<T, U> function, T arg1, U arg2) throws ShanoirException {
		try {
			try { 
				function.apply(arg1, arg2);
			} catch (AccessDeniedException e) {
				fail("This should not return an AccessDeniedException.");
			}			
		} catch (Exception e) {}
	}
	
	public static <T, U, V> void assertAccessAuthorized(AccessCheckedFunction3Arg<T, U, V> function, T arg1, U arg2, V arg3) throws ShanoirException {
		try {
			try { 
				function.apply(arg1, arg2, arg3);
			} catch (AccessDeniedException e) {
				fail("This should not return an AccessDeniedException.");
			}			
		} catch (Exception e) {}
	}
	
	private static String buildFailMsg(Exception e) {
		return "This should return an AccessDeniedException but got a " 
				+ e.getClass().getSimpleName() 
				+ " at " 
				+ e.getStackTrace()[0].getFileName() 
				+ ":" 
				+ e.getStackTrace()[0].getLineNumber();
	}
}
