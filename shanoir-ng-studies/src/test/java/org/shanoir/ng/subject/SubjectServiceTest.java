package org.shanoir.ng.subject;

import static org.mockito.BDDMockito.given;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.shared.exception.ShanoirSubjectException;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Subject service test.
 *
 * @author msimon
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SubjectServiceTest {

	private static final Long SUBJECT_ID = 1L;
	private static final String UPDATED_SUBJECT_DATA = "subject1";
	private static final String JSON_FILE_PATH = "C:/Users/ifakhfak/Documents/Shanoir NG/Study microService/SUBJECT/jsonTest2.json";

	@Mock
	private SubjectRepository subjectRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private SubjectServiceImpl subjectService;

	@Before
	public void setup() {
		given(subjectRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createSubject()));
		given(subjectRepository.findOne(SUBJECT_ID)).willReturn(ModelsUtil.createSubject());
		//given(subjectRepository.save(Mockito.any(Subject.class))).willReturn(ModelsUtil.createSubject());
		given(subjectRepository.save(Mockito.any(Subject.class))).willReturn(createSubjectTosave());

	}

	@Test
	public void deleteByIdTest() throws ShanoirSubjectException {
		subjectService.deleteById(SUBJECT_ID);

		Mockito.verify(subjectRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findAllTest() {
		final List<Subject> subjects = subjectService.findAll();
		Assert.assertNotNull(subjects);
		Assert.assertTrue(subjects.size() == 1);

		Mockito.verify(subjectRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final Subject subject = subjectService.findById(SUBJECT_ID);
		Assert.assertNotNull(subject);
		Assert.assertTrue(ModelsUtil.SUBJECT_NAME.equals(subject.getName()));

		Mockito.verify(subjectRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void saveTest() throws ShanoirSubjectException {
		subjectService.save(createSubjectTosave());
		Mockito.verify(subjectRepository, Mockito.times(1)).save(Mockito.any(Subject.class));
	}

	@Test
	public void saveJsonTest() throws ShanoirSubjectException {
		subjectService.save(createJsonSubjectTosave());
		Mockito.verify(subjectRepository, Mockito.times(1)).save(Mockito.any(Subject.class));
	}

	@Test
	public void updateTest() throws ShanoirSubjectException {
		final Subject updatedSubject = subjectService.update(createSubjectToUpdate());
		Assert.assertNotNull(updatedSubject);
		Assert.assertTrue(UPDATED_SUBJECT_DATA.equals(updatedSubject.getName()));

		Mockito.verify(subjectRepository, Mockito.times(1)).save(Mockito.any(Subject.class));
	}

	@Test
	public void updateFromShanoirOldTest() throws ShanoirSubjectException {
		subjectService.updateFromShanoirOld(createSubjectToUpdate());

		Mockito.verify(subjectRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(subjectRepository, Mockito.times(1)).save(Mockito.any(Subject.class));
	}

	private Subject createSubjectToUpdate() {
		final Subject subject = new Subject();
		subject.setId(SUBJECT_ID);
		subject.setName(UPDATED_SUBJECT_DATA);
		return subject;
	}

	private Subject createSubjectTosave() {
		final Subject subject = new Subject();
		//subject.setName("Toto");
		//subject.setId(1L);
		subject.setBirthDate(new Date(1392122691000L));
		subject.setIdentifier("Titi");

		subject.setImagedObjectCategory(ImagedObjectCategory.PHANTOM);
		subject.setLanguageHemisphericDominance(HemisphericDominance.Left);
		subject.setManualHemisphericDominance(HemisphericDominance.Left);
		PseudonymusHashValues pseudonymusHashValues= new PseudonymusHashValues();
		pseudonymusHashValues.setBirthDateHash("uihuizdhuih");
		subject.setPseudonymusHashValues(pseudonymusHashValues);
		subject.setSex(Sex.F);
		UserPersonalCommentSubject userPersonalCommentList1= new UserPersonalCommentSubject();
		userPersonalCommentList1.setComment("comment1");
		UserPersonalCommentSubject userPersonalCommentList2= new UserPersonalCommentSubject();
		userPersonalCommentList1.setComment("comment2");
		List<UserPersonalCommentSubject> listSubjectComments = new ArrayList<>();
		listSubjectComments.add(userPersonalCommentList1);
		listSubjectComments.add(userPersonalCommentList2);
		subject.setUserPersonalCommentList(listSubjectComments);
		return subject;
	}


	private Subject createJsonSubjectTosave() {
		ObjectMapper mapper = new ObjectMapper();
		Subject subject=new Subject();
		try {
			subject = mapper.readValue(new File(JSON_FILE_PATH), Subject.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return subject;

	}

}
