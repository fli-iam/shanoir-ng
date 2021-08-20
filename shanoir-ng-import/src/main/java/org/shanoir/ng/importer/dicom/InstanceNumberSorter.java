package org.shanoir.ng.importer.dicom;

import java.util.Comparator;

import org.shanoir.ng.importer.model.Instance;

public class InstanceNumberSorter implements Comparator<Instance> {

	@Override
	public int compare(Instance i1, Instance i2) {
		String i1InstanceNumberStr = i1.getInstanceNumber();
		String i2InstanceNumberStr = i2.getInstanceNumber();
		int i1InstanceNumberInt = Integer.parseInt(i1InstanceNumberStr);
		int i2InstanceNumberInt = Integer.parseInt(i2InstanceNumberStr);
		if (i1InstanceNumberInt == i2InstanceNumberInt) {
			return 0;
		} else {
			if (i1InstanceNumberInt < i2InstanceNumberInt) {
				return -1;
			} else {
				return 1;
			}
		}
	}

}