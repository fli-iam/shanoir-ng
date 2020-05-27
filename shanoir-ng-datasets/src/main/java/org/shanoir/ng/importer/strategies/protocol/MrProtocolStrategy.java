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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.emf.MultiframeExtractor;
import org.shanoir.ng.datasetacquisition.model.mr.AcquisitionContrast;
import org.shanoir.ng.datasetacquisition.model.mr.ContrastAgentUsed;
import org.shanoir.ng.datasetacquisition.model.mr.ImagedNucleus;
import org.shanoir.ng.datasetacquisition.model.mr.MrProtocol;
import org.shanoir.ng.datasetacquisition.model.mr.MrProtocolMetadata;
import org.shanoir.ng.datasetacquisition.model.mr.MrSequenceKSpaceFill;
import org.shanoir.ng.datasetacquisition.model.mr.ParallelAcquisitionTechnique;
import org.shanoir.ng.datasetacquisition.model.mr.PatientPosition;
import org.shanoir.ng.importer.dto.CoilDTO;
import org.shanoir.ng.importer.dto.CoilType;
import org.shanoir.ng.importer.dto.Serie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MrProtocolStrategy implements ProtocolStrategy {

	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(MrProtocolStrategy.class);

	@Override
	public MrProtocol generateMrProtocolForSerie(Attributes attributes, Serie serie) {
		if (Boolean.TRUE.equals(serie.getIsEnhancedMR())) {
			// MultiFrameExtractor is only used in case of EnhancedMR MRI.
			MultiframeExtractor emf = new MultiframeExtractor();
			attributes = emf.extract(attributes, 0);
		}
		
		MrProtocol mrProtocol = new MrProtocol();
		MrProtocolMetadata mrProtocolMetadata = createOriginMrProtocolMetadata(attributes, serie);
		mrProtocol.setOriginMetadata(mrProtocolMetadata);

		// Imaged nucleus
		final ImagedNucleus imagedNucleus = getImagedNucleus(attributes, serie.getIsEnhancedMR());
		if (imagedNucleus != null) {
			LOG.debug("extractMetadata : imagedNucleus=" + imagedNucleus.toString());
			mrProtocol.setImagedNucleus(imagedNucleus);
		}

		// filters : private Siemens tag : (0051,1016)
		final String filters = attributes.getString(0x00511016);
		LOG.debug("extractMetadata : filters=" + filters);
		mrProtocol.setFilters(filters);

		// Imaging Frequency
		final Double imagingFrequency = attributes.getDouble(Tag.ImagingFrequency, 0);
		LOG.debug("extractMetadata : imagingFrequency=" + imagingFrequency);
		mrProtocol.setImagingFrequency(imagingFrequency);

		// Acquisition duration
		final Double acquisitionDuration = attributes.getDouble(Tag.AcquisitionDuration, 0);
		LOG.debug("extractMetadata : acquisitionDuration=" + acquisitionDuration);
		mrProtocol.setAcquisitionDuration(acquisitionDuration);

		// Echo Train Length
		final Integer echoTrainLength = attributes.getInt(Tag.EchoTrainLength, 0);
		LOG.debug("extractMetadata : echoTrainLength=" + echoTrainLength);
		mrProtocol.setEchoTrainLength(echoTrainLength);

		// Number of averages
		final Integer numberOfAverages = getNumberOfAverages(attributes);
		LOG.debug("extractMetadata : numberOfAverages=" + numberOfAverages);
		mrProtocol.setNumberOfAverages(numberOfAverages);

		// Number of Phase Encoding Steps
		final Integer numberOfPhaseEncodingSteps = attributes.getInt(Tag.NumberOfPhaseEncodingSteps, 0);
		LOG.debug("extractMetadata : numberOfPhaseEncodingSteps=" + numberOfPhaseEncodingSteps);
		mrProtocol.setNumberOfPhaseEncodingSteps(numberOfPhaseEncodingSteps);

		// Pixel Spacing Spatial resolution X & Y
		final double[] pixelspacing = attributes.getDoubles(Tag.PixelSpacing);
		if (pixelspacing != null && pixelspacing.length == 2) {
			final Double pixelSpacingX = pixelspacing[0];
			final Double pixelSpacingY = pixelspacing[1];
			LOG.debug("extractMetadata : pixelSpacingX=" + pixelSpacingX);
			LOG.debug("extractMetadata : pixelSpacingY=" + pixelSpacingY);
			mrProtocol.setPixelSpacingX(pixelSpacingX);
			mrProtocol.setPixelSpacingY(pixelSpacingY);
		}

		// Slice thickness
		final Double sliceThickness = attributes.getDouble(Tag.SliceThickness, 0);
		LOG.debug("extractMetadata : sliceThickness=" + sliceThickness);
		mrProtocol.setSliceThickness(sliceThickness);

		// Spacing between slices
		final Double sliceSpacing = attributes.getDouble(Tag.SpacingBetweenSlices, 0);
		LOG.debug("extractMetadata : sliceSpacing=" + sliceSpacing);
		mrProtocol.setSliceSpacing(sliceSpacing);

		// Acquisition Resolution X & Y
		final Integer[] acquisitionMatrixDimension = getAcquisitionResolution(attributes, serie.getIsEnhancedMR());
		if (acquisitionMatrixDimension != null && acquisitionMatrixDimension.length == 2) {
			final Integer acquisitionResolutionX = acquisitionMatrixDimension[0];
			final Integer acquisitionResolutionY = acquisitionMatrixDimension[1];
			mrProtocol.setAcquisitionResolutionX(acquisitionResolutionX);
			mrProtocol.setAcquisitionResolutionY(acquisitionResolutionY);
		}

		// Fov X
		/* FOV_x = Rows (0028,0010) x first value of Pixel Spacing (0028,0030) */
		final Integer rows = attributes.getInt(Tag.Rows, 0);
		if (rows != null && mrProtocol.getPixelSpacingX() != null) {
			final Double fovX = rows * mrProtocol.getPixelSpacingX();
			LOG.debug("extractMetadata : fovX=" + fovX);
			mrProtocol.setFovX(fovX);
		}

		// Fov Y
		/*
		 * FOV_Y = Columns (0028,0011) x second value of Pixel Spacing (0028,0030)
		 */
		final Integer columns = attributes.getInt(Tag.Columns, 0);
		if (columns != null && mrProtocol.getPixelSpacingY() != null) {
			final Double fovY = columns * mrProtocol.getPixelSpacingY();
			LOG.debug("extractMetadata : fovY=" + fovY);
			mrProtocol.setFovY(fovY);
		}

		if (serie.getIsEnhancedMR()) {
			Integer acquisitionResolutionX = null;
			Integer acquisitionResolutionY = null;
			final String inPlanePhaseEncodingDirection = attributes.getString(Tag.InPlanePhaseEncodingDirection);
			if (inPlanePhaseEncodingDirection != null) {
				final Integer mRAcquisitionFrequencyEncodingSteps = attributes.getInt(Tag.MRAcquisitionFrequencyEncodingSteps, Integer.MIN_VALUE);
				if (inPlanePhaseEncodingDirection.equals("ROW")) {
					if (mrProtocol.getFovX() != null && mRAcquisitionFrequencyEncodingSteps != null
							&& mRAcquisitionFrequencyEncodingSteps != 0) {
						Double temp = mrProtocol.getFovX() / mRAcquisitionFrequencyEncodingSteps;
						acquisitionResolutionX = temp.intValue();
					}
					if (mrProtocol.getFovY() != null && mrProtocol.getNumberOfPhaseEncodingSteps() != null
							&& mrProtocol.getNumberOfPhaseEncodingSteps() != 0) {
						Double temp = mrProtocol.getFovY() / mrProtocol.getNumberOfPhaseEncodingSteps();
						acquisitionResolutionY = temp.intValue();
					}
				} else if (inPlanePhaseEncodingDirection.equals("COLUMN")) {
					if (mrProtocol.getFovX() != null && mrProtocol.getNumberOfPhaseEncodingSteps() != null
							&& mrProtocol.getNumberOfPhaseEncodingSteps() != 0) {
						Double temp = mrProtocol.getFovX() / mrProtocol.getNumberOfPhaseEncodingSteps();
						acquisitionResolutionX = temp.intValue();
					}
					if (mrProtocol.getFovY() != null && mRAcquisitionFrequencyEncodingSteps != null
							&& mRAcquisitionFrequencyEncodingSteps != 0) {
						Double temp = mrProtocol.getFovY() / mRAcquisitionFrequencyEncodingSteps;
						acquisitionResolutionY = temp.intValue();
					}
				}
				mrProtocol.setAcquisitionResolutionX(acquisitionResolutionX);
				mrProtocol.setAcquisitionResolutionY(acquisitionResolutionY);
			}
		}

		// Number of Temporal Positions
		final Integer numberOfTemporalPositions = attributes.getInt(Tag.NumberOfTemporalPositions, 0);
		LOG.debug("extractMetadata : numberOfTemporalPositions=" + numberOfTemporalPositions);
		mrProtocol.setNumberOfTemporalPositions(numberOfTemporalPositions);

		// Temporal resolution
		final Double temporalResolution = attributes.getDouble(Tag.TemporalResolution, 0);
		LOG.debug("extractMetadata : temporalResolution=" + temporalResolution);
		mrProtocol.setTemporalResolution(temporalResolution);

		// Percent sampling
		final Double percentSampling = attributes.getDouble(Tag.PercentSampling, 0);
		LOG.debug("extractMetadata : percentSampling=" + percentSampling);
		mrProtocol.setPercentSampling(percentSampling);

		// Percent phase field of view
		final Double percentPhaseFieldOfView = attributes.getDouble(Tag.PercentPhaseFieldOfView, 0);
		LOG.debug("extractMetadata : percentPhaseFieldOfView=" + percentPhaseFieldOfView);
		mrProtocol.setPercentPhaseFov(percentPhaseFieldOfView);

		// Pixel bandwidth
		final Double pixelBandwidth = attributes.getDouble(Tag.PixelBandwidth, 0);
		LOG.debug("extractMetadata : pixelBandwidth=" + pixelBandwidth);
		mrProtocol.setPixelBandwidth(pixelBandwidth);

		// Patient position
		final PatientPosition patientPosition = getPatientPosition(attributes);
		mrProtocol.setPatientPosition(patientPosition);

		Field[] fieldArrayMrProtocol = mrProtocol.getClass().getDeclaredFields();
		Field[] fieldArrayMrProtocolMetadata = mrProtocol.getOriginMetadata().getClass().getDeclaredFields();
		
	    SortedSet<Field> fields = new TreeSet<>(new FieldComparator());
	    fields.addAll(Arrays.asList(concat(fieldArrayMrProtocol, fieldArrayMrProtocolMetadata)));

	    StringBuilder b = new StringBuilder("All About ");
	    b.append(mrProtocol.getClass().getName());
	    b.append("\nFields:\n");
	    for(Field field : fields) {
		    field.setAccessible(true);
	        b.append(field.getName());
	        b.append(";");
		    Object value = null;
			try {
				value = field.get(mrProtocol);
			} catch (IllegalArgumentException e) {
				try {
					value = field.get(mrProtocol.getOriginMetadata());
				} catch (IllegalArgumentException | IllegalAccessException e1) {
					LOG.error(e1.getMessage());
				}
			} catch (IllegalAccessException e) {
				LOG.error(e.getMessage());
			}
			if (value != null && !field.getName().contains("Coil")) {
				b.append(value.toString());
			} else {
				b.append("null");
			}
	        b.append("\n");
	    }

	    LOG.debug(b.toString());
        
		return mrProtocol;
	}

	private static Field[] concat(Field[] first, Field[] second) {
	    List<Field> both = new ArrayList<>(first.length + second.length);
	    Collections.addAll(both, first);
	    Collections.addAll(both, second);
	    return both.toArray(new Field[both.size()]);
	}
	
	private static class FieldComparator implements Comparator<Field> {
	    @Override
		public int compare(Field f1, Field f2) {
	        return f1.getName().compareTo(f2.getName());
	    }
	}

	/**
	 * @param attributes
	 * @param serie
	 * @param mrProtocol
	 */
	private MrProtocolMetadata createOriginMrProtocolMetadata(Attributes attributes, Serie serie) {
		MrProtocolMetadata mrProtocolMetadata = new MrProtocolMetadata();
		// Retrieve protocol name and set it as an origin metadata attribute.
		mrProtocolMetadata.setName(serie.getProtocolName());
		mrProtocolMetadata.setMrSequenceName(serie.getSequenceName());

		// Acquisition contrast
		String acquisitionContrast = attributes.getString(Tag.AcquisitionContrast);
		LOG.debug("extractMetadata : Acquisition Contrast=" + acquisitionContrast);
		mrProtocolMetadata.setAcquisitionContrast(AcquisitionContrast.getIdByType(acquisitionContrast));

		// Receiving Coil
		CoilDTO receivingCoil = null;
		final String receivingCoilName = attributes.getString(Tag.ReceiveCoilName);
		final String receivingCoilType = attributes.getString(Tag.ReceiveCoilType);
		if (receivingCoilName != null && !receivingCoilName.equals("")
				|| receivingCoilType != null && !receivingCoilType.equals("")) {
			receivingCoil = new CoilDTO();
			receivingCoil.setName(receivingCoilName);
			receivingCoil.setCoilType(CoilType.valueOf(receivingCoilType));
		}
		if (receivingCoil != null) {
			mrProtocolMetadata.setReceivingCoilId(receivingCoil.getId());
		}

		// Transmitting Coil
		CoilDTO transmittingCoil = null;
		final String transmittingCoilName = attributes.getString(Tag.TransmitCoilName);
		final String transmittingCoilType = attributes.getString(Tag.TransmitCoilType);
		if (transmittingCoilName != null && !transmittingCoilName.equals("")
				|| transmittingCoilType != null && !transmittingCoilType.equals("")) {
			transmittingCoil = new CoilDTO();
			transmittingCoil.setName(transmittingCoilName);
			if (transmittingCoilType != null) {
				transmittingCoil.setCoilType(CoilType.valueOf(transmittingCoilType));
			}
		}
		if (transmittingCoil != null) {
			mrProtocolMetadata.setTransmittingCoilId(transmittingCoil.getId());
		}

		// Volume injected of diluted contrast agent
		final Double injectedVolume = attributes.getDouble(Tag.ContrastBolusVolume, 0);
		LOG.debug("extractMetadata : injectedVolume=" + injectedVolume);
		mrProtocolMetadata.setInjectedVolume(injectedVolume);

		// Contrast agent concentration
		final Double contrastAgentConcentration = attributes.getDouble(Tag.ContrastBolusIngredientConcentration,
				0);
		LOG.debug("extractMetadata : contrastAgentConcentration=" + contrastAgentConcentration);
		mrProtocolMetadata.setContrastAgentConcentration(contrastAgentConcentration);

		// Parallel acquisition. Authorized values : YES, NO
		final String parallelAcquisitionExtracted = attributes.getString(Tag.ParallelAcquisition);
		LOG.debug("extractMetadata : parallelAcquisitionExtracted=" + parallelAcquisitionExtracted);
		if (parallelAcquisitionExtracted != null) {
			if (!parallelAcquisitionExtracted.equals("NO")) {
				mrProtocolMetadata.setParallelAcquisition(true);
			} else {
				mrProtocolMetadata.setParallelAcquisition(false);
			}
		}

		// parallel acquisition technique.
		final String parallelAcquisitionTechniqueExtracted = attributes
				.getString(Tag.ParallelAcquisitionTechnique);
		LOG.debug("extractMetadata : parallelAcquisitionTechniqueExtracted=" + parallelAcquisitionTechniqueExtracted);
		if (parallelAcquisitionTechniqueExtracted != null) {
			mrProtocolMetadata.setParallelAcquisitionTechnique(
					ParallelAcquisitionTechnique.getIdByTechnique(parallelAcquisitionTechniqueExtracted));
		}

		// Time reduction factor for the in-plane direction
		final Double timeReductionFactorForTheInPlaneDirection = attributes
				.getDouble(Tag.ParallelReductionFactorInPlane, 0);
		LOG.debug("extractMetadata : timeReductionFactorForTheInPlaneDirection="
				+ timeReductionFactorForTheInPlaneDirection);
		mrProtocolMetadata.setTimeReductionFactorForTheInPlaneDirection(timeReductionFactorForTheInPlaneDirection);

		// Time reduction factor for the out-of-plane direction
		final Double timeReductionFactorForTheOutOfPlaneDirection = attributes
				.getDouble(Tag.ParallelReductionFactorOutOfPlane, 0);
		LOG.debug("extractMetadata : timeReductionFactorForTheOutOfPlaneDirection="
				+ timeReductionFactorForTheOutOfPlaneDirection);
		mrProtocolMetadata
				.setTimeReductionFactorForTheOutOfPlaneDirection(timeReductionFactorForTheOutOfPlaneDirection);

		final String magnetizationTransferExtracted = attributes.getString(Tag.MagnetizationTransfer);
		LOG.debug("extractMetadata : magnetizationTransferExtracted=" + magnetizationTransferExtracted);
		if (magnetizationTransferExtracted != null) {
			if (magnetizationTransferExtracted.equals("NONE")) {
				mrProtocolMetadata.setMagnetizationTransfer(false);
			} else {
				mrProtocolMetadata.setMagnetizationTransfer(true);
			}
		}

		final String contractAgentUsed = attributes.getString(Tag.ContrastBolusIngredient);
		LOG.debug("extractMetadata : contractAgentUsed=" + contractAgentUsed);
		if (contractAgentUsed != null) {
			mrProtocolMetadata.setContrastAgentUsed(ContrastAgentUsed.getIdByType(contractAgentUsed));
		}

		final String[] sequenceVariant = attributes.getStrings(Tag.SequenceVariant);
		LOG.debug("extractMetadata : sequenceVariant=" + Arrays.toString(sequenceVariant));
		if (sequenceVariant != null) {
			mrProtocolMetadata.setMrSequenceVariant(Arrays.asList(sequenceVariant));
		}

		final String[] scanningSequence = attributes.getStrings(Tag.ScanningSequence);
		LOG.debug("extractMetadata : scanningSequence=" + Arrays.toString(scanningSequence));
		if (scanningSequence != null) {
			mrProtocolMetadata.setMrScanningSequence(Arrays.asList(scanningSequence));
		}

		// K-Space fill
		mrProtocolMetadata.setMrSequenceKSpaceFill(getKSpaceFill(attributes, serie.getIsEnhancedMR()));

		return mrProtocolMetadata;
	}

	private ImagedNucleus getImagedNucleus(final Attributes dicomAttributes, final boolean isEnhancedMR) {
		final String imagedNucleus = getNucleus(dicomAttributes, isEnhancedMR);
		/*
		 * Warning: some dicom images may contain H1 instead of 1H or P31 instead of
		 * 31P.
		 */
		if (imagedNucleus != null && !"".equals(imagedNucleus)) {
			if ("1H".equalsIgnoreCase(imagedNucleus) || "H1".equalsIgnoreCase(imagedNucleus)) {
				return ImagedNucleus.H1;
			} else if ("31P".equalsIgnoreCase(imagedNucleus) || "P31".equalsIgnoreCase(imagedNucleus)) {
				return ImagedNucleus.P31;
			}
		}
		return null;
	}

	/**
	 * acquisition_resolution_x : Simple copy of the second value (frequency
	 * columns) or the fourth value (phase columns) ; Siemens, GE : 2nd ; Philips :
	 * 4th.
	 *
	 * acquisition_resolution_y : Simple copy of the first value (frequency row) or
	 * the third value (phase rows); Siemens, GE : 3rd; Philips : 1st
	 *
	 * @param serieNumber
	 *            the serie number
	 *
	 * @return the acquisition resolution
	 */
	private Integer[] getAcquisitionResolution(final Attributes dicomAttributes, final boolean isEnhancedMR) {
		Integer[] acquisitionResolution = null;
		if (!isEnhancedMR) {
			final int[] acquisitionMatrixDimension = dicomAttributes.getInts(Tag.AcquisitionMatrix);
			final String manufacturer = dicomAttributes.getString(Tag.Manufacturer);
			if (acquisitionMatrixDimension != null && acquisitionMatrixDimension.length == 4 && manufacturer != null) {
				acquisitionResolution = new Integer[2];
				Integer acqX = acquisitionMatrixDimension[1];
				if (acqX != null && acqX.intValue() == 0) {
					acqX = acquisitionMatrixDimension[3];
				}
				Integer acqY = acquisitionMatrixDimension[0];
				if (acqY != null && acqY.intValue() == 0) {
					acqY = acquisitionMatrixDimension[2];
				}
				acquisitionResolution[0] = acqX;
				acquisitionResolution[1] = acqY;
			}
			return acquisitionResolution;
		} else {
			return null;
		}
	}

	/**
	 * Extract the value of the patient position and convert it into a reference
	 * entity.
	 *
	 * @param serieNumber
	 *            the serie number
	 *
	 * @return the corresponding reference entity
	 */
	private PatientPosition getPatientPosition(final Attributes dicomAttributes) {
		final String patientPosition = dicomAttributes.getString(Tag.PatientPosition);
		if (patientPosition != null && !"".equals(patientPosition)) {
			if ("HFP".equals(patientPosition)) {
				return PatientPosition.HEAD_FIRST_PRONE;
			} else if ("HFS".equals(patientPosition)) {
				return PatientPosition.HEAD_FIRST_SUPINE;
			} else if ("HFDR".equals(patientPosition)) {
				return PatientPosition.HEAD_FIRST_DECUBITUS_RIGHT;
			} else if ("HFDL".equals(patientPosition)) {
				return PatientPosition.HEAD_FIRST_DECUBITUS_LEFT;
			} else if ("FFDR".equals(patientPosition)) {
				return PatientPosition.FEET_FIRST_DECUBITUS_RIGHT;
			} else if ("FFDL".equals(patientPosition)) {
				return PatientPosition.FEET_FIRST_DECUBITUS_LEFT;
			} else if ("FFP".equals(patientPosition)) {
				return PatientPosition.FEET_FIRST_PRONE;
			} else if ("FFS".equals(patientPosition)) {
				return PatientPosition.FEET_FIRST_SUPINE;
			}
		}
		return null;
	}

	/**
	 * Extract the value of the K-space fill
	 *
	 *
	 * - 'Conventional Cartesian sequence': (if Echo train length == 1 AND Geometry
	 * category of k-Space traversal == RECTILINEAR )
	 *
	 * - 'Non-Conventional Cartesian sequence' (if Echo train length > 1 AND
	 * Geometry category of k-Space traversal == RECTILINEAR )
	 *
	 * - 'Non-Conventional Non-Cartesian sequence' (if Echo train length > 1 AND
	 * Geometry category of k-Space traversal != RECTILINEAR )
	 *
	 * NOTE: There may be cases in which this method is irrelevant, e.g. series with
	 * multiple echo to determine T1 or T2 maps
	 *
	 * @param serieNumber
	 *            the serie number
	 *
	 * @return the corresponding reference entity
	 */
	private MrSequenceKSpaceFill getKSpaceFill(final Attributes dicomAttributes, final boolean isEnhancedMR) {
		final String geometryOfKSpaceTraversal = dicomAttributes.getString(Tag.GeometryOfKSpaceTraversal);
		final Integer echoTrainLength = dicomAttributes.getInt(Tag.EchoTrainLength, Integer.MIN_VALUE);
		if (geometryOfKSpaceTraversal != null && !"".equals(geometryOfKSpaceTraversal) && echoTrainLength != null) {
			/* The defined terms are RECTILINEAR, RADIAL, SPIRAL */
			if (echoTrainLength == 1 && "RECTILINEAR".equalsIgnoreCase(geometryOfKSpaceTraversal)) {
				return MrSequenceKSpaceFill.CONVENTIONAL_CARTESIAN_SEQUENCE;
			} else if (echoTrainLength > 1 && "RECTILINEAR".equalsIgnoreCase(geometryOfKSpaceTraversal)) {
				return MrSequenceKSpaceFill.NON_CONVENTIONAL_CARTESIAN_SEQUENCE;
			} else if (echoTrainLength > 1 && !"RECTILINEAR".equalsIgnoreCase(geometryOfKSpaceTraversal)) {
				return MrSequenceKSpaceFill.NON_CONVENTIONAL_NON_CARTESIAN_SEQUENCE;
			}
		}
		return null;
	}

	public String getNucleus(final Attributes dicomAttributes, final boolean isEnhancedMR) {
		if (!isEnhancedMR) {
			return dicomAttributes.getString(Tag.ImagedNucleus);
		} else {
			return dicomAttributes.getString(Tag.ResonantNucleus);
		}
	}

	public Integer getNumberOfAverages(final Attributes attributes) {
		String numberOfAverages = attributes.getString(Tag.NumberOfAverages);
		if (numberOfAverages != null) {
			final Double objDouble = Double.valueOf(numberOfAverages);
			return objDouble.intValue();
		} else {
			return null;
		}
	}

}
