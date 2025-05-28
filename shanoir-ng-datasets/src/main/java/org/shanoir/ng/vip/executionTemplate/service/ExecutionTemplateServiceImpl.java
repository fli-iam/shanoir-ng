package org.shanoir.ng.vip.executionTemplate.service;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateFilter;
import org.shanoir.ng.vip.executionTemplate.repository.ExecutionTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExecutionTemplateServiceImpl implements ExecutionTemplateService {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionTemplateServiceImpl.class);

    @Autowired
    private ExecutionTemplateRepository repository;

    @Autowired
    @Lazy
    private PlannedExecutionService plannedExecutionService;

    @Async
    public void createExecutionsFromExecutionTemplates(List<DatasetAcquisition> createdAcquisitions) {
        if (Objects.isNull(createdAcquisitions) || createdAcquisitions.isEmpty()) {
            return;
        }

        Map<Long, List<DatasetAcquisition>> createdAcquisitionsPerStudyIdMap = createdAcquisitions.stream().collect(Collectors.groupingBy(acq -> acq.getExamination().getStudyId()));
        Map<Long, List<Long>> createdAcquisitionsPerTemplatesId = new HashMap<>();

        for(Long studyId : createdAcquisitionsPerStudyIdMap.keySet()) {
            List<ExecutionTemplate> executionTemplates = repository.findByStudyId(studyId);

            for(ExecutionTemplate executionTemplate : executionTemplates.stream().sorted(Comparator.comparing(ExecutionTemplate::getPriority)).toList()) {
                List<Long> acquisitionToProcess = filterAcquisitionToProcess(executionTemplate,  createdAcquisitionsPerStudyIdMap.get(studyId));

                if (!acquisitionToProcess.isEmpty()) {
                    createdAcquisitionsPerTemplatesId.put(executionTemplate.getId(), acquisitionToProcess);
                }
            }
        }
        if(!createdAcquisitionsPerTemplatesId.isEmpty()) {
            plannedExecutionService.savePlannedExecution(createdAcquisitionsPerTemplatesId);
            plannedExecutionService.applyExecution(createdAcquisitionsPerTemplatesId);
        }
    }

    /**
     * This method filters the execution templates available for the given acquisition
     */
    private List<Long> filterAcquisitionToProcess(ExecutionTemplate template, List<DatasetAcquisition> acquisitions) {
        List<Long> templateToApplyIds = new ArrayList<>();
        Map<Integer, Boolean> fieldPositionAndComparisonResults = new HashMap<>();
        String filterCombination = template.getFilterCombination();
        Set<DatasetAcquisition> acquisitionsToFilter = new HashSet<>(acquisitions);

        for(DatasetAcquisition acquisition : acquisitionsToFilter) {
            try{
                fieldPositionAndComparisonResults = getFieldPositionAndComparisonResults(template, acquisition);
                if(Objects.equals(fieldPositionAndComparisonResults.size(), 0)){
                    templateToApplyIds.add(acquisition.getId());
                } else {
                    filterCombination = fillCombinationWithComparisonResults(filterCombination, fieldPositionAndComparisonResults);

                    SpelExpressionParser parser = new SpelExpressionParser();
                    StandardEvaluationContext context = new StandardEvaluationContext();
                    if (parser.parseExpression(filterCombination).getValue(context, Boolean.class)) {
                        templateToApplyIds.add(acquisition.getId());
                    }
                }
            } catch (NullPointerException e) {
                LOG.error("Execution template {} of study {} is badly configured. Please check execution filters.", template.getName(), template.getStudy().getName());
            }
        }
        return templateToApplyIds;
    }

    /**
     * This method returns a map with local evaluations of values and regex to compare, with their position identifier
     */
    private Map<Integer, Boolean> getFieldPositionAndComparisonResults(ExecutionTemplate template, DatasetAcquisition acquisition) throws NullPointerException{
        Map<Integer, Boolean> fieldPositionAndComparison = new HashMap<>();

        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        context.setVariable("acquisition", acquisition);
        context.setVariable("examination", acquisition.getExamination());
        context.setVariable("subject", acquisition.getExamination().getSubject());

        for(ExecutionTemplateFilter filter : template.getFilters()) {
            String value = parser.parseExpression("#" + filter.getFieldName()).getValue(context, String.class);
            String pattern = filter.getComparedRegex().replace("%", ".*");
            Boolean match = value.matches(pattern);

            fieldPositionAndComparison.put(filter.getIdentifier(), match);
        }
        return fieldPositionAndComparison;
    }

    /**
     * This method fill the filter combination with the results of each filters, resulting in a combination of boolean values
     */
    private String fillCombinationWithComparisonResults(String filterCombination, Map<Integer, Boolean> fieldPositionAndComparisonResults) {
        if (Objects.equals(filterCombination, "and") || Objects.equals(filterCombination, "or")) {
            filterCombination = fieldPositionAndComparisonResults.values().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(" " + filterCombination + " "));

        } else {
            for (Map.Entry<Integer, Boolean> entry : fieldPositionAndComparisonResults.entrySet()) {
                filterCombination = filterCombination.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue().toString());
            }
        }
        return filterCombination;
    }
}