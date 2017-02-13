import { Component, ViewChild, ViewContainerRef } from '@angular/core';
import { MdDialog, MdDialogConfig, MdDialogRef } from '@angular/material';

import { ConfirmDialogComponent } from "../../shared/utils/confirm.dialog.component";
import { ConfirmDialogService } from "../../shared/utils/confirm.dialog.service";
import { TableComponent } from "../../shared/table/table.component";
import { User } from '../shared/user.model';
import { UserService } from '../shared/user.service';

@Component({
    selector: 'user-list',
    moduleId: module.id,
    templateUrl: 'user.list.component.html',
    styleUrls: ['../../shared/css/common.css', 'user.list.component.css']
})

export class UserListComponent {
    private users: User[];
    private columnDefs: any[];
    private customActionDefs: any[];
    private loading: boolean = false;

    constructor(private userService: UserService, private confirmDialogService: ConfirmDialogService, private viewContainerRef: ViewContainerRef) {
        this.getUsers();
        this.createColumnDefs();
}

    // Grid data
    getUsers(): void {
        this.loading = true;
        var usersTmp: User[] = [];
        this.userService.getUsers().then(users2 => {
            if (users2) {
                this.users = users2;
            }
            this.loading = false;
        })
        .catch((error) => {
            // TODO: display error
            this.users = [];
        });
    }

    // Grid columns definition
    private createColumnDefs() {
        function dateRenderer(date) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        this.columnDefs = [
            {headerName: "Username", field: "username", widthPercent: "10"},
            {headerName: "First Name", field: "firstName", widthPercent: "10"},
            {headerName: "Last Name", field: "lastName", widthPercent: "10"},
            {headerName: "Email", field: "email", widthPercent: "20"},
            {headerName: "O.D.", tip: "On Demand", field: "accountRequestDemand", type: "boolean", defaultSortCol: true, defaultAsc: false},
            {headerName: "Role", field: "role.displayName", widthPercent: "5"},
            {headerName: "Creation", field: "creationDate", type: "date", cellRenderer: function (params) {
                return dateRenderer(params.data.creationDate);
            }},
            {headerName: "Expiration", field: "expirationDate", type: "date", cellRenderer: function (params) {
                return dateRenderer(params.data.expirationDate);
            }},
            {headerName: "Active", field: "valid", type: "boolean", cellRenderer: function (params) {
                return !params.data.expirationDate || params.data.expirationDate >= new Date();
            }},
            {headerName: "Last Login", field: "lastLogin", type: "date", cellRenderer: function (params) {
                return dateRenderer(params.data.lastLogin);
            }},
            {headerName: "", type: "button", img: "/images/edit.16x16.png", target : "/editUser", getParams: function(item): Object {
                return {id: item.id};
            }},
            {headerName: "", type: "button", img: "/images/delete.16x16.png", action: this.openDeleteUserConfirmDialog}
        ];
        this.customActionDefs = [
            {title: "new user", img: "/images/add.user.24x24.black.png", target: "../editUser"},
        ];
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
        this.userService.delete(userId).then((res) => this.getUsers());
    }

}