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

package org.shanoir.ng.studycard;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.dicom.web.StudyInstanceUIDAndSubjectNameHandler;
import org.shanoir.ng.studycard.model.Operation;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.model.condition.CardCondition;
import org.shanoir.ng.studycard.model.condition.DatasetMetadataCondOnDataset;
import org.shanoir.ng.studycard.model.field.DatasetMetadataField;
import org.shanoir.ng.studycard.model.rule.DatasetRule;
import org.shanoir.ng.studycard.model.rule.StudyCardRule;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.shanoir.ng.studycard.service.StudyCardServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

/**
 * Reproduces the FK violation reported on studycard update, with a payload shape matching what the
 * real frontend actually sends: rules/conditions/assignments NEVER carry an id (StudyCardRuleDTO /
 * StudyCardConditionDTO / StudyCardAssignmentDTO have no id field), only the top-level StudyCard does.
 */
@SpringBootTest
@ActiveProfiles("test")
public class StudyCardUpdateFKReproTest {

    @Autowired
    private StudyCardRepository studyCardRepository;

    @Autowired
    private StudyCardServiceImpl studyCardService;

    @MockBean
    private StudyInstanceUIDAndSubjectNameHandler studyInstanceUIDHandler;

    private DatasetRule buildRule(String value) {
        DatasetRule rule = new DatasetRule();
        rule.setOrConditions(false);

        DatasetMetadataCondOnDataset condition = new DatasetMetadataCondOnDataset();
        condition.setShanoirField(DatasetMetadataField.NAME);
        condition.setOperation(Operation.EQUALS);
        condition.setCardinality(-1);
        condition.setValues(List.of(value));

        List<CardCondition> conditions = new ArrayList<>();
        conditions.add(condition);
        rule.setConditions(conditions);
        rule.setAssignments(new ArrayList<>());
        return rule;
    }

    @Test
    public void updateWithMultipleRulesNoIdsTest() throws Exception {
        // 1) Persist an initial StudyCard with TWO rules, each with one condition -- no ids set anywhere,
        // exactly like a real "create" payload from the frontend.
        StudyCard initial = new StudyCard();
        initial.setName("ReproCardMulti");
        initial.setStudyId(1L);
        initial.setAcquisitionEquipmentId(1L);
        initial.setDisabled(false);

        List<StudyCardRule<?>> rules = new ArrayList<>();
        rules.add(buildRule("foo"));
        rules.add(buildRule("bar"));
        initial.setRules(rules);

        StudyCard persisted = studyCardRepository.save(initial);
        System.out.println("PERSISTED study card id=" + persisted.getId() + " with " + persisted.getRules().size() + " rules");

        // 2) Build an "incoming" update payload the way the real frontend does: same StudyCard id, but a
        // brand new object graph for rules/conditions/assignments, none of them carrying any id (matching
        // StudyCardRuleDTO/StudyCardConditionDTO/StudyCardAssignmentDTO, which have no id field at all).
        StudyCard incoming = new StudyCard();
        incoming.setId(persisted.getId());
        incoming.setName("ReproCardMulti renamed");
        incoming.setStudyId(1L);
        incoming.setAcquisitionEquipmentId(1L);
        incoming.setDisabled(false);

        List<StudyCardRule<?>> incomingRules = new ArrayList<>();
        incomingRules.add(buildRule("foo"));
        incomingRules.add(buildRule("bar"));
        incoming.setRules(incomingRules);

        // 3) Call the real service update() -- this is exactly what the controller does.
        studyCardService.update(incoming);
        System.out.println("FIRST UPDATE SUCCEEDED WITHOUT EXCEPTION");

        // 4) Do it again -- a second no-op-ish update in a row, in case the bug only shows up on repeat.
        StudyCard incoming2 = new StudyCard();
        incoming2.setId(persisted.getId());
        incoming2.setName("ReproCardMulti renamed again");
        incoming2.setStudyId(1L);
        incoming2.setAcquisitionEquipmentId(1L);
        incoming2.setDisabled(false);

        List<StudyCardRule<?>> incomingRules2 = new ArrayList<>();
        incomingRules2.add(buildRule("foo"));
        incomingRules2.add(buildRule("bar"));
        incoming2.setRules(incomingRules2);

        studyCardService.update(incoming2);
        System.out.println("SECOND UPDATE SUCCEEDED WITHOUT EXCEPTION");
    }
}
