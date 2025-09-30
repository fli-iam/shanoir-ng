package org.shanoir.ng.bids.service;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.shanoir.ng.bids.BidsDeserializer;
import org.shanoir.ng.bids.model.BidsElement;
import org.shanoir.ng.bids.model.BidsFile;
import org.shanoir.ng.bids.model.BidsFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for deserializer.
 * @author fli
 *
 */
public class BidsDeserializerTest {

	@TempDir
    public File folder;

	BidsDeserializer deserializer = new BidsDeserializer();

	@Test
	public void testDeserialize() throws IOException {
		// GIVEN a file structure following a bids structure
		File readmeFile = new File(folder, "README");
		FileUtils.write(readmeFile, "blabla", Charsets.UTF_8);

		File subjectFolder = new File(folder, "sub-11");
		File sesFile = new File(subjectFolder.getAbsolutePath() + File.separator + "ses-15");
		sesFile.mkdir();
		File dataFile = new File(sesFile.getAbsolutePath() + File.separator + "data.eeg");
		FileUtils.write(dataFile, "blabla", Charsets.UTF_8);

		BidsFolder studyElement = new BidsFolder(folder.getAbsolutePath());

		// WHEN we deserialize it
		BidsElement result = deserializer.deserializeElement(studyElement);

		// THEN we get the same structure with BidsElements
		// base
		assertNotNull(result);
		assertEquals(result.getPath(), folder.getAbsolutePath());

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
