package org.shanoir.ng.utils.assertion;

import static org.junit.Assert.fail;

import org.shanoir.ng.shared.exception.ShanoirException;
import org.springframework.security.access.AccessDeniedException;

public abstract class AssertUtils {
	
	
	public static void assertAccessDenied(AccessCheckedFunction0 function) throws ShanoirException {
		try { 
			function.apply();
			fail("This should return an AccessDeniedException.");
		} catch (AccessDeniedException e) {}
	}
	
	public static <T> void assertAccessDenied(AccessCheckedFunction1Arg<T> function, T arg) throws ShanoirException {
		try { 
			function.apply(arg);
			fail("This should return an AccessDeniedException.");
		} catch (AccessDeniedException e) {}
	}
	
	public static <T, U> void assertAccessDenied(AccessCheckedFunction2Arg<T, U> function, T arg1, U arg2) throws ShanoirException {
		try { 
			function.apply(arg1, arg2);
			fail("This should return an AccessDeniedException.");
		} catch (AccessDeniedException e) {}
	}
	
	
	public static void assertAccessAuthorized(AccessCheckedFunction0 function) throws ShanoirException {
		try { 
			function.apply();
		} catch (AccessDeniedException e) {
			fail("This should not return an AccessDeniedException.");
		}
	}
	
	public static <T> void assertAccessAuthorized(AccessCheckedFunction1Arg<T> function, T arg) throws ShanoirException {
		try { 
			function.apply(arg);
		} catch (AccessDeniedException e) {
			fail("This should not return an AccessDeniedException.");
		}
	}
	
	public static <T, U> void assertAccessAuthorized(AccessCheckedFunction2Arg<T, U> function, T arg1, U arg2) throws ShanoirException {
		try { 
			function.apply(arg1, arg2);
		} catch (AccessDeniedException e) {
			fail("This should not return an AccessDeniedException.");
		}
	}

}
