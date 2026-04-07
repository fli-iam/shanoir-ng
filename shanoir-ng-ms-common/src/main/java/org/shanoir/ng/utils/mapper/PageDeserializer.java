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

package org.shanoir.ng.utils.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.paging.Page;
import org.shanoir.ng.shared.paging.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;

public class PageDeserializer extends JsonDeserializer<Page<?>> implements ContextualDeserializer {
    private static final String CONTENT = "content";
    private static final String NUMBER = "number";
    private static final String SIZE = "size";
    private static final String TOTAL_ELEMENTS = "totalElements";
    private JavaType valueType;

    @Override
    public Page<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        final CollectionType valuesListType = ctxt.getTypeFactory().constructCollectionType(List.class, valueType);

        List<?> list = new ArrayList<>();
        int pageNumber = 0;
        int pageSize = 0;
        long total = 0;
        if (p.isExpectedStartObjectToken()) {
            p.nextToken();
            if (p.hasTokenId(JsonTokenId.ID_FIELD_NAME)) {
                String propName = p.getCurrentName();
                do {
                    p.nextToken();
                    switch (propName) {
                        case CONTENT:
                            list = ctxt.readValue(p, valuesListType);
                            break;
                        case NUMBER:
                            pageNumber = ctxt.readValue(p, Integer.class);
                            break;
                        case SIZE:
                            pageSize = ctxt.readValue(p, Integer.class);
                            break;
                        case TOTAL_ELEMENTS:
                            total = ctxt.readValue(p, Long.class);
                            break;
                        default:
                            p.skipChildren();
                            break;
                    }
                } while (((propName = p.nextFieldName())) != null);
            } else {
                ctxt.handleUnexpectedToken(handledType(), p);
            }
        } else {
            ctxt.handleUnexpectedToken(handledType(), p);
        }

        //Note that Sort field of Page is ignored here.
        //Feel free to add more switch cases above to deserialize it as well.
        return new PageImpl<>(list, PageRequest.of(pageNumber, pageSize), total);
    }

    /**
     * This is the main point here.
     * The PageDeserializer is created for each specific deserialization with concrete generic parameter type of Page.
     */
    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        //This is the Page actually
        final JavaType wrapperType = ctxt.getContextualType();
        final PageDeserializer deserializer = new PageDeserializer();
        //This is the parameter of Page
        deserializer.valueType = wrapperType.containedType(0);
        return deserializer;
    }
}
