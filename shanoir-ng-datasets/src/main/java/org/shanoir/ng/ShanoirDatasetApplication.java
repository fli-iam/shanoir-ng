package org.shanoir.ng;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.shanoir.ng.shared.paging.PageSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageImpl;

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

	@Bean
	public Module jacksonPageWithJsonViewModule() {
		SimpleModule module = new SimpleModule("jackson-page-with-jsonview", Version.unknownVersion());
		module.addSerializer(PageImpl.class, new PageSerializer());
		return module;
	}

}