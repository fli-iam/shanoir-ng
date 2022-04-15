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

package org.shanoir.uploader.dicom;

import java.util.HashMap;
import java.util.List;

import org.dcm4che2.data.DicomObject;

/**
 * 
 * @author mkain
 *
 */
public interface DicomTreeNode {
	
	public void addTreeNode(final String id, final DicomTreeNode child);
	
	public DicomTreeNode getFirstTreeNode();
	
	public HashMap<String, DicomTreeNode> getTreeNodes();
	
	public void addTreeNodes(final DicomTreeNode firstLevelChild, final DicomTreeNode secondLevelChild, final DicomTreeNode thirdLevelChild);

	public DicomTreeNode initChildTreeNode(final DicomObject dicomObject);
	
	/**
	 * Gets the description map.
	 * 
	 * @return the description map
	 */
	public HashMap<String, String> getDescriptionMap();

	/**
	 * Gets the description keys.
	 * 
	 * @return the description keys
	 */
	public List<String> getDescriptionKeys();

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public String getId();

	/**
	 * String to be displayed on the screen as a description of the object.
	 * 
	 * @return the display string
	 */
	public String getDisplayString();
	
	public void setImagesCount(final int count);
	
}
