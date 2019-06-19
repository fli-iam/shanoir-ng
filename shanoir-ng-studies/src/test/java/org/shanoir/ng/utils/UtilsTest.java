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
