package org.shanoir.ng.dicom.web;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

/**
 * 
 * @author mkain
 *
 */
@Component
public class MultipartRelatedRequestFilter extends GenericFilterBean {

	@Autowired
	private DICOMWebService dicomWebService;
	
    @Override
    public void doFilter(
      ServletRequest request, 
      ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    	HttpServletRequest httpRequest = (HttpServletRequest) request;
    	if (httpRequest.getMethod().equals(HttpMethod.POST.toString())
    			&& httpRequest.getRequestURI().contains("/dicomweb/studies")) {
    	
    		return;
    	}
        chain.doFilter(request, response);
    }
}
