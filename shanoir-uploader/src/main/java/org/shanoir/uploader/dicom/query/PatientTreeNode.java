package org.shanoir.uploader.dicom.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.uploader.dicom.DicomTreeNode;

/**
 * Patient class from the DICOMDIR.
 *
 * @author mkain
 */
public class PatientTreeNode implements DicomTreeNode {

	private Media parent;

	private Patient patient;

	private List<DicomTreeNode> relatedStudies;

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
	public PatientTreeNode(final Patient patient) {
		this.patient = patient;
		this.relatedStudies = new ArrayList<DicomTreeNode>();
	}
	

	/**
	 * Adds the child.
	 *
	 * @param id
	 *            id
	 * @param study
	 *            study
	 */
	public void addTreeNode(final DicomTreeNode study) {
		this.relatedStudies.add(study);
		study.setParent(this);
	}

	/**
	 * Gets the child.
	 *
	 * @param id
	 *            id
	 *
	 * @return the child
	 */
	public TreeNode getChild(final int id) {
		return (TreeNode)relatedStudies.get(id);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.richfaces.model.TreeNodeImpl#getChildren()
	 */
	public Iterator getChildren() {
		return relatedStudies.iterator();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.richfaces.model.TreeNodeImpl#getData()
	 */
	public Patient getPatient() {
		return this.patient;
	}
	
	/*
	 * (non-Javadoc)
	 *
	 * @see org.shanoir.dicom.model.DicomTreeNode#getDisplayString()
	 */
	public String getDisplayString() {
		return this.patient.toTreeString();
	}

	/**
	 * Gets the first study.
	 *
	 * @return the first study
	 */
	public DicomTreeNode getFirstTreeNode() {
		if (getTreeNodes() != null && !getTreeNodes().isEmpty()) {
			return getTreeNodes().iterator().next();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.shanoir.dicom.model.DicomTreeNode#getId()
	 */
	public String getId() {
		return patient.getPatientID();
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
	public List<DicomTreeNode> getTreeNodes() {
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
		relatedStudies.remove(id);
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
	 * Sets the parent.
	 *
	 * @param parent
	 *            parent
	 */
	public void setParent(final DicomTreeNode parent) {
		this.parent = (Media)parent;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "[Patient : " + patient.toString() + "]";
		return result;
	}

	public void addTreeNodes(DicomTreeNode firstLevelChild, DicomTreeNode secondLevelChild, DicomTreeNode thirdLevelChild) {
	}

	public StudyTreeNode initChildTreeNode(Study study) {
		StudyTreeNode studyTreeNode = new StudyTreeNode(study);
		return studyTreeNode;
	}
	
}
