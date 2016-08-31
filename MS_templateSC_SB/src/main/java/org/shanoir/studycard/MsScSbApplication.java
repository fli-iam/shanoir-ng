package org.shanoir.studycard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Project application.
 * 
 * @author msimon
 *
 */
@SpringBootApplication
public class MsScSbApplication {

	@Autowired
	JdbcTemplate jdbcTemplate;
	
	public static void main(String[] args) {
		SpringApplication.run(MsScSbApplication.class, args);
	}
	
}
