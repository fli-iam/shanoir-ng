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
export interface DescriptorEnvironmentvariables { 
    /**
     * The environment variable name (identifier) containing only alphanumeric characters and underscores. Example: \"PROGRAM_PATH\".
     */
    name: string;
    /**
     * The value of the environment variable.
     */
    value: string;
    /**
     * Description of the environment variable.
     */
    description?: string;
}