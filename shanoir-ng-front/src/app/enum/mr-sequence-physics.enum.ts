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

export enum MrSequencePhysics {

	SINGLE_ECHO_SPIN_ECHO_SEQUENCE = 'SINGLE_ECHO_SPIN_ECHO_SEQUENCE',
	MULTI_ECHO_SPIN_ECHO_SEQUENCE = 'MULTI_ECHO_SPIN_ECHO_SEQUENCE',
	STANDARD_SINGLE_ECHO_SPIN_ECHO_SEQUENCE = 'STANDARD_SINGLE_ECHO_SPIN_ECHO_SEQUENCE',
	INVERSION_RECOVERY_SINGLE_ECHO_SPIN_ECHO_SEQUENCE = 'INVERSION_RECOVERY_SINGLE_ECHO_SPIN_ECHO_SEQUENCE',
	SPIN_ECHO_ECHO_PLANAR_IMAGING = 'SPIN_ECHO_ECHO_PLANAR_IMAGING',
	SINGLE_SHOT_SPIN_ECHO_SEQUENCE = 'SINGLE_SHOT_SPIN_ECHO_SEQUENCE',
	SEGMENTED_SPIN_ECHO_SEQUENCE = 'SEGMENTED_SPIN_ECHO_SEQUENCE',
	STANDARD_SINGLE_SHOT_SPIN_ECHO_SEQUENCE = 'STANDARD_SINGLE_SHOT_SPIN_ECHO_SEQUENCE',
	INVERSION_RECOVERY_SINGLE_SHOT_SPIN_ECHO_SEQUENCE = 'INVERSION_RECOVERY_SINGLE_SHOT_SPIN_ECHO_SEQUENCE',
	STANDARD_SEGMENTED_SPIN_ECHO_SEQUENCE = 'STANDARD_SEGMENTED_SPIN_ECHO_SEQUENCE',
	INVERSION_RECOVERY_SEGMENTED_SPIN_ECHO_SEQUENCE = 'INVERSION_RECOVERY_SEGMENTED_SPIN_ECHO_SEQUENCE',
	SINGLE_ECHO_GRADIENT_ECHO_SEQUENCE = 'SINGLE_ECHO_GRADIENT_ECHO_SEQUENCE',
	MULTI_ECHO_GRADIENT_ECHO_SEQUENCE = 'MULTI_ECHO_GRADIENT_ECHO_SEQUENCE',
	MAGNETIZATION_PREPARED_GRE = 'MAGNETIZATION_PREPARED_GRE',
	SPOILED_GRE = 'SPOILED_GRE',
	REFOCUSED_GRE = 'REFOCUSED_GRE',
	MAGNETIZATION_PREPARED_SPOILED_GRE = 'MAGNETIZATION_PREPARED_SPOILED_GRE',
	STEADY_STATE_GRE_FID = 'STEADY_STATE_GRE_FID',
	STEADY_STATE_GRE_SE = 'STEADY_STATE_GRE_SE',
	STEADY_STATE_FID_SE = 'STEADY_STATE_FID_SE',
	SINGLE_SHOT_GRE_EPI = 'SINGLE_SHOT_GRE_EPI',
	SEGMENTED_GRE_EPI = 'SEGMENTED_GRE_EPI',
	STANDARD_SEGMENTED_GRE_EPI = 'STANDARD_SEGMENTED_GRE_EPI',
	MAGNETIZATION_PREPARED_SEGMENTED_GRE_EPI = 'MAGNETIZATION_PREPARED_SEGMENTED_GRE_EPI',
	STANDARD_SINGLE_SHOT_GRE_EPI = 'STANDARD_SINGLE_SHOT_GRE_EPI',
	MAGNETIZATION_PREPARED_SINGLE_SHOT_GRE_EPI = 'MAGNETIZATION_PREPARED_SINGLE_SHOT_GRE_EPI',
	HYBRID_GRADIENT_ECHO_AND_SPIN_ECHO_SEQUENCE = 'HYBRID_GRADIENT_ECHO_AND_SPIN_ECHO_SEQUENCE'

} export namespace MrSequencePhysics {
    
    export function all(): Array<MrSequencePhysics> {
        return allOfEnum<MrSequencePhysics>(MrSequencePhysics);
    }

    export function getLabel(type: MrSequencePhysics): string {
		return capitalsAndUnderscoresToDisplayable(type)
			.replace('gre', 'GRE')
			.replace('epi', 'EPI');
    }

    export function toOptions(): Option<MrSequencePhysics>[] {
        return all().map(prop => new Option<MrSequencePhysics>(prop, getLabel(prop)));
    }
}