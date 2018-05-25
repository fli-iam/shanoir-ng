import { DatasetType } from "./dataset-type.model";
import { DatasetProcessing } from "./dataset-processing.model";

export class Dataset {
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
    //originMetadata: DatasetMetadata
    //updatedMetadata : DatasetMetadata
}