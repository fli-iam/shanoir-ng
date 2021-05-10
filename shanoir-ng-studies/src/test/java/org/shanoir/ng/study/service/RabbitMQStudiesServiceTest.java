package org.shanoir.ng.study.service;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.study.repository.StudyRepository;

@RunWith(MockitoJUnitRunner.class)
public class RabbitMQStudiesServiceTest {

	@Mock
	private StudyRepository studyRepo;

	@InjectMocks
	private RabbitMQStudiesService rabbitMQStudiesService;
	
}
