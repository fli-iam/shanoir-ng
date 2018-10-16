import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

import { Manufacturer } from '../shared/manufacturer.model';
import { ManufacturerService } from '../shared/manufacturer.service';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { FooterState } from '../../shared/components/form-footer/footer-state.model';

@Component({
    selector: 'manufacturer-detail',
    templateUrl: 'manufacturer.component.html'
})

export class ManufacturerComponent implements OnInit {
    
    private manuf: Manufacturer = new Manufacturer();
    public manufForm: FormGroup;
    private id: number;
    @Input() mode: "view" | "edit" | "create";
    @Output() closing: EventEmitter<any> = new EventEmitter();
    private isNameUnique: Boolean = true;
    private footerState: FooterState;

    constructor (private route: ActivatedRoute, private router: Router,
        private manufService: ManufacturerService,   private fb: FormBuilder,
        private location: Location, private keycloakService: KeycloakService) {
            this.mode = this.route.snapshot.data['mode'];
            this.id = +this.route.snapshot.params['id'];
    }

    ngOnInit(): void {
        this.getManufacturer();
        this.footerState = new FooterState(this.mode, this.keycloakService.isUserAdminOrExpert());
    }

    getManufacturer(): void {
        if (this.mode == 'create') {
            this.manuf = new Manufacturer();
        } else {
            this.manufService.getManufacturer(this.id).then((manuf: Manufacturer) => {
                this.manuf = manuf;
                this.buildForm();
            });
        }
    }   

    buildForm(): void {
        this.manufForm = this.fb.group({
            'name': [this.manuf.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200)]]
        });
        this.manufForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
        this.manufForm.statusChanges.subscribe(status => this.footerState.valid = status == 'VALID');
    }

    onValueChanged(data?: any) {
        if (!this.manufForm) { return; }
        const form = this.manufForm;
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

    back(manufId?: number): void {
        if (this.closing.observers.length > 0) {
            this.manuf = new Manufacturer();
            this.closing.emit(manufId);
        } else {
            this.location.back();
        }
    }

    edit(): void {
        this.router.navigate(['/manufacturer/edit/', this.id]);
    }

    create(): void {
        this.manuf = this.manufForm.value;
        this.manufService.create(this.manuf)
        .subscribe((manuf) => {
            this.back(manuf.id);
        }, (err: String) => {
            if (err.indexOf("name should be unique") != -1) {
                this.isNameUnique = false;
            }
        });
    }

    update(): void {
        this.manuf = this.manufForm.value;
        this.manufService.update(this.id, this.manuf)
        .subscribe((manuf) => {
            this.back();
        }, (err: String) => {
            if (err.indexOf("name should be unique") != -1) {
                this.isNameUnique = false;
            }
        });
    }
}