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

import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;

import org.shanoir.uploader.dicom.query.Media;

/**
 * This class represents the DICOM tree,
 * which is displayed after querying the
 * server. The patient is hold as a ref,
 * because we will need the data later
 * e.g. for the anonymization.
 * @author mkain
 *
 */
public class DicomTree extends JTree {

    public DicomTree(final Media media) {
        super(new DicomTreeModel(media));
        setRootVisible(false);
        getSelectionModel().setSelectionMode(
                TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        DicomTreeCellRenderer renderer = new DicomTreeCellRenderer();
        setCellRenderer(renderer);
    }

}
