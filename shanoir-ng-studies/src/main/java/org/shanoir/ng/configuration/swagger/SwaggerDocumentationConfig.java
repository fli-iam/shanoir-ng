/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.configuration.swagger;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2017-01-18T15:36:13.002Z")

@Configuration
public class SwaggerDocumentationConfig {

	/** The name of the authentication token parameter. */
	public static final String XSRF_TOKEN_NAME = "X-XSRF-TOKEN";
	public static final String AUTH_TOKEN_NAME = "x-auth-token";

	ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("Shanoir NG Studies Microservice")
				.description("Microservice API for Shanoir NG study managment").license("").licenseUrl("")
				.termsOfServiceUrl("").version("0.0.1").contact(new Contact("", "", "")).build();
	}

	@Bean
	public Docket customImplementation() {

		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.add(new ParameterBuilder()
	            .name(SwaggerDocumentationConfig.AUTH_TOKEN_NAME)
	            .description("Authentification token")
	            .modelRef(new ModelRef("string"))
	            .parameterType("header")
	            .required(false)
	            .build());

		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("org.shanoir.ng.study.controller.rest")).build()
				.directModelSubstitute(org.joda.time.LocalDate.class, java.sql.Date.class)
				.directModelSubstitute(org.joda.time.DateTime.class, java.util.Date.class)
				.apiInfo(apiInfo())
				.useDefaultResponseMessages(false)
				.globalOperationParameters(parameters);
				//.pathMapping("/shanoir-ng/users");
	}

}
