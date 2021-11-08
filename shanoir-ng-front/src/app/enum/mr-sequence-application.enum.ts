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

export enum MrSequenceApplication {

	CALIBRATION = 'CALIBRATION',
	MORPHOMETRY = 'MORPHOMETRY',
	ANGIOGRAPHY = 'ANGIOGRAPHY',
	CONTRAST_AGENT_ANGIO = 'CONTRAST_AGENT_ANGIO',
	TIME_OF_FLIGHT_ANGIO = 'TIME_OF_FLIGHT_ANGIO',
	VELOCITY_ENCODED_ANGIO = 'VELOCITY_ENCODED_ANGIO',
	PERFUSION = 'PERFUSION',
	DIFFUSION = 'DIFFUSION',
	BOLD = 'BOLD',
	SPECTROSCOPY = 'SPECTROSCOPY',
	H1_SINGLE_VOXEL_SPECTROSCOPY = 'H1_SINGLE_VOXEL_SPECTROSCOPY',
	H1_CHEMICAL_SHIFT_IMAGING_SPECTROSCOPY = 'H1_CHEMICAL_SHIFT_IMAGING_SPECTROSCOPY'

} export namespace MrSequenceApplication {
    
    export function all(): Array<MrSequenceApplication> {
        return allOfEnum<MrSequenceApplication>(MrSequenceApplication);
    }

    export function getLabel(type: MrSequenceApplication): string {
        return capitalsAndUnderscoresToDisplayable(type);
    }

    export var options: Option<MrSequenceApplication>[] = all().map(prop => new Option<MrSequenceApplication>(prop, getLabel(prop)));
}