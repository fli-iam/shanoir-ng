import { Location } from '@angular/common';
import { EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { ServiceLocator } from '../../../utils/locator.service';
import { KeycloakService } from '../../keycloak/keycloak.service';
import { MsgBoxService } from '../../msg-box/msg-box.service';
import { FooterState } from '../form-footer/footer-state.model';
import { Entity } from './entity.interface';
import { BreadcrumbsService, Step } from '../../../breadcrumbs/breadcrumbs.service';

export type Mode =  "view" | "edit" | "create";
export abstract class EntityComponent<T extends Entity> implements OnInit {

    protected id: number;
    protected entity: T;
    @Input() mode: Mode;
    @Output() close: EventEmitter<any> = new EventEmitter();
    private footerState: FooterState;
    private form: FormGroup;

    private router: Router;
    private location: Location;
    private keycloakService: KeycloakService;
    protected formBuilder: FormBuilder;
    private msgBoxService: MsgBoxService; 
    private breadcrumbsService: BreadcrumbsService;

    abstract initView(): Promise<void>;
    abstract initEdit(): Promise<void>;
    abstract initCreate(): Promise<void>;
    abstract buildForm(): FormGroup;

    constructor(
            private activatedRoute: ActivatedRoute,
            private readonly ROUTING_NAME: string) {
        
        this.router = ServiceLocator.injector.get(Router);
        this.location = ServiceLocator.injector.get(Location);
        this.keycloakService = ServiceLocator.injector.get(KeycloakService);
        this.formBuilder = ServiceLocator.injector.get(FormBuilder);
        this.msgBoxService = ServiceLocator.injector.get(MsgBoxService);
        this.breadcrumbsService = ServiceLocator.injector.get(BreadcrumbsService);
        
        this.mode = this.activatedRoute.snapshot.data['mode'];
        this.id = +this.activatedRoute.snapshot.params['id'];
    }

    ngOnInit(): void {
        const choose = (): Promise<void> => {
            switch (this.mode) { 
                case 'create' : return this.initCreate();
                case 'edit' : return this.initEdit();
                case 'view' : return this.initView();
            }
        }
        choose().then(() => {
            if (this.breadcrumbsService.entityToReload()) this.entity = this.breadcrumbsService.lastStep.entity as T;
            this.form = this.buildForm();
            this.form.statusChanges.subscribe(status => this.footerState.valid = status == 'VALID');
            this.addBCStep();
        });
        this.footerState = new FooterState(this.mode, this.keycloakService.isUserAdminOrExpert());
    }

    private addBCStep() {
        let route: string;
        let label: string;
        switch (this.mode) { 
            case 'create' : 
                route = this.getRouteToCreate();
                label = 'new ' + this.ROUTING_NAME;
                break;
            case 'edit' : 
                route = this.getRouteToEdit(this.entity.id);
                label = 'edit ' + this.ROUTING_NAME;
                break;
            case 'view' : 
                route = this.getRouteToView(this.entity.id);
                label = 'view ' + this.ROUTING_NAME;
                break;
        }
        this.breadcrumbsService.addStep(new Step(label, route, this.entity));
    }

    formErrors(field: string): any {
        const control = this.form.get(field);
        if (control && control.dirty && !control.valid) {
            return control.errors;
        }
    }


    save(): Promise<void> {
        if (this.mode == 'create') {
            return this.entity.create().then((entity) => {
                this.goToView(entity.id);
                this.msgBoxService.log('info', 'The new ' + this.ROUTING_NAME + ' has been successfully saved under the number ' + entity.id);
            });
        }
        else if (this.mode == 'edit') {
            return this.entity.update().then(() => {
                this.goToView();
                this.msgBoxService.log('info', 'The ' + this.ROUTING_NAME + ' n°' + this.entity.id + ' has been successfully updated');
            });
        }
    }

    delete(): void {
        this.entity.delete();
    }


    public getRouteToView(id: number): string {
        return '/' + this.ROUTING_NAME + '/details/' + id;
    }

    public getRouteToEdit(id: number): string {
        return '/' + this.ROUTING_NAME + '/edit/' + id;
    }

    public getRouteToCreate(): string {
        return '/' + this.ROUTING_NAME + '/create';
    }

    public getRouteToList(): string {
        return '/' + this.ROUTING_NAME + '/list';
    }

    goToView(id?: number): void {
        if (!id) {
            if (this.mode == 'view') return;
            else if (this.mode == 'edit') id = this.entity.id;
            else throw new Error('Cannot infer id in create mode, maybe you should give an id to the goToView method');
        }
        this.router.navigate([this.getRouteToView(id)]);
    }

    goToEdit(id?: number): void {
        if (!id) {
            if (this.mode == 'edit') return;
            else if (this.mode == 'view') id = this.entity.id;
            else throw new Error('Cannot infer id in create mode, maybe you should give an id to the goToEdit method');
        }
        this.router.navigate([this.getRouteToEdit(id)]);
    }

    goToCreate(): void {
        this.router.navigate([this.getRouteToCreate()]);
    }

    goToList(): void {
        this.router.navigate([this.getRouteToList()]);
    }

    goBack(): void {
        this.breadcrumbsService.notifyBack();
        if (this.mode == 'view' || this.mode == 'create') this.goToList();
        else if (this.mode == 'edit') this.goToView();
    }

}