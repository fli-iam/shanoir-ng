package org.shanoir.ng.dicom.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.shanoir.ng.importer.service.DicomSEGAndSRImporterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.mail.BodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The STOWRSMultipartRequestFilter handles a HTTP-POST request of
 * content type "multipart/related", according to the DICOMWeb standard.
 * 
 * 1) This request filter is used by the OHIF viewer sending DICOM SR or
 * DICOM SEG modalities (SR == Structured Report, SEG = Segmentation).
 * Within the OHIF viewer, the button "save measurements" is used
 * to send the measurements (SR) to the Shanoir backend for storage.
 * Within the OHIF viewer, open for segmentation/export segmentation
 * is used to send segmentations (SEG) to the Shanoir backend for storage.
 * 
 * 2) This request filter is used by Karnak or any other DICOM gateway to
 * import DICOM files into Shanoir.
 * 
 * It might look weird to use a request filter for this purpose and not
 * manage this inside the standard REST controller. After two days of search
 * I seem to have figured out, that the DefaultMultipartResolver of Spring
 * Boot only supports "multipart/form-data", but NOT "multipart/related".
 * I made several tries with CommonsMultipartResolver and the libs commons-
 * fileupload + commons-io (latest versions) and could not get it to work.
 * Everytime my MultipartFile single file or list of files was empty, same
 * using @RequestBody or not.
 * 
 * Furthermore I tried with using HttpServletRequest directly, but as in any
 * case one of the multipart resolvers kicks in before, the input stream was
 * always already consumed and empty, so I passed to a filter.
 * 
 * According to my understanding of the spring security config, this filter
 * comes after the Keycloak filter (addFilterAfter), and does therefore not
 * expose any access from outside.
 * 
 * Instead of writing a multipart/related parser on my own using the below
 * MimeMultipart seems pretty elegant.
 * 
 * @author mkain
 *
 */
@Component
public class STOWRSMultipartRequestFilter extends GenericFilterBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(STOWRSMultipartRequestFilter.class);

	private static final String CONTENT_TYPE_DICOM = "application/dicom";

	private static final String DICOMWEB_STUDIES = "/dicomweb/studies";

	public static final String DICOM_MODALITY_SEG = "SEG";

	private static final String DICOM_MODALITY_SR = "SR";

	private static final String DICOM_MODALITY_MR = "MR";

	private static final String DICOM_MODALITY_CT = "CT";

	private static final String DICOM_MODALITY_PT = "PT";

	@Autowired
	private DicomSEGAndSRImporterService dicomSEGAndSRImporterService;
	
    @Override
    public void doFilter(
			ServletRequest request, 
			ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
    	HttpServletRequest httpRequest = (HttpServletRequest) request;
    	if (httpRequest.getMethod().equals(HttpMethod.POST.toString())
    			&& httpRequest.getRequestURI().contains(DICOMWEB_STUDIES)
    			&& httpRequest.getContentType().contains(MediaType.MULTIPART_RELATED_VALUE)) {
    		try(ByteArrayInputStream bIS = new ByteArrayInputStream(httpRequest.getInputStream().readAllBytes())) {
    			ByteArrayDataSource datasource = new ByteArrayDataSource(bIS, MediaType.MULTIPART_RELATED_VALUE);
    			MimeMultipart multipart = new MimeMultipart(datasource);
    			int count = multipart.getCount();
				LOG.info("Multipart request with {} parts received.", count);
    			for (int i = 0; i < count; i++) {
    				BodyPart bodyPart = multipart.getBodyPart(i);
    				if (bodyPart.isMimeType(CONTENT_TYPE_DICOM)) {
    					manageDICOM(bodyPart);
    				} else {
    					throw new IOException("STOWRSMultipartRequestFilter: exception sending DICOM file to Shanoir (STOW-RS).");
    				}
    			}
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
    		return;
    	}
        chain.doFilter(request, response);
    }

	private void manageDICOM(BodyPart bodyPart) throws Exception {
		// DicomInputStream consumes the input stream to read the data
		try (DicomInputStream dIS = new DicomInputStream(bodyPart.getInputStream())) {
			Attributes metaInformationAttributes = dIS.readFileMetaInformation();
			Attributes datasetAttributes = dIS.readDataset();
			String modality = datasetAttributes.getString(Tag.Modality);
			if (DICOM_MODALITY_SEG.equals(modality) || DICOM_MODALITY_SR.equals(modality)) {
				if(!dicomSEGAndSRImporterService.importDicomSEGAndSR(metaInformationAttributes, datasetAttributes, modality)) {
					LOG.error("Error during import of DICOM SEG/SR.");
					throw new ServletException("Error during import of DICOM SEG/SR.");
				}
			} else if (DICOM_MODALITY_MR.equals(modality)
					|| DICOM_MODALITY_CT.equals(modality)
					|| DICOM_MODALITY_PT.equals(modality)) {
			} else {
				LOG.error("Not supported DICOM modality sent: {}", modality);
				throw new ServletException("Not supported DICOM modality sent: " + modality);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw e;
		}
	}

}
