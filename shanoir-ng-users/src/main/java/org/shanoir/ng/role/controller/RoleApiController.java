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

package org.shanoir.ng.role.controller;

import java.util.List;

import org.shanoir.ng.role.model.Role;
import org.shanoir.ng.role.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class RoleApiController implements RoleApi {

	@Autowired
	private RoleService roleService;

	@Override
	public ResponseEntity<List<Role>> findRoles() {
		List<Role> roles = roleService.findAll();
		if (roles.isEmpty()) {
			return new ResponseEntity<List<Role>>(HttpStatus.NO_CONTENT);
		} else {
			return new ResponseEntity<List<Role>>(roles, HttpStatus.OK);
		}
	}

}
