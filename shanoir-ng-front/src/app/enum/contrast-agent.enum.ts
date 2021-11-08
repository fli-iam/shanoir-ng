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

import { Option } from "../shared/select/select.component";
import { capitalsAndUnderscoresToDisplayable, allOfEnum } from "../utils/app.utils";

export enum ContrastAgent {

	GADOLINIUM = 'GADOLINIUM',
	USPIO = 'USPIO',
	NONE = 'NONE',
	MULTIHANCE = 'MULTIHANCE',
	PROHANCE = 'PROHANCE',
	GADOVIST = 'GADOVIST'

} export namespace ContrastAgent {
    
    export function all(): Array<ContrastAgent> {
        return allOfEnum<ContrastAgent>(ContrastAgent);
    }

    export function getLabel(type: ContrastAgent): string {
        return capitalsAndUnderscoresToDisplayable(type);
    }

    export var options: Option<ContrastAgent>[] = all().map(prop => new Option<ContrastAgent>(prop, getLabel(prop)));
}