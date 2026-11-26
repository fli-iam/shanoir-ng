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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * Datasets microservice.
 */
@SpringBootApplication
@EnableSpringDataWebSupport
@EnableScheduling
@OpenAPIDefinition(
        info = @Info(title = "Shanoir datasets API", version = "1.0"),
        servers = @Server(url = "/shanoir-ng/datasets", description = "Datasets"),
        security = { @SecurityRequirement(name = "BearerAuth"), @SecurityRequirement(name = "OAuth2Auth") }
)
public class ShanoirDatasetApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShanoirDatasetApplication.class, args);
    }

}
