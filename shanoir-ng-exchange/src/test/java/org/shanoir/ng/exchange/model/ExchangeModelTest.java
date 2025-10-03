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

package org.shanoir.ng.exchange.model;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class ExchangeModelTest {


    @Before
    public void setup() {
    }

    @Test
    public void testCreateJson() throws JsonProcessingException {
        Exchange exchange = new Exchange();
        exchange.setAnonymisationProfileToUse("OFSEP Profile");
        ExStudy exStudy = new ExStudy();
        exStudy.setStudyName("NATIVE Divers");
        exchange.setExStudy(exStudy);
        ExStudyCard exStudyCard = new ExStudyCard();
        exStudyCard.setName("Magic studycard");
        ArrayList<ExStudyCard> exStudyCards = new ArrayList<>();
        exStudyCards.add(exStudyCard);
        exStudy.setExStudyCards(exStudyCards);

        ExSubject exSubject = new ExSubject();
        exSubject.setSubjectName("toto001");
        ArrayList<ExSubject> exSubjects = new ArrayList<>();
        exSubjects.add(exSubject);
        exStudy.setExSubjects(exSubjects);

        ExExamination exExamination = new ExExamination();
        exExamination.setId(new Long(1));
        ArrayList<ExExamination> exExaminations = new ArrayList<>();
        exExaminations.add(exExamination);
        exSubject.setExExaminations(exExaminations);

        ObjectMapper mapper = new ObjectMapper();
        String jsonStr = mapper.writeValueAsString(exchange);
        System.out.println(jsonStr);
    }

}
