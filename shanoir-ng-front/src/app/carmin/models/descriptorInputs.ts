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

export interface DescriptorInputs { 
    /**
     * A short, unique, informative identifier containing only alphanumeric characters and underscores. Typically used to generate variable names. Example: \"data_file\".
     */
    id: string;
    /**
     * A human-readable input name. Example: 'Data file'.
     */
    name: string;
    /**
     * Input type.
     */
    type: DescriptorInputs.TypeEnum;
    /**
     * Input description.
     */
    description?: string;
    /**
     * A string contained in command-line, substituted by the input value and/or flag at runtime.
     */
    valueKey?: string;
    /**
     * True if input is a list of value. An input of type \"Flag\" cannot be a list.
     */
    list?: boolean;
    /**
     * True if input is optional.
     */
    optional?: boolean;
    /**
     * Option flag of the input, involved in the value-key substitution. Inputs of type \"Flag\" have to have a command-line flag. Examples: -v, --force.
     */
    commandLineFlag?: string;
    /**
     * Ids of the inputs which must be active for this input to be available.
     */
    requiresInputs?: Array<string>;
    /**
     * Ids of the inputs that are disabled when this input is active.
     */
    disablesInputs?: Array<string>;
    /**
     * Separator used between flags and their arguments. Defaults to a single space.
     */
    commandLineFlagSeparator?: string;
    /**
     * Default value of the input, used by the tool when no option is specified.
     */
    defaultValue?: any;
    /**
     * Permitted choices for input value. May not be used with the Flag type.
     */
    valueChoices?: Array<string | number>;
    /**
     * Specify whether the input should be an integer. May only be used with Number type inputs.
     */
    integer?: boolean;
    /**
     * Specify the minimum value of the input (inclusive). May only be used with Number type inputs.
     */
    minimum?: number;
    /**
     * Specify the maximum value of the input (inclusive). May only be used with Number type inputs.
     */
    maximum?: number;
    /**
     * Specify whether the minimum is exclusive or not. May only be used with Number type inputs.
     */
    exclusiveMinimum?: boolean;
    /**
     * Specify whether the maximum is exclusive or not. May only be used with Number type inputs.
     */
    exclusiveMaximum?: boolean;
    /**
     * Specify the minimum number of entries in the list. May only be used with List type inputs.
     */
    minListEntries?: number;
    /**
     * Specify the maximum number of entries in the list. May only be used with List type inputs.
     */
    maxListEntries?: number;
    /**
     * Specifies that this input must be given as an absolute path. Only specifiable for File type inputs.
     */
    usesAbsolutePath?: boolean;
}
export namespace DescriptorInputs {
    export type TypeEnum = 'String' | 'File' | 'Flag' | 'Number';
    export const TypeEnum = {
        String: 'String' as TypeEnum,
        File: 'File' as TypeEnum,
        Flag: 'Flag' as TypeEnum,
        Number: 'Number' as TypeEnum
    };
}