import { Component, OnInit, Input, Output, EventEmitter, ViewChild } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';

import { KeycloakService } from "../../shared/keycloak/keycloak.service";

@Component({
    selector: 'new-instrument-form',
    templateUrl: 'new-instrument.component.html'
})

export class NewInstrumentComponent implements OnInit {
    

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
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
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