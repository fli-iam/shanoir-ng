package org.shanoir.uploader.action;


import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.shanoir.uploader.gui.ImportFromTableWindow;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.shanoir.uploader.utils.ImportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateTableImportStudyListener implements ItemListener {

    private static final Logger logger = LoggerFactory.getLogger(UpdateTableImportStudyListener.class);

    private final ImportFromTableWindow window;

    private final ShanoirUploaderServiceClient shanoirUploaderServiceClient;

    public UpdateTableImportStudyListener(ShanoirUploaderServiceClient shanoirUploaderServiceClient, ImportFromTableWindow window) {
        this.window = window;
        this.shanoirUploaderServiceClient = shanoirUploaderServiceClient;
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {
        int state = itemEvent.getStateChange();
        if (state == ItemEvent.SELECTED) {
            if (itemEvent.getSource().equals(window.studyCB)) {
                Study study = (Study) itemEvent.getItem();
            }
        }
    }

    public void updateTableImportForStudy() {
        window.studyCB.removeAllItems();
        try {
            List<Study> studies = shanoirUploaderServiceClient.findStudiesNamesAndCenters();
            List<StudyCard> studyCards = ImportUtils.getAllStudyCards(studies);
            for (Study study : studies) {
                if (studyCards != null) {
                    List<StudyCard> studyCardsStudy = new ArrayList<StudyCard>();
                    for (Iterator<StudyCard> itStudyCards = studyCards.iterator(); itStudyCards.hasNext();) {
                        StudyCard studyCard = (StudyCard) itStudyCards.next();
                        // filter all study cards related to the selected study
                        if (study.getId().equals(studyCard.getStudyId())) {
                            studyCardsStudy.add(studyCard);
                        }
                    }
                    study.setStudyCards(studyCardsStudy);
                    study.setCompatible(Boolean.FALSE);
                }
                window.studyCB.addItem(study);
            }
            window.studyCB.setSelectedItem(studies.get(0));
        } catch (Exception e) {
            logger.error("Could not correctly retrieve studies and study cards: ", e);
            this.window.displayError("Something went wrong when loading study and study cards, please retry later.");
        }
    }

}
