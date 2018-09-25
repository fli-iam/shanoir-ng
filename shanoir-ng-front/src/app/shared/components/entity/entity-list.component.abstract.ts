import { ViewContainerRef } from '@angular/core';
import { Router } from '@angular/router';

import { ServiceLocator } from '../../../utils/locator.service';
import { KeycloakService } from '../../keycloak/keycloak.service';
import { ConfirmDialogService } from '../confirm-dialog/confirm-dialog.service';
import { Page, Pageable } from '../table/pageable.model';
import { TableComponent } from '../table/table.component';
import { Entity, EntityRoutes } from './entity.interface';
import { MsgBoxService } from '../../msg-box/msg-box.service';

export abstract class EntityListComponent<T extends Entity> {

    abstract table: TableComponent;  
    protected columnDefs: any[];
    protected customActionDefs: any[];
    protected router: Router;
    protected confirmDialogService: ConfirmDialogService;
    private keycloakService: KeycloakService;
    private entityRoutes: EntityRoutes;
    private msgBoxService: MsgBoxService; 

    private edit: boolean = true;
    private view: boolean = true;
    private delete: boolean = true;
    private new: boolean = true;

    constructor(
            private readonly ROUTING_NAME: string,
            private readonly options?: any) {
        
        this.entityRoutes = new EntityRoutes(ROUTING_NAME);
        this.router = ServiceLocator.injector.get(Router);
        this.confirmDialogService = ServiceLocator.injector.get(ConfirmDialogService);
        this.keycloakService = ServiceLocator.injector.get(KeycloakService);
        this.msgBoxService = ServiceLocator.injector.get(MsgBoxService);
        
        if (options) this.setOptions(options);
        this.columnDefs = this.getColumnDefs();
        this.completeColDefs();
        this.customActionDefs = this.getCustomActionsDefs();
        this.completeCustomActions();        
    }

    private setOptions(options: any) {
        if (options.edit != undefined) this.edit = options.edit;
        if (options.view != undefined) this.view = options.view;
        if (options.delete != undefined) this.delete = options.delete;
        if (options.new != undefined) this.new = options.new;
    }

    private completeColDefs(): void {
        if (this.edit && this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push({ headerName: "", type: "button", awesome: "fa-edit", action: item => this.goToEdit(item.id) });
        }
        if (this.view && !this.keycloakService.isUserGuest()) {
            this.columnDefs.push({ headerName: "", type: "button", awesome: "fa-eye", action: item => this.goToView(item.id) });
        }
        if (this.view && !this.keycloakService.isUserGuest()) {
            this.columnDefs.push({ headerName: "", type: "button", awesome: "fa-trash", action: (item) => this.openDeleteConfirmDialog(item) });
        }
    }

    private completeCustomActions(): void {
        if (this.new && this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.customActionDefs.push({
                title: "new coil.",awesome: "fa-plus", action: item => this.router.navigate(['/coil/create'])
            });
        }
    }

    abstract getPage(pageable: Pageable): Promise<Page<T>>;
    abstract getColumnDefs(): any[];
    abstract getCustomActionsDefs(): any[];
    abstract onDelete(entity: T): void;

    private onRowClick(entity: T) {
        if (!this.keycloakService.isUserGuest()) {
            this.goToView(entity.id);
        }
    }

    protected openDeleteConfirmDialog = (entity: T) => {
        if (this.keycloakService.isUserGuest()) return;
        this.confirmDialogService
            .confirm(
                'Delete', 'Are you sure you want to delete ' + this.ROUTING_NAME + ' n° ' + entity.id + ' ?',
                ServiceLocator.rootViewContainerRef
            ).subscribe(res => {
                if (res) {
                    entity.delete().then(() => {
                        this.onDelete(entity);
                        this.table.refresh();
                        this.msgBoxService.log('info', 'The ' + this.ROUTING_NAME + ' sucessfully deleted');
                    });                    
                }
            })
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

}