import { Component, Output, EventEmitter } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Location } from '@angular/common';

import { Pathology }    from '../shared/pathology.model';
import { PathologyService } from '../shared/pathology.service';

import { KeycloakService } from "../../../../shared/keycloak/keycloak.service";
import { Mode } from "../../../shared/mode/mode.model";
import { Modes } from "../../../shared/mode/mode.enum";
import { ModesAware } from "../../../shared/mode/mode.decorator";

@Component({
    selector: 'pathology-form',
    templateUrl: 'pathology-form.component.html',
    providers: [PathologyService]
})
@ModesAware
export class PathologyFormComponent {

    private pathology = new Pathology();
    @Output() closing = new EventEmitter();
    newPathologyForm: FormGroup;
    private mode: Mode = new Mode();
    private pathoId: number;
    private canModify: Boolean = false;
    private isPathologyUnique: Boolean = true;

    constructor(
        private pathologyService: PathologyService,
        private keycloakService: KeycloakService,
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private location: Location) { }


    getOut(pathology: Pathology = null): void {
        if (this.closing.observers.length > 0) {
            this.closing.emit(pathology);
            this.location.back();
        } else {
            this.location.back();
        }
    }


    getPathology(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let pathoId = queryParams['id'];
                let mode = queryParams['mode'];
                if (mode) {
                    this.mode.setModeFromParameter(mode);
                }
                if (pathoId) {
                    // view or edit mode
                    this.pathoId = pathoId;
                    return this.pathologyService.getPathology(pathoId);
                } else {
                    // create mode
                    return Observable.of<Pathology>();
                }
            })
            .subscribe(pathology => {
                if (!this.mode.isCreateMode()) {
                    this.pathology = pathology;
                }
            });
    }


    goToEditPage(): void {
        this.router.navigate(['/preclinical-pathology'], { queryParams: { id: this.pathoId, mode: "edit" } });
    }

    ngOnInit(): void {
        this.getPathology();
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
    }

    buildForm(): void {
        this.newPathologyForm = this.fb.group({
            'name': [this.pathology.name, Validators.required]
        });

        this.newPathologyForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged();
    }

    onValueChanged(data?: any) {
        if (!this.newPathologyForm) { return; }
        const form = this.newPathologyForm;
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
        'name': ''
    };

    addPathology() {
        if (!this.pathology) { return; }
        this.pathologyService.create(this.pathology)
            .subscribe(pathology => {
                this.getOut(pathology);
            }, (err: String) => {
                console.log('error in update ' + err);
                if (err.indexOf("should be unique") != -1) {
                    this.isPathologyUnique = false;
                }
            });
    }

    updatePathology(): void {
        this.pathologyService.update(this.pathology)
            .subscribe(pathology => {
                this.getOut(pathology);
            }, (err: String) => {
                console.log('error in update ' + err);
                if (err.indexOf("should be unique") != -1) {
                    this.isPathologyUnique = false;
                }
            });
    }

}