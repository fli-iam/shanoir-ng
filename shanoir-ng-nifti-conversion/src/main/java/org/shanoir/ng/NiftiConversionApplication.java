package org.shanoir.ng;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Shanoir-NG microservice nifti-conversion application.
 */
@EnableSwagger2
@EnableWebMvc
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class NiftiConversionApplication {

	public static void main(String[] args) {
		SpringApplication.run(NiftiConversionApplication.class, args);
	}
}