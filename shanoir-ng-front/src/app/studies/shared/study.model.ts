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
import { Entity } from '../../shared/components/entity/entity.abstract';
import { IdName } from '../../shared/models/id-name.model';
import { StudyCard } from '../../study-cards/shared/study-card.model';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { Tag } from '../../tags/tag.model';
import { User } from '../../users/shared/user.model';
import { StudyCenter } from './study-center.model';
import { StudyType } from './study-type.enum';
import { StudyUser } from './study-user.model';
import { Timepoint } from './timepoint.model';
import {Profile} from '../../shared/models/profile.model';
import {StudyStorageVolumeDTO} from "./study.dto";
import { Field } from 'src/app/shared/reflect/field.decorator';
export class Study extends Entity {
    @Field() clinical: boolean;
    @Field() downloadableByDefault: boolean;
    @Field() endDate: Date;
    @Field() experimentalGroupsOfSubjects: IdName[];
    @Field() id: number;
    @Field() challenge: boolean;
    @Field() name: string;
    @Field() nbExaminations: number;
    @Field() nbSubjects: number;
    @Field() nbMembers: number;
    @Field() protocolFilePaths: string[];
    @Field() dataUserAgreementPaths: string[];
    @Field() startDate: Date;
    @Field() studyCenterList: StudyCenter[] = [];
    @Field() studyStatus: 'IN_PROGRESS' | 'FINISHED' = 'IN_PROGRESS';
    @Field() profile: Profile;
    @Field() detailedSizes: Map<String, number> = null;
    totalSize: number;
    @Field() studyType: StudyType;
    @Field() subjectStudyList: SubjectStudy[] = [];
    @Field() studyUserList: StudyUser[] = [];
    @Field() timepoints: Timepoint[];
    @Field() visibleByDefault: boolean = false;
    @Field() withExamination: boolean;
    @Field() studyCardPolicy: string = "MANDATORY";
    @Field() studyCardList: StudyCard[];
    @Field() tags: Tag[];
    @Field() studyTags: Tag[];
    @Field() description: string;
    @Field() license: string;
    accessRequestedByCurrentUser: boolean = false;
    locked: boolean = false; // current user has no access

    private completeMembers(users: User[]) {
        return Study.completeMembers(this, users);
    }

    public static completeMembers(study: Study, users: User[]) {
        if (!study.studyUserList) return;
        for (let studyUser of study.studyUserList) {
            StudyUser.completeMember(studyUser, users);
        }
    }
}

export class SimpleStudy {
    id: number;
    name: string;
    tags: Tag[];
    studyTags: Tag[];
    studyUserList: StudyUser[];

    constructor(study: Study) {
        this.id = study.id ? study.id : null;
        this.name = study.name;
        this.tags = study.tags;
        this.studyTags = study.studyTags;
        this.studyUserList = study.studyUserList;
    }
}
