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
		exStudyCard.setId(new Long(1));
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
