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

package org.shanoir.ng.importer.dcm2nii;

import java.util.Arrays;
import java.util.Set;

import org.shanoir.ng.importer.model.EchoTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This private class only holds 3 values as the keys for sorting the dicom
 * files into separate folders. 1 folder = 1 dataset. In one folder, we group
 * all the files with the same value for the EchoNumbers, AcquisitionNumber and
 * ImageOrientationPatien.
 *
 * @author aferial
 * @author mkain
 */
public class SerieToDatasetsSeparator {

	private static final Logger LOG = LoggerFactory.getLogger(SerieToDatasetsSeparator.class);

	/** corresponding dicom tag. */
	protected int acquisitionNumber = 0;

	/** corresponding dicom tag. */
	protected Set<EchoTime> echoTime = null;

	/** corresponding dicom tag. */
	protected double[] imageOrientationPatient = null;

	/**
	 * Constructor with fields.
	 *
	 * @param echoNumbers
	 *            the echo numbers
	 * @param acquisitionNumber
	 *            the acquisition number
	 * @param imageOrientationPatient
	 *            the image orientation patient
	 */
	public SerieToDatasetsSeparator(final int acquisitionNumber, final Set<EchoTime> echoTime,
			final double[] imageOrientationPatient) {
		this.acquisitionNumber = acquisitionNumber;
		this.echoTime = echoTime;
		this.imageOrientationPatient = imageOrientationPatient;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + acquisitionNumber;
		result = prime * result + echoTime.hashCode();
		result = prime * result + Arrays.hashCode(imageOrientationPatient);
		return result;
	}

	/**
	 * We consider that the image orientation is the same if the difference is very
	 * small.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SerieToDatasetsSeparator other = (SerieToDatasetsSeparator) obj;
		if (acquisitionNumber != other.acquisitionNumber) {
			return false;
		}
		if (echoTime.hashCode() != other.echoTime.hashCode()) {
			return false;
		}
		if (!imageOrientationEquals(imageOrientationPatient, other.imageOrientationPatient)) {
			return false;
		}
		return true;
	}

	/**
	 * Return true if the patient orientations are roughly the same.
	 *
	 * @param imageOrientationPatient
	 * @param otherImageOrientationPatient
	 * @return true if the patient orientations are roughly the same
	 */
	private boolean imageOrientationEquals(final double[] imageOrientationPatient,
			final double[] otherImageOrientationPatient) {
		if (imageOrientationPatient == null) {
			return otherImageOrientationPatient == null;
		}
		if (otherImageOrientationPatient == null) {
			return imageOrientationPatient == null;
		}
		for (int i = 0; i < imageOrientationPatient.length; i++) {
			double diff = imageOrientationPatient[i] - otherImageOrientationPatient[i];
			if (diff != 0) {
				if (Math.abs(diff) < 0.0001) {
					LOG.warn(
							"imageOrientationEquals : Attention! The image orientation is not strictly parallel. Found "
									+ imageOrientationPatient[i] + " != " + otherImageOrientationPatient[i]
									+ ". However, we tolerate this difference.");
				} else {
					return false;
				}
			}
		}
		return true;
	}

}
