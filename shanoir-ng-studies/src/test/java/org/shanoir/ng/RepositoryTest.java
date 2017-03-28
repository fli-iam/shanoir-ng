package org.shanoir.ng;

import static org.junit.Assert.*;

import java.util.List;

import org.shanoir.ng.study.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@DataJpaTest
@ActiveProfiles("test")
public class RepositoryTest {

	/*
	 * Mocks used to avoid unsatisfied dependency exceptions.
	 */
	@MockBean
	private AuthenticationManager authenticationManager;
	@MockBean
	private DocumentationPluginsBootstrapper documentationPluginsBootstrapper;
	@MockBean
	private WebMvcRequestHandlerProvider webMvcRequestHandlerProvider;
	
	@Autowired
	private StudyRepository studyRepository;
	@Autowired
	private RelStudyUserRepository relStudyUserRepository;
	
	@Test
	public void findOneTest() {
		Study s = studyRepository.findOne(Long.valueOf(1));
		assertEquals("shanoirStudy1", s.getName());
	}
	
	/**
	 * 
	 */
	@Test
	public void findStudiesByUserId() {
		List<RelStudyUser> s = relStudyUserRepository.findAllByUserId(Long.valueOf(1));
		assertEquals("shanoirStudy1", s.get(0).getStudy().getName());
	}
	
	@Test
	public void findAll() {
		List<Study> studyList = (List<Study>) studyRepository.findAll();
		assertEquals("shanoirStudy1", studyList.get(0).getName());
	}
	
	@Test
	public void create() {
		Study study=new Study();
		study.setName("StudyTest");
		  study.setId(Long.valueOf(4));
		Study newStudy = studyRepository.save(study);
		assertEquals("StudyTest", newStudy.getName());
	}
	
	@Test
	public void delete() {
		Long id=Long.valueOf(3);
		studyRepository.delete(id);
		//
		List<Study> studyList = (List<Study>) studyRepository.findAll();
		int c=studyList.size();
		assertEquals(2, c);
	}
	
	@Test
	public void update() {
		Study study=new Study();
		study.setName("StudyTest");
		study.setId(Long.valueOf(3));
		//
		final Study studyDb = studyRepository.findOne(study.getId());
		studyDb.setName(study.getName());
		studyDb.setEndDate(study.getEndDate());
		studyDb.setClinical(study.isClinical());
		studyDb.setWithExamination(study.isWithExamination());
		studyDb.setVisibleByDefault(study.isVisibleByDefault());
		studyDb.setDownloadableByDefault(study.isDownloadableByDefault());
		studyDb.setStudyStatus(study.getStudyStatus());

		studyRepository.save(studyDb);
		
		final Study studyFound = studyRepository.findOne(Long.valueOf(3));
		
		assertEquals("StudyTest", studyFound.getName());
	}
	
}
