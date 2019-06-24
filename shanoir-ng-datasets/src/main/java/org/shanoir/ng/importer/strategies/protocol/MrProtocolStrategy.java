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
import org.shanoir.ng.shared.util.ShanoirConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MrProtocolStrategy implements ProtocolStrategy {
	
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(MrProtocolStrategy.class);
	
	// MultiFrameExtractor is only used in case of EnhancedMR MRI.	
	private MultiframeExtractor emf;
	
	/** Returned Default Value when Dicom Tag is not found */
	private static final int valueNotFoundValue = -999999;
	
	@Override
	public MrProtocol generateMrProtocolForSerie(Attributes dicomAttribute, Serie serie) {
		

		Attributes dicomAttributes;
		
		//	MultiframeExtractor emf = new MultiframeExtractor();
		if (serie.getIsEnhancedMR()) {
			emf = new MultiframeExtractor();
			dicomAttributes = emf.extract(dicomAttribute, 0);
		} else {
			dicomAttributes  = dicomAttribute;
		}
		
		MrProtocol mrProtocol = new MrProtocol();
		MrProtocolMetadata mrProtocolMetadata = createOriginMrProtocolMetadata(dicomAttributes, serie);
		mrProtocol.setOriginMetadata(mrProtocolMetadata);
        
        // Imaged nucleus
        final ImagedNucleus imagedNucleus = getImagedNucleus(dicomAttributes, serie.getIsEnhancedMR());
        LOG.debug("extractMetadata : imagedNucleus=" + imagedNucleus.toString());
        mrProtocol.setImagedNucleus(imagedNucleus);
        
        // filters : private Siemens tag : (0051,1016)
        final String filters = dicomAttributes.getString(0x00511016);
        LOG.debug("extractMetadata : filters=" + filters);
        mrProtocol.setFilters(filters);

        // Imaging Frequency
        final Double imagingFrequency = dicomAttributes.getDouble(Tag.ImagingFrequency,0);
        LOG.debug("extractMetadata : imagingFrequency=" + imagingFrequency);
        mrProtocol.setImagingFrequency(imagingFrequency);

        // Acquisition duration
        final Double acquisitionDuration = dicomAttributes.getDouble(Tag.AcquisitionDuration,0);
        LOG.debug("extractMetadata : acquisitionDuration=" + acquisitionDuration);
        mrProtocol.setAcquisitionDuration(acquisitionDuration);

        // Echo Train Length
        final Integer echoTrainLength = dicomAttributes.getInt(Tag.EchoTrainLength,0);
        LOG.debug("extractMetadata : echoTrainLength=" + echoTrainLength);
        mrProtocol.setEchoTrainLength(echoTrainLength);

        // Number of averages
        final Integer numberOfAverages = dicomAttributes.getInt(Tag.NumberOfAverages,0);
        LOG.debug("extractMetadata : numberOfAverages=" + numberOfAverages);
        mrProtocol.setNumberOfAverages(numberOfAverages);

        // Number of Phase Encoding Steps
        final Integer numberOfPhaseEncodingSteps = dicomAttributes.getInt(Tag.NumberOfPhaseEncodingSteps,0);
        LOG.debug("extractMetadata : numberOfPhaseEncodingSteps=" + numberOfPhaseEncodingSteps);
        mrProtocol.setNumberOfPhaseEncodingSteps(numberOfPhaseEncodingSteps);

        // Pixel Spacing Spatial resolution X & Y
        final double[] pixelspacing = dicomAttributes.getDoubles(Tag.PixelSpacing);
        if (pixelspacing != null && pixelspacing.length == 2) {
            final Double pixelSpacingX = pixelspacing[0];
            final Double pixelSpacingY = pixelspacing[1];
            LOG.debug("extractMetadata : pixelSpacingX=" + pixelSpacingX);
            LOG.debug("extractMetadata : pixelSpacingY=" + pixelSpacingY);
            mrProtocol.setPixelSpacingX(pixelSpacingX);
            mrProtocol.setPixelSpacingY(pixelSpacingY);
        }

        // Slice thickness
        final Double sliceThickness = dicomAttributes.getDouble(Tag.SliceThickness,0);
        LOG.debug("extractMetadata : sliceThickness=" + sliceThickness);
        mrProtocol.setSliceThickness(sliceThickness);

        // Spacing between slices
        final Double sliceSpacing = dicomAttributes.getDouble(Tag.SpacingBetweenSlices,0);
        LOG.debug("extractMetadata : sliceSpacing=" + sliceSpacing);
        mrProtocol.setSliceSpacing(sliceSpacing);
        
        // Acquisition Resolution X & Y
        final Integer[] acquisitionMatrixDimension = getAcquisitionResolution(dicomAttributes, serie.getIsEnhancedMR());
        if (acquisitionMatrixDimension != null && acquisitionMatrixDimension.length == 2) {
            final Integer acquisitionResolutionX = acquisitionMatrixDimension[0];
            final Integer acquisitionResolutionY = acquisitionMatrixDimension[1];
            mrProtocol.setAcquisitionResolutionX(acquisitionResolutionX);
            mrProtocol.setAcquisitionResolutionY(acquisitionResolutionY);
        }
        
        // Fov X
        /* FOV_x = Rows (0028,0010) x first value of Pixel Spacing (0028,0030) */
        final Integer rows = dicomAttributes.getInt(Tag.Rows,0);
        if (rows != null && mrProtocol.getPixelSpacingX() != null) {
            final Double fovX = rows * mrProtocol.getPixelSpacingX();
            LOG.debug("extractMetadata : fovX=" + fovX);
            mrProtocol.setFovX(fovX);
        }

        // Fov Y
        /*
         * FOV_Y = Columns (0028,0011) x second value of Pixel Spacing
         * (0028,0030)
         */
        final Integer columns = dicomAttributes.getInt(Tag.Columns,0);
        if (columns != null && mrProtocol.getPixelSpacingY() != null) {
            final Double fovY = columns * mrProtocol.getPixelSpacingY();
            LOG.debug("extractMetadata : fovY=" + fovY);
            mrProtocol.setFovY(fovY);
        }
        
        if (serie.getIsEnhancedMR()) {
            Integer acquisitionResolutionX = null;
            Integer acquisitionResolutionY = null;
            final String inPlanePhaseEncodingDirection = getInPlanePhaseEncodingDirection(dicomAttributes, serie.getIsEnhancedMR());
            if (inPlanePhaseEncodingDirection != null) {
                final Integer mRAcquisitionFrequencyEncodingSteps = getMRAcquisitionFrequencyEncodingSteps(dicomAttributes, serie.getIsEnhancedMR());
                if (getInPlanePhaseEncodingDirection(dicomAttributes, serie.getIsEnhancedMR()).equals("ROW")) {
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
                } else if (getInPlanePhaseEncodingDirection(dicomAttributes, serie.getIsEnhancedMR()).equals("COLUMN")) {
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
        final Integer numberOfTemporalPositions = dicomAttributes.getInt(Tag.NumberOfTemporalPositions,0);
        LOG.debug("extractMetadata : numberOfTemporalPositions=" + numberOfTemporalPositions);
        mrProtocol.setNumberOfTemporalPositions(numberOfTemporalPositions);

        // Temporal resolution
        final Double temporalResolution = dicomAttributes.getDouble(Tag.TemporalResolution,0);
        LOG.debug("extractMetadata : temporalResolution=" + temporalResolution);
        mrProtocol.setTemporalResolution(temporalResolution);

        // Percent sampling
        final Double percentSampling = dicomAttributes.getDouble(Tag.PercentSampling,0);
        LOG.debug("extractMetadata : percentSampling=" + percentSampling);
        mrProtocol.setPercentSampling(percentSampling);

        // Percent phase field of view
        final Double percentPhaseFieldOfView = dicomAttributes.getDouble(Tag.PercentPhaseFieldOfView,0);
        LOG.debug("extractMetadata : percentPhaseFieldOfView=" + percentPhaseFieldOfView);
        mrProtocol.setPercentPhaseFov(percentPhaseFieldOfView);

        // Pixel bandwidth
        final Double pixelBandwidth = dicomAttributes.getDouble(Tag.PixelBandwidth,0);
        LOG.debug("extractMetadata : pixelBandwidth=" + pixelBandwidth);
        mrProtocol.setPixelBandwidth(pixelBandwidth);
        
        // Patient position
        final PatientPosition patientPosition = getPatientPosition(dicomAttributes);
        mrProtocol.setPatientPosition(patientPosition);

        // TODO ATO : Add preclinical mode condition. if preclinical then implement  DicomMetadataExtractor.extractMetadataCompletingMrProtocol(..) method in shanoir old
        // This method is called by method extractMetadata in same class.
        
		Field[] fieldArrayMrProtocol = mrProtocol.getClass().getDeclaredFields();
		Field[] fieldArrayMrProtocolMetadata = mrProtocol.getOriginMetadata().getClass().getDeclaredFields();
		
	    SortedSet<Field> fields = new TreeSet<Field>(new FieldComparator());
	    fields.addAll(Arrays.asList(concat(fieldArrayMrProtocol, fieldArrayMrProtocolMetadata)));

	    StringBuffer b = new StringBuffer("All About ");
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
				}
			} catch (IllegalAccessException e) {
			}
			if (value != null && !field.getName().contains("Coil")) {
				b.append(value.toString());
			} else {
				b.append("null");
			}
	        b.append("\n");
	    }

	    LOG.info(b.toString());
        
		return mrProtocol;
	}

	private static Field[] concat(Field[] first, Field[] second) {
	    List<Field> both = new ArrayList<Field>(first.length + second.length);
	    Collections.addAll(both, first);
	    Collections.addAll(both, second);
	    return both.toArray(new Field[both.size()]);
	}
	
	private static class FieldComparator implements Comparator<Field> {
	    public int compare(Field f1, Field f2) {
	        return (f1.getName().compareTo(f2.getName()));
	    }   
	}

	/**
	 * @param dicomAttributes
	 * @param serie
	 * @param mrProtocol
	 */
	private MrProtocolMetadata createOriginMrProtocolMetadata(Attributes dicomAttributes, Serie serie) {
		MrProtocolMetadata mrProtocolMetadata = new MrProtocolMetadata();
		// Retrieve protocol name and set it as an origin metadata attribute.
		mrProtocolMetadata.setName(serie.getProtocolName());
		mrProtocolMetadata.setMrSequenceName(serie.getSequenceName());

	      /*
         * Mr Sequence Application, RefMrSequenceKSpaceFill, Slice Order,
         * Contrast agent, Slice orientation at acquisition, Acquisition
         * Contrast, MR Sequence Physics -> to be entered later with the study
         * card mechanism
         */
		
        // Acquisition contrast
        String acquisitionContrast = dicomAttributes.getString(Tag.AcquisitionContrast);
        LOG.debug("extractMetadata : Acquisition Contrast=" + acquisitionContrast);
        mrProtocolMetadata.setAcquisitionContrast(AcquisitionContrast.getIdByType(acquisitionContrast));
		

        // Receiving coil name - tag (0018,1251)
        CoilDTO receivingCoil = null;
        final String receivingCoilName = getReceiveCoilName(dicomAttributes, serie.getIsEnhancedMR());
        // Receiving coil type - tag (0018,9051)
        final String receivingCoilType = getReceiveCoilType(dicomAttributes, serie.getIsEnhancedMR());
        if ((receivingCoilName != null && !receivingCoilName.equals(""))
                || (receivingCoilType != null && !receivingCoilType.equals(""))) {
            receivingCoil = new CoilDTO();
            receivingCoil.setName(receivingCoilName);
            receivingCoil.setCoilType(CoilType.valueOf(receivingCoilType));
        }
        if (receivingCoil != null) {
        		mrProtocolMetadata.setReceivingCoilId(receivingCoil.getId());
        }

        // Transmitting Coil
        CoilDTO transmittingCoil = null;
        final String transmittingCoilName = getTransmitCoilName(dicomAttributes, serie.getIsEnhancedMR());
        final String transmittingCoilType = getTransmitCoilType(dicomAttributes, serie.getIsEnhancedMR());
        if ((transmittingCoilName != null && !transmittingCoilName.equals(""))
                || (transmittingCoilType != null && !transmittingCoilType.equals(""))) {
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
        final Double injectedVolume = dicomAttributes.getDouble(Tag.ContrastBolusVolume,0);
        LOG.debug("extractMetadata : injectedVolume=" + injectedVolume);
        mrProtocolMetadata.setInjectedVolume(injectedVolume);

        // Contrast agent concentration
        final Double contrastAgentConcentration = dicomAttributes.getDouble(Tag.ContrastBolusIngredientConcentration, 0);
        LOG.debug("extractMetadata : contrastAgentConcentration=" + contrastAgentConcentration);
        mrProtocolMetadata.setContrastAgentConcentration(contrastAgentConcentration);

        // Parallel acquisition. Authorized values : YES, NO
        final String parallelAcquisitionExtracted = dicomAttributes.getString(Tag.ParallelAcquisition);
        LOG.debug("extractMetadata : parallelAcquisitionExtracted=" + parallelAcquisitionExtracted);
        if (parallelAcquisitionExtracted != null) {
	        	if (!parallelAcquisitionExtracted.equals("NO")) {
	        		mrProtocolMetadata.setParallelAcquisition(true);
	        	} else {
	        		mrProtocolMetadata.setParallelAcquisition(false);
	        	}
		}

        // parallel acquisition technique.
        final String parallelAcquisitionTechniqueExtracted = dicomAttributes.getString(Tag.ParallelAcquisitionTechnique);
        LOG.debug("extractMetadata : parallelAcquisitionTechniqueExtracted=" + parallelAcquisitionTechniqueExtracted);
        if (parallelAcquisitionTechniqueExtracted != null) {
        		mrProtocolMetadata.setParallelAcquisitionTechnique(ParallelAcquisitionTechnique.getIdByTechnique(parallelAcquisitionTechniqueExtracted));
        }

        // Time reduction factor for the in-plane direction
        final Double timeReductionFactorForTheInPlaneDirection = dicomAttributes.getDouble(Tag.ParallelReductionFactorInPlane,0);
        LOG.debug("extractMetadata : timeReductionFactorForTheInPlaneDirection=" + timeReductionFactorForTheInPlaneDirection);
        mrProtocolMetadata.setTimeReductionFactorForTheInPlaneDirection(timeReductionFactorForTheInPlaneDirection);
        
        // Time reduction factor for the out-of-plane direction
        final Double timeReductionFactorForTheOutOfPlaneDirection = dicomAttributes.getDouble(Tag.ParallelReductionFactorOutOfPlane,0);
        LOG.debug("extractMetadata : timeReductionFactorForTheOutOfPlaneDirection=" + timeReductionFactorForTheOutOfPlaneDirection);
        mrProtocolMetadata.setTimeReductionFactorForTheOutOfPlaneDirection(timeReductionFactorForTheOutOfPlaneDirection);

        // Magnetization transfer. Authorized values : YES, NO
        
        // TODO Fix this using either field : (0018,0021) check if MT value in sequence (cf http://dicomlookup.com/lookup.asp?sw=Tnumber&q=(0018,0021) )
        // OR use Tag.MagnetizationTransfer (cf http://dicomlookup.com/lookup.asp?sw=Tnumber&q=(0018,9020) )  possible values: ON_RESONANCE OFF_RESONANCE NONE 
        // 
        final String magnetizationTransferExtracted = dicomAttributes.getString(Tag.MagnetizationTransfer);
        LOG.debug("extractMetadata : magnetizationTransferExtracted=" + magnetizationTransferExtracted);
        if (magnetizationTransferExtracted != null) {
	        	if (magnetizationTransferExtracted.equals("NONE")) {
	        		mrProtocolMetadata.setMagnetizationTransfer(false);
	        	} else {
	        		mrProtocolMetadata.setMagnetizationTransfer(true);
	        	}
		}
        
        final String contractAgentUsed = dicomAttributes.getString(Tag.ContrastBolusIngredient);
        LOG.debug("extractMetadata : contractAgentUsed=" + contractAgentUsed);
        if (contractAgentUsed != null) {
        	mrProtocolMetadata.setContrastAgentUsed(ContrastAgentUsed.getIdByType(contractAgentUsed));
        }
        
        final String[] sequenceVariant = dicomAttributes.getStrings(Tag.SequenceVariant);
        LOG.debug("extractMetadata : sequenceVariant=" + Arrays.toString(sequenceVariant));
        if (sequenceVariant != null) {
        	mrProtocolMetadata.setMrSequenceVariant(Arrays.asList(sequenceVariant));
        }

        final String[] scanningSequence = dicomAttributes.getStrings(Tag.ScanningSequence);
        LOG.debug("extractMetadata : scanningSequence=" + Arrays.toString(scanningSequence));
        if (scanningSequence != null) {
        	mrProtocolMetadata.setMrScanningSequence(Arrays.asList(scanningSequence));
        }
        
        // K-Space fill
        mrProtocolMetadata.setMrSequenceKSpaceFill(getKSpaceFill(dicomAttributes, serie.getIsEnhancedMR()));
        
        return mrProtocolMetadata;
	}	
	
    public String getReceiveCoilName(final Attributes dicomAttributes, final boolean isEnhancedMR) {
        final String result = dicomAttributes.getString(Tag.ReceiveCoilName);
        if (result != null && !"".equals(result)) {
            return result;
        } else if (isEnhancedMR) {
            int[] tagPath = new int[3];
            tagPath[0] = Tag.MRReceiveCoilSequence;
            tagPath[2] = Tag.ReceiveCoilName;
            final Object obj = getValueInSharedAndThenPerFrame(ShanoirConstants.DICOM_RETURNED_TYPES.STRING, Tag.ReceiveCoilName, dicomAttributes);
            if (obj != null) {
                return (String) obj;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public String getReceiveCoilType(final Attributes dicomAttributes, final boolean isEnhancedMR) {
        final String result = dicomAttributes.getString(Tag.ReceiveCoilType);
        if (result != null && !"".equals(result)) {
            return result;
        } else if (isEnhancedMR) {
            int[] tagPath = new int[3];
            tagPath[0] = Tag.MRReceiveCoilSequence;
            tagPath[2] = Tag.ReceiveCoilType;
            final Object obj = getValueInSharedAndThenPerFrame(ShanoirConstants.DICOM_RETURNED_TYPES.STRING, Tag.ReceiveCoilType, dicomAttributes);
            if (obj != null) {
                return (String) obj;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public String getTransmitCoilName(final Attributes dicomAttributes, final boolean isEnhancedMR) {
        final String result = dicomAttributes.getString(Tag.TransmitCoilName);
        if (result != null && !"".equals(result)) {
            return result;
        } else if (isEnhancedMR) {
            int[] tagPath = new int[3];
            tagPath[0] = Tag.MRTransmitCoilSequence;
            tagPath[2] = Tag.TransmitCoilName;
            final Object obj = getValueInSharedAndThenPerFrame(ShanoirConstants.DICOM_RETURNED_TYPES.STRING, Tag.TransmitCoilName, dicomAttributes);
            if (obj != null) {
                return (String) obj;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public String getTransmitCoilType(final Attributes dicomAttributes, final boolean isEnhancedMR) {
        final String result = dicomAttributes.getString(Tag.TransmitCoilType);
       // dicomAttributes.getSequence(tag)
        if (result != null && !"".equals(result)) {
            return result;
        } else if (isEnhancedMR) {
            int[] tagPath = new int[3];
            tagPath[0] = Tag.MRTransmitCoilSequence;
            tagPath[2] = Tag.TransmitCoilType;
            final Object obj = getValueInSharedAndThenPerFrame(ShanoirConstants.DICOM_RETURNED_TYPES.STRING, Tag.TransmitCoilType,dicomAttributes);
            if (obj != null) {
                return (String) obj;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

	/**
	 * Depending on the type given in parameter, call the correct method to get
	 * the value of the dicom tag.
	 *
	 * @param type
	 *            Type of the value to be returned
	 * @param dicomTag
	 *            the dicom tag
	 * @param dcmObj
	 *            the object that represents the dicom image
	 *
	 * @return the value for the given tag with the wanted return type
	 */
	public static Object getValue(final ShanoirConstants.DICOM_RETURNED_TYPES type, final int dicomTag,	final Attributes attributes) {
		if (ShanoirConstants.DICOM_RETURNED_TYPES.INT == type) {
			int result = attributes.getInt(dicomTag, valueNotFoundValue);
			if (result != valueNotFoundValue) {
				return result;
			} else {
				return null;
			}
		} else if (ShanoirConstants.DICOM_RETURNED_TYPES.DATE == type) {
			return attributes.getDate(dicomTag);
		} else if (ShanoirConstants.DICOM_RETURNED_TYPES.FLOAT == type) {
			double result = attributes.getDouble(dicomTag, valueNotFoundValue);
			if (((int) result) != valueNotFoundValue) {
				return result;
			} else {
				return null;
			}
		} else if (ShanoirConstants.DICOM_RETURNED_TYPES.INT_ARRAY == type) {
			return attributes.getInts(dicomTag);
		} else if (ShanoirConstants.DICOM_RETURNED_TYPES.FLOAT_ARRAY == type) {
			return attributes.getDoubles(dicomTag);
		} else if (ShanoirConstants.DICOM_RETURNED_TYPES.DATE_ARRAY == type) {
			return attributes.getDates(dicomTag);
		} else if (ShanoirConstants.DICOM_RETURNED_TYPES.STRING_ARRAY == type) {
			return attributes.getStrings(dicomTag);
		} else if (ShanoirConstants.DICOM_RETURNED_TYPES.STRING == type) {
			return attributes.getString(dicomTag);
		//} else if (ShanoirConstants.DICOM_RETURNED_TYPES.SHORT_ARRAY == type) {
			//return attributes.getShorts(dicomTag);
		} else if (ShanoirConstants.DICOM_RETURNED_TYPES.DOUBLE_ARRAY == type) {
			return attributes.getDoubles(dicomTag);
		} else if (ShanoirConstants.DICOM_RETURNED_TYPES.DOUBLE == type) {
			double result = attributes.getDouble(dicomTag, valueNotFoundValue);
			if (((int) result) != valueNotFoundValue) {
				return result;
			} else {
				return null;
			}
		} else if (ShanoirConstants.DICOM_RETURNED_TYPES.DATE_RANGE == type) {
			return attributes.getDateRange(dicomTag);
		} else if (ShanoirConstants.DICOM_RETURNED_TYPES.BYTE_ARRAY == type) {
			try {
				return attributes.getBytes(dicomTag);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOG.error("Error during import, fail to convert attribute to byte array",e);
			}
		}
		return null;
	}
    
    //TODO ATO fix getString() method...
    public Object getValueInSharedAndThenPerFrame(final ShanoirConstants.DICOM_RETURNED_TYPES type, final int dicomTag, final Attributes dicomAttributes) {
    	//Attributes firstSequenceAttributes = emf.extract(dicomAttributes, 0);
        Object result;
        result = getValue(type,dicomTag,dicomAttributes);


//        for (int o : newTagPath) {
//        	result = dicomAttributes.getString(o);
//        }
////        if (result != null) {
////            return result;
////        } else {
////            newTagPath[0] = Tag.PerFrameFunctionalGroupsSequence;
//           result = getValue(type, newTagPath, dicomAttributes);
////            return result;
////        }
        return null;
    }

    private ImagedNucleus getImagedNucleus(final Attributes dicomAttributes, final boolean isEnhancedMR) {
        final String imagedNucleus = getNucleus(dicomAttributes, isEnhancedMR);
        /*
         * Warning: some dicom images may contain H1 instead of 1H or P31
         * instead of 31P.
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
     * columns) or the fourth value (phase columns) ; Siemens, GE : 2nd ;
     * Philips : 4th.
     *
     * acquisition_resolution_y : Simple copy of the first value (frequency row)
     * or the third value (phase rows); Siemens, GE : 3rd; Philips : 1st
     *
     * @param serieNumber
     *            the serie number
     *
     * @return the acquisition resolution
     */
    private Integer[] getAcquisitionResolution(final Attributes dicomAttributes, final boolean isEnhancedMR) {
        Integer[] acquisitionResolution = null;
        if (!isEnhancedMR) {
            // final Integer[] acquisitionMatrixDimension = getInts(serieNumber,
            // Tag.AcquisitionMatrix);
            final int[] acquisitionMatrixDimension = dicomAttributes.getInts(Tag.AcquisitionMatrix);
            // final String manufacturer = getString(serieNumber,
            // Tag.Manufacturer);
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
    
    public String getInPlanePhaseEncodingDirection(final Attributes dicomAttributes, final boolean isEnhancedMR) {
        if (!isEnhancedMR) {
            return dicomAttributes.getString(Tag.InPlanePhaseEncodingDirection);
        } else {
            int[] tagPath = new int[3];
            tagPath[0] = Tag.MRFOVGeometrySequence;
            tagPath[2] = Tag.InPlanePhaseEncodingDirection;
            final Object obj = getValueInSharedAndThenPerFrame(ShanoirConstants.DICOM_RETURNED_TYPES.STRING, Tag.InPlanePhaseEncodingDirection ,dicomAttributes);
            if (obj != null) {
                return (String) obj;
            } else {
                return null;
            }
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
     * - 'Conventional Cartesian sequence': (if Echo train length == 1 AND
     * Geometry category of k-Space traversal == RECTILINEAR )
     *
     * - 'Non-Conventional Cartesian sequence' (if Echo train length > 1 AND
     * Geometry category of k-Space traversal == RECTILINEAR )
     *
     * - 'Non-Conventional Non-Cartesian sequence' (if Echo train length > 1 AND
     * Geometry category of k-Space traversal != RECTILINEAR )
     *
     * NOTE: There may be cases in which this method is irrelevant, e.g. series
     * with multiple echo to determine T1 or T2 maps
     *
     * @param serieNumber
     *            the serie number
     *
     * @return the corresponding reference entity
     */
    private MrSequenceKSpaceFill getKSpaceFill(final Attributes dicomAttributes, final boolean isEnhancedMR) {
        final String geometryOfKSpaceTraversal = dicomAttributes.getString(Tag.GeometryOfKSpaceTraversal);
        final Integer echoTrainLength = getEchoTrainLength(dicomAttributes, isEnhancedMR);
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
    
    public Integer getEchoTrainLength(final Attributes dicomAttributes, final boolean isEnhancedMR) {
        if (!isEnhancedMR) {
            return dicomAttributes.getInt(Tag.EchoTrainLength,Integer.MIN_VALUE);
        } else {
            int[] tagPath = new int[3];
            tagPath[0] = Tag.MRTimingAndRelatedParametersSequence;
            tagPath[2] = Tag.EchoTrainLength;
            final Object obj = getValueInSharedAndThenPerFrame(ShanoirConstants.DICOM_RETURNED_TYPES.STRING,Tag.EchoTrainLength,dicomAttributes);
            if (obj != null) {
                final Double objDouble = Double.valueOf((String) obj);
                return objDouble.intValue();
            } else {
                return null;
            }
        }
    }
    
    public Integer getMRAcquisitionFrequencyEncodingSteps(final Attributes dicomAttributes, final boolean isEnhancedMR) {
        if (!isEnhancedMR) {
            return dicomAttributes.getInt(Tag.MRAcquisitionFrequencyEncodingSteps, Integer.MIN_VALUE);
        } else {
            int[] tagPath = new int[3];
            tagPath[0] = Tag.MRFOVGeometrySequence;
            tagPath[2] = Tag.MRAcquisitionFrequencyEncodingSteps;
            final Object obj = getValueInSharedAndThenPerFrame(ShanoirConstants.DICOM_RETURNED_TYPES.STRING,Tag.MRAcquisitionFrequencyEncodingSteps,dicomAttributes);
            if (obj != null) {
                return Integer.valueOf((String) obj);
            } else {
                return null;
            }
        }
    }
    
    public String getNucleus(final Attributes dicomAttributes, final boolean isEnhancedMR) {
        if (!isEnhancedMR) {
            return dicomAttributes.getString(Tag.ImagedNucleus);
        } else {
            return dicomAttributes.getString(Tag.ResonantNucleus);
        }
    }
//	
//    
//    public String getString(int[] tagPath) {
//        return toString(get(tagPath), null);
//    }
//
//    public DicomElement get(int[] tagPath) {
//        checkTagPathLength(tagPath);
//        final int last = tagPath.length - 1;
//        final DicomObject item = getItem(tagPath, last, true);
//        return item != null ? item.get(tagPath[last]) : null;
//    }
//
//
//    private DicomObject getItem(int[] itemPath, int pathLen, boolean readonly) {
//        DicomObject item = this;
//        for (int i = 0; i < pathLen; ++i, ++i) {
//            DicomElement sq = item.get(itemPath[i]);
//            if (sq == null || !sq.hasItems()) {
//                if (readonly) {
//                    return null;
//                }
//                sq = item.putSequence(itemPath[i]);
//            }
//            while (sq.countItems() <= itemPath[i + 1]) {
//                if (readonly) {
//                    return null;
//                }
//                sq.addDicomObject(new BasicDicomObject());
//            }
//            item = sq.getDicomObject(itemPath[i + 1]);
//        }
//        return item;
//    }
//
//
//    public String toString() {
//        return toStringBuffer(null, TO_STRING_MAX_VAL_LEN).toString();
//    }
//
//    private String toString(DicomElement a, String defVal) {
//        return a == null || a.isEmpty() ? defVal : a.getString(
//                getSpecificCharacterSet(), cacheGet());
//    }
//
//    public String getString(SpecificCharacterSet cs, boolean cache) {
//        if (cache) {
//            Object tmp = cachedValue;
//            if (tmp instanceof String)
//                return (String) tmp;
//        }
//        String val = vr.toString(value, bigEndian, cs);
//        if (cache)
//            cachedValue = val;
//        return val;
//    }
//
//    
//    public StringBuffer toStringBuffer(StringBuffer sb, int maxValLen) {
//        if (sb == null)
//            sb = new StringBuffer();
//        TagUtils.toStringBuffer(tag, sb);
//        sb.append(' ');
//        sb.append(vr);
//        sb.append(" #");
//        sb.append(length());
//        sb.append(" [");
//        appendValue(sb, maxValLen);
//        sb.append("]");
//        return sb;
//    }
    
	public Long getCoil() {
		return 1L;
	}

}
