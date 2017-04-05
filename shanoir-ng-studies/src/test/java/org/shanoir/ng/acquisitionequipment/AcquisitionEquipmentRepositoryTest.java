package org.shanoir.ng.acquisitionequipment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Tests for repository 'acquisition equipment'.
 * 
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("test")
public class AcquisitionEquipmentRepositoryTest {

	private static final Long ACQ_EQPT_TEST_1_ID = 1L;
	private static final String ACQ_EQPT_TEST_1_SERIAL_NUMBER = "123456789";
	
	@Autowired
	private AcquisitionEquipmentRepository repository;
	
	/*
	 * Mocks used to avoid unsatisfied dependency exceptions.
	 */
	@MockBean
	private AuthenticationManager authenticationManager;
	@MockBean
	private DocumentationPluginsBootstrapper documentationPluginsBootstrapper;
	@MockBean
	private WebMvcRequestHandlerProvider webMvcRequestHandlerProvider;
	
	@Test
	public void findAllTest() throws Exception {
		Iterable<AcquisitionEquipment> equipmentsDb = repository.findAll();
		assertThat(equipmentsDb).isNotNull();
		int nbEquipments = 0;
		Iterator<AcquisitionEquipment> equipmentsIt = equipmentsDb.iterator();
		while (equipmentsIt.hasNext()) {
			equipmentsIt.next();
			nbEquipments++;
		}
		assertThat(nbEquipments).isEqualTo(3);
	}
	
	@Test
	public void findOneTest() throws Exception {
		AcquisitionEquipment equipmentDb = repository.findOne(ACQ_EQPT_TEST_1_ID);
		assertThat(equipmentDb).isNotNull();
		assertThat(equipmentDb.getSerialNumber()).isEqualTo(ACQ_EQPT_TEST_1_SERIAL_NUMBER);
	}
	
}
