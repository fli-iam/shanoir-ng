package org.shanoir.uploader.action;


import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import org.shanoir.uploader.gui.ImportFromTableWindow;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateTableImportStudyListener implements ItemListener {

    private static Logger logger = LoggerFactory.getLogger(UpdateTableImportStudyListener.class);

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
            for (Study study : studies) {
                study.setCompatible(false);
                window.studyCB.addItem(study);
            }
            window.studyCB.setSelectedItem(studies.get(0));
        } catch (Exception e) {
            logger.error("Could not correctly retrieve studies: ", e);
            this.window.displayError("Something went wrong when loading study and study cards, please retry later.");
        }
    }

}
