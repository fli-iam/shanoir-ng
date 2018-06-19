package org.shanoir.ng.dicom;

import java.io.File;
import java.io.IOException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.shanoir.ng.importer.dto.DatasetFile;
import org.springframework.stereotype.Service;

@Service
public class DicomProcessing {
	
//	public static DatasetFile retrieveAdditionalDicomDataForImage(File dicomFile, DatasetFile image) {
//		DicomInputStream dIS = null;
//		try {
//			dIS = new DicomInputStream(dicomFile);
//			Attributes datasetAttributes = dIS.readDataset(-1, -1);
//			String sopClassUID = datasetAttributes.getString(Tag.SOPClassUID);
//			
//			if (sopClassUID.startsWith("1.2.840.10008.5.1.4.1.1.66")) {
//				//((ArrayNode) instances).remove(index);
//				// do nothing here as instances array will be deleted after split
//			} else {
//				// divide here between non-images and images, non-images at first
//				if (UID.PrivateSiemensCSANonImageStorage.equals(sopClassUID)
//					|| UID.MRSpectroscopyStorage.equals(sopClassUID)) {
////					ObjectNode nonImage = mapper.createObjectNode();
////					nonImage.put("path", instanceFilePath);
////					nonImages.add(nonImage);
//					// DO NOTHING FOR NOW
//					
//				// images at the second
//				} else {
//					// do not change here: use absolute path all time and find other solution for image preview
//					String acquisitionNumber = datasetAttributes.getString(Tag.AcquisitionNumber);
////					addImageSeparateDatasetsInfo(image, datasetAttributes);
//				}
//			}
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		return null;
//	}
	
	public Attributes getDicomObjectAttributes(DatasetFile image,boolean isEnhancedMR) throws IOException {
		File dicomFile = new File(image.getPath());
		DicomInputStream dIS = new DicomInputStream(dicomFile);
		Attributes datasetAttributes;
		if (isEnhancedMR) {
			// In case of Enhanced MR, we need to the pixel data in order to use Dcm4chee emf extract method.
			datasetAttributes = dIS.readDataset(-1, -1);
		} else {
			// Else we do not load the picture in Ram for faster performance.
			datasetAttributes = dIS.readDataset(-1, Tag.PixelData);	
		}
		
		return datasetAttributes;
	}

}
