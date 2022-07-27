package org.shanoir.ng.batch;

import java.util.List;

import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExaminationWriter implements ItemWriter<Examination> {
	
	private static final Logger LOG = LoggerFactory.getLogger(ExaminationWriter.class);
	
	@Autowired
	ExaminationRepository examinationRepository;
	
	@Override
	public void write(List<? extends Examination> list) throws Exception {
		for (Examination data : list) {
			LOG.info("ExaminationWriter    : Writing data    : " + data.getId()+" : "+data.getComment());
			examinationRepository.save(data);
		}
	}

}
