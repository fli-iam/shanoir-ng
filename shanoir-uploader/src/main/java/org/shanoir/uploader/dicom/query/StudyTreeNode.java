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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.uploader.dicom.DicomTreeNode;

/**
 * Study representation from DICOMDIR.
 *
 * @author mkain
 */
public class StudyTreeNode implements DicomTreeNode {

    private PatientTreeNode parent;

    private Study study;

    private List<DicomTreeNode> relatedSeries;

    /** Indicates if the node has been selected. */
    private boolean selected = false;

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
    public StudyTreeNode(final Study study) {
        this.study = study;
        this.relatedSeries = new ArrayList<DicomTreeNode>();
    }

    /**
     * Adds the child.
     *
     * @param id
     *            id
     * @param serie
     *            serie
     */
    public void addTreeNode(final DicomTreeNode serie) {
        this.relatedSeries.add(serie);
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
        return (TreeNode) relatedSeries.get(id);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.richfaces.model.TreeNodeImpl#getChildren()
     */
    public Iterator getChildren() {
        return relatedSeries.iterator();
    }

    public Study getStudy() {
        return this.study;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.shanoir.dicom.model.DicomTreeNode#getDisplayString()
     */
    public String getDisplayString() {
        return  study.toTreeString();
    }

    public LocalDate getStudyDate() {
        return study.getStudyDate();
    }

    /**
     * Gets the first serie.
     *
     * @return the first serie
     */
    public DicomTreeNode getFirstTreeNode() {
        if (getTreeNodes() != null && !getTreeNodes().isEmpty()) {
            return getTreeNodes().iterator().next();
        }
        return null;
    }

    /**
     * Gets the id.
     *
     * @return Returns the id.
     */
    public String getId() {
        return study.getStudyInstanceUID();
    }

    /**
     * Gets the related series.
     *
     * @return Returns the relatedSeries.
     */
    public List<DicomTreeNode> getTreeNodes() {
        return relatedSeries;
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
        relatedSeries.remove(id);
    }

    /**
     * Sets the parent.
     *
     * @param parent
     *            parent
     */
    public void setParent(DicomTreeNode parent) {
        this.parent = (PatientTreeNode) parent;
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

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String result = "[Study :" + this.study.getStudyDescription() + "]\n";
        return result;
    }

    /**
     * Initialize serie from DicomObject.
     * @param dicomObject
     * @return
     */
    public SerieTreeNode initChildTreeNode(final Serie serie) {
        final SerieTreeNode serieTreeNode = new SerieTreeNode(serie);
        serieTreeNode.setParent(this);
        return serieTreeNode;
    }

    public void addTreeNodes(DicomTreeNode firstLevelChild, DicomTreeNode secondLevelChild, DicomTreeNode thirdLevelChild) {
    }

    public PatientTreeNode getParent() {
        return this.parent;
    }

}
