/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.studycard.model.condition;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.exception.CheckedIllegalClassException;
import org.shanoir.ng.studycard.model.field.DatasetMetadataField;
import org.shanoir.ng.studycard.model.field.MetadataFieldInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Condition valid for the given DatasetAcquisition if every of it's Datasets metadata fulfill the condition
 */
@Entity
@DiscriminatorValue("AcqMetadataCondOnDatasets")
@JsonTypeName("AcqMetadataCondOnDatasets")
public class AcqMetadataCondOnDatasets extends StudyCardMetadataCondition<Dataset> {

	private static final Logger LOG = LoggerFactory.getLogger(AcqMetadataCondOnDatasets.class);

	@Override
    public DatasetMetadataField getShanoirField() {
        return DatasetMetadataField.getEnum(shanoirField);
    }

    @Override // Don't know why eclipse can't take DatasetAcquisitionMetadataField as input type
    public void setShanoirField(MetadataFieldInterface<Dataset>  field) {
        shanoirField = field.getId();
    }

    public boolean fulfilled(List<Dataset> datasets) {
        if (datasets == null) throw new IllegalArgumentException("datasets can not be null");
        DatasetMetadataField field = this.getShanoirField();
        if (field == null) throw new IllegalArgumentException("field can not be null");
        int nbOk = 0;
        for (Dataset dataset : datasets) {
            String valueFromDb;
            try {
                valueFromDb = field.get(dataset);
            } catch (CheckedIllegalClassException e) {
                valueFromDb = null;
            }
            if (valueFromDb != null) {
                // get all possible values, that can fulfill the condition
                for (String value : this.getValues()) {
                    if (textualCompare(this.getOperation(), valueFromDb, value)) {
                        LOG.info("condition fulfilled: ds.name = " + valueFromDb + ", value=" + value);
                        nbOk++;
                        break;
                    }
                }
            }
        }
        return cardinalityComplies(nbOk, datasets.size());
    }
}
