import { DatasetType } from "./dataset-type.model";
import { DatasetProcessing } from "./dataset-processing.model";

declare type ExploredEntity = 'ANATOMICAL_DATASET' | 'FUNCTIONAL_DATASET' | 'HEMODYNAMIC_DATASET' | 'METABOLIC_DATASET' | 'CALIBRATION';
declare type ProcessedDatasetType = 'RECONSTRUCTEDDATASET' | 'NONRECONSTRUCTEDDATASET';
declare type CardinalityOfRelatedSubjects = 'SINGLE_SUBJECT_DATASET' | 'MULTIPLE_SUBJECTS_DATASET';

export abstract class Dataset {
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
}

export class DatasetMetadata {
    comment: string;
    datasetModalityType: string;
    exploredEntity: ExploredEntity;
    name: string;
    processedDatasetType: ProcessedDatasetType;
    cardinalityOfRelatedSubjects: CardinalityOfRelatedSubjects = 'SINGLE_SUBJECT_DATASET';
}