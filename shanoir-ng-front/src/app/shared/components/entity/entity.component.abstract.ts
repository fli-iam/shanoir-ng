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

import {
    Directive,
    ElementRef,
    EventEmitter,
    HostListener,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    Output,
    SimpleChanges,
    ViewChild
} from '@angular/core';
import { AbstractControl, Form, FormArray, FormGroup, UntypedFormBuilder, UntypedFormGroup, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, NavigationExtras } from '@angular/router';
import { firstValueFrom, Subject, Subscription } from 'rxjs';

import { Router } from '@angular/router';
import { Selection, TreeService } from 'src/app/studies/study/tree.service';
import { SuperPromise } from 'src/app/utils/super-promise';
import { BreadcrumbsService, Step } from '../../../breadcrumbs/breadcrumbs.service';
import { ServiceLocator } from '../../../utils/locator.service';
import { ConsoleService } from '../../console/console.service';
import { KeycloakService } from '../../keycloak/keycloak.service';
import { ShanoirError } from '../../models/error.model';
import { ConfirmDialogService } from '../confirm-dialog/confirm-dialog.service';
import { FooterState } from '../form-footer/footer-state.model';
import { Entity, EntityRoutes } from './entity.abstract';
import { EntityService } from './entity.abstract.service';
import { getDeclaredFields } from '../../reflect/field.decorator';
import { Examination } from 'src/app/examinations/shared/examination.model';


export type Mode = "view" | "edit" | "create";

@Directive()
export abstract class EntityComponent<T extends Entity> implements OnInit, OnDestroy, OnChanges {

    private _entity: T;
    @Input() mode: Mode;
    @Input() id: number; // if not given via url
    @Input() entityInput: T; // if id not given via url
    @Input() embedded: boolean = false;
    @Output() close: EventEmitter<any> = new EventEmitter();
    footerState: FooterState;
    protected onSave: Subject<any> = new Subject<any>();
    protected subscriptions: Subscription[] = [];
    form: UntypedFormGroup;
    protected saveError: ShanoirError;
    protected onSubmitValidatedFields: string[] = [];
    @ViewChild('formContainer', {static: false}) formContainerElement: ElementRef;
    _activeTab: string;
    protected isMainComponent: boolean;
    idPromise: SuperPromise<number> = new SuperPromise();
    entityPromise: SuperPromise<T> = new SuperPromise();
    static ActivateTreeOnThisPage: boolean = true;
    getOnDeleteConfirmMessage?(entity: Entity): Promise<string>;
    protected destroy$: Subject<void> = new Subject<void>();
    private form$: SuperPromise<void> = new SuperPromise<void>();
    protected showTreeByDefault: boolean = true;

    /* services */
    protected confirmDialogService: ConfirmDialogService;
    private entityRoutes: EntityRoutes;
    protected router: Router;
    protected location: Location;
    protected formBuilder: UntypedFormBuilder;
    public keycloakService: KeycloakService;
    protected consoleService: ConsoleService;
    public breadcrumbsService: BreadcrumbsService;
    public treeService: TreeService;

    /* abstract methods */
    abstract initView(): Promise<void>;
    abstract initEdit(): Promise<void>;
    abstract initCreate(): Promise<void>;
    abstract buildForm(): UntypedFormGroup;
    abstract getService(): EntityService<T>;
    protected getTreeSelection: () => Selection; //optional
    protected fetchEntity: () => Promise<any>; // optional

