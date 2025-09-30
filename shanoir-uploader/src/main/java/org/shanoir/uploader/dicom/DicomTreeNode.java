package org.shanoir.uploader.dicom;

import java.util.List;

/**
 *
 * @author mkain
 *
 */
public interface DicomTreeNode {

	public void addTreeNode(final DicomTreeNode child);

	public DicomTreeNode getFirstTreeNode();

	public List<DicomTreeNode> getTreeNodes();

	public void addTreeNodes(final DicomTreeNode firstLevelChild, final DicomTreeNode secondLevelChild, final DicomTreeNode thirdLevelChild);

	public void setParent(DicomTreeNode parent);

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

}
