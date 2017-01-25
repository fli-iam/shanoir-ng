package org.shanoir.ng.study;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import springfox.documentation.swagger2.annotations.EnableSwagger2;
/**
 * 
 * @author ifakhfak
 *
 */
@SpringBootApplication
@EnableSwagger2
public class ShanoirNgStudiesApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShanoirNgStudiesApplication.class, args);
	}
}
