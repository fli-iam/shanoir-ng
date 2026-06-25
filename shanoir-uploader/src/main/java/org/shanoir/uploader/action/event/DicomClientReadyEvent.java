package org.shanoir.uploader.action.event;

import org.shanoir.uploader.dicom.DicomServerClient;
import org.springframework.context.ApplicationEvent;

public class DicomClientReadyEvent extends ApplicationEvent {

    private final DicomServerClient dicomServerClient;

    public DicomClientReadyEvent(Object source, DicomServerClient dicomServerClient) {
        super(source);
        this.dicomServerClient = dicomServerClient;
    }

    public DicomServerClient getDicomServerClient() {
        return dicomServerClient;
    }
}
