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

package org.shanoir.ng.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author sloury
 *
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "preclinical")
public class ShanoirPreclinicalConfiguration {

	private String uploadExtradataFolder;
	private String separator;
	private String uploadBrukerFolder;

	public String getUploadExtradataFolder() {
		if (!uploadExtradataFolder.endsWith(separator))
			return uploadExtradataFolder.concat(separator);
		return uploadExtradataFolder;
	}

	public void setUploadExtradataFolder(String uploadExtradataFolder) {
		this.uploadExtradataFolder = uploadExtradataFolder;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getUploadBrukerFolder() {
		return uploadBrukerFolder;
	}

	public void setUploadBrukerFolder(String uploadBrukerFolder) {
		this.uploadBrukerFolder = uploadBrukerFolder;
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

}
