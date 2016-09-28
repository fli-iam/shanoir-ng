package org.shanoir.challengeScores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Application main class.
 *
 * @author jlouis
 */
@SpringBootApplication
@EnableSwagger2
@ComponentScan(basePackages = {"io.swagger", "org.shanoir.challengeScores"})
public class ShanoirMicroserviceApplication {

	@Autowired
	JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		SpringApplication.run(ShanoirMicroserviceApplication.class, args);
	}

}
