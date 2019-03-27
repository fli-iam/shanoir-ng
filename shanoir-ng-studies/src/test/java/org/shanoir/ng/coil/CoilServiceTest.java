package org.shanoir.ng.coil;

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
import org.shanoir.ng.coil.dto.mapper.CoilMapper;
import org.shanoir.ng.coil.model.Coil;
import org.shanoir.ng.coil.repository.CoilRepository;
import org.shanoir.ng.coil.service.CoilServiceImpl;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Coil service test.
 * 
 * @author msimon
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class CoilServiceTest {

	private static final Long COIL_ID = 1L;
	private static final String UPDATED_COIL_NAME = "test";

	@Mock
	private CoilMapper coilMapper;

	@Mock
	private CoilRepository coilRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private CoilServiceImpl coilService;

	@Before
	public void setup() {
		given(coilRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createCoil()));
		given(coilRepository.findOne(COIL_ID)).willReturn(ModelsUtil.createCoil());
		given(coilRepository.save(Mockito.any(Coil.class))).willReturn(ModelsUtil.createCoil());
	}

	@Test(expected=EntityNotFoundException.class)
	public void deleteByBadIdTest() throws EntityNotFoundException {
		coilService.deleteById(2L);
	}
	
	@Test
	public void deleteByIdTest() throws EntityNotFoundException {
		coilService.deleteById(COIL_ID);

		Mockito.verify(coilRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findAllTest() {
		final List<Coil> coils = coilService.findAll();
		Assert.assertNotNull(coils);
		Assert.assertTrue(coils.size() == 1);

		Mockito.verify(coilRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final Coil coil = coilService.findById(COIL_ID);
		Assert.assertNotNull(coil);
		Assert.assertTrue(ModelsUtil.COIL_NAME.equals(coil.getName()));

		Mockito.verify(coilRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void saveTest() {
		coilService.create(createCoil());

		Mockito.verify(coilRepository, Mockito.times(1)).save(Mockito.any(Coil.class));
	}

	@Test
	public void updateTest() throws EntityNotFoundException {
		final Coil coil = createCoil();
		final Coil updatedCoil = coilService.update(coil);
		Assert.assertNotNull(updatedCoil);
		Assert.assertTrue(UPDATED_COIL_NAME.equals(coil.getName()));

		Mockito.verify(coilRepository, Mockito.times(1)).save(Mockito.any(Coil.class));
	}

	private Coil createCoil() {
		final Coil coil = new Coil();
		coil.setId(COIL_ID);
		coil.setName(UPDATED_COIL_NAME);
		return coil;
	}

}
