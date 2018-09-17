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
import { FooterState } from '../../shared/components/form-footer/footer-state.model';

@Component({
    selector: 'acquisition-equipment-detail',
    templateUrl: 'acquisition-equipment.component.html'
})

export class AcquisitionEquipmentComponent implements OnInit {

    @Output() closing: EventEmitter<any> = new EventEmitter();
    @Input() mode: "view" | "edit" | "create";
    @ViewChild('manufModelModal') manufModelModal: ModalComponent;
    private acqEquip: AcquisitionEquipment;
    public acqEquipForm: FormGroup;
    private id: number;
    public isModelNumberUnique: Boolean = true;
    private manufModels: ManufacturerModel[];
    private centers: Center[];
    private datasetModalityTypeEnumValue: String;
    private addIconPath: string = ImagesUrlUtil.ADD_ICON_PATH;
    public infoIconPath: string = ImagesUrlUtil.INFO_ICON_PATH;
    private footerState: FooterState;
    
    constructor(
            private route: ActivatedRoute, 
            private router: Router,
            private acqEquipService: AcquisitionEquipmentService, 
            private fb: FormBuilder,
            private manufModelService: ManufacturerModelService,
            private centerService: CenterService,
            private location: Location, 
            private keycloakService: KeycloakService) {

        this.mode = this.route.snapshot.data['mode'];
        this.id = +this.route.snapshot.params['id'];   

    }

    ngOnInit(): void {
        this.getManufModels();
        Promise.all([
            this.getCenters(),
            this.getAcquisitionEquipment()
        ]).then(() => {
            if (this.mode == "edit") {
                // Link to objects coming from list requests to display selected item of drop-down list
                this.acqEquip.center = this.getCenterById(this.acqEquip.center.id);
                this.acqEquip.manufacturerModel = this.getManufModelById(this.acqEquip.manufacturerModel.id);
                this.datasetModalityTypeEnumValue = DatasetModalityType[this.acqEquip.manufacturerModel.datasetModalityType];
            }
            this.buildForm();
        });
        this.footerState = new FooterState(
            this.mode,
            this.keycloakService.isUserAdminOrExpert()
        );
    }

    getAcquisitionEquipment(): Promise<void> {
        if (this.mode == 'create') {
            this.acqEquip = new AcquisitionEquipment();
            return Promise.resolve();
        } else {
            return this.acqEquipService.getAcquisitionEquipment(this.id).then(ae => {
                this.acqEquip = ae;
            });
        }
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
            });
    }

    getCenters(): Promise<void> {
        return this.centerService
            .getCentersNames()
            .then(centers => {
                this.centers = centers;
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
        this.acqEquipForm.statusChanges.subscribe(status => this.footerState.valid = status == 'VALID');
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
        this.router.navigate(['/acquisition-equipment/edit/' + this.id]);
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
        this.acqEquipService.update(this.id, this.acqEquip)
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