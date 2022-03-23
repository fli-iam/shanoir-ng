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

export interface DescriptorSuggestedresources { 
    /**
     * The requested number of cpu cores to run the described application
     */
    cpuCores?: number;
    /**
     * The requested number of GB RAM to run the described application
     */
    ram?: number;
    /**
     * The requested number of GB of storage to run the described application
     */
    diskSpace?: number;
    /**
     * The requested number of nodes to spread the described application across
     */
    nodes?: number;
    /**
     * Estimated wall time of a task in seconds.
     */
    walltimeEstimate?: number;
}