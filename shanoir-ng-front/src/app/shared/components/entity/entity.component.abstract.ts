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
import { Location } from '@angular/common';

import { ElementRef, EventEmitter, HostListener, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges, ViewChild, Directive } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, Subscription } from 'rxjs';

import { ConfirmDialogService } from '../confirm-dialog/confirm-dialog.service';
import { BreadcrumbsService } from '../../../breadcrumbs/breadcrumbs.service';
import { Router } from '../../../breadcrumbs/router';
import { ServiceLocator } from '../../../utils/locator.service';
import { KeycloakService } from '../../keycloak/keycloak.service';
import { ShanoirError } from '../../models/error.model';
import { MsgBoxService } from '../../msg-box/msg-box.service';
import { FooterState } from '../form-footer/footer-state.model';
import { Entity, EntityRoutes } from './entity.abstract';
import { EntityService } from './entity.abstract.service';


export type Mode =  "view" | "edit" | "create";
@Directive()
export abstract class EntityComponent<T extends Entity> implements OnInit, OnDestroy, OnChanges {
    
    private _entity: T;
    @Input() mode: Mode;
    @Input() id: number; // optional
    @Output() close: EventEmitter<any> = new EventEmitter();
    footerState: FooterState;
    protected onSave: Subject<any> =  new Subject<any>();
    protected subscribtions: Subscription[] = [];
    form: FormGroup;
    protected saveError: ShanoirError;
    protected onSubmitValidatedFields: string[] = [];
    @ViewChild('formContainer', { static: false }) formContainerElement: ElementRef;

    /* services */
    protected confirmDialogService: ConfirmDialogService;
    private entityRoutes: EntityRoutes;
    protected router: Router;
    private location: Location;
    protected formBuilder: FormBuilder;
    public keycloakService: KeycloakService;
    protected msgBoxService: MsgBoxService; 
    public breadcrumbsService: BreadcrumbsService;

    /* abstract methods */
    abstract initView(): Promise<void>;
    abstract initEdit(): Promise<void>;
    abstract initCreate(): Promise<void>;
    abstract buildForm(): FormGroup;

    constructor(
            protected activatedRoute: ActivatedRoute,
            private readonly ROUTING_NAME: string) {
        this.confirmDialogService = ServiceLocator.injector.get(ConfirmDialogService);
        this.entityRoutes = new EntityRoutes(ROUTING_NAME);
        this.router = ServiceLocator.injector.get(Router);
        this.location = ServiceLocator.injector.get(Location);
        this.keycloakService = ServiceLocator.injector.get(KeycloakService);
        this.formBuilder = ServiceLocator.injector.get(FormBuilder);
        this.msgBoxService = ServiceLocator.injector.get(MsgBoxService);
        this.breadcrumbsService = ServiceLocator.injector.get(BreadcrumbsService);
        
        this.mode = this.activatedRoute.snapshot.data['mode'];
        this.addBCStep();
    }

    public get entity(): T {
        return this._entity;
    }

    public set entity(entity: T) {
        this._entity = entity;
    }

    ngOnInit(): void {
        if (!this.id) this.id = +this.activatedRoute.snapshot.params['id'];
        const choose = (): Promise<void> => {
            switch (this.mode) { 
                case 'create' : return this.initCreate();
                case 'edit' : return this.initEdit();
                case 'view' : return this.initView();
                default: throw Error('mode has to be set!');
            }
        }
        choose().then(() => {
            this.footerState = new FooterState(this.mode);
            this.hasEditRight().then(right => this.footerState.canEdit = right);
            this.hasDeleteRight().then(right => this.footerState.canDelete = right);
            if ((this.mode == 'create' || this.mode == 'edit') && this.breadcrumbsService.currentStep.entity) {
                this.entity = this.breadcrumbsService.currentStep.entity as T;
            }
            this.breadcrumbsService.currentStep.entity = this.entity;
            this.manageFormSubscriptions();
        });
    }
    
    ngOnChanges(changes: SimpleChanges): void {
        if ((changes['id'] && !changes['id'].isFirstChange())
                || (changes['mode'] && !changes['mode'].isFirstChange())) 
            this.ngOnInit();
    }

    private manageFormSubscriptions() {
        this.form = this.buildForm();
        if (this.form) {
            this.subscribtions.push(
                this.form.statusChanges.subscribe(status => {
                    this.footerState.valid = status == 'VALID' && (this.form.dirty || this.mode == 'create');
                    this.footerState.dirty = this.form.dirty;
                })
            );
            if (this.mode != 'view') setTimeout(() => this.styleRequiredLabels());
        } else {
            this.footerState.valid = false;
        }
    }

