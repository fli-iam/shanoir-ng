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
import { OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, Subscription } from 'rxjs';

import { BreadcrumbsService } from '../../../breadcrumbs/breadcrumbs.service';
import { capitalizeFirstLetter } from '../../../utils/app.utils';
import { ServiceLocator } from '../../../utils/locator.service';
import { KeycloakService } from '../../keycloak/keycloak.service';
import { ShanoirError } from '../../models/error.model';
import { MsgBoxService } from '../../msg-box/msg-box.service';
import { ConfirmDialogService } from '../confirm-dialog/confirm-dialog.service';
import { Page, Pageable } from '../table/pageable.model';
import { TableComponent } from '../table/table.component';
import { Entity, EntityRoutes } from './entity.abstract';


export abstract class EntityListComponent<T extends Entity> implements OnDestroy {

    abstract table: TableComponent;  
    columnDefs: any[];
    customActionDefs: any[];
    protected router: Router;
    protected confirmDialogService: ConfirmDialogService;
    protected keycloakService: KeycloakService;
    private entityRoutes: EntityRoutes;
    protected msgBoxService: MsgBoxService;
    protected breadcrumbsService: BreadcrumbsService;
    public onDelete: Subject<any> =  new Subject<any>();
    protected subscribtions: Subscription[] = [];

    private edit: boolean = false;
    private view: boolean = true;
    private delete: boolean = false;
    private new: boolean = false;

    constructor(
            private readonly ROUTING_NAME: string) {
        
        this.entityRoutes = new EntityRoutes(ROUTING_NAME);
        this.router = ServiceLocator.injector.get(Router);
        this.confirmDialogService = ServiceLocator.injector.get(ConfirmDialogService);
        this.msgBoxService = ServiceLocator.injector.get(MsgBoxService);
        this.breadcrumbsService = ServiceLocator.injector.get(BreadcrumbsService);
        this.keycloakService = ServiceLocator.injector.get(KeycloakService);
        
        this.computeOptions();
        this.columnDefs = this.getColumnDefs();
        this.completeColDefs();
        this.customActionDefs = this.getCustomActionsDefs();
        this.completeCustomActions();
        this.breadcrumbsService.markMilestone();
        this.breadcrumbsService.nameStep(capitalizeFirstLetter(ROUTING_NAME) + ' list');
    }

    private computeOptions() {
        let options = this.getOptions();
        if (options.edit != undefined) this.edit = options.edit;
        if (options.view != undefined) this.view = options.view;
        if (options.delete != undefined) this.delete = options.delete;
        if (options.new != undefined) this.new = options.new;
    }

    private completeColDefs(): void {
        if (this.edit) {
            this.columnDefs.push({ headerName: "", type: "button", awesome: "fa-edit", action: item => this.goToEdit(item.id), condition: item => this.canEdit(item) });
        }
        if (this.view) {
            this.columnDefs.push({ headerName: "", type: "button", awesome: "fa-eye", action: item => this.goToView(item.id) });
        }
        if (this.delete) {
            this.columnDefs.push({ headerName: "", type: "button", awesome: "fa-trash", action: (item) => this.openDeleteConfirmDialog(item) , condition: item => this.canDelete(item)});
        }
    }

    private completeCustomActions(): void {
        if (this.new) {
            this.customActionDefs.push({
                title: "New",awesome: "fa-plus", action: item => this.router.navigate([this.entityRoutes.getRouteToCreate()])
            });
        }
    }

    abstract getPage(pageable: Pageable): Promise<Page<T>>;
    abstract getColumnDefs(): any[];
    abstract getCustomActionsDefs(): any[];

    onRowClick(entity: T) {
        this.goToView(entity.id);
    }

    protected openDeleteConfirmDialog = (entity: T) => {
        this.confirmDialogService
            .confirm(
                'Delete', 'Are you sure you want to delete ' + this.ROUTING_NAME + ' n° ' + entity.id + ' ?',
                ServiceLocator.rootViewContainerRef
            ).subscribe(res => {
                if (res) {
                    entity.delete().then(() => {
                        this.onDelete.next(entity);
                        this.table.refresh();
                        this.msgBoxService.log('info', 'The ' + this.ROUTING_NAME + ' sucessfully deleted');
                    }).catch(reason => {
                        if (reason && reason.error) {
                            this.onDelete.next(new ShanoirError(reason));
                            if (reason.error.code != 422) throw Error(reason);
                        } else {
                            console.error(reason);
                        }
                    });                    
                }
            })
    }

    /**
     * Can be overriden to set options
     */
    protected getOptions(): any {
        return {};
    }

    /**
     * Can be overriden to enable/disable the edit button for this item
     */
    protected canEdit(item: T): boolean {
        return true;
    }

    /**
     * Can be overriden to enable/disable the delete button for this item
     */
    protected canDelete(item: T): boolean {
        return true;
    }

    goToView(id: number): void {
        this.router.navigate([this.entityRoutes.getRouteToView(id)]);
    }

    goToEdit(id: number): void {
        this.router.navigate([this.entityRoutes.getRouteToEdit(id)]);
    }

    goToCreate(): void {
        this.router.navigate([this.entityRoutes.getRouteToCreate()]);
    }

    goToList(): void {
        this.router.navigate([this.entityRoutes.getRouteToList()]);
    }

    ngOnDestroy() {
        for(let subscribtion of this.subscribtions) {
            subscribtion.unsubscribe();
        }
    }

}