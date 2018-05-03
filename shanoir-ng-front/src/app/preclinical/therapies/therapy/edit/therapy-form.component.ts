import { Component, Output, EventEmitter } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Location } from '@angular/common';

import { Therapy }    from '../shared/therapy.model';
import { TherapyService } from '../shared/therapy.service';

import { ReferenceService } from '../../../reference/shared/reference.service';
import { Reference }    from '../../../reference/shared/reference.model';

import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { KeycloakService } from "../../../../shared/keycloak/keycloak.service";

import { Enum } from "../../../../shared/utils/enum";
import { TherapyType } from "../../../shared/enum/therapyType";
import { EnumUtils } from "../../../shared/enum/enumUtils";

import { Mode } from "../../../shared/mode/mode.model";
import { Modes } from "../../../shared/mode/mode.enum";
import { ModesAware } from "../../../shared/mode/mode.decorator";


@Component({
    selector: 'therapy-form',
    templateUrl: 'therapy-form.component.html',
    providers: [TherapyService, ReferenceService]
})
@ModesAware
export class TherapyFormComponent {

    private therapy = new Therapy();
    @Output() closing = new EventEmitter();
    newTherapyForm: FormGroup;
    private mode:Mode = new Mode();
    private therapyId: number;
    private canModify: Boolean = false;
    therapyTypes: Enum[] = [];
    private isTherapyUnique: Boolean = true;
    
    constructor(
        private therapyService: TherapyService,
        private referenceService: ReferenceService,
        private keycloakService: KeycloakService,
        private enumUtils: EnumUtils,
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private location: Location) { }


    getOut(therapy: Therapy = null): void {
        if (this.closing.observers.length > 0) {
            this.closing.emit(therapy);
            this.location.back();
        } else {
            this.location.back();
        }
    }
    
    getTherapy(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let therapyId = queryParams['id'];
                let mode = queryParams['mode'];
                if (mode) {
                    this.mode.setModeFromParameter(mode);
                }
                if (therapyId) {
                    // view or edit mode
                    this.therapyId = therapyId;
                    return this.therapyService.getTherapy(therapyId);
                } else {
                    // create mode
                    return Observable.of<Therapy>();
                }
            })
            .subscribe(therapy => {
                if (!this.mode.isCreateMode()) {
                    this.therapy = therapy;
                }
            });
    }

    goToEditPage(): void {
        this.router.navigate(['/preclinical/therapy'], { queryParams: { id: this.therapyId, mode: "edit" } });
    }
    
    ngOnInit(): void {
        this.getEnums();
        this.getTherapy();
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
    }

    getEnums(): void {
        this.therapyTypes = this.enumUtils.getEnumArrayFor('TherapyType');
    }


    buildForm(): void {
        this.newTherapyForm = this.fb.group({
            'name': [this.therapy.name, Validators.required],
            'therapyType': [this.therapy.therapyType, Validators.required],
            'comment': [this.therapy.comment]
        });

        this.newTherapyForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged();
    }

    onValueChanged(data?: any) {
        if (!this.newTherapyForm) { return; }
        const form = this.newTherapyForm;
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
        'name': '',
        'therapyType': ''
    };

    addTherapy() {
        if (!this.therapy) { return; }
        this.therapyService.create(this.therapy)
            .subscribe(therapy => {
                this.getOut(therapy);
            }, (err: String) => {
                console.log('error in update ' + err);
                if (err.indexOf("should be unique") != -1) {
                    this.isTherapyUnique = false;
                }
            });
    }

    updateTherapy(): void {
        this.therapyService.update(this.therapy)
            .subscribe(therapy => {
                this.getOut(therapy);
            }, (err: String) => {
                console.log('error in update ' + err);
                if (err.indexOf("should be unique") != -1) {
                    this.isTherapyUnique = false;
                }
            });
    }

}