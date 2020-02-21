package org.shanoir.ng.bids.utils;

import java.io.File;
import java.util.ArrayList;

import org.shanoir.ng.bids.model.BidsElement;
import org.shanoir.ng.bids.model.BidsFile;
import org.shanoir.ng.bids.model.BidsFolder;
import org.shanoir.ng.bids.service.StudyBIDSService;
import org.shanoir.ng.study.model.Study;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class allows to generate a BidsElement Tree from a BIDS folder
 * The generated Bids Element tree will be transmitted to the front to be visualized
 * @author JComeD
 *
 */
@Component
public class BidsDeserializer {
	
	private static final String STUDY_PREFIX = "stud-";

	@Value("${bids-data-folder}")
	private String bidsStorageDir;

	@Autowired
	private StudyBIDSService bidsService;

	public BidsElement deserialize(Study study) {
		// Get the parent folder
		File studyFile = new File(bidsStorageDir + File.separator + STUDY_PREFIX + study.getId() + '_' + study.getName());

		if (!studyFile.exists()) {
			studyFile = bidsService.createBidsFolderFromScratch(study);
		}
		BidsFolder studyElement = new BidsFolder(studyFile.getAbsolutePath());

		// Iterate recursively over studyFile to get the BidsElement
		return deserializeElement(studyElement);
	}

	/**
	 * Deserialize recusrsively a BidsElement to create sub folders and files
	 * @param studyElement
	 */
	private BidsElement deserializeElement(BidsFolder folderElement) {
		File f = new File(folderElement.getPath());

		if (f.listFiles() == null) {
			return folderElement;
		}
		// Iterate over the list of files
		// If we have a File, juste create a FileElement and add it to the FolderElement list
		// If we have a directory, iterate over the sub-elements of the Folder and then add it to the FolderElement list
		for (File file : f.listFiles()) {
			BidsElement fileElement = null;
			if (file.isDirectory()) {
				fileElement = new BidsFolder(file.getAbsolutePath());
				deserializeElement((BidsFolder) fileElement);
			} else {
				fileElement = new BidsFile(file.getAbsolutePath());
				// LATER: Manage file content here
			}
			if (folderElement.getElements() == null) {
				folderElement.setElements(new ArrayList<>());
			}
			folderElement.getElements().add(fileElement);
		}
		return folderElement;
	}
}