    private addBCStep() {
        let label: string;
        if (this.mode == "create") label = 'New ' + this.ROUTING_NAME;
        else if (this.mode == 'edit') label = 'Edit ' + this.ROUTING_NAME;
        else if (this.mode == 'view') label = 'View ' + this.ROUTING_NAME;
        this.breadcrumbsService.nameStep(label);
    }

    private styleRequiredLabels() {
        if (this.formContainerElement) {
            for (const field in this.form.controls) {
                const control = this.form.get(field);
                if (this.hasRequiredField(control)) {
                    const input = this.formContainerElement.nativeElement.querySelector('li [formControlName="' + field + '"]');
                    if (input) {
                        const li = input.closest('li');
                        if (li) {
                            const label = li.querySelector(':scope > label');
                            if (label) label.classList.add('required-label');
                        }
                    }
                }
            }
        }
    }

    private clearRequiredStyles() {
        if (this.formContainerElement) {
            this.formContainerElement.nativeElement.querySelectorAll('li > label')
            .forEach(label => {
                label.classList.remove('required-label');
            }); 
        }
    }

    protected reloadRequiredStyles() {
        setTimeout(() => {
            this.clearRequiredStyles();
            this.styleRequiredLabels();
        });
    }

    private hasRequiredField (abstractControl: AbstractControl): boolean {
        if (abstractControl.validator) {
            const validator = abstractControl.validator({}as AbstractControl);
            if (validator && validator.required) {
                return true;
            }
        }
        if (abstractControl['controls']) {
            for (const controlName in abstractControl['controls']) {
                if (abstractControl['controls'][controlName]) {
                    if (this.hasRequiredField(abstractControl['controls'][controlName])) {
                        return true;
                    }
                }
            }
        }
        return false;
    };

    formErrors(field: string): any {
        if (!this.form) return;
        const control = this.form.get(field);
        if (control && (control.touched || this.mode != 'create') && !control.valid) {
            return control.errors;
        }
    }

    hasError(fieldName: string, errors: string[]) {
        let formError = this.formErrors(fieldName);
        if (formError) {
            for(let errorName of errors) {
                if(formError[errorName]) return true;
            }
        }
        return false;
    }

    /**
     * Chooses between create() and update(), saves the entity and return a promise
     */
    private modeSpecificSave(): Promise<void> {
        if (this.mode == 'create') {
            return this.getService().create(this.entity).then((entity) => {
                this.entity.id = entity.id;
                this.onSave.next(entity);
                this.chooseRouteAfterSave(entity);
                this.msgBoxService.log('info', 'The new ' + this.ROUTING_NAME + ' has been successfully saved under the number ' + entity.id);
                this._entity.id = entity.id;
            });
        }
        else if (this.mode == 'edit') {
            return this.getService().update(this.entity.id, this.entity).then(() => {
                this.onSave.next(this.entity);
                this.chooseRouteAfterSave(this.entity);
                this.msgBoxService.log('info', 'The ' + this.ROUTING_NAME + ' n°' + this.entity.id + ' has been successfully updated');
            });
        }
    }

    save(): Promise<void> {
        this.footerState.loading = true;
        return this.modeSpecificSave()
            .then(() => {
                this.footerState.loading = false;
            })
            /* manages "after submit" errors like a unique constraint */      
            .catch(reason => {
                this.footerState.loading = false;
                return this.catchSavingErrors(reason);
            });
    }

    protected catchSavingErrors = (reason: any): Promise<any> => {
        if (reason && reason.error && reason.error.code == 422) {
            this.saveError = new ShanoirError(reason);
            for (let managedField of this.onSubmitValidatedFields) {
                let fieldControl: AbstractControl = this.form.get(managedField);
                if (!fieldControl) throw new Error(managedField + 'is not a field managed by this form. Check the arguments of registerOnSubmitValidator().');
                fieldControl.updateValueAndValidity({emitEvent : false});
                if (!fieldControl.valid) fieldControl.markAsTouched();
            }
            this.footerState.valid = this.form.status == 'VALID';
        } throw reason;
    }

