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

package org.shanoir.uploader.dicom.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.shanoir.ng.importer.model.Patient;
import org.shanoir.uploader.dicom.DicomTreeNode;

/**
 * The Class Media. Containing the data for the DICOM tree.
 *
 * @author mkain
 */
public class Media implements DicomTreeNode {

    /** The related patients. */
    private List<DicomTreeNode> relatedPatients;

    /**
     * Creates a new Media object.
     */
    public Media() {
        this.relatedPatients = new ArrayList<DicomTreeNode>();
    }

    /**
     * Adds the child tree node.
     *
     * @param id
     *            id
     * @param patient
     *            patient
     */
    public void addTreeNode(final DicomTreeNode patient) {
        this.relatedPatients.add(patient);
        ((PatientTreeNode) patient).setParent(this);
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
        return (TreeNode) relatedPatients.get(id);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.richfaces.model.TreeNodeImpl#getChildren()
     */
    public Iterator getChildren() {
        return relatedPatients.iterator();
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
    public List<DicomTreeNode> getTreeNodes() {
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
        final Iterator<DicomTreeNode> it = this.getTreeNodes().iterator();
        while (it.hasNext()) {
            result += ("\t" + it.next().toString());
        }
        result += "]";
        return result;
    }

    /**
     * No id.
     *
     * @return the id
     */
    public String getId() {
        return null;
    }

    /**
     * Gets the first patient.
     *
     * @return the first patient
     */
    public DicomTreeNode getFirstTreeNode() {
        if (getTreeNodes() != null && !getTreeNodes().isEmpty()) {
            return getTreeNodes().iterator().next();
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
        if (!getTreeNodes().contains(patient)) {
            addTreeNode(patient);
        }
        if (!patient.getTreeNodes().contains(study)) {
            patient.addTreeNode(study);
        }
        study.addTreeNode(serie);
    }

    /**
     * Initialize patient from DicomObject.
     *
     * @param dicomObject
     * @return
     */
    public PatientTreeNode initChildTreeNode(final Patient patient) {
        final PatientTreeNode patientTreeNode = new PatientTreeNode(patient);
        return patientTreeNode;
    }

    @Override
    public void setParent(DicomTreeNode parent) {
    }

}
