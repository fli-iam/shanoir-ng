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
