import { Component } from '@angular/core';

import { User } from '../shared/user.model';
import { UserService } from '../shared/user.service';
import {TableComponent} from "../../shared/table/table.component";

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

    constructor(private userService: UserService) {
        this.getUsers();
        this.createColumnDefs();
}

    // Grid data
    getUsers(): void {
        var usersTmp: User[] = [];
        this.userService.getUsers().then(users2 => {
            if (users2) {
                for (let user of users2) {
                    if (!user.accountRequestDemand) {
                        usersTmp.push(user);
                    }
                }
                this.users = usersTmp;
            }
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
            {headerName: "Id", field: "id", type: "number"},
            {headerName: "Username", field: "username"},
            {headerName: "First Name", field: "firstName"},
            {headerName: "Last Name", field: "lastName"},
            {headerName: "Email", field: "email"},
            {headerName: "O.D.", tip: "On Demand", field: "accountRequestDemand", type: "boolean"},
            {headerName: "Team", field: "teamName"},
            {headerName: "Role", field: "role.displayName"},
            {headerName: "PACS", tip: "Can import from PACS", field: "canAccessToDicomAssociation", type: "boolean"},
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
            {headerName: "", type: "button", img: "/images/edit.16x16.png", action: function(item): void {
                console.log("TODO : delete item n°" + item.id + " in this function");
            }}
        ];
        this.customActionDefs = [
            {title: "new user", img: "/images/add.user.24x24.black.png", target: "../editUser"},
        ];
    }
}