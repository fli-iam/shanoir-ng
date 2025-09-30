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
    private static final Logger logger = LoggerFactory.getLogger(UniquePatientTreeSelectionModel.class);

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
                logger.warn("Multiple selection allowed only for the same patient.");
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
