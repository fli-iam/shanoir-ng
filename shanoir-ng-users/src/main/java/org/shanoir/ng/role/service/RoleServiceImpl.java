package org.shanoir.ng.role.service;

import java.util.List;

import org.shanoir.ng.role.model.Role;
import org.shanoir.ng.role.repository.RoleRepository;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Role service implementation.
 *
 * @author jlouis
 *
 */
@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	private RoleRepository roleRepository;

	@Override
	public List<Role> findAll() {
		return Utils.toList(roleRepository.findAll());
	}

	@Override
	public Role findByName(String name) {
		return roleRepository.findByName(name);
	}

}
