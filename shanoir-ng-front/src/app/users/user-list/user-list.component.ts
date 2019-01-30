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

import { Component, ViewContainerRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';

import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { User } from '../shared/user.model';
import { UserService } from '../shared/user.service';
import { TableComponent } from '../../shared/components/table/table.component';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';

@Component({
    selector: 'user-list',
    templateUrl: 'user-list.component.html',
    styleUrls: ['user-list.component.css']
})

export class UserListComponent {
    private usersPromise: Promise<void> = this.getUsersPromise();
    private browserPaging: BrowserPaging<User>;
    private columnDefs: any[];
    private customActionDefs: any[] = [];
    @ViewChild('userTable') table: TableComponent;

    constructor(
            private userService: UserService, 
            private confirmDialogService: ConfirmDialogService, 
            private viewContainerRef: ViewContainerRef,
            private router: Router,
            private msgService: MsgBoxService) {
        this.createColumnDefs();
    }

    getUsersPromise(): Promise<void> {
        return this.userService.getUsers().then(users => {
            if (users) {
                this.browserPaging = new BrowserPaging(users, this.columnDefs);
            }
        });
    }

    getPage(pageable: FilterablePageable): Promise<Page<User>> {
        return new Promise((resolve) => {
            this.usersPromise.then(() => {
                resolve(this.browserPaging.getPage(pageable));
            });
        });
    }

    private onRowClick(user: User) {
        this.router.navigate(['/user/edit/' + user.id])
    }

    // Grid columns definition
    private createColumnDefs() {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        this.columnDefs = [
            {headerName: "Username", field: "username" },
            {headerName: "First Name", field: "firstName" },
            {headerName: "Last Name", field: "lastName" },
            {headerName: "Email", field: "email", width: "200%"},
            {headerName: "O.D.", tip: "On Demand", field: "onDemand", type: "boolean", defaultSortCol: true, defaultAsc: false, cellRenderer: function (params: any) {
                return params.data.accountRequestDemand || params.data.extensionRequestDemand;
            }},
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
            }}, 
            {headerName: "", type: "button", awesome: "fa-edit", action: (user) => this.router.navigate(['/user/edit/' + user.id])},
            {headerName: "", type: "button", awesome: "fa-trash", action: this.openDeleteUserConfirmDialog}
        ];
        this.customActionDefs.push({
            title: "new user", img: ImagesUrlUtil.ADD_ICON_PATH, target: "/user/create"
        });
    }

    openDeleteUserConfirmDialog = (item: User) => {
        this.confirmDialogService
            .confirm('Delete user', 'Are you sure you want to delete user ' + item.firstName + ' ' + item.lastName + '?',
                this.viewContainerRef)
            .subscribe(res => {
                if (res) {
                    this.deleteUser(item.id);
                }
            })
    }

    deleteUser(userId: number) {
        // Delete user and refresh page
        this.userService.delete(userId).then(() => {
            this.userService.getUsers().then(users => {
                this.browserPaging.setItems(users);
                this.table.refresh();
                this.msgService.log('info', 'The user has been sucessfully deleted');
            });
        });
    }

}