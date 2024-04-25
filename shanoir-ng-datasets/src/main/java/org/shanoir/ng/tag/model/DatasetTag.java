package org.shanoir.ng.tag.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.hateoas.HalEntity;

@Entity
public class DatasetTag extends HalEntity {

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private StudyTag tag;
    @ManyToOne
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;

    public StudyTag getTag() {
        return tag;
    }

    public void setTag(StudyTag tag) {
        this.tag = tag;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }
}
