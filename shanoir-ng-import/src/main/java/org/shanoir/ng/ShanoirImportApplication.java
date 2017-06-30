package org.shanoir.ng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Shanoir application.
 *
 * @author ifakhfakh
 *
 */
@SpringBootApplication
@EnableSwagger2
public class ShanoirImportApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShanoirImportApplication.class, args);
	}

}