    constructor(
        protected activatedRoute: ActivatedRoute,
        private readonly ROUTING_NAME: string) {
        this.confirmDialogService = ServiceLocator.injector.get(ConfirmDialogService);
        this.entityRoutes = new EntityRoutes(ROUTING_NAME);
        this.router = ServiceLocator.injector.get(Router);
        this.location = ServiceLocator.injector.get(Location);
        this.keycloakService = ServiceLocator.injector.get(KeycloakService);
        this.formBuilder = ServiceLocator.injector.get(UntypedFormBuilder);
        this.consoleService = ServiceLocator.injector.get(ConsoleService);
        this.breadcrumbsService = ServiceLocator.injector.get(BreadcrumbsService);
        this.treeService = ServiceLocator.injector.get(TreeService);

        this.mode = this.activatedRoute.snapshot.data['mode'];

        queueMicrotask(() => { // force it to be after child constructor, we need this.fetchEntity
            if (this.mode != 'create' && this.getTreeSelection) this.treeService.activateTree(this.activatedRoute);
            let userId: number = +this.activatedRoute.snapshot.paramMap.get('id');
            if (!this.showTreeByDefault
                && this.treeService.treeOpened
                && (!this.treeService.memberStudyOpenedAndTreeActive(userId)) 
            ) {
                this.treeService.closeTemporarily();
            }
            this.subscriptions.push(this.activatedRoute.params.subscribe(
                params => {
                    this.mode = this.activatedRoute.snapshot.data['mode'];
                    if (this.mode != 'create' && this.getTreeSelection) this.treeService.activateTree(this.activatedRoute); // at each routing event
                    this.addBCStep();
                    this.isMainComponent = true;
                    const id = +params['id'];
                    this.id = id;
                    this.idPromise = new SuperPromise();
                    this.entityPromise = new SuperPromise();
                    this.idPromise.resolve(id);
                    this.init();
                })
            );
        });
    }

    ngOnInit(): void {
        this.addBCStep();
    }

    public get entity(): T {
        return this._entity;
    }

    public set entity(entity: T) {
        if (!entity) {
            this._entity = null;
            if (this.form) this.form.reset();
            this.entityPromise.resolve(null);
        } else {
            if (!EntityComponent.isProxy(entity)) {
                if (this.form) {
                    this._entity = this.wrapAsProxy(entity, this.form);
                    EntityComponent.mapEntityToForm(entity, this.form);
                } else {
                    this._entity = entity;
                    this.form$.then(() => {
                        this._entity = this.wrapAsProxy(entity, this.form);
                        EntityComponent.mapEntityToForm(entity, this.form);
                    })
                }
            }
            this.entityPromise.resolve(entity);
        }
    }

    private wrapAsProxy<T extends Object>(object: T, form: FormGroup): T {
        return new Proxy(object, {
            get: (target, prop, receiver) => {
                if (prop === 'IS_PROXY') return true;
                if (prop === 'GET_TARGET') return target;
                return Reflect.get(target, prop, receiver);
            },
            set: (target, prop, value, receiver) => {
                const oldValue = Reflect.get(target, prop, receiver);
                let isEqual = EntityComponent.deepEquals(oldValue, value);
                const ctrl = form.get(String(prop));
                // If the new value is an entity and a sub formgroup is set, we have to wrap it as proxy too
                if (ctrl && ctrl instanceof FormGroup) {
                    if (value instanceof Entity) {
                        let wrapedValue = this.wrapAsProxy(value, form);
                        const ok = Reflect.set(target, prop, wrapedValue, receiver); // set the value in the entity
                        if (!ok) return false;
                        EntityComponent.mapEntityToForm(value, ctrl); // map all sub properties
                        this.subscribeEntityPropsUpdatesFromForm(ctrl, value);
                    } else if (!value) {
                        ctrl.reset({}, {emitEvent: true});
                    }
                } else {
                    const ok = Reflect.set(target, prop, value, receiver); // set the value in the entity
                    if (!ok) return false;
                    // Sync form
                    if (!isEqual && ctrl) {
                        ctrl.setValue(value, {emitEvent: true});
                    }
                }
                return true;
            }
        });
    }

    private static deepEquals(a: any, b: any): boolean {
        if (a === b) return true;
        else if (a instanceof Entity && b instanceof Entity) return Entity.equals(a, b);
        else if (a instanceof Object && b instanceof Object) {
            const aKeys = Object.keys(a);
            const bKeys = Object.keys(b);
            if (aKeys.length !== bKeys.length) return false;
            return aKeys.every(key => this.deepEquals(a[key], b[key]));
        } else if (a instanceof Array && b instanceof Array) {
            return a.length === b.length && a.every((v, i) => this.deepEquals(v, b[i]));
        } else return false;
    }

    static isProxy(entity: any): boolean {
        return entity && entity['IS_PROXY'] === true;
    }

    public get activeTab(): string {
        return this._activeTab;
    }

    public set activeTab(param: string) {
        this._activeTab = param;
    }

