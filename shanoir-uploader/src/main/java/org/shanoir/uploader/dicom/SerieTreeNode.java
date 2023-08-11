package org.shanoir.uploader.dicom;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.tree.TreeNode;

import org.shanoir.ng.importer.model.EquipmentDicom;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.uploader.dicom.query.StudyTreeNode;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

/**
 * SerieTreeNode, points to Serie in model of ms-import, but implements the interface for the JTree.
 *
 * @author mkain
 */
@XmlType(propOrder={"id", "modality", "protocol", "description", "seriesDate", "seriesNumber", "imagesCount", "selected", "fileNames","mriInformation"})
public class SerieTreeNode implements DicomTreeNode {

	private StudyTreeNode parent;

	private Serie serie;
	
	private List<String> fileNames;

	/**
	 * Creates a new Serie object.
	 *
	 * @param modality
	 *            the modality
	 * @param protocol
	 *            the protocol
	 * @param desc
	 *            the desc
	 * @param id
	 *            the id
	 * @param date
	 *            the date
	 */
	public SerieTreeNode(final Serie serie) {
		this.serie = serie;
	}

	@XmlElement
	public String getId() {
		return this.serie.getSeriesInstanceUID();
	}

	@XmlElement
	public String getModality() {
		return this.serie.getModality();
	}
	
	@XmlElement
	public String getProtocol() {
		return this.serie.getProtocolName();
	}
	
	@XmlElement
	public String getDescription() {
		return this.serie.getSeriesDescription();
	}
	
	@XmlElement
	public String getSeriesDate() {
		return this.serie.getSeriesDate().toString();
	}
	
	@XmlElement
	public String getSeriesNumber() {
		return this.serie.getSeriesNumber();
	}
	
	@XmlElement
	public String getImagesCount() {
		return this.serie.getImagesNumber().toString();
	}

	/**
	 * Gets the child.
	 *
	 * @param id
	 *            id
	 *
	 * @return the child
	 */
	public TreeNode getChild(final Object id) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.richfaces.model.TreeNodeImpl#getChildren()
	 */
	public Iterator getChildren() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.richfaces.model.TreeNodeImpl#getData()
	 */
	@XmlTransient
	public Object getData() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.shanoir.dicom.model.DicomTreeNode#getDisplayString()
	 */
	@XmlTransient
	public String getDisplayString() {
		return this.serie.toString();
	}

	/**
	 * Gets the type.\
	 *
	 * @return the type
	 */
	@XmlTransient
	public String getType() {
		return "Serie";
	}

	@XmlTransient
	public boolean isMultiFrame() {
		return this.serie.getIsMultiFrame();
	}

	/**
	 * Checks if is selected.
	 *
	 * @return true, if is selected
	 */
	public boolean isSelected() {
		return this.serie.getSelected();
	}

	/**
	 * Removes the child.
	 *
	 * @param id
	 *            id
	 */
	public void removeChild(final Object id) {
		// leaf
	}

	/**
	 * Sets the data.
	 *
	 * @param arg0
	 *            arg0
	 */
	public void setData(final Object arg0) {
	}

	/**
	 * Sets the selected.
	 *
	 * @param selected
	 *            selected
	 */
	public void setSelected(final boolean selected) {
		this.serie.setSelected(selected);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.serie.toString();
	}

	public void addTreeNode(String arg0, DicomTreeNode arg1) {
	}

	public void addTreeNodes(DicomTreeNode arg0, DicomTreeNode arg1,
			DicomTreeNode arg2) {
	}

	public DicomTreeNode getFirstTreeNode() {
		return null;
	}

	public HashMap<String, DicomTreeNode> getTreeNodes() {
		return new HashMap<String, DicomTreeNode>();
	}

	public DicomTreeNode initChildTreeNode(Object arg0) {
		return null;
	}

	@XmlElementWrapper(name="fileNames")
	@XmlElement(name="fileName")
	public List<String> getFileNames() {
		return fileNames;
	}
	
	public void setFileNames(List<String> fileNames) {
		this.fileNames = fileNames;
	}
	
	public EquipmentDicom getMriInformation() {
		return this.serie.getEquipment();
	}

	@Override
	public void setParent(DicomTreeNode parent) {
		this.parent = (StudyTreeNode) parent;
	}

	public StudyTreeNode getParent() {
		return this.parent;
	}

}