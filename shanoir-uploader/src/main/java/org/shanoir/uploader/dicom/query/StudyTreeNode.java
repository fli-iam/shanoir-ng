package org.shanoir.uploader.dicom.query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

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

	private DicomTreeNode parent;
	
	private Study study;

	/** List of children. */
	private HashMap<String, DicomTreeNode> relatedSeries;

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
		this.relatedSeries = new LinkedHashMap<String, DicomTreeNode>();
	}

	/**
	 * Adds the child.
	 *
	 * @param id
	 *            id
	 * @param serie
	 *            serie
	 */
	public void addTreeNode(final String id, final DicomTreeNode serie) {
		this.relatedSeries.put(id, (SerieTreeNode) serie);
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
		return (TreeNode)relatedSeries.get(id);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.richfaces.model.TreeNodeImpl#getChildren()
	 */
	public Iterator getChildren() {
		List<Entry<String, DicomTreeNode>> child = new ArrayList<Entry<String, DicomTreeNode>>(relatedSeries.entrySet());
		Collections.sort(child, new SeriesComparator());
		return child.iterator();
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
		return study.getStudyDescription();
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
			return getTreeNodes().values().iterator().next();
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
	public HashMap<String, DicomTreeNode> getTreeNodes() {
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
		relatedSeries.remove((String) id);
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent
	 *            parent
	 */
	public void setParent(final DicomTreeNode parent) {
		this.parent = parent;
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
		return serieTreeNode;
	}

	public void addTreeNodes(DicomTreeNode firstLevelChild, DicomTreeNode secondLevelChild, DicomTreeNode thirdLevelChild) {
	}
	
}
