import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';

import { ManufacturerModel } from '../../shared/manufModel.model';
import { ManufacturerModelService } from '../../shared/manufModel.service';
import { Manufacturer } from '../../shared/manuf.model';
import { ManufacturerService } from '../../shared/manuf.service';
import { KeycloakService } from "../../../shared/keycloak/keycloak.service";
import { DatasetModalityType } from "../../../shared/enum/datasetModalityType";
import { Enum } from "../../../shared/utils/enum";

@Component({
    selector: 'manufModelDetail',
    templateUrl: 'manufModel.detail.component.html'
})

export class ManufacturerModelDetailComponent implements OnInit {
    
    private manufModel: ManufacturerModel = new ManufacturerModel();
    private manufModelDetailForm: FormGroup;
    private manufModelId: number;
    private mode: "view" | "edit" | "create";
    private isNameUnique: Boolean = true;
    private canModify: Boolean = false;
    private datasetModalityTypes: Enum[] = []; 
    private isMR: Boolean = false;
    private manufs: Manufacturer[];

    constructor (private route: ActivatedRoute, private router: Router,
        private manufModelService: ManufacturerModelService, private manufService: ManufacturerService,   
        private fb: FormBuilder, private location: Location, 
        private keycloakService: KeycloakService) {

    }

    ngOnInit(): void {
        this.getEnum();
        this.getManufs();
        this.getManufacturerModel();
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

    getManufs(): void {
    this.manufService
        .getManufacturers()
        .then(manufs => {
            this.manufs = manufs;
        })
        .catch((error) => {
            // TODO: display error
            console.log("error getting manufacturer list!");
        });
    }

    getManufacturerModel(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let manufModelId = queryParams['id'];
                let mode = queryParams['mode'];
                if (mode) {
                    this.mode = mode;
                }
                if (manufModelId) {
                    // view or edit mode
                    this.manufModelId = manufModelId;
                    return this.manufModelService.getManufacturerModel(manufModelId);
                } else { 
                    // create mode
                    return Observable.of<ManufacturerModel>();
                }
            })
            .subscribe((manufModel: ManufacturerModel) => {
                this.manufModel = manufModel;
            });
    }   

    buildForm(): void {
        let magneticFieldFC: FormControl;
        if (this.isMR) {
            magneticFieldFC = new FormControl('this.manufModel.magneticField', Validators.required);
        } else {
            magneticFieldFC = new FormControl('this.manufModel.magneticField');
        }
        this.manufModelDetailForm = this.fb.group({
            'name': [this.manufModel.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
            'manufacturer': [this.manufModel.manufacturer, Validators.required],
            'magneticField': magneticFieldFC,
            'datasetModalityType': [this.manufModel.datasetModalityType, Validators.required]
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

    onSelect(datasetModalityType) {
        if (datasetModalityType.indexOf("MR") != -1) {
            this.isMR = true;
        } else {
            this.isMR = false;
        }
         this.buildForm();
    }

    formErrors = {
        'manufacturer': '',
        'name': '',
        'datasetModalityType': '',
        'magneticField': ''
    };

    back(): void {
        this.location.back();
        // this.getOut();
    }

    edit(): void {
        this.router.navigate(['/manufModelDetail'], { queryParams: {id: this.manufModelId, mode: "edit"}});
    }

    create(): void {
        this.manufModel = this.manufModelDetailForm.value;
        if (!this.isMR) {
            this.manufModel.magneticField = null;
        }
        this.manufModelService.create(this.manufModel)
        .subscribe((manufModel) => {
            this.back();
        }, (err: String) => {
            if (err.indexOf("name should be unique") != -1) {
                this.isNameUnique = false;
            }
        });
    }

    update(): void {
        this.manufModel = this.manufModelDetailForm.value;
        if (!this.isMR) {
            this.manufModel.magneticField = null;
        }
        this.manufModelService.update(this.manufModelId, this.manufModel)
        .subscribe((manufModel) => {
            this.back();
        }, (err: String) => {
            if (err.indexOf("name should be unique") != -1) {
                this.isNameUnique = false;
            }
        });
    }

    // getOut(manufModel: ManufacturerModel = null): void {
    //     if (this.closing.observers.length > 0) {
    //         this.closing.emit(manufModel);
    //     } else {
    //         this.location.back();
    //     }
    // }

}