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

package org.shanoir.ng.importer.eeg.edf;

import java.util.List;

/**
 * This class represents the complete content of an EDF-File.
 */
public class EDFParserResult {

    private EDFHeader header;
    private EDFSignal signal;
    private List<EDFAnnotation> annotations;

    public EDFHeader getHeader() {
        return header;
    }

    public EDFSignal getSignal() {
        return signal;
    }

    public List<EDFAnnotation> getAnnotations() {
        return annotations;
    }

    public void setHeader(EDFHeader header) {
        this.header = header;
    }

    public void setSignal(EDFSignal signal) {
        this.signal = signal;
    }

    public void setAnnotations(List<EDFAnnotation> annotations) {
        this.annotations = annotations;
    }
}
