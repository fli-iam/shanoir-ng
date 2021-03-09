package org.shanoir.ng.bids;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.shanoir.ng.bids.model.BidsElement;
import org.shanoir.ng.bids.model.BidsFile;
import org.shanoir.ng.bids.model.BidsFolder;
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
	
	@Value("${bids-data-folder}")
	private String bidsStorageDir;

	public BidsElement deserialize(File studyFile) throws IOException {

		BidsFolder studyElement = new BidsFolder(studyFile.getAbsolutePath());

		// Iterate recursively over studyFile to get the BidsElement
		return deserializeElement(studyElement);
	}

	/**
	 * Deserialize recusrsively a BidsElement to create sub folders and files
	 * @param studyElement
	 * @throws IOException
	 */
	public BidsElement deserializeElement(BidsFolder folderElement) throws IOException {
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
				if (fileElement.getPath().endsWith(".tsv") || fileElement.getPath().endsWith(".json")) {
					String content = String.join("\n", Files.readAllLines(Paths.get(file.getAbsolutePath())));
					((BidsFile)fileElement).setContent(content);
				}
			}
			if (folderElement.getElements() == null) {
				folderElement.setElements(new ArrayList<>());
			}
			folderElement.getElements().add(fileElement);
		}
		return folderElement;
	}
}
