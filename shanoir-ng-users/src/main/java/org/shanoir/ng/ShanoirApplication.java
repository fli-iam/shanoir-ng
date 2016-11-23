package org.shanoir.ng;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Shanoir application.
 *
 * @author msimon
 *
 */
@SpringBootApplication
@EnableSwagger2
@ComponentScan(basePackages = {"org.shanoir.ng"})

public class ShanoirApplication {

	@Autowired
	JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		SpringApplication.run(ShanoirApplication.class, args);
	}

}