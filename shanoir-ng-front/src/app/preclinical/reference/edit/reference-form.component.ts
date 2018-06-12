import { Component, Input, Output, EventEmitter, OnChanges } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Location } from '@angular/common';

import { Reference }   from '../shared/reference.model';
import { ReferenceService } from '../shared/reference.service';

import { KeycloakService } from "../../../shared/keycloak/keycloak.service";
import { Mode } from "../../shared/mode/mode.model";
import { Modes } from "../../shared/mode/mode.enum";
import { ModesAware } from "../../shared/mode/mode.decorator";
import { ImagesUrlUtil } from "../../../shared/utils/images-url.util";

@Component({
    selector: 'reference-form',
    templateUrl: 'reference-form.component.html',
    styleUrls: ['reference-form.component.css'],
    providers: [ReferenceService]
})
@ModesAware
export class ReferenceFormComponent {

    private reference = new Reference();
    @Output() closing = new EventEmitter();
    newRefForm: FormGroup;
    private mode: Mode = new Mode();
    private refId: number;
    private canModify: Boolean = false;
    categories: string[];
    @Input() reftypes: string[];
    private isFreeCategory: boolean = false;
    private isFreeRefType: boolean = false;
    private isEditableCategory: boolean = true;
    private isEditableRefType: boolean = true;
    private addIconPath: string = ImagesUrlUtil.ADD_ICON_PATH;

    constructor(
        private referenceService: ReferenceService,
        private keycloakService: KeycloakService,
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private location: Location) { }


    loadCategories() {
        this.referenceService.getCategories().then(categories => this.categories = categories);
        this.categories = [];
    }

    loadTypesByCategory(category: string) {
        this.referenceService.getTypesByCategory(category).then(reftypes => {
            this.reftypes = reftypes;

        });
        this.reftypes = [];
    }

    getReference() {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let refId = queryParams['id'];
                let mode = queryParams['mode'];
                let category = queryParams['category'];
                let reftype = queryParams['reftype'];
                if (mode) {
                    this.mode.setModeFromParameter(mode);
                }
                if (refId) {
                    this.refId = refId;
                    return this.referenceService.getReference(refId);
                } else {
                    if (category) {
                        this.reference.category = category;
                        this.isEditableCategory = false;
                        if (!this.isValueInArray(category, this.categories)) {
                            this.isFreeCategory = true;
                            this.isFreeRefType = true;
                        } else {
                            this.loadTypesByCategory(category);
                        }

                    }

                    if (reftype) {
                        this.reference.reftype = reftype;
                        this.isEditableRefType = false;
                        if (!this.isValueInArray(reftype, this.reftypes)) {
                            this.isFreeRefType = true;
                        }
                    } else {
                        this.isFreeRefType = true;
                    }
                    return Observable.of<Reference>();
                }
            })
            .subscribe(reference => {
                if (!this.mode.isCreateMode()) {
                    this.reference = reference;
                    if (this.reference && this.reference.category) this.loadTypesByCategory(this.reference.category);
                }
            });
    }

    onChangeCategory() {
        this.loadTypesByCategory(this.reference.category);
        //Reinitialise reference reftype
        this.reference.reftype = undefined;
    }

    getOut(reference: Reference = null): void {
        if (this.closing.observers.length > 0) {
            this.closing.emit(reference);
            this.location.back();
        } else {
            this.location.back();
        }
    }

    goToEditPage(): void {
        this.router.navigate(['/preclinical-reference'], { queryParams: { id: this.refId, mode: "edit" } });
    }

    isValueInArray(value: string, array: string[]): boolean {
        for (let search in array) {
            if (search == value) return true;
        }
        return false;
    }


    switchToCreate(formControlName: string) {
        if (formControlName == "category") {
            this.mode.createMode();
            this.isFreeCategory = true;
            this.reference.category = undefined;
            this.isFreeRefType = true;
            this.reference.reftype = undefined;
            this.reference.value = undefined;
        } else if (formControlName == "reftype") {
            this.mode.createMode();
            this.isFreeRefType = true;
            this.reference.reftype = undefined;
            this.reference.value = undefined;
        }
    }


    ngOnInit(): void {
        this.loadCategories();
        this.getReference();
        //if(this.reference && this.reference.category) this.loadTypesByCategory(this.reference.category);
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
    }

    buildForm(): void {
        this.newRefForm = this.fb.group({
            'category': [this.reference.category, Validators.required],
            'reftype': [this.reference.reftype, Validators.required],
            'value': [this.reference.value, Validators.required]
        });

        this.newRefForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
    }

    onValueChanged(data?: any) {
        if (!this.newRefForm) { return; }
        const form = this.newRefForm;
        for (const field in this.formErrors) {
            // clear previous error message (if any)
            this.formErrors[field] = '';
            const control = form.get(field);
            if (control && control.dirty && !control.valid) {
                for (const key in control.errors) {
                    this.formErrors[field] += key;
                }
            }
        }
    }

    formErrors = {
        'category': '',
        'reftype': '',
        'value': ''
    };

    addReference() {
        if (!this.reference) { return; }
        this.referenceService.create(this.reference)
            .subscribe(reference => {
                this.getOut(reference);
            });
    }

    updateReference(): void {
        this.referenceService.update(this.reference)
            .subscribe(reference => {
                this.getOut(reference);
            });
    }

}