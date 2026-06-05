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

package org.shanoir.ng.studycard.model.rule;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.studycard.model.assignment.DatasetAcquisitionAssignment;
import org.shanoir.ng.studycard.model.assignment.DatasetAssignment;
import org.shanoir.ng.studycard.model.assignment.StudyCardAssignment;
import org.shanoir.ng.studycard.model.condition.AcqMetadataCondOnAcq;
import org.shanoir.ng.studycard.model.condition.AcqMetadataCondOnDatasets;
import org.shanoir.ng.studycard.model.condition.StudyCardCondition;
import org.shanoir.ng.studycard.model.condition.StudyCardDICOMConditionOnDatasets;

import com.fasterxml.jackson.annotation.JsonTypeName;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * A rule that applies to a {@link DatasetAcquisition}
 */
@Entity
@DiscriminatorValue("DatasetAcquisition")
@JsonTypeName("DatasetAcquisition")
public class DatasetAcquisitionRule extends StudyCardRule<DatasetAcquisition> { }