    /**
     * Get a validator to manage form errors catched after an entity submited, like a unique contrainte. 
     * @param constraintName The code name of the contraint, received from the rest service (e.g. 'unique')
     * @param controlFieldName The field name specified in the FormGroup definition (in buildForm())
     * @param errorFieldName (optional) The field name of the error received from the rest service if different from controlFieldName
     */
    protected registerOnSubmitValidator(constraintName: string, controlFieldName: string, errorFieldName?: string): (control: AbstractControl) => ValidationErrors | null {
        if (this.onSubmitValidatedFields.indexOf(controlFieldName) == -1) this.onSubmitValidatedFields.push(controlFieldName);        
        return (control: AbstractControl): ValidationErrors | null => {
            if (this.saveError && this.saveError.hasFieldError(errorFieldName ? errorFieldName : controlFieldName, constraintName, control.value)
                    //&& this.form.get(controlFieldName).pristine
            ) {
                let ret = {};
                ret[constraintName] = true;
                return ret;
            }
            return null;
        }
    }

    protected chooseRouteAfterSave(entity: Entity) {
        this.breadcrumbsService.currentStep.notifySave(entity);
        if (this.breadcrumbsService.previousStep && this.breadcrumbsService.previousStep.isWaitingFor(this.breadcrumbsService.currentStep)) {
            this.breadcrumbsService.goBack();
        }
        else {
            this.goToView(entity.id);
        }
    }

    delete(): void {
        this.openDeleteConfirmDialog(this.entity)
    }

    protected openDeleteConfirmDialog = (entity: T) => {
        this.confirmDialogService
            .confirm(
                'Delete ' + this.ROUTING_NAME, 
                'Are you sure you want to delete the ' + this.ROUTING_NAME 
                + (entity['name'] ? ' "' + entity['name'] + '"' : ' with id n° ' + entity.id) + ' ?'
            ).then(res => {
                if (res) {
                    entity.delete().then(() => {
                        this.msgBoxService.log('info', 'The ' + this.ROUTING_NAME + ' sucessfully deleted');
                        this.goToList();
                    }).catch(reason => {
                        if (reason && reason.error) {
                            if (reason.error.code != 422) {
                               throw Error(reason); 
                            } else {
                                this.msgBoxService.log('warn', 'This ' + this.ROUTING_NAME + ' is linked to other entities, it was not deleted.');
                            }
                        } else {
                            console.error(reason);
                        }
                    });                    
                }
            })
    }

    goToView(id?: number): void {
        if (!id) {
            if (this.mode == 'view') return;
            else if (this.mode == 'edit') id = this.entity.id;
            else throw new Error('Cannot infer id in create mode, maybe you should give an id to the goToView method');
        }
        let replace: boolean = this.breadcrumbsService.currentStep && (
                this.breadcrumbsService.currentStep.route == this.entityRoutes.getRouteToEdit(id)
                || this.breadcrumbsService.currentStep.route == this.entityRoutes.getRouteToCreate()
                // Create route can be contained in incoming route (more arguments for example)
                || this.breadcrumbsService.currentStep.route.indexOf(this.entityRoutes.getRouteToCreate()) != -1);
        this.router.navigate([this.entityRoutes.getRouteToView(id)], {replaceUrl: replace});
    }

    goToEdit(id?: number): void {
        if (!id) {
            if (this.mode == 'edit') return;
            else if (this.mode == 'view') id = this.entity.id;
            else throw new Error('Cannot infer id in create mode, maybe you should give an id to the goToEdit method');
        }
        let replace: boolean = this.breadcrumbsService.currentStep && this.breadcrumbsService.currentStep.route == this.entityRoutes.getRouteToView(id);
        this.router.navigate([this.entityRoutes.getRouteToEdit(id)], {replaceUrl: replace});
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

    public compareEntities(e1: Entity, e2: Entity) : boolean {
        return e1 && e2 && e1.id === e2.id;
    }

    ngOnDestroy() {
        for(let subscribtion of this.subscribtions) {
            subscribtion.unsubscribe();
        }
    }

    /**
     * Says if current user has the right to display the edit button.
     * Default is true and this method should be overriden when rights control is needed.
     * It is called after initialization so the entity value can be used inside.
     */
    public async hasEditRight(): Promise<boolean> {
        return true;
    }

    /**
     * Says if current user has the right to display the delete button.
     * Default is true and this method should be overriden when rights control is needed.
     * It is called after initialization so the entity value can be used inside.
     */
    public async hasDeleteRight(): Promise<boolean> {
        return this.keycloakService.isUserAdminOrExpert();
    }

    @HostListener('document:keypress', ['$event']) onKeydownHandler(event: KeyboardEvent) {
        if (event.key == '²') {
            console.log('form', this.form);
            console.log('entity', this.entity);
        }
    }

    abstract getService(): EntityService<T>;
}