    private loadEntity(): Promise<T> {
        let promise: Promise<T>;
        if (this.entityInput) {
            promise = Promise.resolve(this.entityInput);
        } else {
            if (this.fetchEntity) {
                promise = this.fetchEntity();
            } else {
                promise = this.idPromise.then(id => {
                    return this.getService().get(id);
                });
            }
        }
        return promise.then(entity => {
            this._entity = entity;
            return entity;
        });
    }

    init(): void {
        const choose = (): Promise<void> => {
            switch (this.mode) {
                case 'create' :
                    return this.initCreate();
                case 'edit' :
                    return this.loadEntity().then(() => this.initEdit());
                case 'view' :
                    return this.loadEntity().then(() => this.initView());
                default:
                    throw Error('mode has to be set!');
            }
        }
        let choosePromise: Promise<void> = choose();
        Promise.all([this.entityPromise, choosePromise]).then(() => {
            if (this.mode != 'create' && this.getTreeSelection) this.treeService.select(this.getTreeSelection());
        });

        choosePromise.then(() => {
            this.footerState = new FooterState(this.mode);
            this.footerState.backButton = this.isMainComponent;
            this.hasEditRight().then(right => this.footerState.canEdit = right);
            this.hasDeleteRight().then(right => this.footerState.canDelete = right);
            this.manageFormSubscriptions();
            if ((this.mode == 'create' || this.mode == 'edit')) {
                if (this.breadcrumbsService.currentStep.isPrefilled("entity")) {
                    this.breadcrumbsService.currentStep.getPrefilledValue("entity").then(res => {
                        this.mapEntityToEntity(res)
                        this.prefillProperties();
                    });
                } else {
                    this.prefillProperties();
                }
            }
            this.entity = this._entity; // to wrap it as proxy after form is set
        });

        // load called tab
        this.subscriptions.push(
            this.activatedRoute.fragment.subscribe(fragment => {
                if (fragment) {
                    this._activeTab = fragment;
                    this.reloadRequiredStyles();
                }
            })
        );
    }

    ngOnChanges(changes: SimpleChanges): void {
        if ((changes['id'] && !changes['id'].isFirstChange()) && this.id !=  changes['id'].previousValue) {
            this.idPromise = new SuperPromise();
            this.entityPromise = new SuperPromise();
            this.init();
        }
        if (changes['id']) {
            this.isMainComponent = false;
            this.idPromise.resolve(this.id);
        }
        if (changes['mode'] && !changes['mode'].isFirstChange()) {
            this.init();
        }
    }

    private manageFormSubscriptions() {
        this.form = this.buildForm();
        this.form$.resolve();
        if (this.form) {
            this.subscriptions.push(
                this.form.statusChanges.subscribe(status => {
                    this.footerState.valid = status == 'VALID' && (this.form.dirty || this.mode == 'create');
                    this.footerState.dirty = this.form.dirty;
                }),
            );
            this.subscribeEntityPropsUpdatesFromForm(this.form, this.entity);
            if (this.mode != 'view') setTimeout(() => this.styleRequiredLabels());
        } else {
            this.footerState.valid = false;
        }
    }

    private subscribeEntityPropsUpdatesFromForm<T extends Entity>(formGroup: FormGroup, entity: T) {
        Object.keys(formGroup.controls).forEach(key => {
            const control = formGroup.get(key);
            if (control instanceof FormGroup) {
                this.subscribeEntityPropsUpdatesFromForm(control, entity ? entity[key] : null);
            } else { 
                let sub: Subscription = control.valueChanges.subscribe(value => {
                    const declaredFields: string[] = getDeclaredFields(entity);
                    if (entity
                            && declaredFields.indexOf(key) >= 0
                            && entity[key] !== value 
                            && !Entity.equals(entity?.[key], value)) {
                        entity[key] = value;
                    }
                });
                this.subscriptions.push(sub);
            }
        });
    }

    private addBCStep() {
        if (!this.embedded) {
            let label: string;
            if (this.mode == "create") label = 'New ' + this.ROUTING_NAME;
            else if (this.mode == 'edit') label = 'Edit ' + this.ROUTING_NAME;
            else if (this.mode == 'view') label = 'View ' + this.ROUTING_NAME;
            this.breadcrumbsService.nameStep(label);
        }
    }

    private styleRequiredLabels() {
        if (this.formContainerElement && this.form?.controls) {
            this.styleRequiredLabelsFromForm(this.form);
        }
    }

