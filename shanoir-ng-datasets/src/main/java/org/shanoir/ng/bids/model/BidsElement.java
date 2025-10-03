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
 * Abstract class of composit object for BIDS elements.
 * These classes exists to serialize/deserialize a BIDS folder to be visualized in front
 * And to be able to modify a bids file from the front
 * @author JComeD
 *
 */
public abstract class BidsElement {

    /** Path of the Bids file. */
    String path;

    public BidsElement(String path) {
        this.path = path;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    public abstract boolean isFile();
}
