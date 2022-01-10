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

export interface DescriptorGroups { 
    /**
     * A short, unique, informative identifier containing only alphanumeric characters and underscores. Typically used to generate variable names. Example: \"outfile_group\".
     */
    id: string;
    /**
     * A human-readable name for the input group.
     */
    name: string;
    /**
     * Description of the input group.
     */
    description?: string;
    /**
     * IDs of the inputs belonging to this group.
     */
    members: Array<string>;
    /**
     * True if only one input in the group may be active at runtime.
     */
    mutuallyExclusive?: boolean;
    /**
     * True if at least one of the inputs in the group must be active at runtime.
     */
    oneIsRequired?: boolean;
    /**
     * True if members of the group need to be toggled together
     */
    allOrNone?: boolean;
}