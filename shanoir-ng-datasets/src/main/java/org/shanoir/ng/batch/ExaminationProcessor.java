package org.shanoir.ng.batch;

import org.shanoir.ng.examination.model.Examination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class ExaminationProcessor implements ItemProcessor<Examination, Examination> {

	private static final Logger LOG = LoggerFactory.getLogger(ExaminationProcessor.class);

	@Override
	public Examination process(Examination examination) throws Exception {
		LOG.info("MyBatchProcessor : Processing data : " + examination);
		examination.setComment(examination.getComment()+"asdf");
		return examination;
	}

}
