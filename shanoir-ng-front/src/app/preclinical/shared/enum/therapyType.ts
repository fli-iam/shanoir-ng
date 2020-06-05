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

import { Option } from "../../../shared/select/select.component";
import { allOfEnum, capitalsAndUnderscoresToDisplayable } from "../../../utils/app.utils";

export enum TherapyType {
    DRUG = "DRUG",
    RADIATION = "RADIATION",
    SURGERY = "SURGERY",
    ULTRASOUND = "ULTRASOUND"

} export namespace TherapyType {
    
    export function all(): Array<TherapyType> {
        return allOfEnum<TherapyType>(TherapyType);
    }

    export function getLabel(type: TherapyType): string {
        return capitalsAndUnderscoresToDisplayable(type);
    }

    export function toOptions(): Option<TherapyType>[] {
        return all().map(prop => new Option<TherapyType>(prop, getLabel(prop)));
    }
}