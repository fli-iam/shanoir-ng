import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';

import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { Center } from '../../centers/shared/center.model';
import { CenterService } from '../../centers/shared/center.service';
import { Coil } from '../shared/coil.model';
import { CoilService } from '../shared/coil.service';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { ManufacturerModel } from '../../acquisition-equipments/shared/manufacturer-model.model';
import { ManufacturerModelService } from '../../acquisition-equipments/shared/manufacturer-model.service';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { Enum } from "../../shared/utils/enum";
import { CoilType } from '../shared/coil-type.enum';
import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';

@Component({
    selector: 'coil',
    templateUrl: 'coil.component.html',
    styleUrls: ['coil.component.css'],
})

export class CoilComponent implements OnInit {
    @ViewChild('manufModelModal') manufModelModal: ModalComponent;
    @ViewChild('centerModal') centerModal: ModalComponent;
    @Input() mode: "view" | "edit" | "create";
    @Input() acqEquip: AcquisitionEquipment;
    public coil: Coil = new Coil();
    public coilId: number;
    public coilForm: FormGroup;
    public canModify: Boolean = false;
    public centers: Center[] = [];
    public manufModels: ManufacturerModel[] = [];
    public addIconPath: string = ImagesUrlUtil.ADD_ICON_PATH;
    public coilTypes: Enum[] = [];

    constructor(private route: ActivatedRoute, 
        private router: Router,
        private coilService: CoilService, 
        private fb: FormBuilder,
        private centerService: CenterService,
        private manufModelService: ManufacturerModelService,
        private location: Location, 
        private keycloakService: KeycloakService) {
    }

    ngOnInit(): void {
        if (this.acqEquip) {
            this.coil.center = this.acqEquip.center;
            this.coil.manufacturerModel = this.acqEquip.manufacturerModel;
        }
        this.getEnum();
        this.getCenters();
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
        this.getCoil();
    }

    getCoil(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let coilId = queryParams['id'];
                let mode = queryParams['mode'];
                if (mode) {
                    this.mode = mode;
                }
                if (coilId && this.mode !== 'create') {
                    // view or edit mode
                    this.coilId = coilId;
                    return this.coilService.getCoil(coilId);
                } else {
                    // create mode
                    return Observable.of<Coil>();
                }
            })
            .subscribe((coil: Coil) => {
                if (this.mode === 'edit') {
                    // Link to objects coming from list requests to display selected item of drop-down list
                    coil.center = this.getCenterById(coil.center.id);
                    this.onSelectCenter(coil.center);
                    coil.manufacturerModel = this.getManufModelById(coil.manufacturerModel.id);
                }
                this.coil = coil;
            });
    }

    getCenters(): void {
        this.centerService
            .getCenters()
            .then(centers => {
                this.centers = centers;
                this.getCoil();
            })
        .catch((error) => {
            // TODO: display error
            console.log("error getting center list!");
        });
    }
    
    getCenterById(id: number): Center {
        for (let center of this.centers) {
            if (id == center.id) {
                return center;
            }
        }
        return null;
    }
    
    onSelectCenter(center: Center): void {
        this.manufModels = [];
        if (center) {
            for (let acquisitionEquipmentOfCenter of center.acquisitionEquipments) {
                this.manufModels.push(acquisitionEquipmentOfCenter.manufacturerModel);
            }
        }
    }
    
    getManufModelById(id: number): ManufacturerModel {
        for (let manufModel of this.manufModels) {
            if (id == manufModel.id) {
                return manufModel;
            }
        }
        return null;
    }
    
    getEnum(): void {
        var types = Object.keys(CoilType);
        for (var i = 0; i < types.length; i = i + 1) {
            var newEnum: Enum = new Enum();
            newEnum.key = types[i];
            newEnum.value = CoilType[types[i]];
            this.coilTypes.push(newEnum);
        }
    }

    buildForm(): void {
        this.coilForm = this.fb.group({
            'name': [this.coil.name],
            'acquiEquipModel': [this.coil.manufacturerModel],
            'center': [this.coil.center],
            'coilType': [this.coil.coilType],
            'nbChannel': [this.coil.numberOfChannels],
            'serialNb': [this.coil.serialNumber]
        });

        this.coilForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
    }

    onValueChanged(data?: any) {
        if (!this.coilForm) { return; }
        const form = this.coilForm;
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
        'center': '',
        'manufacturerModel': ''
    };

    back(): void {
        this.location.back();
    }

    edit(): void {
        this.router.navigate(['/coil'], {queryParams: { id: this.coilId, mode: "edit" }});
    }

    submit(): void {
        this.coil = this.coilForm.value;
    }

    create(): void {
        this.submit();
        this.coilService.create(this.coil)
            .subscribe((coil) => {
                this.back();
            }, (err: String) => {
        });
    }

    update(): void {
        this.submit();
        this.coilService.update(this.coilId, this.coil)
            .subscribe((coil) => {
                this.back();
            }, (err: String) => {
        });
    }

    closePopin() {
        this.manufModelModal.hide();
    }

    closeCenterPopin() {
        this.centerModal.hide();
    }
}