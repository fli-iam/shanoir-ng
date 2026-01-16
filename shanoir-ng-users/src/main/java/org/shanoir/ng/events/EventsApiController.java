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

package org.shanoir.ng.events;

import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;


@Controller
public class EventsApiController implements EventsApi {

    @Autowired
    private ShanoirEventsService shanoirEventsService;

    private static final Logger LOG = LoggerFactory.getLogger(EventsApiController.class);
    @Override
    public ResponseEntity<Page<ShanoirEvent>> findEventsByStudyId(final Pageable pageable, Long studyId, String searchStr, String searchField) throws RestServiceException {
        LOG.error("findEventsByStudyId : studyId=" + studyId + " / searchStr = " + searchStr + " / searchField = " + searchField);
        try {
            Page<ShanoirEvent> events = shanoirEventsService.findByStudyId(pageable, studyId, searchStr, searchField);

            if (events == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(events, HttpStatus.OK);

        } catch (Exception e) {
            throw new RestServiceException(new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }

    }

}
