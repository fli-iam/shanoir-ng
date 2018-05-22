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

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-03-23T14:51:04.625Z")

@Configuration
public class SwaggerDocumentationConfig {

	public static final String AUTH_TOKEN_NAME = "x-auth-token";

	ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("Preclinical").description("Preclinical microservice for shanoir-ng")
				.license("").licenseUrl("http://unlicense.org").termsOfServiceUrl("").version("1.0.0")
				.contact(new Contact("", "", "")).build();
	}

	@Bean
	public Docket customImplementation() {

		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.add(new ParameterBuilder().name(SwaggerDocumentationConfig.AUTH_TOKEN_NAME)
				.description("Authentification token").modelRef(new ModelRef("string")).parameterType("header")
				.required(false).build());

		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("org.shanoir.ng.preclinical.controller.rest")).build()
				.directModelSubstitute(org.joda.time.LocalDate.class, java.sql.Date.class)
				.directModelSubstitute(org.joda.time.DateTime.class, java.util.Date.class).apiInfo(apiInfo())
				.useDefaultResponseMessages(false).globalOperationParameters(parameters);
		// .pathMapping("/shanoir-ng/users");
	}

}
