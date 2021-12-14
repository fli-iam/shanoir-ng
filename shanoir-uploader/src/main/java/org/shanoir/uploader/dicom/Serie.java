package org.shanoir.uploader.dicom;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.TreeNode;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.shanoir.uploader.utils.Util;

/**
 * The Class Serie.
 *
 * @author mkain
 */
@XmlType(propOrder={"id", "modality", "protocol", "description", "seriesDate", "seriesNumber", "imagesCount", "selected", "fileNames","mriInformation"})
public class Serie implements DicomTreeNode {
	
	private static final String IMAGES = "images";

	private static final String SERIES_NUMBER = "seriesNumber";

	private static final String SERIES_DATE = "seriesDate";

	private static final String ID = "id";

	private static final String DESCRIPTION = "description";

	private static final String PROTOCOL = "protocol";

	private static final String MODALITY = "modality";

	/** Description map, contains data about this object. */
	private HashMap<String, String> descriptionMap;

	private List<String> fileNames;
	
	private String studyInstanceUID;
	
	private MRI mriInformation;
	

	/** Indicates if the associated DICOM files serie has been selected. */
	private boolean isMultiFrame = false;

	private static Logger logger = Logger.getLogger(Serie.class);

	/** Indicates if the node has been selected. */
	private boolean selected = false;

	public Serie() {
		descriptionMap = new HashMap<String, String>();
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
	public Serie(final String id, final String modality, final String protocol, final String description, final String date,
			final String number) {
		descriptionMap = new HashMap<String, String>();
		// given as arguments
		descriptionMap.put(ID, id);
		descriptionMap.put(MODALITY, modality);
		descriptionMap.put(PROTOCOL, protocol);
		descriptionMap.put(DESCRIPTION, description);
		descriptionMap.put(SERIES_DATE, date);
		descriptionMap.put(SERIES_NUMBER, number);
	}

	@XmlElement
	public String getId() {
		return descriptionMap.get(ID);
	}
	
	public void setId(final String id) {
		descriptionMap.put(ID, id);
	}
	
	@XmlElement
	public String getModality() {
		return descriptionMap.get(MODALITY);
	}

	public void setModality(final String modality) {
		descriptionMap.put(MODALITY, modality);
	}
	
	@XmlElement
	public String getProtocol() {
		return descriptionMap.get(PROTOCOL);
	}
	
	public void setProtocol(final String protocol) {
		descriptionMap.put(PROTOCOL, protocol);
	}
	
	@XmlElement
	public String getDescription() {
		return descriptionMap.get(DESCRIPTION);
	}	
	
	public void setDescription(final String description) {
		descriptionMap.put(DESCRIPTION, description);
	}
	
	@XmlElement
	public String getSeriesDate() {
		return descriptionMap.get(SERIES_DATE);
	}
	
	public void setSeriesDate(final String date) {
		descriptionMap.put(SERIES_DATE, date);
	}
	
	@XmlElement
	public String getSeriesNumber() {
		return descriptionMap.get(SERIES_NUMBER);
	}

	public void setSeriesNumber(final String number) {
		descriptionMap.put(SERIES_NUMBER, number);
	}
	
	@XmlElement
	public String getImagesCount() {
		return descriptionMap.get(IMAGES);
	}
	
	/**
	 * Sets the images count.
	 *
	 * @param count
	 *            the new images count
	 */
	public void setImagesCount(final int count) {
		getDescriptionMap().put(IMAGES, "" + count);
	}

	/**
	 * Used for JAXB mapping.
	 * @param count
	 */
	public void setImagesCount(final String count) {
		getDescriptionMap().put(IMAGES, count);
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
	 * @see org.shanoir.dicom.model.DicomTreeNode#getDescriptionKeys()
	 */
	@XmlTransient
	public List<String> getDescriptionKeys() {
		final ArrayList<String> keys = new ArrayList<String>();
		for (final String key : getDescriptionMap().keySet()) {
			keys.add(key);
		}
		return keys;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.shanoir.dicom.model.DicomTreeNode#getDescriptionMap()
	 */
	@XmlTransient
	public HashMap<String, String> getDescriptionMap() {
		return descriptionMap;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.shanoir.dicom.model.DicomTreeNode#getDisplayString()
	 */
	@XmlTransient
	public String getDisplayString() {
		String result = "";
		final String modality = this.descriptionMap.get(MODALITY);
		if (modality != null && !"".equals(modality)) {
			result += "[" + modality + "] ";
		}
		final String description = this.descriptionMap.get(DESCRIPTION);
		final String id = descriptionMap.get(ID);
		if (description != null && !"".equals(description)) {
			result += description;
		} else if (id != null && !id.equals("")) {
			result += id;
		}
		final String date = this.descriptionMap.get(SERIES_DATE);
		if (date != null && !"".equals(date)) {
			final Date seriesDate = Util.convertStringDicomDateToDate(date);
			final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			final String seriesDateStr = formatter.format(seriesDate);
			result += " - " + seriesDateStr;
		}
		String stationName= this.mriInformation.getStationName();
		if (stationName != null && !"".equals(stationName)) {
			
			result += " [ " + stationName + " , ";
		}
		String instituationName= this.mriInformation.getInstitutionName();
		if (instituationName != null && !"".equals(instituationName)) {
			
			result +=  instituationName + " ] ";
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
		return isMultiFrame;
	}

	/**
	 * Checks if is selected.
	 *
	 * @return true, if is selected
	 */
	public boolean isSelected() {
		return selected;
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

	public void setMultiFrame(boolean isMultiFrame) {
		this.isMultiFrame = isMultiFrame;
	}

	/**
	 * Sets the selected.
	 *
	 * @param selected
	 *            selected
	 */
	public void setSelected(final boolean selected) {
		this.selected = selected;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "[Serie: " + descriptionMap + ", isSelected=" + selected + "]";
		return result;
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

	public DicomTreeNode initChildTreeNode(DicomObject arg0) {
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

	@XmlTransient
	public String getStudyInstanceUID() {
		return studyInstanceUID;
	}

	public void setStudyInstanceUID(String studyInstanceUID) {
		this.studyInstanceUID = studyInstanceUID;
	}
	
	public MRI getMriInformation() {
		return mriInformation;
	}

	public void setMriInformation(MRI mriInformation) {
		this.mriInformation = mriInformation;
	}

}
