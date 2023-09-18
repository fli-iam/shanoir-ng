package org.shanoir.ng;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;


/**
 * Shanoir-NG microservice nifti-conversion application.
 */
@OpenAPIDefinition
@EnableWebMvc
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class,HibernateJpaAutoConfiguration.class})
public class NiftiConversionApplication {

	public static void main(String[] args) {
		SpringApplication.run(NiftiConversionApplication.class, args);
	}
}