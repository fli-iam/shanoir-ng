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
import { Field } from 'src/app/shared/reflect/field.decorator';
import { Examination } from '../../examinations/shared/examination.model';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { ImagedObjectCategory } from './imaged-object-category.enum';
import { SubjectStudy } from './subject-study.model';
import { Sex } from './subject.types';


export class Subject extends Entity {

    @Field() id: number;
    @Field() examinations: Examination[];
    @Field() name: string;
    @Field() identifier: string;
    @Field() birthDate: Date;
    @Field() preclinical: boolean;
    @Field() languageHemisphericDominance: "Left" | "Right";
    @Field() manualHemisphericDominance: "Left" | "Right";
    @Field() imagedObjectCategory: ImagedObjectCategory;
    @Field() sex: Sex;
    @Field() selected: boolean = false;
    @Field() subjectStudyList: SubjectStudy[] = [];
    @Field() isAlreadyAnonymized: boolean = false;

    public static makeSubject(id: number, name: string, identifier: string, subjectStudy: SubjectStudy): Subject {
        let subject = new Subject();
        subject.id = id;
        subject.name = name;
        subject.identifier = identifier;
        subject.subjectStudyList = [subjectStudy];
        return subject;
    }
}

export interface SimpleSubject {
    id: number;
    name: string;
    identifier: string;
    subjectStudyList: SubjectStudy[];
}
