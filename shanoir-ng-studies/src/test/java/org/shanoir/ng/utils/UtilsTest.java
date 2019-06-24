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

package org.shanoir.ng.utils;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.model.Study;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * User security service test.
 * 
 * @author jlouis
 * 
 */
@RunWith(SpringRunner.class)
public class UtilsTest {
	
	@Test
	public void testCopyList() throws ShanoirException {

		List<Study> list = Arrays.asList(ModelsUtil.createStudy());
		List<Study> copiedList = Utils.copyList(list);

		Assert.assertTrue(copiedList.equals(list));
		Assert.assertNotNull(copiedList);
		Assert.assertTrue(copiedList.size() == list.size());
		Assert.assertTrue(list.get(0).getStudyCenterList().size() > 0);
		Assert.assertTrue(copiedList.get(0).getStudyCenterList().size() == list.get(0).getStudyCenterList().size());
	}
}
