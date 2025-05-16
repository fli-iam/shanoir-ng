package org.shanoir.ng.dicom.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;

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
 * The StowRSMultipartRelatedRequestFilter handles a POST request of
 * content type "multipart/related", as send by the OHIF-viewer according
 * to the DICOM standard, containing a DICOM SR modality, == Structured
 * Report. Within the OHIF viewer, the button "save measurements" is used
 * to send the measurements to shanoir backend for storage.
 * 
 * It might look weird to use a request filter for this purpose and not
 * manage this inside the standard rest controller. After two days of search
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
public class StowRSMultipartRelatedRequestFilter extends GenericFilterBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(StowRSMultipartRelatedRequestFilter.class);

	private static final String CONTENT_TYPE_DICOM = "application/dicom";

	private static final String DICOMWEB_STUDIES = "/dicomweb/studies";

	@Autowired
	private DicomSEGAndSRImporterService dicomSRImporterService;
	
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
    			for (int i = 0; i < count; i++) {
    				BodyPart bodyPart = multipart.getBodyPart(i);
    				if (bodyPart.isMimeType(CONTENT_TYPE_DICOM)) {
    					if(!dicomSRImporterService.importDicomSEGAndSR(bodyPart.getInputStream())) {
    						throw new ServletException("Error in importDicomSEGAndSR.");
    					}
    				} else {
    					throw new IOException("StowRSMultipartRelatedRequestFilter: exception sending dicom file to pacs (stow-sr).");
    				}
    			}
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
    		return;
    	}
        chain.doFilter(request, response);
    }

}
