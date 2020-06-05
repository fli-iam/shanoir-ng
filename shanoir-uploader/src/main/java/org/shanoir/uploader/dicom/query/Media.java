package org.shanoir.uploader.dicom.query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.shanoir.dicom.model.DicomTreeNode;

/**
 * The Class Media. Containing the data for the DICOM tree.
 *
 * @author mkain
 */
public class Media implements DicomTreeNode {

	private static final String NO_ID = "No ID";

	/** The related patients. */
	// private HashMap<String, DicomTreeNode> relatedPatients;
	private LinkedHashMap<String, DicomTreeNode> relatedPatients;

	/**
	 * Creates a new Media object.
	 */
	public Media() {
		// this.relatedPatients = new HashMap<String, DicomTreeNode>();
		this.relatedPatients = new LinkedHashMap<String, DicomTreeNode>();
	}

	/**
	 * Adds the child tree node.
	 *
	 * @param id
	 *            id
	 * @param patient
	 *            patient
	 */
	public void addTreeNode(final String id, final DicomTreeNode patient) {
		this.relatedPatients.put(id, patient);
		((Patient) patient).setParent(this);
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
		return (TreeNode) relatedPatients.get(id);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.richfaces.model.TreeNodeImpl#getChildren()
	 */
	public Iterator getChildren() {
		return relatedPatients.entrySet().iterator();
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
	 * @see org.shanoir.dicom.model.DicomTreeNode#getDisplayString()
	 */
	public String getDisplayString() {
		return "Media";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.richfaces.model.TreeNodeImpl#getParent()
	 */
	public TreeNode getParent() {
		return null;
	}

	/**
	 * Gets the related patients.
	 *
	 * @return Returns the relatedPatients.
	 */
	public HashMap<String, DicomTreeNode> getTreeNodes() {
		return relatedPatients;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return "Media";
	}

	/**
	 * Removes the child.
	 *
	 * @param id
	 *            id
	 */
	public void removeChild(final Object id) {
		this.relatedPatients.remove(id);
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
	 * @param arg0
	 *            arg0
	 */
	public void setParent(final TreeNode arg0) {
		// root node, do nothing
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "[Media:";
		final Iterator<DicomTreeNode> it = this.getTreeNodes().values().iterator();
		while (it.hasNext()) {
			result += ("\t" + it.next().toString());
		}
		result += "]";
		return result;
	}

	/**
	 * No description for a media object.
	 *
	 * @return the description map
	 */
	public HashMap<String, String> getDescriptionMap() {
		return new HashMap<String, String>();
	}

	/**
	 * No id.
	 *
	 * @return the id
	 */
	public String getId() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.shanoir.dicom.model.DicomTreeNode#getDescriptionKeys()
	 */
	public List<String> getDescriptionKeys() {
		return null;
	}

	/**
	 * Gets the first patient.
	 *
	 * @return the first patient
	 */
	public DicomTreeNode getFirstTreeNode() {
		if (getTreeNodes() != null && !getTreeNodes().isEmpty()) {
			return getTreeNodes().values().iterator().next();
		}
		return null;
	}

	/**
	 * Adds a serie for the given patient and study. If the patient doesn't exist,
	 * creates a new one. Idem for the study.
	 *
	 * @param patient
	 *            the patient
	 * @param study
	 *            the study
	 * @param serie
	 *            the serie
	 */
	public void addTreeNodes(final DicomTreeNode patient, final DicomTreeNode study, final DicomTreeNode serie) {
		if (!getTreeNodes().containsKey(patient.getId())) {
			addTreeNode(patient.getId(), patient);
		}
		if (!patient.getTreeNodes().containsKey(study.getId())) {
			patient.addTreeNode(study.getId(), study);
		}
		study.addTreeNode(serie.getId(), serie);
	}

	/**
	 * Initialize patient from DicomObject.
	 * 
	 * @param dicomObject
	 * @return
	 */
	public DicomTreeNode initChildTreeNode(final DicomObject dicomObject) {
		final String patientIdName = dicomObject.dataset().getString(Tag.PatientID) + " "
				+ dicomObject.dataset().getString(Tag.PatientName);
		final Patient patient = new Patient(patientIdName, 
				dicomObject.dataset().getString(Tag.PatientBirthDate),
				dicomObject.dataset().getString(Tag.PatientSex),
				dicomObject.dataset().getString(Tag.PatientName),
				dicomObject.dataset().getString(Tag.PatientBirthName));

		// if no id is defined for the patient
		if (patientIdName == null || "".equals(patientIdName)) {
			patient.setId(NO_ID);
		}
		return patient;
	}

	public void setImagesCount(int count) {
	}

}
