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
import { CardinalityOfRelatedSubjects } from '../../enum/cardinality-of-related-subjects.enum';
import { ExploredEntity } from '../../enum/explored-entity.enum';
import { ProcessedDatasetType } from '../../enum/processed-dataset-type.enum';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { DatasetProcessing } from './dataset-processing.model';
import { Study } from '../../studies/shared/study.model';
import { Subject } from '../../subjects/shared/subject.model';
import { DatasetType } from './dataset-type.model';
import { DatasetAcquisition } from '../../dataset-acquisitions/shared/dataset-acquisition.model';
import { BidsDataType } from '../../enum/bids-data-type.enum';
import {Tag} from "../../tags/tag.model";
import { Field } from 'src/app/shared/reflect/field.decorator';

export abstract class Dataset extends Entity {

    @Field() id: number;
    @Field() creationDate: Date;
    @Field() name: string;
    @Field() type: DatasetType;
    @Field() datasetAcquisition: DatasetAcquisition
    @Field() datasetProcessing: DatasetProcessing
    @Field() study : Study;
    @Field() subject : Subject;
    @Field() originMetadata: DatasetMetadata;
    @Field() updatedMetadata : DatasetMetadata = new DatasetMetadata();
    @Field() processings: DatasetProcessing[] = [];
    @Field() inPacs: boolean;
    @Field() tags: Tag[];
    @Field() copies: number[];
    @Field() source: number;
    private _hasProcessing: boolean;

    get hasProcessings(): boolean {
        return this.processings?.length > 0;
    }

    set hasProcessing(hasProcessing: boolean) {
        this._hasProcessing = hasProcessing;
    }

    get hasProcessing(): boolean {
        return this._hasProcessing != undefined ? this._hasProcessing : !!this.datasetProcessing;
    }
}

export class DatasetMetadata {
    comment: string;
    datasetModalityType: string;
    exploredEntity: ExploredEntity;
    name: string;
    processedDatasetType: ProcessedDatasetType;
    cardinalityOfRelatedSubjects: CardinalityOfRelatedSubjects = CardinalityOfRelatedSubjects.SINGLE_SUBJECT_DATASET;
	bidsDataType: BidsDataType;
}
