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
import { Subject } from 'src/app/subjects/shared/subject.model';
import { SubjectDTO } from 'src/app/subjects/shared/subject.dto';

import { Entity } from "../../../shared/components/entity/entity.abstract";
import { ImagedObjectCategory } from "../../../subjects/shared/imaged-object-category.enum";
import { Sex } from "../../../subjects/shared/subject.types";
import { SubjectPathology } from '../../pathologies/subjectPathology/shared/subjectPathology.model';
import { SubjectTherapy } from '../../therapies/subjectTherapy/shared/subjectTherapy.model';
import { Reference } from '../../reference/shared/reference.model';

export class AnimalSubject extends Entity {

    @Field() id: number;
    @Field() name: string;
    @Field() imagedObjectCategory: ImagedObjectCategory;
    @Field() sex: Sex;
    @Field() studyId: number;
    @Field() specie: Reference;
    @Field() strain: Reference;
    @Field() biotype: Reference;
    @Field() provider : Reference;
    @Field() stabulation: Reference;
    @Field() subjectPathologies: SubjectPathology[];
	@Field() subjectTherapies: SubjectTherapy[];
    @Field() subject: Subject;
}


export class AnimalSubjectDTO {

    constructor(animalSubject: AnimalSubject) {
        this.id = animalSubject.id;
        this.name = animalSubject.name;
        this.imagedObjectCategory = animalSubject.imagedObjectCategory;
        this.sex = animalSubject.sex;
        this.studyId = animalSubject.studyId;
        this.specie = animalSubject.specie;
        this.strain = animalSubject.strain;
        this.biotype = animalSubject.biotype;
        this.provider = animalSubject.provider;
        this.stabulation = animalSubject.stabulation;
        this.subjectPathologies = animalSubject.subjectPathologies;
        this.subjectTherapies = animalSubject.subjectTherapies;
        this.subject = new SubjectDTO(animalSubject.subject);
    }

    id: number;
    name: string;
    imagedObjectCategory: ImagedObjectCategory;
    sex: Sex;
    studyId: number;
    specie: Reference;
    strain: Reference;
    biotype: Reference;
    provider : Reference;
    stabulation: Reference;
    subjectPathologies: SubjectPathology[];
	subjectTherapies: SubjectTherapy[];
    subject: SubjectDTO;
}