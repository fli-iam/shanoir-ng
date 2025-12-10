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

package org.shanoir.uploader.dicom;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

import org.shanoir.uploader.dicom.query.PatientTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom TreeSelectionModel to allow selection of multiple nodes only if they belong to the same patient.
 *
 * @author lvallet
 *
 */
public class UniquePatientTreeSelectionModel extends DefaultTreeSelectionModel {
    private static final Logger LOG = LoggerFactory.getLogger(UniquePatientTreeSelectionModel.class);

    private final JTree tree;

    public UniquePatientTreeSelectionModel(JTree tree) {
        this.tree = tree;
        setSelectionMode(DISCONTIGUOUS_TREE_SELECTION);
    }

    @Override
    public void addSelectionPaths(TreePath[] paths) {
        if (paths == null || paths.length == 0) return;

        // Get all selected patients
        Set<Object> selectedPatients = new HashSet<>();
        if (getSelectionPaths() != null) {
            for (TreePath p : getSelectionPaths()) {
                selectedPatients.add(getPatientNode(p));
            }
        }

        for (TreePath p : paths) {
            Object patient = getPatientNode(p);

            // If a patient is already selected and this one is different → ignore
            if (!selectedPatients.isEmpty() && !selectedPatients.contains(patient)) {
                LOG.warn("Multiple selection allowed only for the same patient.");
                return;
            }
            selectedPatients.add(patient);
        }

        super.addSelectionPaths(paths);
    }

    // Climb up the tree to find the parent PatientTreeNode
    private Object getPatientNode(TreePath path) {
        Object[] nodes = path.getPath();
        for (Object node : nodes) {
            if (node instanceof PatientTreeNode) {
                return node;
            }
        }
        return null;
    }
}
