package org.shanoir.ng.configuration.batch;

import org.shanoir.ng.batch.ExaminationProcessor;
import org.shanoir.ng.batch.ExaminationReader;
import org.shanoir.ng.batch.ExaminationWriter;
import org.shanoir.ng.examination.model.Examination;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	ExaminationReader examinationReader;

	@Autowired
	ExaminationWriter examinationWriter;

	@Autowired
	ExaminationProcessor examinationProcessor;

	@Bean
	public Job createJob() {
		return jobBuilderFactory
				.get("MyJob")
				.incrementer(new RunIdIncrementer())
				.flow(createStep())
				.end()
				.build();
	}

	@Bean
	public Step createStep() {
		return stepBuilderFactory.get("MyStep")
				.<Examination, Examination>chunk(100)
				.reader(examinationReader)
				.processor(examinationProcessor)
				.writer(examinationWriter)
				.build();
	}

}