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
import { ParameterType } from './parameterType';

export interface PipelineParameter { 
    name: string;
    type: ParameterType;
    isOptional: boolean;
    isReturnedValue: boolean;
    /**
     * Default value. It must be consistent with the parameter type.
     */
    defaultValue?: any; // @alaeessaki not described in swagger in java client, it's described as an Object, but i think to avoid errors for now i'll make it any. TODO specify the tyoe.
    description?: string;
}