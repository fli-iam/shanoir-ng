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
import { Directive, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, Subscription } from 'rxjs';

import { BreadcrumbsService } from '../../../breadcrumbs/breadcrumbs.service';
import { capitalizeFirstLetter } from '../../../utils/app.utils';
import { ServiceLocator } from '../../../utils/locator.service';
import { KeycloakService } from '../../keycloak/keycloak.service';
import { ShanoirError } from '../../models/error.model';
import { ConsoleService } from '../../console/console.service';
import { WindowService } from '../../services/window.service';
import { ConfirmDialogService } from '../confirm-dialog/confirm-dialog.service';
import { Page, Pageable } from '../table/pageable.model';
import { TableComponent } from '../table/table.component';
import { ColumnDefinition } from '..//table/column.definition.type';
import { Entity, EntityRoutes } from './entity.abstract';
import { EntityService } from './entity.abstract.service';
import { TreeService } from 'src/app/studies/study/tree.service';
import {SubjectComponent} from "../../../subjects/subject/subject.component";
import {SubjectService} from "../../../subjects/shared/subject.service";

@Directive()
export abstract class EntityListComponent<T extends Entity> implements OnDestroy {

    abstract table: TableComponent;
    columnDefs: ColumnDefinition[];
    customActionDefs: any[];
    protected router: Router;
    protected confirmDialogService: ConfirmDialogService;
    protected keycloakService: KeycloakService;
    private entityRoutes: EntityRoutes;
    protected consoleService: ConsoleService;
    protected breadcrumbsService: BreadcrumbsService;
    public windowService: WindowService;
    private treeService: TreeService;
    public onDelete: Subject<{entity: Entity, error?: ShanoirError}> =  new Subject();
    public onAdd: Subject<any> =  new Subject<any>();
    protected subscriptions: Subscription[] = [];
    private selectedId:  number;

    private edit: boolean = false;
    private view: boolean = true;
    private delete: boolean = false;
    private new: boolean = false;
    private showId: boolean = true;

    abstract getService(): EntityService<T>;
    getOnDeleteConfirmMessage?(entity: T): Promise<string>;

    constructor(
            protected readonly ROUTING_NAME: string) {

        this.entityRoutes = new EntityRoutes(ROUTING_NAME);
        this.router = ServiceLocator.injector.get(Router);
        this.confirmDialogService = ServiceLocator.injector.get(ConfirmDialogService);
        this.consoleService = ServiceLocator.injector.get(ConsoleService);
        this.breadcrumbsService = ServiceLocator.injector.get(BreadcrumbsService);
        this.keycloakService = ServiceLocator.injector.get(KeycloakService);
        this.windowService = ServiceLocator.injector.get(WindowService);
        this.treeService = ServiceLocator.injector.get(TreeService);

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
        if (options.id != undefined) this.showId = options.id;
    }

    protected completeColDefs(): void {
        if (this.edit) {
            this.columnDefs.push({ headerName: "", type: "button", awesome: "fa-regular fa-edit", action: item => this.goToEdit(item.id), condition: item => this.canEdit(item) });
        }
        if (this.delete) {
            this.columnDefs.push({ headerName: "", type: "button", awesome: "fa-regular fa-trash-can", action: (item) => this.openDeleteConfirmDialog(item) , condition: item => this.canDelete(item)});
        }
        if (this.showId && this.keycloakService.isUserAdmin && !this.columnDefs.find(col => col.field == 'id')) {
            this.columnDefs.unshift({ headerName: 'Id', field: 'id', type: 'number', width: '30px'});
        }
    }

    private completeCustomActions(): void {
        if (this.new) {
            this.customActionDefs.push({
                title: "New",awesome: "fa-solid fa-plus", action: item => this.router.navigate([this.entityRoutes.getRouteToCreate()])
            });
        }
    }

    abstract getPage(pageable: Pageable): Promise<Page<T>>;
    abstract getColumnDefs(): ColumnDefinition[];
    abstract getCustomActionsDefs(): any[];

    public onRowClick(entity: T) {
        this.goToViewFromEntity(entity);
    }

    protected openDeleteConfirmDialog = (entity: T) => {
        let dialogTitle : string = 'Delete ' + this.ROUTING_NAME;
        let dialogMsg : string = 'Are you sure you want to finally delete the ' + this.ROUTING_NAME
            + (entity['name'] ? ' \"' + entity['name'] + '\"' : ' with id n° ' + entity.id) + ' ?';

        let promise: Promise<string>;
        if (this.getOnDeleteConfirmMessage) {
            promise = this.getOnDeleteConfirmMessage(entity);
        } else {
            promise = Promise.resolve('');
        }
        promise.then(studyListStr => {
            this.confirmDialogService
                .confirm(
                    dialogTitle,
                    dialogMsg + studyListStr
                ).then(res => {
                if (res) {
                    this.getService().delete(entity.id).then(() => {
                        this.onDelete.next({entity: entity});
                        this.table.refresh().then(() => {
                            this.consoleService.log('info', 'The ' + this.ROUTING_NAME + ' n°' + entity.id + ' sucessfully deleted');
                        });
                        this.treeService.updateTree();
                    }).catch(reason => {
                        if (!reason){
                            return;
                        }
                        if (reason instanceof ShanoirError && reason.code == 422) {
                            this.dealWithDeleteError(reason, entity);
                            return;
                        } else if (reason.error){
                            this.dealWithDeleteError(new ShanoirError(reason), entity);
                            return;
                        }
                        throw Error(reason);
                    });
                }
            })
        });

    }

    private dealWithDeleteError(error: ShanoirError, entity: any) {
        let warn = 'The ' + this.ROUTING_NAME + (entity['name'] ? ' ' + entity['name'] : '') + ' with id ' + entity.id + ' is linked to other entities, it was not deleted.';
        if (error.message){
            warn = warn + ' ' + error.message;
        }
        this.consoleService.log('warn', warn, [error.details]);
        this.onDelete.next({error: error, entity: entity});
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

    goToViewFromEntity(item: T): void {
        if (item?.id) {
            this.router.navigate([this.entityRoutes.getRouteToView(item.id)]);
        }
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

    getRowRoute(item): string {
        if (item.visibleByDefault && item.locked && !this.keycloakService.isUserAdmin()) {
            return null;
        }
        return this.entityRoutes.getRouteToView(item.id);
    }

    ngOnDestroy() {
        for(let subscribtion of this.subscriptions) {
            subscribtion.unsubscribe();
        }
    }
}
