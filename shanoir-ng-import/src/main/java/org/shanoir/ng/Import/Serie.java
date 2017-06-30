package org.shanoir.ng.Import;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Serie.
 *
 * @author ifakhfakh
 */
public class Serie implements DicomTreeNode {
	
	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(Serie.class);

	/**
	 * Get the frame count of the given dicom object.
	 *
	 * @param dcmObj
	 *            the dcmObj
	 * @return the frame count
	 */
	private static int getFrameCount(final DicomObject dcmObj) {
		if (dcmObj != null) {
			DicomElement pffgs = dcmObj.get(Tag.PerFrameFunctionalGroupsSequence);
			if (pffgs != null) {
				return pffgs.countItems();
			} else {
				return 0;
			}
		} else {
			return -1;
		}
	}

	/** Only used when importing from a DICOMDIR. */
	private String archivePath;

	/** Description map, contains data about this object. */
	private HashMap<String, String> descriptionMap;

	/** The equipment associated to the Serie */
	//private Equipment equipment;

	/** Only used for multi-frame series. */
	private int frameCount;

	/** Image path of the series images. */
	private ArrayList<String> imagesPathList;

	/** Is the image compressed? */
	private Boolean isCompressed = null;

	/** Indicates if the associated DICOM files serie has been selected. */
	private boolean isMultiFrame = false;


	/** Non-Image path of the series images. */
	private ArrayList<String> nonImagesPathList;

	/** Parent node. */
	//private TreeNode parent;

	/** Indicates if the node has been selected. */
	private boolean selected = false;

	/** The sopClassUID. */
	private String sopClassUID;

	/**
	 * Only used for multi-frame series. We cache the file because it is too
	 * heavy to unzip.
	 */
	private String tmpImageDicomPath;

	/**
	 * Creates a new Serie object.
	 *
	 * @param modality
	 *            the modality
	 * @param protocol
	 *            the protocol
	 * @param desc
	 *            the desc
	 * @param id
	 *            the id
	 * @param date
	 *            the date
	 */
	public Serie(final String modality, final String protocol, final String desc, final String id, final String date,
			final String number) {
		descriptionMap = new HashMap<String, String>();
		descriptionMap.put("id", id);
		descriptionMap.put("modality", modality);
		descriptionMap.put("protocol", protocol);
		descriptionMap.put("description", desc);
		descriptionMap.put("images", "0");
		descriptionMap.put("non-images", "0");
		descriptionMap.put("seriesDate", date);
		descriptionMap.put("seriesNumber", number);
		imagesPathList = new ArrayList<String>();
		nonImagesPathList = new ArrayList<String>();
	}




	/**
	 * Adds the image.
	 *
	 * @param imagePath
	 *            the image path
	 */
	private void addImage(final String imagePath) {
		getImagesPathList().add(imagePath);
		getDescriptionMap().put("images", new Integer(getImagesPathList().size()).toString());
	}

	/**
	 * Adds the non-image object.
	 *
	 * @param imagePath
	 *            the image path
	 */
	private void addNonImage(final String imagePath) {
		getNonImagesPathList().add(imagePath);
		getDescriptionMap().put("non-images", new Integer(getNonImagesPathList().size()).toString());
	}

	/**
	 * Gets the child.
	 *
	 * @param id
	 *            id
	 *
	 * @return the child
	 */
	/*public TreeNode getChild(final Object id) {
		return null;
	}*/

