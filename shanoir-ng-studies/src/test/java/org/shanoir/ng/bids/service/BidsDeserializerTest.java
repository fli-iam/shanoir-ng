package org.shanoir.ng.bids.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.shanoir.ng.bids.model.BidsElement;
import org.shanoir.ng.bids.model.BidsFile;
import org.shanoir.ng.bids.model.BidsFolder;
import org.shanoir.ng.bids.utils.BidsDeserializer;

import com.google.common.base.Charsets;

/**
 * Test class for deserializer.
 * @author fli
 *
 */
public class BidsDeserializerTest {

	@Rule
    public TemporaryFolder folder = new TemporaryFolder();

	BidsDeserializer deserializer = new BidsDeserializer();

	@Test
	public void testDeserialize() throws IOException {
		// GIVEN a file structure following a bids structure
		File readmeFile = folder.newFile("README");
		FileUtils.write(readmeFile, "blabla", Charsets.UTF_8);

		File subjectFolder = folder.newFolder("sub-11");
		File sesFile = new File(subjectFolder.getAbsolutePath() + File.separator + "ses-15");
		sesFile.mkdir();
		File dataFile = new File(sesFile.getAbsolutePath() + File.separator + "data.eeg");
		FileUtils.write(dataFile, "blabla", Charsets.UTF_8);

		
		BidsFolder studyElement = new BidsFolder(folder.getRoot().getAbsolutePath());
		
		// WHEN we deserialize it
		BidsElement result = deserializer.deserializeElement(studyElement);

		// THEN we get the same structure with BidsElements
		// base
		assertNotNull(result);
		assertEquals(result.getPath(), folder.getRoot().getAbsolutePath());
		
		// readme
		BidsFile readme;
		BidsFolder subj;
		BidsElement firstElement = studyElement.getElements().get(0);
		if (firstElement instanceof BidsFile) {
			readme = (BidsFile) firstElement;
			subj = (BidsFolder) studyElement.getElements().get(1);
		} else {
			subj = (BidsFolder) firstElement;
			readme = (BidsFile) studyElement.getElements().get(1);
		}

		assertNotNull(readme);
		assertEquals(readme.getPath(), readmeFile.getAbsolutePath());

		// subject
		assertNotNull(subj);
		assertEquals(subj.getPath(), subjectFolder.getAbsolutePath());

		// session
		BidsFolder sess = (BidsFolder) subj.getElements().get(0);
		assertNotNull(sess);
		assertEquals(sess.getPath(), sesFile.getAbsolutePath());

		// data
		BidsFile data = (BidsFile) sess.getElements().get(0);
		assertNotNull(readme);
		assertEquals(data.getPath(), dataFile.getAbsolutePath());

	}
	
}
