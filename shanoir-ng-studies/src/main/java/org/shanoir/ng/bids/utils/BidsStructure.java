package org.shanoir.ng.bids.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.shanoir.ng.bids.model.BidsElement;
import org.shanoir.ng.bids.model.BidsFile;
import org.shanoir.ng.bids.model.BidsFolder;

import com.google.gson.JsonObject;

public class BidsStructure {

	static BidsElement bidsStructure = getStructure();

	private static BidsElement getStructure() {
		// TODO: define content check
		// Study folder
		StringBuffer iterativePath = new StringBuffer();
		// TODO: delete this stupid idea of stringbuffer not working at all
		BidsFolder studyFolder = new BidsFolder(iterativePath.append("stud-*").toString());

		// README
		BidsFile readMeFile = new BidsFile(iterativePath.append("/README").toString());
		
		// CHANGES
		BidsFile changesFile = new BidsFile(iterativePath.append("/CHANGES").toString());
		
		// dataset_description.json
		BidsFile dsDescriptionFile = new BidsFile(iterativePath.append("/dataset_description.json").toString());
		String dsDescriptionJsonStructure;
		JsonObject object = new JsonObject();
		object.addProperty("field1", "possibleValuesAsRegex");
		dsDescriptionFile.setContent(object.getAsString());
		// Code folder

		// Datasource folder

		// SUBFolder
		BidsFolder subFolder = new BidsFolder(iterativePath.append("/sub-*").toString());
		
		// SES Folder
		BidsFolder sesFolder = new BidsFolder(iterativePath.append("/ses-*").toString());

		// Dataset folder
		//TODO: Manage multiple possibilities: /eeg/anat/func/ => Do one folder by possibility ? YES
		
		// Dataset Files
		// For every dataset type, define file type and content
		BidsFolder eegFolder = new BidsFolder("");
		
		// TODO: define constant for unmodifiable files
		// OR juste define them as unmodifiable ? => Add it to BidsElement
		BidsFile eegFile = new BidsFile("/EEG/*.eeg");
		
		return null;
	}

	/**
	 * This method checks the BIDS content for a BIDS folder from a reference structure
	 * @param element
	 * @param reference
	 * @return
	 */
	public static boolean checkBidsContent(BidsElement element, BidsElement reference) {
		// Compare structure with bidsStructure
		// Compare study, then element by element (recursively ?)
		boolean result = true;
        Pattern pattern = Pattern.compile(bidsStructure.getPath());
        Matcher matcher = pattern.matcher(element.getPath());
        // If path does not match, it's a fail
        if (!matcher.matches()) {
        	return false;
        }
    	if (element instanceof BidsFile) {
    		// Check file Content
    		return checkFileContent(element, reference);
    	}

    	// Iterate over children
    	// TOOD: add a check on structure TYPE (instance of == instance of => Risk of failure otherwise)
		for (BidsElement child : ((BidsFolder) element).getElements()) {
			// We need at least ONE valid matching subElement
			boolean foundOne = true;
    		for (BidsElement structureChild : ((BidsFolder) reference).getElements()) {
    			// IF it's the wrong one, GO BACK and try again until one is good
    			// (recursive call here)
    			foundOne &=checkBidsContent(child, structureChild);
    			
    			// Whenever we find one that is correct, STOP IT and continue to loop over tested elements
    			if (foundOne) {
    				break;
    			}
    		}
    		// If we didn't find ANY correct subElement from the structure, then it's a fail
    		if (!foundOne) {
    			return false;
    		}
		}
		// If we didn't return until now, we consider it's a success
        return true;
	}

	private static boolean checkFileContent(BidsElement element, BidsElement reference) {
		// TODO Complete this method to check only .tsv and .json files from reference
		return true;
	}
	
}