	/*
	 * (non-Javadoc)
	 *
	 * @see org.richfaces.model.TreeNodeImpl#getChildren()
	 */
	public Iterator getChildren() {
		return null;
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
	 * @see org.shanoir.dicom.model.DicomTreeNode#getDescriptionKeys()
	 */
	public List<String> getDescriptionKeys() {
		final ArrayList<String> keys = new ArrayList<String>();
		for (final String key : getDescriptionMap().keySet()) {
			keys.add(key);
		}
		return keys;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.shanoir.dicom.model.DicomTreeNode#getDescriptionMap()
	 */
	public HashMap<String, String> getDescriptionMap() {
		return descriptionMap;
	}


	/**
	 * @return the equipment
	 */
	/*public Equipment getEquipment() {
		return equipment;
	}*/

	public int getFrameCount() {
		if (!isMultiFrame) {
			return getImagesPathList().size();
		} else {
			return frameCount;
		}
	}

	/**
	 * Gets the id.
	 *
	 * @return Returns the uid.
	 */
	public String getId() {
		return descriptionMap.get("id");
	}

	/**
	 * Gets the images path list.
	 *
	 * @return the images path list
	 */
	public ArrayList<String> getImagesPathList() {
		return imagesPathList;
	}

	/**
	 * Gets the non images path list.
	 *
	 * @return the non images path list
	 */
	public ArrayList<String> getNonImagesPathList() {
		return nonImagesPathList;
	}


	/**
	 * Get the sopCalssUID.
	 *
	 * @param file
	 *            the dicom File
	 * @return the sopClassUID
	 */
	private String getSopclassUID(final File file) {
		String sopClassUID = null;
		if (!isMultiFrame) {
			try {
				final InputStream stream = new FileInputStream(file.getAbsolutePath());
				if (stream != null) {
					DicomObject dcmObj;
					DicomInputStream din = null;

					din = new DicomInputStream(stream);
					din.setHandler(new StopTagInputHandler(Tag.PixelData));
					dcmObj = din.readDicomObject();
					din.close();
					stream.close();
					sopClassUID = dcmObj.getString(Tag.SOPClassUID);

				}
			} catch (final IOException exc) {
				exc.printStackTrace();
			}
		} else {
			sopClassUID = this.sopClassUID;
		}
		return sopClassUID;
	}


	/**
	 * Gets the type.\
	 *
	 * @return the type
	 */
	public String getType() {
		return "Serie";
	}



	/*
	 * (non-Javadoc)
	 *
	 * @see org.richfaces.model.TreeNodeImpl#isLeaf()
	 */
	public boolean isLeaf() {
		return true;
	}

	public boolean isMultiFrame() {
		return isMultiFrame;
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
		// leaf
	}

	/**
	 * Sets the data.
	 *
	 * @param arg0
	 *            arg0
	 */
	public void setData(final Object arg0) {
		// TODO Auto-generated method stub
	}

	/**
	 * @param equipment
	 *            the equipment to set
	 */

	/**
	 * Sets the images count.
	 *
	 * @param count
	 *            the new images count
	 */
	public void setImagesCount(final int count) {
		getDescriptionMap().put("images", "" + count);
	}

	/**
	 * Sets the images path list.
	 *
	 * @param imagesPathList
	 *            the new images path list
	 */
	public void setImagesPathList(final ArrayList<String> imagesPathList) {
		this.imagesPathList = imagesPathList;
	}

	public void setMultiFrame(boolean isMultiFrame) {
		this.isMultiFrame = isMultiFrame;
	}

	/**
	 * Sets the non images path list.
	 *
	 * @param nonImagesPathList
	 *            the new non images path list
	 */
	public void setNonImagesPathList(final ArrayList<String> nonImagesPathList) {
		this.nonImagesPathList = nonImagesPathList;
	}


	/**
	 * Sets the selected.
	 *
	 * @param selected
	 *            selected
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
		String result = "[Serie: " + descriptionMap + ", isSelected=" + selected + " imagefilePath =  " + imagesPathList + " nonimagefilePath =  " + nonImagesPathList + "]";

		return result;
	}

	@Override
	public void addTreeNode(String id, DicomTreeNode child) {
	}

	@Override
	public DicomTreeNode getFirstTreeNode() {
		return null;
	}

	@Override
	public HashMap<String, DicomTreeNode> getTreeNodes() {
		return null;
	}

	@Override
	public void addTreeNodes(DicomTreeNode firstLevelChild, DicomTreeNode secondLevelChild, DicomTreeNode thirdLevelChild) {
	}

	@Override
	public DicomTreeNode initChildTreeNode(DicomObject dicomObject) {
		return null;
	}




	@Override
	public String getDisplayString() {
		// TODO Auto-generated method stub
		return null;
	}
}

