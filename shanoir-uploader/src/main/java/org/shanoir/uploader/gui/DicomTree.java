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