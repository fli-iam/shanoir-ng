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

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

}
