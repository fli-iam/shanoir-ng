package org.shanoir.ng.processing.carmin.service;

import java.util.Optional;

import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.repository.CarminDatasetProcessingRepository;
import org.shanoir.ng.shared.core.service.BasicEntityServiceImpl;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author KhalilKes
 */
@Service
public class CarminDatasetPorcessingServiceImpl extends BasicEntityServiceImpl<CarminDatasetProcessing>
                implements CarminDatasetProcessingService {

        @Autowired
        private CarminDatasetProcessingRepository carminDatasetProcessingRepository;

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
        public CarminDatasetProcessing createCarminDatasetProcessing(
                        final CarminDatasetProcessing carminDatasetProcessing) {
                CarminDatasetProcessing savedEntity = carminDatasetProcessingRepository.save(carminDatasetProcessing);
                return savedEntity;
        }

        @Override
        public Optional<CarminDatasetProcessing> findByIdentifier(String identifier) {
                return carminDatasetProcessingRepository.findByIdentifier(identifier);
        }

        @Override
        public CarminDatasetProcessing updateCarminDatasetProcessing(final CarminDatasetProcessing carminDatasetProcessing)
                        throws EntityNotFoundException {
                final Optional<CarminDatasetProcessing> entityDbOpt = carminDatasetProcessingRepository
                                .findById(carminDatasetProcessing.getId());
                final CarminDatasetProcessing entityDb = entityDbOpt.orElseThrow(
                                () -> new EntityNotFoundException(carminDatasetProcessing.getClass(),
                                                carminDatasetProcessing.getId()));

                updateValues(carminDatasetProcessing, (CarminDatasetProcessing) entityDb);
                return (CarminDatasetProcessing) carminDatasetProcessingRepository.save(entityDb);

        }

}
