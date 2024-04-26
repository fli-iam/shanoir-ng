package org.shanoir.uploader;

import org.shanoir.uploader.action.init.StartupStateContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * This is the new version main class of the ShanoirUploader.
 * Introduced in Release 5.2
 * 
 * @author atouboul
 * 
 */
public class ShanoirUploader {

	/**
	 * Main method, heart of the application.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		ApplicationContext ctx = new AnnotationConfigApplicationContext(ShanoirUploaderSpringConfig.class);
		StartupStateContext sSC = new StartupStateContext();
		sSC.nextState();
	}

}