package org.shanoir.ng.events;

import org.shanoir.ng.accessrequest.controller.AccessRequestService;
import org.shanoir.ng.shared.exception.AccountNotOnDemandException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class EventsApiController implements EventsApi {
    @Autowired
    private ShanoirEventsService shanoirEventsService;

    @Override
    public ResponseEntity<List<ShanoirEvent>> findEventsByStudyId(Long studyId) throws RestServiceException {
        try {
            List<ShanoirEvent> events = shanoirEventsService.findByStudyId(studyId);

            if (events == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(events, HttpStatus.OK);

        } catch (Exception e) {
            throw new RestServiceException(new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }

    }
}
