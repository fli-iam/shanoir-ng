package org.shanoir.ng.service.impl;

import java.util.List;

import org.shanoir.ng.model.Role;
import org.shanoir.ng.service.RoleService;
import org.shanoir.ng.service.repository.RoleRepository;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	private RoleRepository roleRepository;

	@Override
	public List<Role> findAll() {
		return Utils.toList(roleRepository.findAll());
	}

}