    private styleRequiredLabelsFromForm(formGroup: FormGroup) {
        Object.keys(formGroup.controls).forEach(field => {
            const control: AbstractControl = formGroup.get(field);
            if (control instanceof FormGroup) {
                this.styleRequiredLabelsFromForm(control);
            } else {
                if (this.hasRequiredField(control)) {
                    const input = this.formContainerElement.nativeElement.querySelector('li [formControlName="' + field + '"]');
                    if (input) {
                        // adding * to input labels
                        const li = input.closest('li');
                        if (li) {
                            const label = li.querySelector(':scope > label');
                            if (label) label.classList.add('required-label');
                        }
                        // adding * to tab labels
                        const tabName = input.closest('fieldset')?.getAttribute('tab');
                        if (tabName) {
                            const tabLabelElt = this.formContainerElement.nativeElement.querySelector('ul.tabs .' + tabName);
                            tabLabelElt?.classList.add('required-label');
                        }
                    }
                }
            }
        });
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

    private hasRequiredField(abstractControl: AbstractControl): boolean {
        if (abstractControl.validator) {
            const validator = abstractControl.validator({} as AbstractControl);
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
        if (control && control.touched && !control.valid) {
            return control.errors;
        }
    }

    hasError(fieldName: string, errors: string[]) {
        let formError = this.formErrors(fieldName);
        if (formError) {
            for (let errorName of errors) {
                if (formError[errorName]) return true;
            }
        }
        return false;
    }

    private modeSpecificSave(afterSave?: () => Promise<void>): Promise<T> {
        if (this.mode == 'create') {
            return this.getService().create(this.entity).then((entity) => {
                this.entity.id = entity.id;
                this.onSave.next(entity);
                return (afterSave ? afterSave() : Promise.resolve()).then(() => {
                    this.chooseRouteAfterSave(entity);
                    if (entity['name']) {
                        this.consoleService.log('info', this.ROUTING_NAME[0].toUpperCase() + this.ROUTING_NAME.slice(1) + ' ' + entity['name'] + ' has been successfully saved under the id ' + entity.id);
                    } else {
                        this.consoleService.log('info', 'New ' + this.ROUTING_NAME + ' successfully saved with n° ' + entity.id);
                    }
                    this._entity.id = entity.id;
                    return entity;
                });
            });
        } else if (this.mode == 'edit') {
            return this.getService().update(this.entity.id, this.entity).then(() => {
                this.onSave.next(this.entity);
                return (afterSave ? afterSave() : Promise.resolve()).then(() => {
                    this.chooseRouteAfterSave(this.entity);
                    this.consoleService.log('info', this.ROUTING_NAME + ' n°' + this.entity.id + ' successfully updated');
                    return this.entity;
                });
            });
        }
    }

    save(afterSave?: () => Promise<void>): Promise<T> {
        this.footerState.loading = true;
        EntityComponent.mapFormToEntity(this._entity, this.form);
        return this.modeSpecificSave(afterSave)
            .then(entity => {
                this.footerState.loading = false;
                this.treeService.updateTree();
                return entity;
            })
            /* manages "after submit" errors like a unique constraint */
            .catch(reason => {
                this.footerState.loading = false;
                this.catchSavingErrors(reason);
                return null;
            });
    }

    /**
     * Maps the form values to the entity properties.
     * This method should be called after the form is built and before saving the entity.
     */
    protected static mapFormToEntity<T extends Object>(entity: T, form: FormGroup) {
        Object.keys(form.controls).forEach((control) => {
            const name = control as keyof T;
            const declaredFields: string[] = getDeclaredFields(entity);
            if (declaredFields.indexOf(name as string) >= 0) {
                if (form.get(control) instanceof FormGroup && entity[name] instanceof Object) {
                    EntityComponent.mapFormToEntity(entity[name], form.get(control) as FormGroup);
                } else {
                    entity[name] = form.get(control).value;
                }
            }
        });
    };

    /**
     * Maps the current entity properties to the form values.
     * Used to sync the form when the entity is updated outside of the form.
     */
    protected static mapEntityToForm<T extends Object>(entity: T, form: FormGroup) {
        if (form && entity) {
            const declaredFields: string[] = getDeclaredFields(entity);
            Object.keys(form.controls).forEach(control => {
                if (form.get(control).value instanceof FormArray) return; // not supported yet
                const name = control as keyof T;
                if (entity[name] !== undefined) {
                    if (form.get(control) instanceof FormGroup && entity[name] instanceof Object) {
                        EntityComponent.mapEntityToForm(entity[name], form.get(control) as FormGroup);
                    } else if (!!form.get(control)) {
                        form.get(control).setValue(entity[name], {emitEvent: false});
                    }
                } else if (declaredFields.indexOf(name as string) !== -1) {
                    const ctrl = form.get(control);
                    if (ctrl instanceof FormArray) {
                        ctrl.clear({ emitEvent: false }); // vide le tableau
                    } else if (ctrl instanceof FormGroup) {
                        ctrl.reset({}, { emitEvent: false });
                    } else {
                        ctrl.setValue(null, { emitEvent: false });
                    }
                }
            });
        }
    }

    /** Maps an entity to the current entity. Used when restoring an entity from a back navigation. */
    protected mapEntityToEntity(entity: T) {
        if (this.entity && entity) {
            const declaredFields: string[] = getDeclaredFields(this.entity);
            declaredFields.forEach((key) => {
                if (entity[key] !== undefined) {
                    this.entity[key] = entity[key];
                } else {
                    this.entity[key] = null;
                }
            });
        }
    }

    protected catchSavingErrors = (reason: any) => {
        if (reason && reason.error && reason.error.code == 422) {
            this.saveError = new ShanoirError(reason);
            for (let rawFieldName of this.onSubmitValidatedFields) {
                const fieldPath: string[] = rawFieldName.split('.');
                let fieldControl: AbstractControl = this.form;
                fieldPath.forEach(pathPart => {
                    if (fieldControl) {
                        fieldControl = fieldControl.get(pathPart);
                    }
                });
                if (!fieldControl) throw new Error(rawFieldName + ' is not a field managed by this form. Check the arguments of registerOnSubmitValidator().');
                fieldControl.updateValueAndValidity({emitEvent: false});
                if (!fieldControl.valid) fieldControl.markAsTouched();
            }
            this.footerState.valid = this.form.status == 'VALID';
            return null;
        } else {
            throw reason;
        }
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
        } else {
            this.goToView(entity.id);
        }
    }

    /** 
     * Navigate to a create step for an attribute of the current entity.
     * This method will wait for the step to be saved and return the created entity.
     */
    navigateToAttributeCreateStep(route: string, attributeName: string, prefills?: {propName: string, value: any}[], extras?: NavigationExtras): Promise<void> {
        if (prefills) {
            // Add read only properties to the intermediate step
            prefills.forEach(readOnlyProp => {
                this.breadcrumbsService.addNextStepPrefilled('entity.' + readOnlyProp.propName, readOnlyProp.value, true);
            });
        } 
        let currentStep: Step = this.breadcrumbsService.currentStep;
        return this.router.navigate([route], extras).then(success => {
            return firstValueFrom(currentStep.waitFor(this.breadcrumbsService.currentStep)).then(savedDependency => {
                currentStep.addPrefilled('entity.' + attributeName, savedDependency);
                // Then it will be used to automatically prefill the entity when going back to the current step.
                // This has to be done in two methods since 'this' changes when going back, leading to difficulties.
                // see @this.prefillProperties() method
            });
        });
        
    }

    /**
     * Automatically prefill the entity properties with the values stored in the breadcrumbs service.
     */
    protected prefillProperties() {
        const initializedProps = Object.keys(this.entity);
        this.breadcrumbsService.currentStep.getPrefilledKeys().forEach(key => {
            if (key.startsWith('entity.')) {
                const propKey = key.substring(7); // remove 'entity.' prefix
                const propValue = this.breadcrumbsService.currentStep.getPrefilled(key).then(res => {
                    if (res?.value === undefined || res?.value === null) return;
                    let propKeyParts: string[] = propKey.split('.');
                    let currentObj = this.entity;
                    let currentFormObj: FormGroup = this.form; 
                    const lastPart = propKeyParts.pop();
                    propKeyParts.forEach(part => {
                        currentObj = currentObj[part];
                        currentFormObj = currentFormObj.get(part) as FormGroup;
                    });
                    currentObj[lastPart] = res.value;
                    currentFormObj.get(lastPart)?.setValue(res.value, {emitEvent: false});
                    if (res.readonly) currentFormObj.get(lastPart)?.disable({emitEvent: false});
                });
            }
        });
    }

    delete(): void {
        this.openDeleteConfirmDialog(this.entity);
    }

    protected openDeleteConfirmDialog = (entity: T) => {
        let promise: Promise<string>;
        if (this.getOnDeleteConfirmMessage) {
            promise = this.getOnDeleteConfirmMessage(entity);
        } else {
            promise = Promise.resolve('');
        }
        promise.then(studyListStr => {
            this.getService().deleteWithConfirmDialog(this.ROUTING_NAME, entity, studyListStr).then(deleted => {
                if (deleted) {
                    if (this.treeService.treeOpened && this.treeService.treeAvailable) {
                        this.goToParent();
                    } else {
                        this.goBack();
                    }
                }
            });
        });
    }

    goToView(id?: number): void {
        if (!id) {
            if (this.mode == 'view') return;
            else if (this.mode == 'edit') id = this.entity.id;
            else throw new Error('Cannot infer id in create mode, maybe you should give an id to the goToView method');
        }
        let currentRoute: string = this.breadcrumbsService.currentStep?.route?.split('#')[0];
        let replace: boolean = this.breadcrumbsService.currentStep && (
            currentRoute == this.entityRoutes.getRouteToEdit(id)
            || currentRoute == this.entityRoutes.getRouteToCreate()
            // Create route can be contained in incoming route (more arguments for example)
            || currentRoute.indexOf(this.entityRoutes.getRouteToCreate()) != -1);
        this.router.navigate([this.entityRoutes.getRouteToView(id)], {replaceUrl: replace, fragment: this.activeTab});
    }

    goToEdit(id?: number): void {
        if (this.activeTab == "quality" || this.activeTab == "tree" || this.activeTab == "bids") {
            this.activeTab = "general";
        }
        if (!id) {
            if (this.mode == 'edit') return;
            else if (this.mode == 'view') id = this.entity.id;
            else throw new Error('Cannot infer id in create mode, maybe you should give an id to the goToEdit method');
        }
        let replace: boolean = this.breadcrumbsService.currentStep && this.breadcrumbsService.currentStep.route?.split('#')[0] == this.entityRoutes.getRouteToView(id);
        this.router.navigate([this.entityRoutes.getRouteToEdit(id)], {replaceUrl: replace, fragment: this.activeTab});
    }

    goToCreate(): void {
        this.router.navigate([this.entityRoutes.getRouteToCreate()]);
    }

    goToList(): void {
        this.router.navigate([this.entityRoutes.getRouteToList()]);
    }

    goToParent(): void {
        this.treeService.goToParent();
        this.treeService.removeCurrentNode();
    }

    goBack(): void {
        this.location.back();
    }

    public compareEntities(e1: Entity, e2: Entity): boolean {
        return e1 && e2 && e1.id === e2.id;
    }

    ngOnDestroy() {
        this.breadcrumbsService.currentStep.addPrefilled("entity", this.entity);
        for (let subscribtion of this.subscriptions) {
            subscribtion.unsubscribe();
        }
        this.destroy$?.next();
        this.destroy$?.complete();
    }

    public isUserAdmin(): boolean {
        return this.keycloakService.isUserAdmin();
    }

    /**
     * Says if current user has the right to display the edit button.
     * Default is true and this method should be overriden when rights control is needed.
     * It is called after initialization so the entity value can be used inside.
     */
    public async hasEditRight(): Promise<boolean> {
        return this.keycloakService.isUserAdminOrExpert();
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
            console.log('entity', this.entity);
            console.log('form controls:', this.mapFormControls(this.form), 
                this.form.status, this.form.dirty, this.form.touched);
            console.log('footer state', this.footerState);
        }
    }

    private mapFormControls(controls: FormGroup): any[] {
        return Object.entries(controls.controls).map(([k, c]) => {
            if (c instanceof FormGroup) {
                return {k, controls: this.mapFormControls(c)};
            } else {
                return {k, valid: c.valid, errors: c.errors, value: c.value};
            }
        });
    }
}
