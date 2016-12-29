import { Component } from '@angular/core';
import {GridOptions} from 'ag-grid/main';

import { User } from '../shared/user.model';
import { UserService } from '../shared/user.service';
import GridUtils from 'app/shared/utils/grid.utils';
import {ClickableComponent} from "app/shared/utils/clickable.component";
import {ClickableParentComponent} from "app/shared/utils/clickable.parent.component";


@Component({
    selector: 'user-list',
    moduleId: module.id,
    templateUrl: 'user.list.component.html',
    styleUrls: ['../../shared/css/common.css', 'user.list.component.css']
})

export class UserListComponent {
    private gridOptions:GridOptions;
    private headerCellTemplate: string;
    private users: User[];
    private rowCount: number = 0;
    private columnDefs: any[];
    private rowHeight: number;

    constructor(private userService: UserService) {
        // we pass an empty gridOptions in, so we can grab the api out
        this.gridOptions = <GridOptions>{
            onGridReady: () => {
                 this.gridOptions.api.sizeColumnsToFit();
             }
         };
        this.getUsers();
        this.createColumnDefs();
        this.setHeaderCellTemplate();
        this.rowHeight = 24;
    }
    
    // Grid data
    getUsers(): void {
        this.userService.getUsers().then(users => { 
            this.users = users;
            if (users) {
                this.rowCount = users.length;
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
        function booleanTrueRenderer(bool) {
            if (bool) {
                var imageElement = document.createElement("img");
                imageElement.src = "/images/passed.16x16.png";
                return imageElement;
            }
            return null;
        };
        this.columnDefs = [
            {headerName: "Id", field: "id", width: 40, suppressMenu: true},
            {headerName: "Username", field: "username", width: 120, suppressMenu: true},
            {headerName: "First Name", field: "firstName", width: 130, suppressMenu: true},
            {headerName: "Last Name", field: "lastName", width: 130, suppressMenu: true},
            {headerName: "Email", field: "email", width: 200, suppressMenu: true},
            {headerName: "Team", field: "teamName", width: 100, suppressMenu: true},
            {headerName: "Role", field: "role.displayName", width: 100, suppressMenu: true},
            {headerName: "Can import from PACS", field: "canAccessToDicomAssociation", width: 160, suppressMenu: true, cellRenderer: function (params) {
                return booleanTrueRenderer(params.data.canAccessToDicomAssociation);
            }, cellStyle: {"text-align": "center"}},
            {headerName: "Created on", field: "creationDate", width: 110, suppressMenu: true, cellRenderer: function (params) {
                return dateRenderer(params.data.creationDate);
            }},
            {headerName: "Expiration Date", field: "expirationDate", width: 110, suppressMenu: true, cellRenderer: function (params) {
                return dateRenderer(params.data.expirationDate);
            }},
            {headerName: "Active", field: "valid", width: 60, suppressMenu: true, cellRenderer: function (params) {
                return booleanTrueRenderer(!params.data.expirationDate || params.data.expirationDate >= new Date());
            }, cellStyle: {"text-align": "center"}},
            {headerName: "Last Login", field: "lastLogin", width: 100, suppressMenu: true, cellRenderer: function (params) {
                return dateRenderer(params.data.lastLogin);
            }},
            {headerName: "Edit", field: "id", width: 50, suppressMenu: true, 
              cellRendererFramework: ClickableParentComponent
            , cellStyle: {"text-align": "center"}}
        ];
    }
    
    // Header cell template
    private setHeaderCellTemplate() {
        this.headerCellTemplate = 
        '<div class="ag-header-cell AgGrid-header">' +
            '<div id="agResizeBar" class="ag-header-cell-resize"></div>' +
            '<span id="agMenu" class="ag-header-icon ag-header-cell-menu-button"></span>' +
            '<div id="agHeaderCellLabel" class="ag-header-cell-label">' +
                '<span id="agSortAsc" class="ag-header-icon ag-sort-ascending-icon"></span>' +
                '<span id="agSortDesc" class="ag-header-icon ag-sort-descending-icon"></span>' +
                '<span id="agNoSort" class="ag-header-icon ag-sort-none-icon"></span>' +
                '<span id="agFilter" class="ag-header-icon ag-filter-icon"></span>' +
                '<span id="agText" class="ag-header-cell-text"></span>' +
            '</div>' +
        '</div>';
    }
    
    // Grid height with a max value
    private gridHeight() {
        if (this.rowCount == 0) {
            // No row => 120px
            return 120;
        } else if (this.rowCount <= GridUtils.maxDisplayedRows) {
            // Rows height + header + 2px for borders
            return (this.rowCount + 1) * this.rowHeight + 2;
        } else {
            // Rows height + header + 2px for borders + scrollbar height
            return (GridUtils.maxDisplayedRows + 1) * this.rowHeight + 2 + 17;
        }
    }
}