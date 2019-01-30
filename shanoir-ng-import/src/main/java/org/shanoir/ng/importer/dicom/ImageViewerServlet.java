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

/**
 * This class handles requests from the front end, processes them
 * to replay back with an response. This response will let the front
 * display the images within a viewer.
 */
package org.shanoir.ng.importer.dicom;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author yyao
 *
 */

@WebServlet(name = "ImageViewerServlet", description = "Image Viewer Servlet", urlPatterns = {
		"/viewer/ImageViewerServlet/*" })
public class ImageViewerServlet extends HttpServlet {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -1984476107087162922L;

	/** The Constant KB. */
	private static final int KB = 1024;

	/** The Constant BUFFER_SIZE. */
	private static final int BUFFER_SIZE = 10 * KB;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String pathInfo = request.getPathInfo();
		URL url = new URL("file:///" + pathInfo);
		final URLConnection uCon = url.openConnection();
		final InputStream is = uCon.getInputStream();
		response.setContentType("application/dicom");
		if (uCon.getContentLength() >= 0) {
			response.setContentLength(uCon.getContentLength());
		}
		final ServletOutputStream os = response.getOutputStream();
		byte[] buffer = new byte[BUFFER_SIZE];
		int length;
		while ((length = is.read(buffer)) > 0) {
			os.write(buffer, 0, length);
		}
		os.flush();
		os.close();
		is.close();
	}
}
