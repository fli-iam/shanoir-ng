package org.shanoir.ng.vip.executionTemplate.service;

import com.fasterxml.jackson.core.ObjectCodec;
import org.shanoir.ng.shared.service.TransactionRunner;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.processing.dto.GroupByEnum;
import org.shanoir.ng.vip.execution.dto.ExecutionCandidateDTO;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionInQueue;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateParameter;
import org.shanoir.ng.vip.executionTemplate.model.PlannedExecution;
import org.shanoir.ng.vip.executionTemplate.repository.ExecutionTemplateRepository;
import org.shanoir.ng.vip.executionTemplate.repository.PlannedExecutionRepository;
import org.shanoir.ng.vip.shared.dto.DatasetParameterDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class PlannedExecutionServiceImpl implements PlannedExecutionService {

    private static final Logger LOG = LoggerFactory.getLogger(PlannedExecutionServiceImpl.class);

    @Autowired
    private PlannedExecutionRepository plannedExecutionRepository;

    @Autowired
    private ExecutionTemplateRepository executionTemplateRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private DatasetAcquisitionRepository acquisitionRepository;

    @Autowired
    private TransactionRunner transactionRunner;

    @Autowired
    @Lazy
    private PlannedExecutionRunner plannedExecutionRunner;
    @Autowired
    private ObjectCodec objectCodec;

    //A call is corresponding to all or a part of an examination acquisitions. If various examination imported, then multiple calls will be done
    public void applyExecution(Map<Long, List<Long>> createdAcquisitionsPerTemplateId) {
        LOG.info("Auto executions for newly imported DICOM started.");

        //Executions have to be processed according to priority order
        LinkedHashMap<Long, List<Long>> createdAcquisitionsPerTemplateIdOrderedPerPrio = sortingPerPrio(createdAcquisitionsPerTemplateId);

        for (Long templateId : createdAcquisitionsPerTemplateIdOrderedPerPrio.keySet()) {
            ExecutionTemplate template = executionTemplateRepository.findById(templateId).orElse(null);

            if(Objects.isNull(template)) {
                //If the template has been removed for some reason
                createdAcquisitionsPerTemplateId.get(templateId).forEach(acqId -> plannedExecutionRepository.deleteByAcquisitionIdAndTemplateId(templateId, acqId));
            }  else {
                //Manage the executions according to the group scale
                // (one exec per examination, one per acquisition or one per dataset)
                String executionLevel = getExecutionLevel(template);
                switch (executionLevel) {
                    case "examination" ->
                            transactionRunner.runInTransaction(em -> createExecutionAtExaminationLevel(template, createdAcquisitionsPerTemplateId.get(templateId)));
                    case "acquisition" ->
                            transactionRunner.runInTransaction(em -> createExecutionsAtAcquisitionLevel(template, createdAcquisitionsPerTemplateId.get(templateId)));
                    case "dataset" ->
                            transactionRunner.runInTransaction(em -> createExecutionsAtDatasetLevel(template, createdAcquisitionsPerTemplateId.get(templateId)));
                }
            }
        }
    }

    @Transactional
    public void savePlannedExecution(Map<Long, List<Long>> createdAcquisitionsPerTemplatesId) {
        for (Map.Entry<Long, List<Long>> entry : createdAcquisitionsPerTemplatesId.entrySet()) {
            Long templateId = entry.getKey();
            for (Long acquisitionId : entry.getValue()) {
                PlannedExecution plannedExecution = new PlannedExecution(templateId, acquisitionId);
                plannedExecutionRepository.save(plannedExecution);
            }
        }
        LOG.info("Planned executions for newly imported DICOMs saved.");
    }

    /**
     * Create execution when it's one execution for all the newly imported data
     */
    private void createExecutionAtExaminationLevel(ExecutionTemplate template, List<Long> acquisitionIds) {
        int attempt = 0;
        DatasetAcquisition acquisition = acquisitionRepository.findById(acquisitionIds.getFirst()).orElse(null);
        while(Objects.isNull(acquisition) && attempt < 5) {
            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {}
            attempt++;
            acquisition = acquisitionRepository.findById(acquisitionIds.getFirst()).orElse(null);
        }

        if(Objects.isNull(acquisition)) {
            LOG.error("Did not achieve to get newly imported acquisitions for auto execution with template {}. Giving up.", template.getId());
        } else {
            Long examinationId = acquisition.getExamination().getId();
            DatasetAcquisition finalAcquisition = acquisition;

            plannedExecutionRunner.addToExecutionsQueue(new ExecutionInQueue(template, examinationId, "examination", acquisitionIds));
        }
    }

    /**
     * Create executions when it's one execution per newly acquisition
     */
    private void createExecutionsAtAcquisitionLevel(ExecutionTemplate template, List<Long> acquisitionIds) {
        for (Long acquisitionId : acquisitionIds) {

            plannedExecutionRunner.addToExecutionsQueue(new ExecutionInQueue(template, acquisitionId, "acquisition", List.of(acquisitionId)));
            try {
                Thread.sleep(1000); // Delay between submissions, VIP needs it
            } catch (InterruptedException e) {
                LOG.error("Issue while serializing executions with template {}. Stopping session.", template.getId());
                break;
            }
        }
    }

    /**
     * Create executions when it's one execution per newly dataset
     */
    private void createExecutionsAtDatasetLevel(ExecutionTemplate template, List<Long> acquisitionIds) {
        int attempt = 0;
        List<Long> datasetIds = datasetRepository.findFilteredIdsByDatasetAcquisitionIdIn(acquisitionIds, "%");
        while(datasetIds.isEmpty() && attempt < 5) {
            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {}
            attempt++;
            datasetIds = datasetRepository.findFilteredIdsByDatasetAcquisitionIdIn(acquisitionIds, "%");
        }

        for (Long acquisitionId : acquisitionIds) {
            DatasetAcquisition acquisition = acquisitionRepository.findById(acquisitionId).orElse(null);
            while(Objects.isNull(acquisition) && attempt < 5) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {}
                attempt++;
                acquisition = acquisitionRepository.findById(acquisitionIds.getFirst()).orElse(null);
            }

            if(Objects.nonNull(acquisition)){
                List<Dataset> datasetList = acquisition.getDatasets();
                for (int i = 0; i < datasetList.size(); i++) {
                    Dataset dataset = datasetList.get(i);

                    //For the execution submission, the list needs to be final, so the plannedExecutionToRemove list has to be managed that way.
                    List<Long> plannedExecutionToRemove;
                    if(Objects.equals(i, datasetList.size() - 1)){
                        plannedExecutionToRemove = List.of(acquisitionId);
                    } else {
                        plannedExecutionToRemove = List.of(acquisitionId);
                    }

                    plannedExecutionRunner.addToExecutionsQueue(new ExecutionInQueue(template, dataset.getId(), "dataset", plannedExecutionToRemove));
                    try {
                        Thread.sleep(1000); // Delay between submissions, VIP needs it
                    } catch (InterruptedException e) {
                        LOG.error("Issue while serializing executions with template {}. Stopping session.", template.getId());
                        break;
                    }
                }
            } else {
                LOG.error("Did not achieve to get newly imported acquisitions for auto execution with template {}. Giving up.", template.getId());
            }
        }
    }

    /**
     * Prepare the execution candidate DTO relatively to the given templateId and acquisitionId
     */
    public ExecutionCandidateDTO prepareExecutionCandidate(ExecutionTemplate template, String executionLevel, Long objectId) {
        Long studyId = template.getStudy().getId();
        Long examId = null;
        switch (executionLevel) {
            case "examination" -> examId = objectId;
            case "acquisition" -> {
                DatasetAcquisition acquisition = acquisitionRepository.findById(objectId).orElse(null);
                if(Objects.nonNull(acquisition)) {
                    examId = acquisition.getExamination().getId();
                }
            }
            case "dataset" -> {
                Dataset dataset = datasetRepository.findById(objectId).orElse(null);
                if(Objects.nonNull(dataset)) {
                    examId = dataset.getDatasetAcquisition().getExamination().getId();
                }
            }
        }

        ExecutionCandidateDTO candidate = new ExecutionCandidateDTO();
        candidate.setInputParameters(new HashMap<>());
        candidate.setClient("shanoir-uploader");
        candidate.setName(template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), "execution_name")).findFirst().get().getValue());
        candidate.setName(candidate.getName() + "_" + (Objects.isNull(examId) ? "" : examId + "_") + LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE));
        candidate.setConverterId(Long.valueOf(template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), "converter")).findFirst().get().getValue()));
        candidate.setPipelineIdentifier(template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), "pipeline_identifier")).findFirst().get().getValue());
        candidate.setProcessingType(template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), "processing_type")).findFirst().get().getValue());
        candidate.setStudyIdentifier(studyId);

        candidate.setDatasetParameters(prepareDatasetParameters(template, objectId));
        return Objects.isNull(candidate.getDatasetParameters()) ? null : candidate;
    }

    public List<Long> getInvolvedData(ExecutionInQueue execution) {
        switch (execution.getType()) {
            case "dataset" -> { return List.of(execution.getObjectId()); }
            case "acquisition" -> { return datasetRepository.findFilteredIdsByDatasetAcquisitionId(execution.getObjectId(), ""); }
            case "examination" -> { return datasetRepository.findIdsByExaminationId(execution.getObjectId()); }
            default -> { return null;}
        }
    }

    /**
     * Prepare the execution candidate DTO dataset parameters
     */
    private List<DatasetParameterDTO> prepareDatasetParameters(ExecutionTemplate template, Long objectId) {
        List<String> prefixes = template.getParameters().stream().filter(parameter -> parameter.getName().endsWith("_group")).map(parameter -> parameter.getName().substring(0, parameter.getName().indexOf("_group"))).toList();
        List<DatasetParameterDTO> datasetParameters = new ArrayList<>();

        for(String prefixe: prefixes) {
            String parameterGroup = template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), prefixe + "_group")).findFirst().get().getValue();
            String parameterValue = template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), prefixe)).findFirst().get().getValue();
            List<Long> inputIds = new ArrayList<>();

            switch (parameterGroup) {
                case "examination" -> {
                    inputIds = datasetRepository.findFilteredIdsByDatasetAcquisitionIdIn(acquisitionRepository.findIdsByExaminationId(objectId), parameterValue);
                }
                case "acquisition" -> {
                    inputIds = datasetRepository.findFilteredIdsByDatasetAcquisitionId(objectId, parameterValue);
                }
                case "dataset" -> {
                    Dataset dataset = datasetRepository.findById(objectId).get();
                    if(dataset.getName().contains(parameterValue)){
                        inputIds.add(dataset.getId());
                    }
                    return null;
                }
            }
            if(!inputIds.isEmpty()) {
                DatasetParameterDTO datasetParameterDTO = new DatasetParameterDTO();
                datasetParameterDTO.setDatasetIds(inputIds);
                datasetParameterDTO.setName(prefixe);
                datasetParameterDTO.setConverterId(Long.valueOf(template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), "converter")).findFirst().get().getValue()));
                datasetParameterDTO.setExportFormat(template.getParameters().stream().filter(parameter -> Objects.equals(parameter.getName(), "export_format")).findFirst().get().getValue());
                datasetParameterDTO.setGroupBy(GroupByEnum.valueOf(parameterGroup.toUpperCase()));
                datasetParameters.add(datasetParameterDTO);
            }
        }
        return datasetParameters;
    }

    /**
     * Get the specific parameters of the pipeline relative to the id given as parameter
     */
    private List<ExecutionTemplateParameter> getSpecificPipelineParameter(Long templateId){
        ExecutionTemplate template = executionTemplateRepository.findById(templateId).get();
        List<String> specificPrefixe = template.getParameters().stream().filter(parameter -> parameter.getName().endsWith("_group")).map(parameter -> parameter.getName().substring(0, parameter.getName().indexOf("_group"))).toList();
        return template.getParameters().stream().filter(parameter -> specificPrefixe.stream().anyMatch(parameter.getName()::startsWith)).toList();
    }

    /**
     * Get the lower level of group for specific parameters. It will decide if it's one exec per (new) exam / per new acquisition / per new dataset
     */
    private String getExecutionLevel(ExecutionTemplate template) {
        List<String> distinctsGroup  = template.getParameters().stream().filter(parameter -> parameter.getName().endsWith("_group")).map(ExecutionTemplateParameter::getValue).toList();
        return Stream.of("dataset", "acquisition")
                .filter(distinctsGroup::contains)
                .findFirst()
                .orElse("examination");
    }

    private LinkedHashMap<Long, List<Long>> sortingPerPrio(Map<Long, List<Long>> createdAcquisitionsPerTemplateId) {
        LinkedHashMap<Long, List<Long>> sortedAcquisitionsPerTemplateId = new LinkedHashMap<>();
        List<ExecutionTemplate> involvedTemplates = StreamSupport.stream(executionTemplateRepository.findAllById(createdAcquisitionsPerTemplateId.keySet()).spliterator(), false).toList();

        involvedTemplates.stream()
                .sorted(Comparator.comparingInt(ExecutionTemplate::getPriority))
                .forEach(template -> {
                    Long templateId = template.getId();
                    if (createdAcquisitionsPerTemplateId.containsKey(templateId)) {
                        sortedAcquisitionsPerTemplateId.put(templateId, createdAcquisitionsPerTemplateId.get(templateId));
                    }
                });

        return sortedAcquisitionsPerTemplateId;
    }
}