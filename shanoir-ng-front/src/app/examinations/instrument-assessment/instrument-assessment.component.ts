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
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";


@Component({
    selector: 'instrument-assessment-detail',
    templateUrl: 'instrument-assessment.component.html'
})

export class InstrumentAssessmentComponent implements OnInit {

    public instAssessemntForm: FormGroup;
    public mode: "view" | "edit" | "create";
    @Output() closing: EventEmitter<any> = new EventEmitter();
    public canModify: Boolean = false;
  
    constructor(private route: ActivatedRoute, private router: Router,
        private fb: FormBuilder, private location: Location,
        private keycloakService: KeycloakService) {

    }

    ngOnInit(): void {
        this.buildForm();
        if (this.keycloakService.isUserAdminOrExpert()) {
            this.canModify = true;
        }
    }



    buildForm(): void {
        this.instAssessemntForm = this.fb.group({
            select: 'select'
        });
        this.instAssessemntForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
    }

    onValueChanged(data?: any) {
        if (!this.instAssessemntForm) { return; }
        const form = this.instAssessemntForm;
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

    };

    back(id?: number): void {
        if (this.closing.observers.length > 0) {
            this.closing.emit(id);
        } else {
            this.location.back();
        }
    }

    add(): void {
      
    }


}