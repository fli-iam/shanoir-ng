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
import { Id } from '../../shared/models/id.model';
import { User } from '../../users/shared/user.model';
import { StudyUserRight } from './study-user-right.enum';
import { Study } from './study.model';


export class StudyUser {
    id: number;
    study: Study;
    userId: number;
    studyId: number;
    receiveStudyUserReport: boolean;
    receiveNewImportReport: boolean;
    studyUserRights: StudyUserRight[];
    userName: string;
    user: User;
    confirmed: boolean = false;
    centers: Center[];

    public completeMember(users: User[]) {
        StudyUser.completeMember(this, users);
    }

    public static completeMember(studyUser: StudyUser, users: User[]) {
        let user: User = users.find(u => u.id == studyUser.userId);
        if (user) studyUser.user = user;
    }
}

export class StudyUserDTO {
    id: number;
    study: Id;
    userId: number;
    receiveStudyUserReport: boolean;
    receiveNewImportReport: boolean;
    studyUserRights: StudyUserRight[];
    userName: string;
    user: User;
    confirmed: boolean = false;
    centerIds: number[];

    constructor(studyUser: StudyUser) {
        this.id = studyUser.id;
        this.study = new Id(studyUser.study?.id);
        this.userId = studyUser.userId;
        this.receiveStudyUserReport = studyUser.receiveStudyUserReport;
        this.receiveNewImportReport = studyUser.receiveNewImportReport;
        this.studyUserRights = studyUser.studyUserRights;
        this.userName = studyUser.userName;
        this.user = studyUser.user;
        this.confirmed = studyUser.confirmed;
        this.centerIds = studyUser.centers?.map(center => center.id);
    }
}