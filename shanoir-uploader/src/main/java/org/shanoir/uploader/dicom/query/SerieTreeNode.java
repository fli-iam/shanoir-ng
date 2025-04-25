package org.shanoir.uploader.dicom.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.shared.dicom.EquipmentDicom;
import org.shanoir.ng.shared.dicom.InstitutionDicom;
import org.shanoir.uploader.dicom.DicomTreeNode;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

/**
 * SerieTreeNode, wraps a Serie in model of ms-import, but implements the interface for the JTree,
 * that is required to show the JTree in the GUI of ShUp. The setters and getters are used, when
 * the upload-job.xml is written to the disk (tab 1: select series + download), so the getters are
 * called and get the info of the Serie to write with JAXB to the xml on the disk. The setters are
 * used and called to read the serie information for tab 2: import into server, to prepare already
 * the Serie object, that is injected into the ImportJob json and send to the server.
 * 
 * In the SerieTreeNode XML we use the fileNames as wrapper for the instances. We write them to the
 * disk as list of strings, fileNames, and read them into instances to be sent to the server. As the
 * usage of XML shall be refactored in the future as well, e.g. used by the GUI for list of imports
 * I did not want to introduce a separate instance XML class, that will be deleted later.
 *
 * @author mkain
 * 
 */
@XmlType(propOrder={"id", "modality", "protocol", "description", "seriesDate", "seriesNumber", "imagesCount", "selected", "fileNames"})
public class SerieTreeNode implements DicomTreeNode {

	private StudyTreeNode parent;

	private Serie serie;
	
	private List<String> fileNames;
	
	// constructor for JAXB
	public SerieTreeNode() {
		this.serie = new Serie();
	}

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
	
	public Serie getSerie() {
		return this.serie;
	}

	@XmlElement
	public String getId() {
		return this.serie.getSeriesInstanceUID();
	}
	
	public void setId(String seriesInstanceUID) {
		this.serie.setSeriesInstanceUID(seriesInstanceUID);
	}

	@XmlElement
	public String getModality() {
		return this.serie.getModality();
	}
	
	public void setModality(String modality) {
		this.serie.setModality(modality);
	}
	
	@XmlElement
	public String getProtocol() {
		return this.serie.getProtocolName();
	}
	
	public void setProtocol(String protocolName) {
		this.serie.setProtocolName(protocolName);
	}
	
	@XmlElement
	public String getDescription() {
		return this.serie.getSeriesDescription();
	}
	
	public void setDescription(String seriesDescription) {
		this.serie.setSeriesDescription(seriesDescription);
	}
	
	@XmlElement
	public String getSeriesDate() {
		if (this.serie.getSeriesDate() != null) {
			return this.serie.getSeriesDate().toString();
		} 
		return "";
	}
	
	@XmlElement
	public String getSeriesNumber() {
		return this.serie.getSeriesNumber();
	}
	
	public void setSeriesNumber(String seriesNumber) {
		this.serie.setSeriesNumber(seriesNumber);
	}
	
	@XmlElement
	public String getImagesCount() {
		if (this.serie.getImagesNumber() != null) {
			return this.serie.getImagesNumber().toString();
		} 
		return "";
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
	@SuppressWarnings("rawtypes")
	public Iterator getChildren() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.shanoir.dicom.model.DicomTreeNode#getDisplayString()
	 */
	@XmlTransient
	public String getDisplayString() {
		String result = "";
		final String seriesNumber = this.serie.getSeriesNumber();
		if (seriesNumber != null && !seriesNumber.isEmpty()) {
			result += seriesNumber + " ";
		}
		final String modality = this.serie.getModality();
		if (modality != null && !"".equals(modality)) {
			result += "[" + modality + "] ";
		}
		final String description = this.serie.getSeriesDescription();
		final String id = this.serie.getSeriesInstanceUID();
		if (description != null && !"".equals(description)) {
			result += description;
		} else if (id != null && !id.equals("")) {
			result += id;
		}
		Integer numberOfSeriesRelatedInstances = this.serie.getNumberOfSeriesRelatedInstances();
		if (numberOfSeriesRelatedInstances != 0) {
			result += " (" + numberOfSeriesRelatedInstances + ")";
		}
		EquipmentDicom equipment = this.serie.getEquipment();
		if (equipment != null) {
			String stationName = equipment.getStationName();
			if (stationName != null && !"".equals(stationName)) {
				result += " [ " + stationName + " , ";
			}			
		}
		InstitutionDicom institution = this.serie.getInstitution();
		if (institution != null) {
			String institutionName = institution.getInstitutionName();
			if (institutionName != null && !"".equals(institutionName)) {
				result += institutionName + " ] ";
			}			
		}
		return result;
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

	public void addTreeNode(DicomTreeNode arg1) {
	}

	public void addTreeNodes(DicomTreeNode arg0, DicomTreeNode arg1,
			DicomTreeNode arg2) {
	}

	public DicomTreeNode getFirstTreeNode() {
		return null;
	}

	public List<DicomTreeNode> getTreeNodes() {
		return new ArrayList<DicomTreeNode>();
	}

	public DicomTreeNode initChildTreeNode(Object arg0) {
		return null;
	}

	@Override
	public void setParent(DicomTreeNode parent) {
		this.parent = (StudyTreeNode) parent;
	}

	public StudyTreeNode getParent() {
		return this.parent;
	}
	
	@XmlElementWrapper(name="fileNames")
	@XmlElement(name="fileName")
	public List<String> getFileNames() {
		return fileNames;
	}
	
	public void setFileNames(List<String> fileNames) {
		this.fileNames = fileNames;
	}

}
