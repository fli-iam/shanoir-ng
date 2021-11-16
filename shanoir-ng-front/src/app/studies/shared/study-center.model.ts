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
import { Center } from '../../centers/shared/center.model';
import { IdName } from '../../shared/models/id-name.model';
import { Id } from '../../shared/models/id.model';
import { Study } from './study.model';


export class StudyCenter {
    center: Center;
    id: number;
    study: Study;
}

export class StudyCenterDTO {
    
    id: number;
    center: IdName;
    study: IdName;
   
    constructor(studyCenter: StudyCenter) {
        this.id = studyCenter.id;
        this.center = studyCenter.center ? new IdName(studyCenter.center.id, studyCenter.center.name) : null;
        this.study = studyCenter.study ? new IdName(studyCenter.study.id, studyCenter.study.name) : null;
    }
}