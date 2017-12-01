import { Component, OnInit, Input, Output, EventEmitter, ViewChild } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';

import { AcquisitionEquipment } from '../shared/acquisition-equipment.model';
import { AcquisitionEquipmentService } from '../shared/acquisition-equipment.service';
import { Center } from '../../centers/shared/center.model';
import { CenterService } from '../../centers/shared/center.service';
import { DatasetModalityType } from "../../shared/enums/dataset-modality-type";
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { ImagesUrlUtil } from "../../shared/utils/images-url.util";
import { ManufacturerModel } from '../shared/manufacturer-model.model';
import { ManufacturerModelService } from '../shared/manufacturer-model.service';
import { ManufacturerService } from '../shared/manufacturer.service';
import { ModalComponent } from '../../shared/components/modal/modal.component';

@Component({
    selector: 'acquisition-equipment-detail',
    templateUrl: 'acquisition-equipment.component.html'
})

export class AcquisitionEquipmentComponent implements OnInit {

    @Output() closing: EventEmitter<any> = new EventEmitter();
    @Input() modeFromCenterList: "view" | "edit" | "create";
    @ViewChild('manufModelModal') manufModelModal: ModalComponent;
    private acqEquip: AcquisitionEquipment = new AcquisitionEquipment();
    public acqEquipForm: FormGroup;
    private acqEquipId: number;
    public mode: "view" | "edit" | "create";
    public isModelNumberUnique: Boolean = true;
    public canModify: Boolean = false;
    private manufModels: ManufacturerModel[];
    private centers: Center[];
    private datasetModalityTypeEnumValue: String;
    private addIconPath: string = ImagesUrlUtil.ADD_ICON_PATH;
    public infoIconPath: string = ImagesUrlUtil.INFO_ICON_PATH;
    
    constructor(private route: ActivatedRoute, private router: Router,
        private acqEquipService: AcquisitionEquipmentService, private fb: FormBuilder,
        private manufService: ManufacturerService, private manufModelService: ManufacturerModelService,
        private centerService: CenterService,
        private location: Location, private keycloakService: KeycloakService) {

    }

    ngOnInit(): void {
        if (this.modeFromCenterList) { this.mode = this.modeFromCenterList; }
        this.getManufModels();
        this.getCenters();
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
                if (this.mode == "edit") {
                    acqEquip.center = this.getCenterById(acqEquip.center.id);
                    acqEquip.manufacturerModel = this.getManufModelById(acqEquip.manufacturerModel.id);
                }
                this.acqEquip = acqEquip;
                this.datasetModalityTypeEnumValue = DatasetModalityType[this.acqEquip.manufacturerModel.datasetModalityType];
            });
    }

    getManufModels(manufModelId?: number): void {
        this.manufModelService
            .getManufacturerModels()
            .then(manufModels => {
                this.manufModels = manufModels;
                if (manufModelId) {
                    for (let manufModel of this.manufModels) {
                        if (manufModelId == manufModel.id) {
                            this.acqEquip.manufacturerModel = manufModel;
                            break;
                        }
                    }
                }
            })
            .catch((error) => {
                // TODO: display error
                console.log("error getting manufacturer model list!");
            });
    }

    getCenters(): void {
        this.centerService
            .getCentersNames()
            .then(centers => {
                this.centers = centers;
                this.getAcquisitionEquipment();
            })
            .catch((error) => {
                // TODO: display error
                console.log("error getting center list!");
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

    getCenterById(id: number): Center {
        for (let center of this.centers) {
            if (id == center.id) {
                return center;
            }
        }
        return null;
    }

    buildForm(): void {
        this.acqEquipForm = this.fb.group({
            'serialNumber': [this.acqEquip.serialNumber],
            'manufacturerModel': [this.acqEquip.manufacturerModel, Validators.required],
            'center': [this.acqEquip.center, Validators.required]
        });
        this.acqEquipForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
    }

    onValueChanged(data?: any) {
        if (!this.acqEquipForm) { return; }
        const form = this.acqEquipForm;
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
        'manufacturerModel': '',
        'center': ''
    };

    back(): void {
        if (this.closing.observers.length > 0) {
            this.closing.emit(null);
        } else {
            this.location.back();
        }
    }

    edit(): void {
        this.router.navigate(['/acquisition-equipment'], { queryParams: { id: this.acqEquipId, mode: "edit" } });
    }

    create(): void {
        this.acqEquip = this.acqEquipForm.value;
        this.acqEquipService.create(this.acqEquip)
            .subscribe((acqEquip) => {
                this.back();
            }, (err: String) => {
                if (err.indexOf("should be unique") != -1) {
                    this.isModelNumberUnique = false;
                }
            });
    }

    update(): void {
        this.acqEquip = this.acqEquipForm.value;
        this.acqEquipService.update(this.acqEquipId, this.acqEquip)
            .subscribe((acqEquip) => {
                this.back();
            }, (err: String) => {
                if (err.indexOf("should be unique") != -1) {
                    this.isModelNumberUnique = false;
                }
            });
    }

    closePopin(manufModelId?: number) {
        this.manufModelModal.hide();
        if (manufModelId) {
            this.getManufModels(manufModelId);
        }
    }
}