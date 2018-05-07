export class Dataset {
    id: number;
    creationDate: Date;
    name: string;
    //datasetAcquisition: DatasetAcquisition
    //datasetExpressions: List<DatasetExpression>
    //datasetProcessing: DatasetProcessing
    groupOfSubjectsId: number;
    //inputOfDatasetProcessings: Array<InputOfDatasetProcessing>
    //originMetadata: DatasetMetadata
    referencedDatasetForSuperimposition: Dataset;
    //referencedDatasetForSuperimpositionChildrenList: Array<Dataset>
    studyId : number;
    subjectId : number;
    //updatedMetadata : DatasetMetadata
}