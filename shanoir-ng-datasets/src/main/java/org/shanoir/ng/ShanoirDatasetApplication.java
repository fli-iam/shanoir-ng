package org.shanoir.ng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Shanoir application.
 *
 * @author msimon
 *
 */
@SpringBootApplication
@EnableSwagger2
public class ShanoirDatasetApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShanoirDatasetApplication.class, args);
	}

}