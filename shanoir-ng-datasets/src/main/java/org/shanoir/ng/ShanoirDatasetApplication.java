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

package org.shanoir.ng;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.shanoir.ng.shared.paging.PageSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Shanoir application.
 *
 * @author msimon
 *
 */
@SpringBootApplication
@EnableSwagger2
@EnableSpringDataWebSupport
@EnableScheduling
public class ShanoirDatasetApplication {

	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
	private static final String BOUTIQUES = "boutiques";
	private static final String INPUT = "input";
	private static final String OUTPUT = "output";
	
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    static final Runnable fileCleaner = new Runnable() {
        public void run() {
        	String tmp = System.getProperty(JAVA_IO_TMPDIR);
        	String boutiquesInput = tmp + File.separator + BOUTIQUES + File.separator + INPUT; 
        	String boutiquesOutput = tmp + File.separator + BOUTIQUES + File.separator + OUTPUT; 
        	 
        	String[] directories = {tmp, boutiquesInput, boutiquesOutput};
        	for(String directory: directories) {
        		File tmpDir = new File(directory);
        		File[] files = tmpDir.listFiles();
        		long now = System.currentTimeMillis();
        	    if(files != null) {
        	        for(File f: files) {
        	            if(now - f.lastModified() > Duration.ofDays(7).toMillis()) {
        	            	f.delete();
        	            }
        	        }
        	    }
        	}

        }
    };
    
	public static void main(String[] args) {
		SpringApplication.run(ShanoirDatasetApplication.class, args);
        scheduler.scheduleAtFixedRate(fileCleaner, 7, 7, TimeUnit.DAYS);
	}

	@Bean
	public Module jacksonPageWithJsonViewModule() {
		SimpleModule module = new SimpleModule("jackson-page-with-jsonview", Version.unknownVersion());
		module.addSerializer(PageImpl.class, new PageSerializer());
		return module;
	}
}