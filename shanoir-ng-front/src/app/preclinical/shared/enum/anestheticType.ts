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

import { allOfEnum, capitalsAndUnderscoresToDisplayable } from "../../../utils/app.utils";
import { Option } from "../../../shared/select/select.component";

export enum AnestheticType {
    GAS = "GAS",
    INJECTION = "INJECTION"

} export namespace AnestheticType {
    
    export function all(): Array<AnestheticType> {
        return allOfEnum<AnestheticType>(AnestheticType);
    }

    export function getLabel(type: AnestheticType): string {
        return capitalsAndUnderscoresToDisplayable(type);
    }
    
    export var options: Option<AnestheticType>[] = all().map(prop => new Option<AnestheticType>(prop, getLabel(prop)));
}