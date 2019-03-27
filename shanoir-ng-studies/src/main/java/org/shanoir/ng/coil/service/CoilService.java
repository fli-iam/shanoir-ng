package org.shanoir.ng.coil.service;

import java.util.Optional;

import org.shanoir.ng.coil.model.Coil;
import org.shanoir.ng.shared.core.service.BasicEntityService;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Coil service.
 *
 * @author msimon
 *
 */
public interface CoilService extends BasicEntityService<Coil> {

	/**
	 * Find coil by name.
	 *
	 * @param name name.
	 * @return a coil.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	Optional<Coil> findByName(String name);

}
