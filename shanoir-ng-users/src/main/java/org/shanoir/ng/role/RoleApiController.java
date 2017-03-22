package org.shanoir.ng.role;

import java.util.List;

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
