import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';

import { AcquisitionEquipment } from '../shared/acqEquip.model';
import { AcquisitionEquipmentService } from '../shared/acqEquip.service';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";

const MMNS : String [] = ['Signal - GE', 'Vivio - SIEMENS', 'Test - WhyNot'];

@Component({
    selector: 'acqEquipDetail',
    templateUrl: 'acqEquip.detail.component.html'
})

export class AcquisitionEquipmentDetailComponent implements OnInit {
    
    @Output() closing = new EventEmitter();
    private acqEquip: AcquisitionEquipment = new AcquisitionEquipment();
    private acqEquipDetailForm: FormGroup;
    private acqEquipId: number;
    private mode: "view" | "edit" | "create";
    private isNameUnique: Boolean = true;
    private canModify: Boolean = false;
    private manuModelNames = MMNS;
    

    constructor (private route: ActivatedRoute, private router: Router,
        private acqEquipService: AcquisitionEquipmentService,   private fb: FormBuilder,
        private location: Location, private keycloakService: KeycloakService) {

    }

    ngOnInit(): void {
        this.getAcquisitionEquipment();
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
    }

    getAcquisitionEquipment(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let acqEquipId = queryParams['id'];
                let mode = queryParams['mode'];
                if (mode) {
                    this.mode = mode;
                }
                if (acqEquipId) {
                    // view or edit mode
                    this.acqEquipId = acqEquipId;
                    return this.acqEquipService.getAcquisitionEquipment(acqEquipId);
                } else { 
                    // create mode
                    return Observable.of<AcquisitionEquipment>();
                }
            })
            .subscribe((acqEquip: AcquisitionEquipment) => {
                this.acqEquip = acqEquip;
            });
    }   

    buildForm(): void {
        this.acqEquipDetailForm = this.fb.group({
            'serialNumber': [this.acqEquip.serialNumber],
            'manuModelName': [this.acqEquip.manuModelName]
        });
        this.acqEquipDetailForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
    }

    onValueChanged(data?: any) {
        if (!this.acqEquipDetailForm) { return; }
        const form = this.acqEquipDetailForm;
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
        'manuModelName': ''
    };

    back(): void {
        //this.location.back();
        this.getOut();
    }

    edit(): void {
        this.router.navigate(['/acqEquipDetail'], { queryParams: {id: this.acqEquipId, mode: "edit"}});
    }

    create(): void {
        this.acqEquip = this.acqEquipDetailForm.value;
        this.acqEquipService.create(this.acqEquip)
        .subscribe((acqEquip) => {
            this.getOut();
        }, (err: String) => {
            if (err.indexOf("name should be unique") != -1) {
                this.isNameUnique = false;
            }
        });
    }

    update(): void {
        this.acqEquip = this.acqEquipDetailForm.value;
        this.acqEquipService.update(this.acqEquipId, this.acqEquip)
        .subscribe((acqEquip) => {
            this.getOut();
        }, (err: String) => {
            if (err.indexOf("name should be unique") != -1) {
                this.isNameUnique = false;
            }
        });
    }

    getOut(acqEquip: AcquisitionEquipment = null): void {
        if (this.closing.observers.length > 0) {
            this.closing.emit(acqEquip);
        } else {
            this.location.back();
        }
    }

}