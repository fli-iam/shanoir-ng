package org.shanoir.ng.accessrequest.controller;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.accessrequest.model.AccessRequest;
import org.shanoir.ng.accessrequest.repository.AccessRequestRepository;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccessRequestServiceImpl implements AccessRequestService {

	@Autowired
	AccessRequestRepository accessRequestRepository;
	
	@Override
	public Optional<AccessRequest> findById(Long id) {
		return this.accessRequestRepository.findById(id);
	}

	@Override
	public List<AccessRequest> findAll() {
		return Utils.toList(this.accessRequestRepository.findAll());
	}

	@Override
	public AccessRequest create(AccessRequest entity) {
		return this.accessRequestRepository.save(entity);
	}

	@Override
	public AccessRequest update(AccessRequest entity) throws EntityNotFoundException {
		return this.accessRequestRepository.save(entity);
	}

	@Override
	public void deleteById(Long id) throws EntityNotFoundException {
		this.accessRequestRepository.deleteById(id);
	}

	@Override
	public List<AccessRequest> findByStudyId(List<Long> studiesId) {
		return this.accessRequestRepository.findByStudyIdIn(studiesId);
	}

}
