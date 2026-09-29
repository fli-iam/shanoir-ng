package org.shanoir.uploader.model.rest;

import java.util.List;

import org.shanoir.ng.shared.security.rights.StudyUserRight;

public class StudyUser {

    private Long id;

    private Long studyId;

    private Long userId;

    private String userName;

    private List<StudyUserRight> studyUserRights;

    private List<Long> centerIds;

    private boolean confirmed;

    private boolean receiveStudyUserReport;

    private boolean receiveNewImportReport;

    public StudyUser() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<StudyUserRight> getStudyUserRights() {
        return studyUserRights;
    }

    public void setStudyUserRights(List<StudyUserRight> studyUserRights) {
        this.studyUserRights = studyUserRights;
    }

    public List<Long> getCenterIds() {
        return centerIds;
    }

    public void setCenterIds(List<Long> centerIds) {
        this.centerIds = centerIds;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isReceiveStudyUserReport() {
        return receiveStudyUserReport;
    }

    public void setReceiveStudyUserReport(boolean receiveStudyUserReport) {
        this.receiveStudyUserReport = receiveStudyUserReport;
    }

    public boolean isReceiveNewImportReport() {
        return receiveNewImportReport;
    }

    public void setReceiveNewImportReport(boolean receiveNewImportReport) {
        this.receiveNewImportReport = receiveNewImportReport;
    }

}
