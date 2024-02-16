import {DatasetParameterDTO} from "./dataset-parameter.dto";

export class ExecutionCandidate {

    identifier: string;
    name: string;
    pipelineIdentifier: string;
    inputParameters: { [key: string]: any; };
    parametersResources: DatasetParameterDTO[];
    studyIdentifier: number;
    outputProcessing: string;
    processingType: string;
    refreshToken: string;
    client: string;

}
