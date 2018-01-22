package org.shanoir.ng.datasetacquisition.mr;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.importer.dto.Serie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class MrProtocolStrategy {
	
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(MrProtocolStrategy.class);
	
	@Autowired
	MrProtocol mrProtocol;
	
//	@Autowired
//	DicomProcessing dicomProcessing;
	
	public MrProtocol generateMrProtocolForSerie(Attributes dicomAttributes, Serie serie) {
		
		mrProtocol.getOriginMetadata().setName(dicomAttributes.getString(Tag.ProtocolName));
		
	      /*
         * Mr Sequence Application, RefMrSequenceKSpaceFill, Slice Order,
         * Contrast agent, Slice orientation at acquisition, Acquisition
         * Contrast, MR Sequence Physics -> to be entered later with the study
         * card mechanism
         */
		
        // Receiving coil

        // tag (0018,1251)
        final String receivingCoilName = dicomAttributes.getString(Tag.ProtocolName);

        // tag (0018,9051)
        final String receivingCoilType = dicomAttributes.getString(Tag.ReceiveCoilType);
        
        // TODO ATO : Implement Coil below ..

//        if ((receivingCoilName != null && !receivingCoilName.equals(""))
//                || (receivingCoilType != null && !receivingCoilType.equals(""))) {
//            receivingCoil = new Coil();
//            receivingCoil.setName(receivingCoilName);
//            final RefCoilType refCoilType = refCoilTypeHome.getRefEntity(receivingCoilType);
//            LOG.debug("extractMetadata : refCoilType=" + refCoilType);
//            receivingCoil.setRefCoilType(refCoilType);
//        }
//        mrProtocol.setReceivingCoil(receivingCoil);
//
//        // Transmitting Coil
//        Coil transmittingCoil = null;
//        // tag (0018,1251)
//        final String transmittingCoilName = getString(file, Tag.TransmitCoilName);
//        LOG.debug("extractMetadata : transmittingCoilName=" + transmittingCoilName);
//        // tag (0018,9051)
//        final String transmittingCoilType = getString(file, Tag.TransmitCoilType);
//        LOG.debug("extractMetadata : transmittingCoilType=" + transmittingCoilType);
//        if ((transmittingCoilName != null && !transmittingCoilName.equals(""))
//                || (transmittingCoilType != null && !transmittingCoilType.equals(""))) {
//            transmittingCoil = new Coil();
//            transmittingCoil.setName(transmittingCoilName);
//            final RefCoilType refCoilType = refCoilTypeHome.getRefEntity(transmittingCoilType);
//            LOG.debug("extractMetadata : refCoilType=" + refCoilType);
//            transmittingCoil.setRefCoilType(refCoilType);
//        }
//        mrProtocol.setTransmittingCoil(transmittingCoil);
        
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

        // Volume injected of diluted contrast agent
        final Double injectedVolume = dicomAttributes.getDouble(Tag.ContrastBolusVolume,-1D);
        LOG.debug("extractMetadata : injectedVolume=" + injectedVolume);
        mrProtocol.setInjectedVolume(injectedVolume);

        // Contrast agent concentration
        final Double contrastAgentConcentration = dicomAttributes.getDouble(Tag.ContrastBolusIngredientConcentration,-1D);
        LOG.debug("extractMetadata : contrastAgentConcentration=" + contrastAgentConcentration);
        mrProtocol.setContrastAgentConcentration(contrastAgentConcentration);

        // Parallel acquisition. Authorized values : YES, NO
        final String parallelAcquisitionExtracted = dicomAttributes.getString(Tag.ParallelAcquisition);
        LOG.debug("extractMetadata : parallelAcquisitionExtracted=" + parallelAcquisitionExtracted);
        if (parallelAcquisitionExtracted != null) {
        	if (!parallelAcquisitionExtracted.equals("NO")) {
        		mrProtocol.setParallelAcquisition(true);
        	} else {
        		mrProtocol.setParallelAcquisition(false);
        	}
		}

        // parallel acquisition technique.
        final String parallelAcquisitionTechniqueExtracted = dicomAttributes.getString(Tag.ParallelAcquisitionTechnique);
        LOG.debug("extractMetadata : parallelAcquisitionTechniqueExtracted=" + parallelAcquisitionTechniqueExtracted);
        if (parallelAcquisitionTechniqueExtracted != null) {
        	mrProtocol.setParallelAcquisitionTechnique(ParallelAcquisitionTechnique.getIdByTechnique(parallelAcquisitionTechniqueExtracted));

        }

        // Time reduction factor for the in-plane direction
        final Double timeReductionFactorForTheInPlaneDirection = dicomAttributes.getDouble(Tag.ParallelReductionFactorInPlane,-1D);
        LOG.debug("extractMetadata : timeReductionFactorForTheInPlaneDirection=" + timeReductionFactorForTheInPlaneDirection);
        mrProtocol.setTimeReductionFactorForTheInPlaneDirection(timeReductionFactorForTheInPlaneDirection);
        
        // Time reduction factor for the out-of-plane direction
        final Double timeReductionFactorForTheOutOfPlaneDirection = dicomAttributes.getDouble(Tag.ParallelReductionFactorOutOfPlane,-1D);
        LOG.debug("extractMetadata : timeReductionFactorForTheOutOfPlaneDirection=" + timeReductionFactorForTheOutOfPlaneDirection);
        mrProtocol.setTimeReductionFactorForTheOutOfPlaneDirection(timeReductionFactorForTheOutOfPlaneDirection);

        // Magnetization transfer. Authorized values : YES, NO
        final String magnetizationTransferExtracted = dicomAttributes.getString(Tag.MagnetizationTransfer);
        LOG.debug("extractMetadata : magnetizationTransferExtracted=" + magnetizationTransferExtracted);
        if (magnetizationTransferExtracted != null) {
        	if (!magnetizationTransferExtracted.equals("NO")) {
        		mrProtocol.setMagnetizationTransfer(true);
        	} else {
        		mrProtocol.setMagnetizationTransfer(false);
        	}
		}

        // TODO ATO : Add preclinical mode condition. if preclinical then implement  DicomMetadataExtractor.extractMetadataCompletingMrProtocol(..) method in shanoir old
        // This method is called by method extractMetadata in same class.
        
		return mrProtocol;
	}

	
	public Long getCoil() {
		
		return 1L;

	}

}
