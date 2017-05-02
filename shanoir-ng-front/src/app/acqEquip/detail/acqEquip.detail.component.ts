import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';

import { AcquisitionEquipment } from '../shared/acqEquip.model';
import { AcquisitionEquipmentService } from '../shared/acqEquip.service';
import { ManufacturerService } from '../shared/manuf.service';
import { ManufacturerModelService } from '../shared/manufModel.service';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { ManufacturerModel } from '../shared/manufModel.model';

@Component({
    selector: 'acqEquipDetail',
    templateUrl: 'acqEquip.detail.component.html'
})

export class AcquisitionEquipmentDetailComponent implements OnInit {
    
    @Output() closing = new EventEmitter();
    @Input() modeFromCenterList: "view" | "edit" | "create";
    private acqEquip: AcquisitionEquipment = new AcquisitionEquipment();
    private acqEquipDetailForm: FormGroup;
    private acqEquipId: number;
    private mode: "view" | "edit" | "create";
    private isNameUnique: Boolean = true;
    private canModify: Boolean = false;
    private manufModels: ManufacturerModel[];

    constructor (private route: ActivatedRoute, private router: Router,
        private acqEquipService: AcquisitionEquipmentService, private fb: FormBuilder,
        private manufService: ManufacturerService, private manufModelService: ManufacturerModelService,
        private location: Location, private keycloakService: KeycloakService) {

    }

    ngOnInit(): void {
        if (this.modeFromCenterList) {this.mode = this.modeFromCenterList;}
        this.getManufModels();
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

    getManufModels(): void {
        this.manufModelService
            .getManufacturerModels()
            .then(manufModels => {
                this.manufModels = manufModels;
            })
            .catch((error) => {
                // TODO: display error
                console.log("error getting manufacturer models list!");
            });
    }

    getManufModelById(id: number): ManufacturerModel {
        for (let manufModel of this.manufModels) {
            if (id == manufModel.id) {
                return manufModel;
            }
        }
        return null;
    }

    buildForm(): void {
        this.acqEquipDetailForm = this.fb.group({
            'serialNumber': [this.acqEquip.serialNumber],
            'manufModel': [this.acqEquip.manufacturerModel, Validators.required],
            'center': [this.acqEquip.center, Validators.required]
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
       'manufModel': '',
       'center': ''
    };

    back(): void {
        //this.location.back();
        this.getOut();
    }

    edit(): void {
        this.router.navigate(['/acqEquipDetail'], {queryParams: {id: this.acqEquipId, mode: "edit"}});
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