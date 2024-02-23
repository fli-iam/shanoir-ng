import {DatasetParameterDTO} from "./dataset-parameter.dto";

export class ExecutionCandidateDto {

    identifier: string;
    name: string;
    pipelineIdentifier: string;
    inputParameters: { [key: string]: any; };
    datasetParameters: DatasetParameterDTO[];
    studyIdentifier: number;
    outputProcessing: string;
    processingType: string;
    refreshToken: string;
    client: string;

}
