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
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Shanoir-NG microservice users application.
 * MK: @EnableSwagger2 removed explicitly - only for ms users - to protect the best the interface.
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableScheduling
public class ShanoirUsersApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShanoirUsersApplication.class, args);
	}

}