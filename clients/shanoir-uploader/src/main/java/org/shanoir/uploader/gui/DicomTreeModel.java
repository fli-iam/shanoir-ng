package org.shanoir.uploader.gui;

import java.util.Vector;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.shanoir.dicom.model.DicomTreeNode;
import org.shanoir.uploader.dicom.query.Media;

/**
 * This is the model used to display a tree when
 * using the tree which is represented with the
 * Media and his corresponding objects.
 * @author mkain
 *
 */
public class DicomTreeModel implements TreeModel {
	
	private static Logger logger = Logger.getLogger(DicomTreeModel.class);

    private Vector<TreeModelListener> treeModelListeners =
        new Vector<TreeModelListener>();
    
    private Media media;
 
    public DicomTreeModel(Media media) {
        this.media = media;
    } 
 
    /**
     * Returns the child of parent at index index in the parent's child array.
     */
    public Object getChild(Object parent, int index) {
    	DicomTreeNode p = (DicomTreeNode) parent;
    	return p.getTreeNodes().values().toArray()[index];
    }
 
    /**
     * Returns the number of children of parent.
     */
    public int getChildCount(Object parent) {
        DicomTreeNode p = (DicomTreeNode) parent;
        return p.getTreeNodes().size();
    }
 
    /**
     * Returns the index of child in parent.
     */
    public int getIndexOfChild(Object parent, Object child) {
        DicomTreeNode p = (DicomTreeNode) parent;
        DicomTreeNode[] children = (DicomTreeNode[])
        		p.getTreeNodes().values().toArray();
        for (int i = 0; i < children.length; i++) {
			if (children[i].equals(child)) {
				return i;
			}
		}
        return -1;
    }
 
    /**
     * Returns the root of the tree.
     */
    public Object getRoot() {
        return media;
    }
 
    /**
     * Returns true if node is a leaf.
     */
    public boolean isLeaf(Object node) {
        DicomTreeNode d = (DicomTreeNode) node;
        return d.getTreeNodes().size() == 0;
    }

    /**
     * Adds a listener for the TreeModelEvent posted after the tree changes.
     */
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.addElement(l);
    }
 
    /**
     * Removes a listener previously added with addTreeModelListener().
     */
    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.removeElement(l);
    }
 
    /**
     * Messaged when the user has altered the value for the item
     * identified by path to newValue. Not used by this model.
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
        logger.info("*** valueForPathChanged : " + path + " --> " + newValue);
    }

}