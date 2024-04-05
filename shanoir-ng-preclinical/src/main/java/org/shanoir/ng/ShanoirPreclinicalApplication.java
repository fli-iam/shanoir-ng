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

package org.shanoir.ng;

import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;

/**
 * Shanoir-NG microservice preclinical application.
 */
@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(title = "Shanoir preclinical API"),
		servers = @Server(url = "/shanoir-ng/preclinical", description = "Preclinical"))
public class ShanoirPreclinicalApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShanoirPreclinicalApplication.class, args);
	}

}