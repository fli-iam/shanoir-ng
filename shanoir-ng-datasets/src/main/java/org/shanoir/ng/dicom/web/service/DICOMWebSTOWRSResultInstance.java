package org.shanoir.ng.dicom.web.service;

public class DICOMWebSTOWRSResultInstance {

    private String sopInstanceUID;

    private String sopClassUID;

    private String status;

    private Integer failureReason;

    private String failureReasonText;

    public String getSopInstanceUID() {
        return sopInstanceUID;
    }

    public void setSopInstanceUID(String uid) {
        this.sopInstanceUID = uid;
    }

    public String getSopClassUID() {
        return sopClassUID;
    }

    public void setSopClassUID(String uid) {
        this.sopClassUID = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(Integer reason) {
        this.failureReason = reason;
    }

    public String getFailureReasonText() {
        return failureReasonText;
    }

    public void setFailureReasonText(String text) {
        this.failureReasonText = text;
    }

    public boolean isDuplicate() {
        // 0xB306 (45830) = Instance already stored
        // 0xB305 (45829) = Instance received but not stored
        return failureReason != null &&
                (failureReason == 0xB306 || failureReason == 45830 ||
                        failureReason == 0xB305 || failureReason == 45829);
    }

}
