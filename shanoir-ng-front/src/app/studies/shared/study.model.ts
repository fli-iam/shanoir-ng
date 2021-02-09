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
import { Id } from '../../shared/models/id.model';
import { StudyCard } from '../../study-cards/shared/study-card.model';
import { SubjectStudy, SubjectStudyDTO } from '../../subjects/shared/subject-study.model';
import { User } from '../../users/shared/user.model';
import { StudyCenter, StudyCenterDTO } from './study-center.model';
import { StudyType } from './study-type.enum';
import { StudyUser } from './study-user.model';
import { Timepoint } from './timepoint.model';


export class Study extends Entity {
    clinical: boolean;
    compatible: boolean = false;
    downloadableByDefault: boolean;
    endDate: Date;
    experimentalGroupsOfSubjects: IdName[];
    id: number;
    monoCenter: boolean;
    name: string;
    nbExaminations: number;
    nbSujects: number;
    protocolFilePaths: string[];
    startDate: Date;
    studyCenterList: StudyCenter[] = [];
    studyStatus: 'IN_PROGRESS' | 'FINISHED'  = 'IN_PROGRESS';
    studyType: StudyType;
    subjectStudyList: SubjectStudy[] = [];
    studyUserList: StudyUser[] = [];
    timepoints: Timepoint[];
    visibleByDefault: boolean;
    withExamination: boolean;
    studyCardList: StudyCard[];
    consentFormPaths: string[];

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


export class StudyDTO {
    id: number;
    clinical: boolean;
    downloadableByDefault: boolean;
    endDate: Date;
    //examinationIds: number[];
    experimentalGroupsOfSubjects: Id[];
    monoCenter: boolean;
    name: string;
    protocolFilePaths: string[];
    startDate: Date;
    studyCenterList: StudyCenterDTO[];
    studyStatus: 'IN_PROGRESS' | 'FINISHED';
    studyType: StudyType;
    studyUserList: StudyUser[];
    subjectStudyList: SubjectStudyDTO[] = [];
    //timepoints: Timepoint[];
    visibleByDefault: boolean;
    withExamination: boolean;
    consentFormPaths: string[];

    constructor(study: Study) {
        this.id = study.id ? study.id : null;
        this.clinical = study.clinical;
        this.downloadableByDefault = study.downloadableByDefault;
        this.endDate = study.endDate;
        this.experimentalGroupsOfSubjects = study.experimentalGroupsOfSubjects ? study.experimentalGroupsOfSubjects.map(egos => new Id(egos.id)) : null;
        this.monoCenter = study.monoCenter;
        this.name = study.name;
        this.protocolFilePaths = study.protocolFilePaths;
        this.startDate = study.startDate;
        this.studyCenterList = study.studyCenterList ? study.studyCenterList.map(sc => {
            let dto = new StudyCenterDTO(sc);
            dto.study = null;
            return dto;
        }) : null;
        this.studyStatus = study.studyStatus;
        this.studyType = study.studyType;
        this.studyUserList = study.studyUserList;
        this.subjectStudyList = study.subjectStudyList ? study.subjectStudyList.map(ss => {
            let dto = new SubjectStudyDTO(ss);
            dto.study = null;
            return dto;
        }) : null;
        this.visibleByDefault = study.visibleByDefault;
        this.withExamination = study.withExamination;
        this.consentFormPaths = study.consentFormPaths;
    }
}