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
    public static void main(String[] args) {
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
    public static void initShanoirUploaderFolders() {
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
        for (String beanName : allBeanNames) {
            System.out.println(beanName);
        }
    }

}
