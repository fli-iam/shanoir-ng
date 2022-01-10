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
import { DescriptorEnvironmentvariables } from './descriptorEnvironmentvariables';
import { DescriptorErrorcodes } from './descriptorErrorcodes';
import { DescriptorGroups } from './descriptorGroups';
import { DescriptorInputs } from './descriptorInputs';
import { DescriptorOutputfiles } from './descriptorOutputfiles';
import { DescriptorSuggestedresources } from './descriptorSuggestedresources';
import { DescriptorTests } from './descriptorTests';

export interface Descriptor { 
    /**
     * Tool name.
     */
    name: string;
    /**
     * Tool version.
     */
    toolVersion: string;
    /**
     * Tool description.
     */
    description: string;
    /**
     * A string that describes the tool command line, where input and output values are identified by \"keys\". At runtime, command-line keys are substituted with flags and values.
     */
    commandLine: string;
    containerImage?: any;
    /**
     * Version of the schema used.
     */
    schemaVersion: Descriptor.SchemaVersionEnum;
    /**
     * An array of key-value pairs specifying environment variable names and their values to be used in the execution environment.
     */
    environmentVariables?: Array<DescriptorEnvironmentvariables>;
    /**
     * Sets of identifiers of inputs, each specifying an input group.
     */
    groups?: Array<DescriptorGroups>;
    inputs: Array<DescriptorInputs>;
    tests?: Array<DescriptorTests>;
    outputFiles: Array<DescriptorOutputfiles>;
    invocationSchema?: any;
    suggestedResources?: DescriptorSuggestedresources;
    /**
     * An set of key-value pairs specifying tags describing the pipeline. The tag names are open, they might be more constrained in the future.
     */
    tags?: { [key: string]: string; };
    /**
     * An array of key-value pairs specifying exit codes and their description. Can be used for tools to specify the meaning of particular exit codes. Exit code 0 is assumed to indicate a successful execution.
     */
    errorCodes?: Array<DescriptorErrorcodes>;
    custom?: any;
}
export namespace Descriptor {
    export type SchemaVersionEnum = '0.5';
    export const SchemaVersionEnum = {
        _05: '0.5' as SchemaVersionEnum
    };
}