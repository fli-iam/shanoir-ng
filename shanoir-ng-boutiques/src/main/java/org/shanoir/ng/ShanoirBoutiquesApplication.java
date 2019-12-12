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

import org.shanoir.ng.boutiques.BoutiquesUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Shanoir application.
 *
 * @author ArthurMasson
 *
 */
@SpringBootApplication
@EnableSwagger2
public class ShanoirBoutiquesApplication {
        
	public static void main(String[] args) {
		SpringApplication.run(ShanoirBoutiquesApplication.class, args);
        // Retrieve all tool descriptors (run `bosh.py search -m 1000`) to store them in ~/.cache/boutiques/zenodo-ID.json files
        BoutiquesUtils.updateToolDatabase();
	}

}