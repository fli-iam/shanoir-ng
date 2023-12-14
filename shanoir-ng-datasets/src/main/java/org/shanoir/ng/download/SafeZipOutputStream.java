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

package org.shanoir.ng.download;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SafeZipOutputStream extends ZipOutputStream {
	
	private Set<String> entryNameSet = new HashSet<>();
	
	public SafeZipOutputStream(OutputStream out) {
		super(out);
	}

	public SafeZipOutputStream(OutputStream out, Charset charset) {
		super(out, charset);
	}

	@Override
	public void putNextEntry(ZipEntry e) throws IOException {
		if (e != null && !entryNameSet.contains(e.getName())) {
			super.putNextEntry(e);
			entryNameSet.add(e.getName());
		}
	}	
}