package org.shanoir.ng.examination.schedule;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ExaminationLastChecked {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long examinationId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExaminationId() {
        return examinationId;
    }

    public void setExaminationId(Long examinationId) {
        this.examinationId = examinationId;
    }

}
