package org.shanoir.ng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Shanoir application.
 *
 * @author msimon
 *
 */
@SpringBootApplication
@EnableSwagger2
@EnableScheduling
public class ShanoirUsersApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShanoirUsersApplication.class, args);
	}

}