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

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.datasetacquisition.model.pet.PetProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author yyao
 *
 */
@Component
public class PetProtocolStrategy {
	
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(PetProtocolStrategy.class);
	
	public PetProtocol generateProtocolForSerie(Attributes attributes) {		
		PetProtocol petProtocol = new PetProtocol();  
		
		/** (0028, 0010) Rows */
		final Integer dimensionX = attributes.getInt(Tag.Rows, 0);
		LOG.debug("extractMetadata : dimensionX=" + dimensionX);
		petProtocol.setDimensionX(dimensionX);

		/** (0028, 0011) Columns */
		final Integer dimensionY = attributes.getInt(Tag.Columns, 0);
		LOG.debug("extractMetadata : dimensionY=" + dimensionY);
		petProtocol.setDimensionY(dimensionY);
		
		/** (0054, 0081) Number of Slices */
		final Integer numberOfSlices = attributes.getInt(Tag.NumberOfSlices, 0);
		LOG.debug("extractMetadata : numberOfSlices=" + numberOfSlices);
		petProtocol.setNumberOfSlices(numberOfSlices);
		
		/**
		 * (0028, 0030) Pixel Spacing in X and Y direction in mm. 
		 * The unit of measure of voxel size X, must be in mm.
		 */
		final double[] pixelspacing = attributes.getDoubles(Tag.PixelSpacing);
		if (pixelspacing != null && pixelspacing.length == 2) {
			final Double voxelSizeX = pixelspacing[0];
			final Double voxelSizeY = pixelspacing[1];
			LOG.debug("extractMetadata : pixelSpacingX=" + voxelSizeX);
			LOG.debug("extractMetadata : pixelSpacingY=" + voxelSizeY);
			petProtocol.setVoxelSizeX(voxelSizeX);
			petProtocol.setVoxelSizeY(voxelSizeY);
		}
		
		/**
		 * (0018, 0050) Slice Thickness in mm. 
		 * The unit of measure of voxel size Z, must be in mm.
		 */
		final Double voxelSizeZ = attributes.getDouble(Tag.SliceThickness, 0);
		LOG.debug("extractMetadata : voxelSizeZ=" + voxelSizeZ);
		petProtocol.setVoxelSizeZ(voxelSizeZ);
		
		return petProtocol;
	}

}
