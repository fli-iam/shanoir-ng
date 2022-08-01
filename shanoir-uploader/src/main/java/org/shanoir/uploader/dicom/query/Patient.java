package org.shanoir.uploader.dicom.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.tree.TreeNode;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.shanoir.uploader.dicom.DicomTreeNode;

/**
 * Patient class from the DICOMDIR.
 *
 * @author mkain
 */
public class Patient implements DicomTreeNode {

	/** Description map, contains data about this object. */
	private HashMap<String, String> descriptionMap;

	/** Parent node. */
	private Media parent;

	/** List of children. */
	private HashMap<String, DicomTreeNode> relatedStudies;

	/**
	 * Creates a new Patient object.
	 *
	 * @param id
	 *            the id
	 * @param birthDate
	 *            the birth date
	 * @param sex
	 *            the sex
	 * @param name
	 *            the name
	 */
	public Patient(final String id, final String birthDate, final String sex, final String name, final String birthName) {
		this.descriptionMap = new HashMap<String, String>();
		this.descriptionMap.put("id", id);
		this.descriptionMap.put("birthDate", birthDate);
		this.descriptionMap.put("sex", sex);
		this.descriptionMap.put("name", name);
		this.descriptionMap.put("birthName", birthName);
		this.relatedStudies = new HashMap<String, DicomTreeNode>();
	}
	

	/**
	 * Adds the child.
	 *
	 * @param id
	 *            id
	 * @param study
	 *            study
	 */
	public void addTreeNode(final String id, final DicomTreeNode study) {
		this.relatedStudies.put(id, study);
		((Study)study).setParent(this);
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
		return (TreeNode)relatedStudies.get(id);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.richfaces.model.TreeNodeImpl#getChildren()
	 */
	public Iterator getChildren() {
		List<Entry<String, DicomTreeNode>> child = new ArrayList<Entry<String, DicomTreeNode>>(relatedStudies.entrySet());
		Collections.sort(child, new StudyComparator());
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

	/**
	 * No description for a media object.
	 *
	 * @return the description map
	 */
	public HashMap<String, String> getDescriptionMap() {
		return descriptionMap;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.shanoir.dicom.model.DicomTreeNode#getDisplayString()
	 */
	public String getDisplayString() {
		final String name = this.descriptionMap.get("name");
		if (name != null && !"".equals(name)) {
			return name;
		}
		return descriptionMap.get("id");
	}

	/**
	 * Gets the first study.
	 *
	 * @return the first study
	 */
	public DicomTreeNode getFirstTreeNode() {
		if (getTreeNodes() != null && !getTreeNodes().isEmpty()) {
			return getTreeNodes().values().iterator().next();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.shanoir.dicom.model.DicomTreeNode#getId()
	 */
	public String getId() {
		return descriptionMap.get("id");
	}

	/**
	 * Gets the parent.
	 *
	 * @return Returns the parent.
	 */
	public Media getParent() {
		return parent;
	}

	/**
	 * Gets the related studies.
	 *
	 * @return Returns the relatedStudies.
	 */
	public HashMap<String, DicomTreeNode> getTreeNodes() {
		return relatedStudies;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return "Patient";
	}

	/**
	 * Removes the child.
	 *
	 * @param id
	 *            id
	 */
	public void removeChild(final Object id) {
		relatedStudies.remove((String) id);
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
	 * Sets the id.
	 *
	 * @param id
	 *            the new id
	 */
	public void setId(final String id) {
		descriptionMap.put("id", id);
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent
	 *            parent
	 */
	public void setParent(final Media parent) {
		this.parent = parent;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "[Patient : " + descriptionMap + "]";
		final Iterator<DicomTreeNode> it = this.getTreeNodes().values().iterator();
		while (it.hasNext()) {
			result += ("\t" + it.next().toString());
		}
		result += "]";
		return result;
	}

	public void addTreeNodes(DicomTreeNode firstLevelChild, DicomTreeNode secondLevelChild, DicomTreeNode thirdLevelChild) {
	}

	public DicomTreeNode initChildTreeNode(DicomObject dicomObject) {
		final String studyDescriptionFromDicomTags = dicomObject.dataset().getString(Tag.StudyDescription);
		final Study study = new Study(
				dicomObject.dataset().getString(Tag.StudyInstanceUID),
				dicomObject.dataset().getString(Tag.StudyDate),
				studyDescriptionFromDicomTags);
		study.setStudyDescriptionOverwrite(studyDescriptionFromDicomTags);
		return study;
	}

	public void setImagesCount(int count) {
	}
	
}
