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

package org.shanoir.uploader.gui;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.shanoir.uploader.dicom.DicomTreeNode;
import org.shanoir.uploader.dicom.query.Media;
import org.shanoir.uploader.dicom.query.PatientTreeNode;
import org.shanoir.uploader.dicom.query.SerieTreeNode;
import org.shanoir.uploader.dicom.query.StudyTreeNode;

/**
 * This class is the tree cell renderer of the DICOM tree.
 * The icons known from the Shanoir web application are in
 * use within this rendering component.
 * @author mkain
 *
 */
public class DicomTreeCellRenderer extends DefaultTreeCellRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(DicomTreeCellRenderer.class);

    private Icon mediaIcon;
    private Icon patientIcon;
    private Icon studyIcon;
    private Icon serieIcon;

    public DicomTreeCellRenderer() {
        ImageIcon mediaIcon = createImageIcon("/images/media.16x16.png");
        ImageIcon patientIcon = createImageIcon("/images/subject.16x16.png");
        ImageIcon studyIcon = createImageIcon("/images/study.dicom.16x16.png");
        ImageIcon serieIcon = createImageIcon("/images/serie.16x16.png");
        this.mediaIcon = mediaIcon;
        this.patientIcon = patientIcon;
        this.studyIcon = studyIcon;
        this.serieIcon = serieIcon;
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = DicomTreeCellRenderer.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            LOG.error("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * This is the method which is called for rendering
     * each instance of a DicomTreeNode.
     */
    public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {
        super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus);
        DicomTreeNode treeNode = (DicomTreeNode) value;
        setText(treeNode.getDisplayString());
        setToolTipText(treeNode.getDisplayString());
        if (value instanceof Media) {
            setIcon(mediaIcon);
        } else if (value instanceof PatientTreeNode) {
            setIcon(patientIcon);
        } else if (value instanceof StudyTreeNode) {
            setIcon(studyIcon);
        } else if (value instanceof SerieTreeNode) {
            setIcon(serieIcon);
        }
        return this;
    }

}
