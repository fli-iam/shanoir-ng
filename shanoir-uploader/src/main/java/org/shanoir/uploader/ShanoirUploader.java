package org.shanoir.uploader;

import java.io.File;

import org.shanoir.uploader.action.init.StartupStateContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * This is the new version main class of the ShanoirUploader.
 * Introduced in Release 5.2
 *
 * @author atouboul
 * @author mkain
 *
 */
public class ShanoirUploader {

	public static ShUpOnloadConfig shUpOnloadConfig = ShUpOnloadConfig.getInstance();

	/**
	 * Main method, heart of the application.
	 *
	 * @param args
	 */
	public static void main(String args[]) {
		initShanoirUploaderFolders();
		ApplicationContext ctx = new AnnotationConfigApplicationContext(ShanoirUploaderSpringConfig.class);
		displayAllBeans(ctx);
		StartupStateContext sSC = ctx.getBean(StartupStateContext.class);
		sSC.configure();
		sSC.nextState();
	}

	/**
	 * Initialize personal properties folder + workFolder of ShanoirUploader.
	 */
	private static void initShanoirUploaderFolders() {
		final String userHomeFolderPath = System.getProperty(ShUpConfig.USER_HOME);
		final String shanoirUploaderFolderPath = userHomeFolderPath
				+ File.separator + ShUpConfig.SU + "_" + ShUpConfig.SHANOIR_UPLOADER_VERSION;
		final File shanoirUploaderFolder = new File(shanoirUploaderFolderPath);
		boolean shanoirUploaderFolderExists = shanoirUploaderFolder.exists();
		if (shanoirUploaderFolderExists) {
			// do nothing
		} else {
			shanoirUploaderFolder.mkdirs();
		}
		ShUpConfig.shanoirUploaderFolder = shanoirUploaderFolder;
		final File workFolder = new File(shanoirUploaderFolder + File.separator + ShUpConfig.WORK_FOLDER);
		if (workFolder.exists()) {
			// do nothing
		} else {
			workFolder.mkdirs();
		}
		shUpOnloadConfig.setWorkFolder(workFolder);
	}

	public static void displayAllBeans(ApplicationContext ctx) {
        String[] allBeanNames = ctx.getBeanDefinitionNames();
        for(String beanName : allBeanNames) {
            System.out.println(beanName);
        }
    }

}