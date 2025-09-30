package org.shanoir.uploader.action;


import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.shanoir.uploader.gui.ImportFromFolderWindow;
import org.shanoir.uploader.model.rest.IdList;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateFolderImportStudyListener implements ItemListener {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateFolderImportStudyListener.class);

    private final ImportFromFolderWindow window;
    private final ShanoirUploaderServiceClient shanoirUploaderServiceClient;

    public UpdateFolderImportStudyListener(ShanoirUploaderServiceClient shanoirUploaderServiceClient, ImportFromFolderWindow window) {
        this.window = window;
        this.shanoirUploaderServiceClient = shanoirUploaderServiceClient;
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {
        int state = itemEvent.getStateChange();
        if (state == ItemEvent.SELECTED) {
            if (itemEvent.getSource().equals(window.studyCB)) {
                Study study = (Study) itemEvent.getItem();
                updateStudyCards(study);
            }
            if (itemEvent.getSource().equals(window.studyCardCB)) {
                StudyCard studycard = (StudyCard) itemEvent.getItem();
                window.openButton.setEnabled(studycard != null);
            }
        }
    }

    private void updateStudyCards(Study study) {
        window.studyCardCB.removeAllItems();
        if (study.getStudyCards() != null) {
            for (StudyCard studyCard : study.getStudyCards()) {
                studyCard.setCompatible(false);
                window.studyCardCB.addItem(studyCard);
            }
        }
    }

    public void updateFolderImportForStudyAndStudyCard() {
        window.studyCB.removeAllItems();
        window.studyCardCB.removeAllItems();
        try {
            List<Study> studies = shanoirUploaderServiceClient.findStudiesNamesAndCenters();
            List<StudyCard> studyCards = getAllStudyCards(studies);

            for (Study study : studies) {
                study.setCompatible(false);
                window.studyCB.addItem(study);
                study.setStudyCards(new ArrayList<>());
                for (StudyCard studycard : studyCards) {
                    if (studycard.getStudyId().equals(study.getId())) {
                        study.getStudyCards().add(studycard);
                    }
                }
            }

            window.studyCB.setSelectedItem(studies.get(0));
            this.updateStudyCards(studies.get(0));

        } catch (Exception e) {
            LOG.error("Could not correctly retrieve study and study cards: ", e);
            // Set as error here
            this.window.displayError("Something went wrong when loading study and study cards, please retry later.");
        }
    }

    private List<StudyCard> getAllStudyCards(List<Study> studies) throws Exception {
        IdList idList = new IdList();
        for (Iterator<Study> iterator = studies.iterator(); iterator.hasNext();) {
            Study study = (Study) iterator.next();
            idList.getIdList().add(study.getId());
        }
        List<StudyCard> studyCards = shanoirUploaderServiceClient.findStudyCardsByStudyIds(idList);
        return studyCards;
    }

}
