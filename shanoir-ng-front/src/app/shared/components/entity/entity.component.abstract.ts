import { Location } from '@angular/common';
import { EventEmitter, Input, OnInit, Output, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { BreadcrumbsService, Step } from '../../../breadcrumbs/breadcrumbs.service';
import { ServiceLocator } from '../../../utils/locator.service';
import { KeycloakService } from '../../keycloak/keycloak.service';
import { MsgBoxService } from '../../msg-box/msg-box.service';
import { FooterState } from '../form-footer/footer-state.model';
import { Entity, EntityRoutes } from './entity.abstract';
import { Subject, Subscription } from 'rxjs';
import { ShanoirError } from '../../models/error.model';

export type Mode =  "view" | "edit" | "create";
export abstract class EntityComponent<T extends Entity> implements OnInit, OnDestroy {

    protected id: number;
    protected entity: T;
    @Input() mode: Mode;
    @Output() close: EventEmitter<any> = new EventEmitter();
    private footerState: FooterState;
    protected form: FormGroup;
    private bcStep: Step;

    private entityRoutes: EntityRoutes;
    protected router: Router;
    private location: Location;
    private keycloakService: KeycloakService;
    protected formBuilder: FormBuilder;
    private msgBoxService: MsgBoxService; 
    protected breadcrumbsService: BreadcrumbsService;
    public onSave: Subject<any> =  new Subject<any>();
    protected subscribtions: Subscription[] = [];

    abstract initView(): Promise<void>;
    abstract initEdit(): Promise<void>;
    abstract initCreate(): Promise<void>;
    abstract buildForm(): FormGroup;

    constructor(
            private activatedRoute: ActivatedRoute,
            private readonly ROUTING_NAME: string) {
        
        this.entityRoutes = new EntityRoutes(ROUTING_NAME);
        this.router = ServiceLocator.injector.get(Router);
        this.location = ServiceLocator.injector.get(Location);
        this.keycloakService = ServiceLocator.injector.get(KeycloakService);
        this.formBuilder = ServiceLocator.injector.get(FormBuilder);
        this.msgBoxService = ServiceLocator.injector.get(MsgBoxService);
        this.breadcrumbsService = ServiceLocator.injector.get(BreadcrumbsService);
        
        this.mode = this.activatedRoute.snapshot.data['mode'];
        this.id = +this.activatedRoute.snapshot.params['id'];
        this.addBCStep();
    }

    ngOnInit(): void {
        const choose = (): Promise<void> => {
            switch (this.mode) { 
                case 'create' : return this.initCreate();
                case 'edit' : return this.initEdit();
                case 'view' : return this.initView();
                default: throw Error('mode has to be set!');
            }
        }
        choose().then(() => {
            if (this.breadcrumbsService.entityToReload()) this.entity = this.breadcrumbsService.reloadSavedEntity<T>();
            this.bcStep.entity = this.entity;
            this.form = this.buildForm();
            if (this.form) 
                this.form.statusChanges.subscribe(status => this.footerState.valid = status == 'VALID');
            else 
                this.footerState.valid = true;
        });
        this.footerState = new FooterState(this.mode, this.keycloakService.isUserAdminOrExpert());
    }

    private addBCStep() {
        let label: string;
        switch (this.mode) { 
            case 'create' : 
                label = 'New ' + this.ROUTING_NAME;
                break;
            case 'edit' : 
                label = 'Edit ' + this.ROUTING_NAME;
                break;
            case 'view' : 
                label = 'View ' + this.ROUTING_NAME;
                break;
        }
        this.bcStep = new Step(label, this.router.url);
        this.breadcrumbsService.addStep(this.bcStep);
    }

    formErrors(field: string): any {
        if (!this.form) return;
        const control = this.form.get(field);
        if (control && control.dirty && !control.valid) {
            return control.errors;
        }
    }

    hasError(fieldName: string, errors: string[] ) {
        let formError = this.formErrors(fieldName);
        if (formError) {
            for(let errorName of errors) {
                if(formError[errorName]) return true;
            }
        }
        return false;
    }

    save(): Promise<void> {
        return this.saveEntity().then(() => {
            this.onSave.next(this.entity);
        }).catch(reason => {
            if (reason && reason.error) {
                this.onSave.next(new ShanoirError(reason.error.code, reason.error.details, reason.error.message));
                if (reason.error.code != 422) throw Error(reason);
            }
        });
    }

    private saveEntity(): Promise<void> {
        if (this.mode == 'create') {
            return this.entity.create().then((entity) => {
                this.chooseRoute(entity);
                this.msgBoxService.log('info', 'The new ' + this.ROUTING_NAME + ' has been successfully saved under the number ' + entity.id);
            });
        }
        else if (this.mode == 'edit') {
            return this.entity.update().then(() => {
                this.chooseRoute(this.entity);
                this.msgBoxService.log('info', 'The ' + this.ROUTING_NAME + ' n°' + this.entity.id + ' has been successfully updated');
            });
        }
    }

    private chooseRoute(entity: Entity) {
        this.breadcrumbsService.lastStep.notifySave(entity);
        if (this.breadcrumbsService.beforeLastStep && this.breadcrumbsService.beforeLastStep.isWaitingFor(this.breadcrumbsService.lastStep)) {
            this.breadcrumbsService.goBack();
        }
        else {
            this.goToView(entity.id);
        }
    }

    delete(): void {
        this.entity.delete();
    }

    goToView(id?: number): void {
        if (!id) {
            if (this.mode == 'view') return;
            else if (this.mode == 'edit') id = this.entity.id;
            else throw new Error('Cannot infer id in create mode, maybe you should give an id to the goToView method');
        }
        if (this.breadcrumbsService.lastStep && (
                this.breadcrumbsService.lastStep.route == this.entityRoutes.getRouteToEdit(id)
                || this.breadcrumbsService.lastStep.route == this.entityRoutes.getRouteToCreate())){
            this.breadcrumbsService.disableLastStep();
        }
        this.router.navigate([this.entityRoutes.getRouteToView(id)]);
    }

    goToEdit(id?: number): void {
        if (!id) {
            if (this.mode == 'edit') return;
            else if (this.mode == 'view') id = this.entity.id;
            else throw new Error('Cannot infer id in create mode, maybe you should give an id to the goToEdit method');
        }
        if (this.breadcrumbsService.lastStep && this.breadcrumbsService.lastStep.route == this.entityRoutes.getRouteToView(id)) {
            this.breadcrumbsService.disableLastStep();
        }
        this.router.navigate([this.entityRoutes.getRouteToEdit(id)]);
    }

    goToCreate(): void {
        this.router.navigate([this.entityRoutes.getRouteToCreate()]);
    }

    goToList(): void {
        this.router.navigate([this.entityRoutes.getRouteToList()]);
    }

    goBack(): void {
        this.location.back();
    }

    private compareEntities(e1: Entity, e2: Entity) : boolean {
        return e1 && e2 && e1.id === e2.id;
    }

    ngOnDestroy() {
        for(let subscribtion of this.subscribtions) {
            subscribtion.unsubscribe();
        }
    }

}