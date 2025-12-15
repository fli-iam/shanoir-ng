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

package org.shanoir.ng.importer.dicom;

import java.util.Comparator;

import org.shanoir.ng.importer.model.Instance;

public class InstanceNumberSorter implements Comparator<Instance> {

    @Override
    public int compare(Instance i1, Instance i2) {
        int i1InstanceNumberInt = parseInstanceNumber(i1.getInstanceNumber());
        int i2InstanceNumberInt = parseInstanceNumber(i2.getInstanceNumber());
        return Integer.compare(i1InstanceNumberInt, i2InstanceNumberInt);
    }

    int parseInstanceNumber(String instanceNumberStr) {
        try {
            return instanceNumberStr != null ? Integer.parseInt(instanceNumberStr) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
