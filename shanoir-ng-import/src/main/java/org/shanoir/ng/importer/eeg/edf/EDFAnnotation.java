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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EDFAnnotation {

    private double onSet = 0;
    private double duration = 0;
    private final List<String> annotations = new ArrayList<>();

    EDFAnnotation(String onSet, String duration, String[] annotations) {
        this.onSet = Double.parseDouble(onSet);
        if (duration != null && !Objects.equals(duration, "")) {
            this.duration = Double.parseDouble(duration);
        }
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i] == null || annotations[i].trim().equals("")) {
                continue;
            }
            this.annotations.add(annotations[i]);
        }
    }

    public double getOnSet() {
        return onSet;
    }

    public double getDuration() {
        return duration;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    @Override
    public String toString() {
        return "Annotation [onSet=" + onSet + ", duration=" + duration + ", annotations=" + annotations + "]";
    }
}
