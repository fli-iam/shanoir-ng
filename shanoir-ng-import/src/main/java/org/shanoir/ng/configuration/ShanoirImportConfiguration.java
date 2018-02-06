package org.shanoir.ng.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * As Spring Boot >= 1.4 does not automatically provide a RestTemplate
 * we have to pass by the builder and init it here for the entire app.
 * 
 * @author mkain
 */
@Configuration
public class ShanoirImportConfiguration {

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

}