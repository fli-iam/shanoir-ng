import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';

import { ManufacturerModel } from '../../shared/manufModel.model';
import { AcquisitionEquipmentService } from '../../shared/acqEquip.service';
import { KeycloakService } from "../../../shared/keycloak/keycloak.service";
import { DatasetModalityType } from "../../../shared/enum/datasetModalityType";
import { Enum } from "../../../shared/utils/enum";

@Component({
    selector: 'manufModelDetail',
    templateUrl: 'manufModel.detail.component.html'
})

export class ManufacturerModelDetailComponent implements OnInit {
    
    @Output() closing = new EventEmitter();
    @Input() modeFromCenterList: "view" | "edit" | "create";
    private manufModel: ManufacturerModel = new ManufacturerModel();
    private manufModelDetailForm: FormGroup;
    private manufModelId: number;
    private mode: "view" | "edit" | "create";
    private isNameUnique: Boolean = true;
    private canModify: Boolean = false;
    private datasetModalityTypes: Enum[] = []; 

    constructor (private route: ActivatedRoute, private router: Router,
        private acqEquipService: AcquisitionEquipmentService,   private fb: FormBuilder,
        private location: Location, private keycloakService: KeycloakService) {

    }

    ngOnInit(): void {
        if (this.modeFromCenterList) {this.mode = this.modeFromCenterList;}
        this.getEnum();
        // this.getManufacturerModel();
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
    }

    getEnum(): void {
        var types = Object.keys(DatasetModalityType);
        for (var i = 0; i < types.length; i = i+2) {
            var enum2: Enum = new Enum();
            enum2.key = types[i];
            enum2.value = DatasetModalityType[types[i]];
            this.datasetModalityTypes.push(enum2);
        }
    }

    // getManufacturerModel(): void {
    //     this.route.queryParams
    //         .switchMap((queryParams: Params) => {
    //             let manufModelId = queryParams['id'];
    //             let mode = queryParams['mode'];
    //             if (mode) {
    //                 this.mode = mode;
    //             }
    //             if (manufModelId) {
    //                 // view or edit mode
    //                 this.manufModelId = manufModelId;
    //                 return this.acqEquipService.getManufacturerModel(manufModelId);
    //             } else { 
    //                 // create mode
    //                 return Observable.of<ManufacturerModel>();
    //             }
    //         })
    //         .subscribe((manufModel: ManufacturerModel) => {
    //             this.manufModel = manufModel;
    //         });
    // }   

    buildForm(): void {
        this.manufModelDetailForm = this.fb.group({
            'name': [this.manufModel.name],
            'magneticField': [this.manufModel.magneticField],
            'datasetModalityType': [this.manufModel.datasetModalityType]
        });
        this.manufModelDetailForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
    }

    onValueChanged(data?: any) {
        if (!this.manufModelDetailForm) { return; }
        const form = this.manufModelDetailForm;
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

    back(): void {
        //this.location.back();
        this.getOut();
    }

    edit(): void {
        this.router.navigate(['/manufModelDetail'], { queryParams: {id: this.manufModelId, mode: "edit"}});
    }

    // create(): void {
    //     this.manufModel = this.manufModelDetailForm.value;
    //     this.acqEquipService.create(this.manufModel)
    //     .subscribe((manufModel) => {
    //         this.getOut();
    //     }, (err: String) => {
    //         if (err.indexOf("name should be unique") != -1) {
    //             this.isNameUnique = false;
    //         }
    //     });
    // }

    // update(): void {
    //     this.manufModel = this.manufModelDetailForm.value;
    //     this.acqEquipService.update(this.manufModelId, this.manufModel)
    //     .subscribe((manufModel) => {
    //         this.getOut();
    //     }, (err: String) => {
    //         if (err.indexOf("name should be unique") != -1) {
    //             this.isNameUnique = false;
    //         }
    //     });
    // }

    getOut(manufModel: ManufacturerModel = null): void {
        if (this.closing.observers.length > 0) {
            this.closing.emit(manufModel);
        } else {
            this.location.back();
        }
    }

}