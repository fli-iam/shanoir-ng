import { Component, ViewChild } from '@angular/core';
import { BrowserPaginEntityListComponent } from '../../shared/components/entity/entity-list.browser.component.abstract';
import { TableComponent } from '../../shared/components/table/table.component';
import { User } from '../shared/user.model';
import { UserService } from '../shared/user.service';


@Component({
    selector: 'user-list',
    templateUrl: 'user-list.component.html',
    styleUrls: ['user-list.component.css']
})

export class UserListComponent extends BrowserPaginEntityListComponent<User>{
    @ViewChild('userTable') table: TableComponent;

    constructor(private userService: UserService) {
           super('user');
    }

    getEntities(): Promise<User[]> {
        return this.userService.getAll();
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
            }}
        ];

        return columnDefs;
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
}