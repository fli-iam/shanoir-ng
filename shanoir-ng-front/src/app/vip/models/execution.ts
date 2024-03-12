/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2022 Inria - https://www.inria.fr/
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
import {DatasetProcessingType} from "../../enum/dataset-processing-type.enum";

export class Execution {

    /* local ID for angular, remove it before creatin it. */
    id: number;

    /**
     * execution id. Must always be present in responses.
     */
    readonly identifier?: string;
    /**
     * execution name
     */
    name: string;
    /**
     * which pipeline that started this execution
     */
    pipelineIdentifier: string;
    /**
     * The timeout in seconds until which the execution is killed and deleted with all its files. 0 or absence means no timeout
     */
    timeout?: number;
    /**
     * The status of the execution. Must always be present in responses.
     */
    readonly status?: StatusEnum;
    /**
     * Represents the input as a key/value object. The types should respect the parameters of the pipeline used for the execution.
     */
    inputValues: { [key: string]: any; };
    /**
     * TODO : parameterResources
     */
    /**
     * Study Id
     */
    studyIdentifier?: number;
    /**
     * Time in seconds since Unix epoch
     */
    readonly startDate?: number;
    /**
     * Time in seconds since Unix epoch
     */
    readonly endDate?: number;
    /**
     * The result location
     */
    resultsLocation: string;

    outputProcessing: string;

    processingType: DatasetProcessingType;
    /**
     * Executable location
     */
     executable: string;
}
export type StatusEnum = 'Initializing' | 'Ready' | 'Running' | 'Finished' | 'InitializationFailed' | 'ExecutionFailed' | 'Unknown' | 'Killed';
