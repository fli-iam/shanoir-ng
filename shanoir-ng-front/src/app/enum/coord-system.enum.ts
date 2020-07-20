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

import { allOfEnum, capitalsAndUnderscoresToDisplayable } from '../utils/app.utils';
import { Option } from '../shared/select/select.component';

export enum CoordSystems {

    ACPC = "ACPC",
    ALLEN= "ALLEN",     
    ANALYZE= "ANALYZE",  
    BTI_4D= "BTI_4D",
    CTF_MRI= "CTF MRI", 
    CTF_GRADIOMETER = "CTF_GRADIOMETER",
    CAPTRAK= "CAPTRAK",
    CHIETI= "CHIETI",
    DICOM= "DICOM",
    FREESURFER= "FREESURFER",  
    MNI= "MNI",
    NIFTI= "NIFTI",
    NEUROMAG_ELEKTA= "NEUROMAG_ELEKTA",
    PAXINOS_FRANKLIN= "PAXINOS_FRANKLIN",
    TALAIRACH_TOURNOUX= "TALAIRACH_TOURNOUX", 
    YOKOGAWA= "YOKOGAWA"

} export namespace CoordSystems {
    
    export function all(): Array<CoordSystems> {
        return allOfEnum<CoordSystems>(CoordSystems);
    }

    export function getLabel(type: CoordSystems): string {
        return capitalsAndUnderscoresToDisplayable(type);
    }

    export function toOptions(): Option<CoordSystems>[] {
        return all().map(prop => new Option<CoordSystems>(prop, getLabel(prop)));
    }
}  