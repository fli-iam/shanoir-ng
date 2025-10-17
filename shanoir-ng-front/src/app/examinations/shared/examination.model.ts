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
import { Entity } from '../../shared/components/entity/entity.abstract';
import { IdName } from '../../shared/models/id-name.model';
import { Study } from '../../studies/shared/study.model';
import { Subject } from '../../subjects/shared/subject.model';
import { InstrumentBasedAssessment } from '../instrument-assessment/instrument.model';
import { UnitOfMeasure } from "../../enum/unitofmeasure.enum";
import { Field } from '../../shared/reflect/field.decorator';


export class Examination extends Entity {
    @Field() id: number;
    @Field() examinationDate: Date;
    @Field() subject: IdName | Subject;
    @Field() study: IdName | Study;
    @Field() center: IdName | Center;
    @Field() examinationExecutive: IdName;
    @Field() comment: string;
    @Field() note: string;
    @Field() subjectWeight: number;
    @Field() weightUnitOfMeasure: UnitOfMeasure;
    @Field() instrumentBasedAssessmentList: InstrumentBasedAssessment[];
    @Field() extraDataFilePathList: string[] = [];
    @Field() preclinical: boolean;
    @Field() hasStudyCenterData: boolean = false;
    @Field() copies: number[];
    @Field() source: number;
}
