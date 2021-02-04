package org.shanoir.ng.study.dua;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataUserAgreementService {

	@Autowired
	DataUserAgreementRepository repository;

	private static final Logger LOG = LoggerFactory.getLogger(DataUserAgreementService.class);

}
