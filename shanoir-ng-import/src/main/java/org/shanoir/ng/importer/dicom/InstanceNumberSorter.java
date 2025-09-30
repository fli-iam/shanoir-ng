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
