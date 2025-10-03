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

package org.shanoir.ng.bids.model;

/**
 * Leaf class of composite object for BIDS elements.
 * These classes exists to serialize/deserialize a BIDS folder to be visualized in front
 * And to be able to modify a bids file from the front
 * This class represents a File with content
 * @author JComeD
 *
 */
public class BidsFile extends BidsElement {

    /** The content of the bids file. */
    String content;

    public BidsFile(String absolutePath) {
        super(absolutePath);
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * This methods checks from the file name if it is editable or not.
     * @return <code>true</code> if the file can be edited, <code>false</code> otherwise
     */
    public boolean isEditable() {
        return false;
    }

    /**
     * This methods checks from the file name if it is deletable or not.
     * @return <code>true</code> if the file can be deleted, <code>false</code> otherwise
     */
    public boolean isDeletable() {
        return false;
    }

    @Override
    public boolean isFile() {
        return true;
    }
}
