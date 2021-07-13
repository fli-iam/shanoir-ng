/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
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

export class Range {
    constructor(
        public lowerBound: number, 
        public upperBound: number
    ) {}

    hasBound(): boolean {
        return this.hasLowerBound() || this.hasUpperBound();
    }
    
    hasLowerBound(): boolean {
        return this.lowerBound != undefined && this.lowerBound != null;
    }

    hasUpperBound(): boolean {
        return this.upperBound != undefined && this.upperBound != null;
    }
}