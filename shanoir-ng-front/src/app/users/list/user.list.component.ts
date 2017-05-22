import { Component, ViewChild, ViewContainerRef } from '@angular/core';
import { MdDialog, MdDialogConfig, MdDialogRef } from '@angular/material';

import { ConfirmDialogComponent } from "../../shared/utils/confirm.dialog.component";
import { ConfirmDialogService } from "../../shared/utils/confirm.dialog.service";
import { TableComponent } from "../../shared/table/table.component";
import { User } from '../shared/user.model';
import { UserService } from '../shared/user.service';

@Component({
    selector: 'user-list',
    templateUrl: 'user.list.component.html',
    styleUrls: ['user.list.component.css']
})

export class UserListComponent {
    public users: User[];
    public columnDefs: any[];
    public customActionDefs: any[];
    public rowClickAction: Object;
    public loading: boolean = false;

    constructor(private userService: UserService, private confirmDialogService: ConfirmDialogService, private viewContainerRef: ViewContainerRef) {
        this.getUsers();
        this.createColumnDefs();
    }

    // Grid data
    getUsers(): void {
        this.loading = true;
        this.userService.getUsers().then(users => {
            if (users) {
                this.users = users;
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
            {headerName: "", type: "button", img: "/assets/images/icons/edit.png", target : "/editUser", getParams: function(item: any): Object {
                return {id: item.id};
            }},
            {headerName: "", type: "button", img: "/assets/images/icons/garbage-1.png", action: this.openDeleteUserConfirmDialog}
        ];
        this.customActionDefs = [
            {title: "new user", img: "/assets/images/icons/add-1.png", target: "../editUser"},
            {title: "delete selected", img: "/assets/images/icons/garbage-1.png", action: this.deleteAll } 
        ];
        this.rowClickAction = {target : "/editUser", getParams: function(item: any): Object {
                return {id: item.id};
        }};
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

    deleteAll = () => {
        let ids: number[] = [];
        for (let user of this.users) {
            if (user["isSelectedInTable"]) ids.push(user.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }

    deleteUser(userId: number) {
        // Delete user and refresh page
        this.userService.delete(userId).then((res) => this.getUsers());
    }

}