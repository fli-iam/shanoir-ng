package org.shanoir.ng.controller.rest;

import java.util.List;

import org.shanoir.ng.model.Role;
import org.shanoir.ng.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class RoleApiController implements RoleApi {

	private static final Logger LOG = LoggerFactory.getLogger(RoleApiController.class);

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
