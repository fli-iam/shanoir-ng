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

export interface DescriptorOutputfiles { 
    /**
     * A short, unique, informative identifier containing only alphanumeric characters and underscores. Typically used to generate variable names. Example: \"data_file\"
     */
    id: string;
    /**
     * A human-readable output name. Example: 'Data file'
     */
    name: string;
    /**
     * Output description.
     */
    description?: string;
    /**
     * A string contained in command-line, substituted by the output value and/or flag at runtime.
     */
    valueKey?: string;
    /**
     * Describes the output file path relatively to the execution directory. May contain input value keys. Example: \"results/[INPUT1]_brain.mnc\".
     */
    pathTemplate: string;
    /**
     * List of file extensions that will be stripped from the input values before being substituted in the path template. Example: [\".nii\",\".nii.gz\"].
     */
    pathTemplateStrippedExtensions?: Array<string>;
    /**
     * True if output is a list of value.
     */
    list?: boolean;
    /**
     * True if output may not be produced by the tool.
     */
    optional?: boolean;
    /**
     * Option flag of the output, involved in the value-key substitution. Examples: -o, --output
     */
    commandLineFlag?: string;
    /**
     * Separator used between flags and their arguments. Defaults to a single space.
     */
    commandLineFlagSeparator?: string;
    /**
     * Specifies that this output filepath will be given as an absolute path.
     */
    usesAbsolutePath?: boolean;
    /**
     * An array of strings that may contain value keys. Each item will be a line in the configuration file.
     */
    fileTemplate?: Array<string>;
}