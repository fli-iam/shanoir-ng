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