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
import org.shanoir.ng.datasetacquisition.mr.ImagedNucleus;
import org.shanoir.ng.datasetacquisition.mr.MrProtocol;
import org.shanoir.ng.datasetacquisition.mr.MrProtocolMetadata;
import org.shanoir.ng.datasetacquisition.mr.MrSequenceKSpaceFill;
import org.shanoir.ng.datasetacquisition.mr.ParallelAcquisitionTechnique;
import org.shanoir.ng.datasetacquisition.mr.PatientPosition;
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
	public MrProtocol generateMrProtocolForSerie(Attributes dicomAttributes, Serie serie) {
		
		MrProtocol mrProtocol = new MrProtocol();
		MrProtocolMetadata mrProtocolMetadata = createMrProtocolMetadata(dicomAttributes, serie);
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
        final Double imagingFrequency = dicomAttributes.getDouble(Tag.ImagingFrequency,-1D);
        LOG.debug("extractMetadata : imagingFrequency=" + imagingFrequency);
        mrProtocol.setImagingFrequency(imagingFrequency);

        // Acquisition duration
        final Double acquisitionDuration = dicomAttributes.getDouble(Tag.AcquisitionDuration,-1D);
        LOG.debug("extractMetadata : acquisitionDuration=" + acquisitionDuration);
        mrProtocol.setAcquisitionDuration(acquisitionDuration);

        // Echo Train Length
        final Integer echoTrainLength = dicomAttributes.getInt(Tag.EchoTrainLength,-1);
        LOG.debug("extractMetadata : echoTrainLength=" + echoTrainLength);
        mrProtocol.setEchoTrainLength(echoTrainLength);

        // Number of averages
        final Integer numberOfAverages = dicomAttributes.getInt(Tag.NumberOfAverages,-1);
        LOG.debug("extractMetadata : numberOfAverages=" + numberOfAverages);
        mrProtocol.setNumberOfAverages(numberOfAverages);

        // Number of Phase Encoding Steps
        final Integer numberOfPhaseEncodingSteps = dicomAttributes.getInt(Tag.NumberOfPhaseEncodingSteps,-1);
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
        final Double sliceThickness = dicomAttributes.getDouble(Tag.SliceThickness,-1D);
        LOG.debug("extractMetadata : sliceThickness=" + sliceThickness);
        mrProtocol.setSliceThickness(sliceThickness);

        // Spacing between slices
        final Double sliceSpacing = dicomAttributes.getDouble(Tag.SpacingBetweenSlices,-1D);
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
        final Integer rows = dicomAttributes.getInt(Tag.Rows,-1);
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
        final Integer columns = dicomAttributes.getInt(Tag.Columns,-1);
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
        final Integer numberOfTemporalPositions = dicomAttributes.getInt(Tag.NumberOfTemporalPositions,-1);
        LOG.debug("extractMetadata : numberOfTemporalPositions=" + numberOfTemporalPositions);
        mrProtocol.setNumberOfTemporalPositions(numberOfTemporalPositions);

        // Temporal resolution
        final Double temporalResolution = dicomAttributes.getDouble(Tag.TemporalResolution,-1D);
        LOG.debug("extractMetadata : temporalResolution=" + temporalResolution);
        mrProtocol.setTemporalResolution(temporalResolution);

        // Percent sampling
        final Double percentSampling = dicomAttributes.getDouble(Tag.PercentSampling,-1D);
        LOG.debug("extractMetadata : percentSampling=" + percentSampling);
        mrProtocol.setPercentSampling(percentSampling);

        // Percent phase field of view
        final Double percentPhaseFieldOfView = dicomAttributes.getDouble(Tag.PercentPhaseFieldOfView,-1D);
        LOG.debug("extractMetadata : percentPhaseFieldOfView=" + percentPhaseFieldOfView);
        mrProtocol.setPercentPhaseFov(percentPhaseFieldOfView);

        // Pixel bandwidth
        final Double pixelBandwidth = dicomAttributes.getDouble(Tag.PixelBandwidth,-1D);
        LOG.debug("extractMetadata : pixelBandwidth=" + pixelBandwidth);
        mrProtocol.setPixelBandwidth(pixelBandwidth);
        
        // Patient position
        final PatientPosition patientPosition = getPatientPosition(dicomAttributes);
        mrProtocol.setPatientPosition(patientPosition);
        
        /*** TODO
         * Check if is study card managed
        //Contrast agent Used
        final String contrastAgentUsed = DicomMetadataExtractor.getString(Tag.ContrastBolusIngredient, dcmObj);
        if (log.isDebugEnabled()) {
            log.debug("extractMetadata : contrastAgentUsed=" + contrastAgentUsed);
        }
        //Get RefEntity
        IRefContrastAgentUsedHome contrastAgentUsedHome = (IRefContrastAgentUsedHome) Component.getInstance("refContrastAgentUsedHome");
        RefContrastAgentUsed refContrastAgentUsed = contrastAgentUsedHome.getRefEntity(contrastAgentUsed);
        mrProtocol.setRefContrastAgentUsed(refContrastAgentUsed);

        //Contrast agent Product
        final String contrastAgentProduct = DicomMetadataExtractor.getString(Tag.ContrastBolusAgent, dcmObj);
        if (log.isDebugEnabled()) {
            log.debug("extractMetadata : contrastAgentProduct=" + contrastAgentProduct);
        }
        mrProtocol.setContrastAgentProduct(contrastAgentProduct);
        **/

        /*
         * K-space fill - the calculation may not be relevant for multi-echo
         * sequences
         */
//        if (mrProtocol.getEchoTimes().size() < 2) {
//            final MrSequenceKSpaceFill refMrSequenceKSpaceFill = getKSpaceFill(dicomAttributes, serie.getIsEnhancedMR());
//            mrProtocol.getOriginMetadata().setMrSequenceKSpaceFill(refMrSequenceKSpaceFill);
//        }
        
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
	private MrProtocolMetadata createMrProtocolMetadata(Attributes dicomAttributes, Serie serie) {
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
		
//        // Receiving coil
//
//        // tag (0018,1251)
//        final String receivingCoilName = dicomAttributes.getString(Tag.ProtocolName);
//
//        // tag (0018,9051)
//        final String receivingCoilType = dicomAttributes.getString(Tag.ReceiveCoilType);
        
        // TODO ATO : Implement Coil below ..

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
        final Double injectedVolume = dicomAttributes.getDouble(Tag.ContrastBolusVolume,-1D);
        LOG.debug("extractMetadata : injectedVolume=" + injectedVolume);
        mrProtocolMetadata.setInjectedVolume(injectedVolume);

        // Contrast agent concentration
        final Double contrastAgentConcentration = dicomAttributes.getDouble(Tag.ContrastBolusIngredientConcentration,-1D);
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
        final Double timeReductionFactorForTheInPlaneDirection = dicomAttributes.getDouble(Tag.ParallelReductionFactorInPlane,-1D);
        LOG.debug("extractMetadata : timeReductionFactorForTheInPlaneDirection=" + timeReductionFactorForTheInPlaneDirection);
        mrProtocolMetadata.setTimeReductionFactorForTheInPlaneDirection(timeReductionFactorForTheInPlaneDirection);
        
        // Time reduction factor for the out-of-plane direction
        final Double timeReductionFactorForTheOutOfPlaneDirection = dicomAttributes.getDouble(Tag.ParallelReductionFactorOutOfPlane,-1D);
        LOG.debug("extractMetadata : timeReductionFactorForTheOutOfPlaneDirection=" + timeReductionFactorForTheOutOfPlaneDirection);
        mrProtocolMetadata.setTimeReductionFactorForTheOutOfPlaneDirection(timeReductionFactorForTheOutOfPlaneDirection);

        // Magnetization transfer. Authorized values : YES, NO
        final String magnetizationTransferExtracted = dicomAttributes.getString(Tag.MagnetizationTransfer);
        LOG.debug("extractMetadata : magnetizationTransferExtracted=" + magnetizationTransferExtracted);
        if (magnetizationTransferExtracted != null) {
	        	if (!magnetizationTransferExtracted.equals("NO")) {
	        		mrProtocolMetadata.setMagnetizationTransfer(true);
	        	} else {
	        		mrProtocolMetadata.setMagnetizationTransfer(false);
	        	}
		}

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
            final Object obj = getValueInSharedAndThenPerFrame(tagPath, dicomAttributes);
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
            final Object obj = getValueInSharedAndThenPerFrame(tagPath, dicomAttributes);
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
            final Object obj = getValueInSharedAndThenPerFrame(tagPath, dicomAttributes);
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
        if (result != null && !"".equals(result)) {
            return result;
        } else if (isEnhancedMR) {
            int[] tagPath = new int[3];
            tagPath[0] = Tag.MRTransmitCoilSequence;
            tagPath[2] = Tag.TransmitCoilType;
            final Object obj = getValueInSharedAndThenPerFrame(tagPath,dicomAttributes);
            if (obj != null) {
                return (String) obj;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    //TODO ATO fix getString() method...
    public Object getValueInSharedAndThenPerFrame(final int[] dicomTagPath, final Attributes dicomAttributes) {
//        Object result;
//        int[] newTagPath = new int[dicomTagPath.length + 2];
//        System.arraycopy(dicomTagPath, 0, newTagPath, 2, dicomTagPath.length);
//        newTagPath[0] = Tag.SharedFunctionalGroupsSequence;
//        
//        for (int o : newTagPath) {
//        	result = dicomAttributes.getString(newTagPath);
//        }
//        if (result != null) {
//            return result;
//        } else {
//            newTagPath[0] = Tag.PerFrameFunctionalGroupsSequence;
//            result = dicomAttributes.getValue(type, newTagPath, dicomAttributes);
//            return result;
//        }
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
            final Object obj = getValueInSharedAndThenPerFrame(tagPath,dicomAttributes);
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
            /* The defined termes are RECTILINEAR, RADIAL, SPIRAL */
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
            final Object obj = getValueInSharedAndThenPerFrame(tagPath,dicomAttributes);
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
            final Object obj = getValueInSharedAndThenPerFrame(tagPath,dicomAttributes);
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
