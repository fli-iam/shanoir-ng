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

package org.shanoir.uploader.model;

import java.util.List;

public class Center implements Comparable<Center> {

    private Long id;

    private String name;

    private List<Investigator> investigators;

    public Center() {
    }

    public Center(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Investigator> getInvestigatorList() {
        return investigators;
    }

    public void setInvestigatorList(List<Investigator> investigatorList) {
        this.investigators = investigatorList;
    }

    public String toString() {
        return this.getName();
    }

    public int compareTo(Center o) {
        return Long.compare(this.getId(), o.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Center other = (Center) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            final int prime = 31;
            int result = 1;
            result = prime * result + getId().hashCode();
            return result;
        }
        // ID is not set, return a unique constant hash code to avoid
        // all objects having the same hash code of 31.
        return System.identityHashCode(this);
    }
}
