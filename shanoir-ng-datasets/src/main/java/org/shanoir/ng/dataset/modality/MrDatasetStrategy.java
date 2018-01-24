package org.shanoir.ng.dataset.modality;

import java.util.Date;
import java.util.List;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.datasetacquisition.mr.MrProtocol;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class MrDatasetStrategy {
	
	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(MrDatasetStrategy.class);
	
	@Autowired
	MrProtocol mrProtocol;
	
	public List<MrDataset> generateMrDatasetsForSerie(Attributes dicomAttributes,Serie serie) {
		
		// TODO ATO : implement MrDAtasetAcquisitionHome.createMrDataset (issue by createMrDatasetAcquisitionFromDicom()
		for (Dataset dataset : serie.getDatasets()) {
			//TODO ATO : implement line 350 - 372 MrDAtasetAcquisitionHome.createMrDataset
			
			
            /*
             * First acquisition time of the dicom media.
             */
            Date firstImageAcquisitionTime = null;

            /*
             * Last acquisition time of the dicom media.
             */
            Date lastImageAcquisitionTime = null;
            
			MrDataset mrDataset = generateSingleMrDatasetForSerie(dicomAttributes,serie,dataset);
		}
		return null;
		
	}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	public MrDataset generateSingleMrDatasetForSerie(Attributes dicomAttributes,Serie serie,Dataset dataset) {
        MrDataset mrDataset = new MrDataset();

        // Determine which class of dataset must be created
        final String sopClassUID = dicomAttributes.getString(Tag.SOPClassUID);
        LOG.debug("createSingleDataset : sopClassUID=" + sopClassUID);
 
  
         mrDataset = new MrDataset();



 
        mrDataset.setCreationDate(Utils.DateToLocalDate(dicomAttributes.getDate(Tag.SeriesDate)));
        
        final String serieDescription = dicomAttributes.getString(Tag.SeriesDescription);
        final String modality = dicomAttributes.getString(Tag.Modality);
//        final RefDatasetModalityType refDatasetModalityType = refDatasetModalityTypeHome.getRefEntity(modality
//                + " Dataset");
//        LOG.debug("createSingleDataset : modality=" + modality);
//        LOG.debug("createSingleDataset : refDatasetModalityType=" + refDatasetModalityType);
//        LOG.debug("createSingleDataset : refProcessedDatasetType=" + refProcessedDatasetType);
//        LOG.debug("createSingleDataset : serieDescription=" + serieDescription);
//        LOG.debug("createSingleDataset : seriesDate=" + serieDate);
//
//
//
//        // set the series description as the dataset comment & name if empty
//        if (serieDescription != null && !"".equals(serieDescription)) {
//            if (mrDataset.getName() == null || "".equals(mrDataset.getName()) || serieDescription.equals(mrDataset.getName())) {
//                final String name = computeDatasetName(mrDatasetAcquisition, serieDescription);
//                mrDataset.setName(name);
//            }
//            if (mrDataset.getComment() == null || "".equals(mrDataset.getComment())) {
//                mrDataset.setComment(serieDescription);
//            }
//        }
//
//        // Pre-select the type Reconstructed dataset
//        mrDataset.setRefProcessedDatasetType(refProcessedDatasetType);
//
//        // Set the study and the subject
//        mrDataset.setSubject(dicomImporter.getSubject());
//        mrDataset.setGroupOfSubjects(dicomImporter.getExperimentalGroupOfSubjects());
//        mrDataset.setStudy(dicomImporter.getStudy());
//
//        // Set the modality from dicom fields
//        mrDataset.setRefDatasetModalityType(refDatasetModalityType);
//
//        RefCardinalityOfRelatedSubjects refCardinalityOfRelatedSubjects = null;
//        if (dicomImporter.getSubject() != null) {
//            refCardinalityOfRelatedSubjects = refCardinalityOfRelatedSubjectsHome.getSingleSubjectDataset();
//        } else {
//            refCardinalityOfRelatedSubjects = refCardinalityOfRelatedSubjectsHome.getMultipleSubjectsDataset();
//        }
//        mrDataset.setRefCardinalityOfRelatedSubjects(refCardinalityOfRelatedSubjects);
//
//        if (mrDatasetAcquisition != null) {
//            mrDatasetAcquisition.getDatasetList().add(mrDataset);
//            mrDataset.setDatasetAcquisition(mrDatasetAcquisition);
//        }
//
//        LOG.debug("createSingleDataset : End, return mrDataset=" + mrDataset);
        return mrDataset;
	}

}
