package org.shanoir.ng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;

import springfox.documentation.swagger2.annotations.EnableSwagger2;
/**
 *
 * @author ifakhfak
 *
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
@EnableSwagger2
public class ShanoirStudiesApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShanoirStudiesApplication.class, args);
	}
}
