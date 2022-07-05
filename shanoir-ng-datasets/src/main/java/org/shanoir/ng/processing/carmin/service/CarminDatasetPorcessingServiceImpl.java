package org.shanoir.ng.processing.carmin.service;

import java.util.Optional;

import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.repository.CarminDatasetProcessingRepository;
import org.shanoir.ng.shared.core.service.BasicEntityServiceImpl;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author KhalilKes
 */
@Service
public class CarminDatasetPorcessingServiceImpl extends BasicEntityServiceImpl<CarminDatasetProcessing>  implements CarminDatasetProcessingService {

        @Autowired
        private CarminDatasetProcessingRepository carminDatasetProcessingRepository;

        @Autowired
        private ShanoirEventService eventService;

        @Override
        protected CarminDatasetProcessing updateValues(CarminDatasetProcessing from, CarminDatasetProcessing to) {
                to.setIdentifier(from.getIdentifier());
                to.setStatus(from.getStatus());
                to.setName(from.getName());
                to.setPipelineIdentifier(from.getPipelineIdentifier());
                to.setStartDate(from.getStartDate());
                to.setEndDate(from.getEndDate());
                to.setTimeout(from.getTimeout());
                to.setResultsLocation(from.getResultsLocation());

                to.setDatasetProcessingType(from.getDatasetProcessingType());
                to.setComment(from.getComment());
                to.setInputDatasets(from.getInputDatasets());
                to.setOutputDatasets(from.getOutputDatasets());
                to.setProcessingDate(from.getProcessingDate());
                to.setStudyId(from.getStudyId());

                return to;
        }

        @Override
        public CarminDatasetProcessing createCarminDatasetProcessing(final CarminDatasetProcessing carminDatasetProcessing) {
                ShanoirEvent event = new ShanoirEvent(ShanoirEventType.IMPORT_DATASET_EVENT,
                                carminDatasetProcessing.getResultsLocation(), KeycloakUtil.getTokenUserId(),
                                "Starting import...",
                                ShanoirEvent.IN_PROGRESS, 0f);
                eventService.publishEvent(event);

                CarminDatasetProcessing savedEntity = carminDatasetProcessingRepository.save(carminDatasetProcessing);

                event.setStatus(ShanoirEvent.SUCCESS);
                event.setMessage(carminDatasetProcessing.getPipelineIdentifier() + "("
                                + carminDatasetProcessing.getStudyId() + ")"
                                + ": Successfully created carmin dataset processing ");
                event.setProgress(1f);
                eventService.publishEvent(event);

                return savedEntity;
        }

        @Override
        public Optional<CarminDatasetProcessing> findByIdentifier(String identifier) {
                return carminDatasetProcessingRepository.findByIdentifier(identifier);
        }

        @Override
        public CarminDatasetProcessing updateCarminDatasetProcessing(final Long datasetProcessingId,
                        final CarminDatasetProcessing carminDatasetProcessing)
                        throws EntityNotFoundException {
                final Optional<CarminDatasetProcessing> entityDbOpt = carminDatasetProcessingRepository
                                .findById(datasetProcessingId);
                final CarminDatasetProcessing entityDb = entityDbOpt.orElseThrow(
                                () -> new EntityNotFoundException(carminDatasetProcessing.getClass(),
                                                carminDatasetProcessing.getId()));

                updateValues(carminDatasetProcessing, (CarminDatasetProcessing) entityDb);
                return (CarminDatasetProcessing) carminDatasetProcessingRepository.save(entityDb);

        }

}
