import { Component, ViewContainerRef } from '@angular/core';
import { Router } from '@angular/router';

import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { User } from '../shared/user.model';
import { UserService } from '../shared/user.service';

@Component({
    selector: 'user-list',
    templateUrl: 'user-list.component.html',
    styleUrls: ['user-list.component.css']
})

export class UserListComponent {
    private users: User[];
    private usersPromise: Promise<void> = this.getUsers();
    private browserPaging: BrowserPaging<User>;

    private columnDefs: any[];
    private customActionDefs: any[];

    constructor(
            private userService: UserService, 
            private confirmDialogService: ConfirmDialogService, 
            private viewContainerRef: ViewContainerRef,
            private router: Router) {

        this.createColumnDefs();
    }

    getUsers(): Promise<void> {
        return this.userService.getUsers().then(users => {
            if (users) {
                this.users = users;
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
        this.router.navigate(['/user'], { queryParams: { id: user.id, mode: "view" } });
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
            {headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, target : "/user", getParams: function(item: any): Object {
                return {id: item.id};
            }},
            {headerName: "", type: "button", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.openDeleteUserConfirmDialog}
        ];
        this.customActionDefs = [
            {title: "new user", img: ImagesUrlUtil.ADD_ICON_PATH, target: "../user"},
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