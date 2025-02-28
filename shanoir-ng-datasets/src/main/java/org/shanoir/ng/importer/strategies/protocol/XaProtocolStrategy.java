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

package org.shanoir.ng.importer.strategies.protocol;

import java.io.IOException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.datasetacquisition.model.xa.XaProtocol;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.importer.dto.Serie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class XaProtocolStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(XaProtocolStrategy.class);

	public XaProtocol generateProtocolForSerie(AcquisitionAttributes<String> acquisitionAttributes, Serie serie) {		
		XaProtocol protocol = new XaProtocol();
		Attributes attributes = acquisitionAttributes.getFirstDatasetAttributes();

		// Slice thickness
		Double sliceThickness = attributes.getDouble(Tag.SliceThickness, -1);
		sliceThickness = (sliceThickness != -1 ? sliceThickness : null);
		LOG.debug("extractMetadata : sliceThickness=" + sliceThickness);
		protocol.setSliceThickness(sliceThickness);

		/** (0054, 0081) Number of Slices */
		Integer numberOfSlices = attributes.getInt(Tag.NumberOfSlices, -1);
		numberOfSlices = (numberOfSlices != -1) ? numberOfSlices : null;
		LOG.debug("extractMetadata : numberOfSlices=" + numberOfSlices);
		if (numberOfSlices == null) {
			try {
				numberOfSlices = DicomProcessing.countUniqueInstances(serie, false);
				LOG.debug("count nb of slices within the serie : numberOfSlices=" + numberOfSlices);
			} catch (IOException e) {}
		}
		protocol.setNumberOfSlices(numberOfSlices);
		
		return protocol;
	}

}
