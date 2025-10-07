package org.shanoir.ng.accessrequest.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.user.model.User;

/**
 * This class is used by a user to ask an access to a given study.
 * @author jcome
 *
 */
@Entity
public class AccessRequest extends AbstractEntity {

    private static final long serialVersionUID = 4662874539537675259L;

    public static final int APPROVED = 1;

    public static final int REFUSED = -1;

    public static final int ON_DEMAND = 0;

    private String studyName;

    private Long studyId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String motivation;

    /** 0: unresolved
     *  1: accepted
     * -1:  refused
     */
    private int status;

    /**
     * @return the motivation
     */
    public String getMotivation() {
        return motivation;
    }

    /**
     * @param motivation the motivation to set
     */
    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }
}
