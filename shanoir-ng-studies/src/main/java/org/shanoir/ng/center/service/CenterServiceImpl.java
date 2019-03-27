package org.shanoir.ng.center.service;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.shared.core.service.BasicEntityServiceImpl;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.error.FieldError;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.UndeletableDependenciesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * center service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class CenterServiceImpl extends BasicEntityServiceImpl<Center> implements CenterService {

	@Autowired
	private CenterRepository centerRepository;


	@Override
	public void deleteByIdCheckDependencies(final Long id) throws EntityNotFoundException, UndeletableDependenciesException {
		final Center center = centerRepository.findOne(id);
		if (center == null) {
			throw new EntityNotFoundException(Center.class, id);
		}
		final List<FieldError> errors = new ArrayList<FieldError>();
		if (!center.getAcquisitionEquipments().isEmpty()) {
			errors.add(new FieldError("unauthorized", "Center linked to entities", "acquisitionEquipments"));
		}
		if (!center.getStudyCenterList().isEmpty()) {
			errors.add(new FieldError("unauthorized", "Center linked to entities", "studies"));
		}
		if (!errors.isEmpty()) {
			final FieldErrorMap errorMap = new FieldErrorMap();
			errorMap.put("delete", errors);
			throw new UndeletableDependenciesException(errorMap);
		}
		centerRepository.delete(id);
	}

	@Override
	public List<IdNameDTO> findIdsAndNames() {
		return centerRepository.findIdsAndNames();
	}

	@Override
	protected Center updateValues(final Center from, final Center to) {
		to.setCity(from.getCity());
		to.setCountry(from.getCountry());
		to.setName(from.getName());
		to.setPhoneNumber(from.getPhoneNumber());
		to.setPostalCode(from.getPostalCode());
		to.setStreet(from.getStreet());
		to.setWebsite(from.getWebsite());
		return to;
	}

	@Override
	public Center findByName(String name) {
		return centerRepository.findByName(name);
	}

	@Override
	public List<Center> findBy(String fieldName, Object value) {
		return this.findBy(fieldName, value, Center.class);
	}

}
