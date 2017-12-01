import { Component, OnInit, Input, Output, EventEmitter, ViewChild } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';

import { DatasetModalityType } from "../../shared/enums/dataset-modality-type";
import { Enum } from "../../shared/utils/enum";
import { ImagesUrlUtil } from "../../shared/utils/images-url.util";
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { ManufacturerModel } from '../shared/manufacturer-model.model';
import { ManufacturerModelService } from '../shared/manufacturer-model.service';
import { Manufacturer } from '../shared/manufacturer.model';
import { ManufacturerService } from '../shared/manufacturer.service';
import { ModalComponent } from '../../shared/components/modal/modal.component';

@Component({
    selector: 'manufacturer-model-detail',
    templateUrl: 'manufacturer-model.component.html'
})

export class ManufacturerModelComponent implements OnInit {

    private manufModel: ManufacturerModel = new ManufacturerModel();
    public manufModelForm: FormGroup;
    private manufModelId: number;
    public mode: "view" | "edit" | "create";
    @Input() modeFromAcqEquip: "view" | "edit" | "create";
    @Output() closing: EventEmitter<any> = new EventEmitter();
    @ViewChild('manufModal') manufModal: ModalComponent;
    private isNameUnique: Boolean = true;
    public canModify: Boolean = false;
    private datasetModalityTypes: Enum[] = [];
    public isMR: Boolean = false;
    private manufs: Manufacturer[];
    private addIconPath: string = ImagesUrlUtil.ADD_ICON_PATH;
    private datasetModalityTypeEnumValue: string;

    constructor(private route: ActivatedRoute, private router: Router,
        private manufModelService: ManufacturerModelService, private manufService: ManufacturerService,
        private fb: FormBuilder, private location: Location,
        private keycloakService: KeycloakService) {

    }

    ngOnInit(): void {
        if (this.modeFromAcqEquip) { this.mode = this.modeFromAcqEquip; }
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
        for (var i = 0; i < types.length; i = i + 2) {
            var enum2: Enum = new Enum();
            enum2.key = types[i];
            enum2.value = DatasetModalityType[types[i]];
            this.datasetModalityTypes.push(enum2);
        }
    }

    getManufs(manufId?: number): void {
        this.manufService
            .getManufacturers()
            .then(manufs => {
                this.manufs = manufs;
                if (manufId) {
                    for (let manuf of this.manufs) {
                        if (manufId == manuf.id) {
                            this.manufModel.manufacturer = manuf;
                            break;
                        }
                    }
                }
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
                if (!this.modeFromAcqEquip) {
                    let mode = queryParams['mode'];
                    if (mode) {
                        this.mode = mode;
                    }
                }
                if (manufModelId && this.mode !== 'create') {
                    // view or edit mode
                    this.manufModelId = manufModelId;
                    return this.manufModelService.getManufacturerModel(manufModelId);
                } else {
                    // create mode
                    return Observable.of<ManufacturerModel>();
                }
            })
            .subscribe((manufModel: ManufacturerModel) => {
                if (this.mode == "edit") {
                    manufModel.manufacturer = this.getManufById(manufModel.manufacturer.id);
                }
                this.manufModel = manufModel;
                this.datasetModalityTypeEnumValue = DatasetModalityType[this.manufModel.datasetModalityType];
                this.checkDatasetModalityType(this.datasetModalityTypeEnumValue);
            });
    }

    buildForm(): void {
        let magneticFieldFC: FormControl;
        if (this.isMR) {
            magneticFieldFC = new FormControl(this.manufModel.magneticField, Validators.required);
        } else {
            magneticFieldFC = new FormControl(this.manufModel.magneticField);
        }
        this.manufModelForm = this.fb.group({
            'name': [this.manufModel.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
            'manufacturer': [this.manufModel.manufacturer, Validators.required],
            'magneticField': magneticFieldFC,
            'datasetModalityType': [this.manufModel.datasetModalityType, Validators.required]
        });
        this.manufModelForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
    }

    onValueChanged(data?: any) {
        if (!this.manufModelForm) { return; }
        const form = this.manufModelForm;
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

    onSelect(datasetModalityType: string) {
        this.checkDatasetModalityType(datasetModalityType);
        this.buildForm();
    }

    checkDatasetModalityType(datasetModalityType: string) {
        if (datasetModalityType.indexOf("MR") != -1) {
            this.isMR = true;
        } else {
            this.isMR = false;
        }
    }

    formErrors = {
        'manufacturer': '',
        'name': '',
        'datasetModalityType': '',
        'magneticField': ''
    };

    back(manufModelId?: number): void {
        if (this.closing.observers.length > 0) {
            this.manufModel = new ManufacturerModel();
            this.isMR = false;
            this.closing.emit(manufModelId);
        } else {
            this.location.back();
        }
    }

    edit(): void {
        this.router.navigate(['/manufacturer-model'], { queryParams: { id: this.manufModelId, mode: "edit" } });
    }

    create(): void {
        this.manufModel = this.manufModelForm.value;
        if (!this.isMR) {
            this.manufModel.magneticField = null;
        }
        this.manufModelService.create(this.manufModel)
            .subscribe((manufModel) => {
                this.back(manufModel.id);
            }, (err: String) => {
                if (err.indexOf("name should be unique") != -1) {
                    this.isNameUnique = false;
                }
            });
    }

    update(): void {
        this.manufModel = this.manufModelForm.value;
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

    getManufById(id: number): Manufacturer {
        for (let manuf of this.manufs) {
            if (id == manuf.id) {
                return manuf;
            }
        }
        return null;
    }

    closePopin(manufId?: number) {
        this.manufModal.hide();
        if (manufId) {
            this.getManufs(manufId);
        }
    }
}