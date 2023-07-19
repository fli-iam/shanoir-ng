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

export class Study extends Entity {
    clinical: boolean;
    downloadableByDefault: boolean;
    endDate: Date;
    experimentalGroupsOfSubjects: IdName[];
    id: number;
    challenge: boolean;
    monoCenter: boolean;
    name: string;
    nbExaminations: number;
    nbSubjects: number;
    nbSujects: number;
    nbMembers: number;
    protocolFilePaths: string[];
    dataUserAgreementPaths: string[];
    startDate: Date;
    studyCenterList: StudyCenter[] = [];
    studyStatus: 'IN_PROGRESS' | 'FINISHED'  = 'IN_PROGRESS';
    profile: Profile;
    size: number = null;
    studyType: StudyType;
    subjectStudyList: SubjectStudy[] = [];
    studyUserList: StudyUser[] = [];
    timepoints: Timepoint[];
    visibleByDefault: boolean = false;
    withExamination: boolean;
    studyCardList: StudyCard[];
    tags: Tag[];
    studyTags: Tag[];
    description: string;

    license: string;
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
