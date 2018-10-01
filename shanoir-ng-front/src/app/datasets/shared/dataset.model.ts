import { Entity } from '../../shared/components/entity/entity.interface';
import { ServiceLocator } from '../../utils/locator.service';
import { DatasetProcessing } from './dataset-processing.model';
import { DatasetType } from './dataset-type.model';
import { DatasetService } from './dataset.service';

declare type ExploredEntity = 'ANATOMICAL_DATASET' | 'FUNCTIONAL_DATASET' | 'HEMODYNAMIC_DATASET' | 'METABOLIC_DATASET' | 'CALIBRATION';
declare type ProcessedDatasetType = 'RECONSTRUCTEDDATASET' | 'NONRECONSTRUCTEDDATASET';
declare type CardinalityOfRelatedSubjects = 'SINGLE_SUBJECT_DATASET' | 'MULTIPLE_SUBJECTS_DATASET';

export abstract class Dataset implements Entity {
    id: number;
    creationDate: Date;
    name: string;
    type: DatasetType;
    //datasetAcquisition: DatasetAcquisition
    //datasetExpressions: List<DatasetExpression>
    datasetProcessing: DatasetProcessing
    groupOfSubjectsId: number;
    //inputOfDatasetProcessings: Array<InputOfDatasetProcessing>
    referencedDatasetForSuperimposition: Dataset;
    studyId : number;
    subjectId : number;
    originMetadata: DatasetMetadata;
    updatedMetadata : DatasetMetadata = new DatasetMetadata();

    private datasetService: DatasetService = ServiceLocator.injector.get(DatasetService);

    create(): Promise<Entity> {
        return this.datasetService.create(this);
    }

    update(): Promise<void> {
        return this.datasetService.update(this);
    }

    delete(): Promise<void> {
        return this.datasetService.delete(this.id);
    }
}

export class DatasetMetadata {
    comment: string;
    datasetModalityType: string;
    exploredEntity: ExploredEntity;
    name: string;
    processedDatasetType: ProcessedDatasetType;
    cardinalityOfRelatedSubjects: CardinalityOfRelatedSubjects = 'SINGLE_SUBJECT_DATASET';
}