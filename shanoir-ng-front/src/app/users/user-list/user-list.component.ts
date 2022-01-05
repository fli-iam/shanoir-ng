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

import { Component, ViewChild } from '@angular/core';
import { BrowserPaginEntityListComponent } from '../../shared/components/entity/entity-list.browser.component.abstract';
import { TableComponent } from '../../shared/components/table/table.component';
import { User } from '../shared/user.model';
import { UserService } from '../shared/user.service';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { StudyService } from '../../studies/shared/study.service';
import { StudyUser } from '../../studies/shared/study-user.model';


@Component({
    selector: 'user-list',
    templateUrl: 'user-list.component.html',
    styleUrls: ['user-list.component.css']
})

export class UserListComponent extends BrowserPaginEntityListComponent<User>{
    @ViewChild('userTable') table: TableComponent;

    constructor(private userService: UserService, private studyService: StudyService) {
           super('user');
    }
    
    getService(): EntityService<User> {
        return this.userService;
    }

    getOptions() {
        return {
            new: true,
            view: true, 
            edit: true, 
            delete: true
        };
    }

    getEntities(): Promise<User[]> {
        let userPromise = this.userService.getAll();
        // get the study-users
        Promise.all([userPromise, this.studyService.getAll()]).then(([users, studies]) => {
            users.forEach(user => {
                user.studyUserList = [];
                studies.forEach(study => Array.prototype.push.apply(
                    user.studyUserList, 
                    study.studyUserList
                        .filter(studyUser => (studyUser.user ? studyUser.user.id : studyUser.userId) == user.id)
                        .map(studyUser => {
                            studyUser.study = study;
                            return studyUser;
                        })
                ));
            })
        });
        return userPromise;
    }

    // Grid columns definition
    getColumnDefs(): any[] {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        let columnDefs = [
            {headerName: "Username", field: "username" },
            {headerName: "First Name", field: "firstName" },
            {headerName: "Last Name", field: "lastName" },
            {headerName: "Email", field: "email", width: "200%"},
            {headerName: "Studies", field: "studyUserList", cellRenderer: function (params: any) {
                if (params.data.studyUserList) {
                    return (params.data.studyUserList as StudyUser[]).map(su => su.study?.name).join(' - ');
                } else {
                    return '';
                }
            }},
            {headerName: "O.D.", tip: "On Demand", field: "onDemand", type: "boolean", defaultSortCol: true, defaultAsc: false, cellRenderer: function (params: any) {
                return params.data.accountRequestDemand || params.data.extensionRequestDemand;
            }},
            {headerName: "Challenge", field: "accountRequestInfo.challenge", type: "boolean", defaultSortCol: true},
            {headerName: "Role", field: "role.displayName", width: "63px"},
            {headerName: "Creation", field: "creationDate", type: "date", cellRenderer: function (params: any) {
                return dateRenderer(params.data.creationDate);
            }},
            {headerName: "Expiration", field: "expirationDate", type: "date", cellRenderer: function (params: any) {
                return dateRenderer(params.data.expirationDate);
            }},
            {headerName: "Active", field: "valid", type: "boolean", cellRenderer: function (params: any) {
                return !params.data.expirationDate || params.data.expirationDate >= new Date();
            }},
            {headerName: "Last Login", field: "lastLogin", type: "date", cellRenderer: function (params: any) {
                return dateRenderer(params.data.lastLogin);
            }}
        ];

        return columnDefs;
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
}