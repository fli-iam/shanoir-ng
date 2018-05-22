package org.shanoir.ng.preclinical.references;

import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.utils.ReferenceModelUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * References service test.
 * 
 * @author sloury
 */
 
@RunWith(MockitoJUnitRunner.class)
public class ReferenceServiceTest {

	private static final Long REFERENCE_ID = 1L;
	private static final String UPDATED_REFERENCE_VALUE = "monkey";

	@Mock
	private RefsRepository refsRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private RefsServiceImpl referenceService;

	@Before
	public void setup() {
		given(refsRepository.findAll()).willReturn(Arrays.asList(ReferenceModelUtil.createReferenceSpecie()));
		given(refsRepository.findOne(REFERENCE_ID)).willReturn(ReferenceModelUtil.createReferenceSpecie());
		given(refsRepository.save(Mockito.any(Reference.class))).willReturn(ReferenceModelUtil.createReferenceSpecie());
	}

	@Test
	public void deleteByIdTest() throws ShanoirPreclinicalException {
		referenceService.deleteById(REFERENCE_ID);

		Mockito.verify(refsRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findAllTest() {
		final List<Reference> references = referenceService.findAll();
		Assert.assertNotNull(references);
		Assert.assertTrue(references.size() == 1);

		Mockito.verify(refsRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final Reference reference = referenceService.findById(REFERENCE_ID);
		Assert.assertNotNull(reference);
		Assert.assertTrue(ReferenceModelUtil.REFERENCE_CATEGORY.equals(reference.getCategory()));
		Assert.assertTrue(ReferenceModelUtil.REFERENCE_TYPE.equals(reference.getReftype()));
		Assert.assertTrue(ReferenceModelUtil.REFERENCE_VALUE.equals(reference.getValue()));

		Mockito.verify(refsRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void saveTest() throws ShanoirPreclinicalException {
		referenceService.save(createReference());

		Mockito.verify(refsRepository, Mockito.times(1)).save(Mockito.any(Reference.class));
	}

	@Test
	public void updateTest() throws ShanoirPreclinicalException {
		final Reference updatedRef = referenceService.update(createReference());
		Assert.assertNotNull(updatedRef);
		Assert.assertTrue(UPDATED_REFERENCE_VALUE.equals(updatedRef.getValue()));

		Mockito.verify(refsRepository, Mockito.times(1)).save(Mockito.any(Reference.class));
	}

	/*
	@Test
	public void updateFromShanoirOldTest() throws ShanoirTemplateException {
		templateService.updateFromShanoirOld(createTemplate());

		Mockito.verify(refsRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(refsRepository, Mockito.times(1)).save(Mockito.any(Template.class));
	}
	*/

	private Reference createReference() {
		final Reference ref = new Reference();
		ref.setId(REFERENCE_ID);
		ref.setValue(UPDATED_REFERENCE_VALUE);
		return ref;
	}

}
