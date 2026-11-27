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

package org.shanoir.ng.studycard.model.field;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;;

public class MetadataFieldDeserializer extends StdDeserializer<MetadataFieldInterface<?>> {

    public MetadataFieldDeserializer() {
        this(MetadataFieldInterface.class);
    }

    public MetadataFieldDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public MetadataFieldInterface<?> deserialize(JsonParser jp, DeserializationContext ctxt) {
        String name = jp.readValueAsTree().toString().replace("\"", "");
        if (DatasetAcquisitionMetadataField.has(name)) {
            return DatasetAcquisitionMetadataField.getEnum(name);
        } else if (DatasetMetadataField.has(name)) {
            return DatasetMetadataField.getEnum(name);
        } else throw new IllegalStateException("this MetadataFieldInterface enum does not exist : " + name);
    }
}
