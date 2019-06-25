/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.template;

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
import org.shanoir.ng.shared.exception.ShanoirTemplateException;
import org.shanoir.ng.template.Template;
import org.shanoir.ng.template.TemplateRepository;
import org.shanoir.ng.template.TemplateServiceImpl;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Template service test.
 * 
 * @author msimon
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class TemplateServiceTest {

	private static final Long TEMPLATE_ID = 1L;
	private static final String UPDATED_TEMPLATE_DATA = "test";

	@Mock
	private TemplateRepository templateRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private TemplateServiceImpl templateService;

	@Before
	public void setup() {
		given(templateRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createTemplate()));
		given(templateRepository.findOne(TEMPLATE_ID)).willReturn(ModelsUtil.createTemplate());
		given(templateRepository.save(Mockito.any(Template.class))).willReturn(ModelsUtil.createTemplate());
	}

	@Test
	public void deleteByIdTest() throws ShanoirTemplateException {
		templateService.deleteById(TEMPLATE_ID);

		Mockito.verify(templateRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	public void findAllTest() {
		final List<Template> templates = templateService.findAll();
		Assert.assertNotNull(templates);
		Assert.assertTrue(templates.size() == 1);

		Mockito.verify(templateRepository, Mockito.times(1)).findAll();
	}

	@Test
	public void findByIdTest() {
		final Template template = templateService.findById(TEMPLATE_ID);
		Assert.assertNotNull(template);
		Assert.assertTrue(ModelsUtil.TEMPLATE_DATA.equals(template.getData()));

		Mockito.verify(templateRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	public void saveTest() throws ShanoirTemplateException {
		templateService.save(createTemplate());

		Mockito.verify(templateRepository, Mockito.times(1)).save(Mockito.any(Template.class));
	}

	@Test
	public void updateTest() throws ShanoirTemplateException {
		final Template updatedTemplate = templateService.update(createTemplate());
		Assert.assertNotNull(updatedTemplate);
		Assert.assertTrue(UPDATED_TEMPLATE_DATA.equals(updatedTemplate.getData()));

		Mockito.verify(templateRepository, Mockito.times(1)).save(Mockito.any(Template.class));
	}

	@Test
	public void updateFromShanoirOldTest() throws ShanoirTemplateException {
		templateService.updateFromShanoirOld(createTemplate());

		Mockito.verify(templateRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(templateRepository, Mockito.times(1)).save(Mockito.any(Template.class));
	}

	private Template createTemplate() {
		final Template template = new Template();
		template.setId(TEMPLATE_ID);
		template.setData(UPDATED_TEMPLATE_DATA);
		return template;
	}

}
