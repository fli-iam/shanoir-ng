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

import { Examination } from '../../examinations/shared/examination.model';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { ServiceLocator } from '../../utils/locator.service';
import { ImagedObjectCategory } from './imaged-object-category.enum';
import { SubjectStudy } from './subject-study.model';
import { SubjectService } from './subject.service';
import { Sex } from './subject.types';

export class Subject extends Entity {

    id: number;
    examinations: Examination[];
    name: string;
    identifier: string;
    birthDate: Date;
    preclinical: boolean;
    languageHemisphericDominance: "Left" | "Right";
    manualHemisphericDominance: "Left" | "Right";
    imagedObjectCategory: ImagedObjectCategory;
    sex: Sex;
    selected: boolean = false;
    subjectStudyList: SubjectStudy[] = [];

    public static makeSubject(id: number, name: string, identifier: string, subjectStudy: SubjectStudy): Subject {
        let subject = new Subject();
        subject.id = id;
        subject.name = name;
        subject.identifier = identifier;
        subject.subjectStudyList = [subjectStudy];
        return subject;
    }

    service = ServiceLocator.injector.get(SubjectService);
}
