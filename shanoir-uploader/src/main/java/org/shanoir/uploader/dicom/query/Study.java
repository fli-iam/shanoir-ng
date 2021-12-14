package org.shanoir.uploader.dicom.query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.tree.TreeNode;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.shanoir.uploader.dicom.DicomTreeNode;
import org.shanoir.uploader.dicom.MRI;
import org.shanoir.uploader.dicom.Serie;

/**
 * Study representation from DICOMDIR.
 *
 * @author mkain
 */
public class Study implements DicomTreeNode {

	/** Description map, contains data about this object. */
	private HashMap<String, String> descriptionMap;

	/** Parent node. */
	private Patient parent;

	/** List of children. */
	private HashMap<String, DicomTreeNode> relatedSeries;

	/** Indicates if the node has been selected. */
	private boolean selected = false;

	/**
	 * The value for the study description of dicom files to be imported. The
	 * user can overwrite the existing value and enter a new one.
	 */
	private String studyDescriptionOverwrite;

	/**
	 * Creates a new Serie object.
	 *
	 * @param id
	 *            the id
	 * @param date
	 *            the date
	 * @param description
	 *            the description
	 */
	public Study(final String id, final String date, final String description) {
		descriptionMap = new HashMap<String, String>();
		descriptionMap.put("id", id);
		descriptionMap.put("date", date);
		studyDescriptionOverwrite = description;
		this.relatedSeries = new HashMap<String, DicomTreeNode>();
	}

	/**
	 * Adds the child.
	 *
	 * @param id
	 *            id
	 * @param serie
	 *            serie
	 */
	public void addTreeNode(final String id, final DicomTreeNode serie) {
		this.relatedSeries.put(id, (Serie) serie);
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
		return (TreeNode)relatedSeries.get(id);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.richfaces.model.TreeNodeImpl#getChildren()
	 */
	public Iterator getChildren() {
		List<Entry<String, DicomTreeNode>> child = new ArrayList<Entry<String, DicomTreeNode>>(relatedSeries.entrySet());
		Collections.sort(child, new SeriesComparator());
		return child.iterator();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.richfaces.model.TreeNodeImpl#getData()
	 */
	public Object getData() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.shanoir.dicom.model.DicomTreeNode#getDescriptionKeys()
	 */
	public List<String> getDescriptionKeys() {
		ArrayList<String> keys = new ArrayList<String>();
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
	public HashMap<String, String> getDescriptionMap() {
		// add dynamically the study description
		HashMap<String, String> cloneMap = new HashMap<String, String>();
		cloneMap.putAll(descriptionMap);
		cloneMap.put("description", studyDescriptionOverwrite);
		return cloneMap;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.shanoir.dicom.model.DicomTreeNode#getDisplayString()
	 */
	public String getDisplayString() {
		
		SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat format2 = new SimpleDateFormat("dd/MM/yyyy");
		Date studyDate=new Date();
		try {
			studyDate = format1.parse(descriptionMap.get("date"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String dateToDisplay =format2.format(studyDate);

		if (studyDescriptionOverwrite != null && !"".equals(studyDescriptionOverwrite)) {
			return studyDescriptionOverwrite + " ("+dateToDisplay+")";
		}
		return descriptionMap.get("id") /*+ " ("+dateToDisplay+")"*/;
	}

	/**
	 * Gets the first serie.
	 *
	 * @return the first serie
	 */
	public DicomTreeNode getFirstTreeNode() {
		if (getTreeNodes() != null && !getTreeNodes().isEmpty()) {
			return getTreeNodes().values().iterator().next();
		}
		return null;
	}

	/**
	 * Gets the id.
	 *
	 * @return Returns the id.
	 */
	public String getId() {
		return descriptionMap.get("id");
	}

	/**
	 * Gets the related series.
	 *
	 * @return Returns the relatedSeries.
	 */
	public HashMap<String, DicomTreeNode> getTreeNodes() {
		return relatedSeries;
	}

	/**
	 * Gets the study description overwrite.
	 *
	 * @return the study description overwrite
	 */
	public String getStudyDescriptionOverwrite() {
		return studyDescriptionOverwrite;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return "Study";
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
		relatedSeries.remove((String) id);
	}

	/**
	 * Sets the data.
	 *
	 * @param arg0
	 *            arg0
	 */
	public void setData(final Object arg0) {
		// TODO Auto-generated method stub
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent
	 *            parent
	 */
	public void setParent(final Patient parent) {
		this.parent = parent;
	}

	/**
	 * Sets the selected.
	 *
	 * @param selected
	 *            the new selected
	 */
	public void setSelected(final boolean selected) {
		this.selected = selected;
	}

	/**
	 * Sets the study description overwrite.
	 *
	 * @param studyDescriptionOverwrite
	 *            the new study description overwrite
	 */
	public void setStudyDescriptionOverwrite(final String studyDescriptionOverwrite) {
		this.studyDescriptionOverwrite = studyDescriptionOverwrite;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "[Study :" + descriptionMap + "]\n";
		final Iterator<DicomTreeNode> it = this.getTreeNodes().values().iterator();
		while (it.hasNext()) {
			result += ("\t" + it.next().toString() + "\n");
		}
		result += "]";
		return result;
	}
	
	/**
	 * Initialize serie from DicomObject.
	 * @param dicomObject
	 * @return
	 */
	public DicomTreeNode initChildTreeNode(final DicomObject dicomObject) {
		final Serie serie = new Serie(
				dicomObject.dataset().getString(Tag.SeriesInstanceUID),
				dicomObject.dataset().getString(Tag.Modality),
				dicomObject.dataset().getString(Tag.ProtocolName),
				dicomObject.dataset().getString(Tag.SeriesDescription),
				dicomObject.dataset().getString(Tag.SeriesDate),
				dicomObject.dataset().getString(Tag.SeriesNumber));
		// add MRI information to serie
		MRI mri=new MRI();
		mri.setInstitutionName(dicomObject.dataset().getString(Tag.InstitutionName));
		mri.setInstitutionAddress(dicomObject.dataset().getString(Tag.InstitutionAddress));
		mri.setStationName(dicomObject.dataset().getString(Tag.StationName));
		mri.setManufacturer(dicomObject.dataset().getString(Tag.Manufacturer));
		mri.setManufacturersModelName(dicomObject.dataset().getString(Tag.ManufacturerModelName));
		mri.setDeviceSerialNumber(dicomObject.dataset().getString(Tag.DeviceSerialNumber));
		serie.setMriInformation(mri);
		
		serie.setStudyInstanceUID(dicomObject.dataset().getString(Tag.StudyInstanceUID));
				
		final String imagesCount = dicomObject.dataset().getString(Tag.NumberOfSeriesRelatedInstances);
		if (imagesCount != null) {
			serie.setImagesCount(Integer.valueOf(imagesCount));
		}
		return serie;
	}

	public void addTreeNodes(DicomTreeNode firstLevelChild, DicomTreeNode secondLevelChild, DicomTreeNode thirdLevelChild) {
	}

	public void setImagesCount(int count) {
	}
	
}
