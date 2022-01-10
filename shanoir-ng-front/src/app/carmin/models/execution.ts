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

export class Execution { 
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
     * Absent when not available (e.g. execution still running). Empty if no returned file is produced. Each key/value of the \"returnedFiles\" object corresponds to an output pipeline parameter (with \"isReturnedValue\" set to true) and the key must be the parameter name. The value must be an array of valid URL strings. A value array can be empty if the output parameter produces no value. It can have several URLs entries when the output is a directory with several files, a big file split in several download links, several files or any other implementation specific design.
     */
    readonly returnedFiles?: { [key: string]: Array<string>; };
    studyIdentifier?: string;
    readonly errorCode?: number;
    /**
     * Time in seconds since Unix epoch
     */
    readonly startDate?: number;
    /**
     * Time in seconds since Unix epoch
     */
    readonly endDate?: number;
}
export type StatusEnum = 'Initializing' | 'Ready' | 'Running' | 'Finished' | 'InitializationFailed' | 'ExecutionFailed' | 'Unknown' | 'Killed';

// export namespace Execution {
//     export const StatusEnum = {
//         Initializing: 'Initializing' as StatusEnum,
//         Ready: 'Ready' as StatusEnum,
//         Running: 'Running' as StatusEnum,
//         Finished: 'Finished' as StatusEnum,
//         InitializationFailed: 'InitializationFailed' as StatusEnum,
//         ExecutionFailed: 'ExecutionFailed' as StatusEnum,
//         Unknown: 'Unknown' as StatusEnum,
//         Killed: 'Killed' as StatusEnum
//     };
// }