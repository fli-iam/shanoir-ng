import { Location } from '@angular/common';
import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { ManufacturerModel } from '../../acquisition-equipments/shared/manufacturer-model.model';
import { Center } from '../../centers/shared/center.model';
import { CenterService } from '../../centers/shared/center.service';
import { ModalComponent } from '../../shared/components/modal/modal.component';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { Enum } from '../../shared/utils/enum';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { CoilType } from '../shared/coil-type.enum';
import { Coil } from '../shared/coil.model';
import { CoilService } from '../shared/coil.service';
import { FooterState } from '../../shared/components/form-footer/footer-state.model';

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
    public coil: Coil;
    public id: number;
    public coilForm: FormGroup;
    public centers: Center[] = [];
    public manufModels: ManufacturerModel[] = [];
    public addIconPath: string = ImagesUrlUtil.ADD_ICON_PATH;
    public coilTypes: Enum[] = [];
    private footerState: FooterState;

    constructor(
            private route: ActivatedRoute, 
            private router: Router,
            private coilService: CoilService, 
            private fb: FormBuilder,
            private centerService: CenterService,
            private location: Location, 
            private keycloakService: KeycloakService) {

        this.mode = this.route.snapshot.data['mode'];
        this.id = +this.route.snapshot.params['id'];   
    }


    ngOnInit(): void {
        this.getEnum();
        this.coil = new Coil();
        this.getCoil().then(() => this.buildForm());
        this.footerState = new FooterState(
            this.mode,
            this.keycloakService.isUserAdminOrExpert()
        );
    }

    getCoil(): Promise<Coil> {
        if (this.mode == 'create') {
            return new Promise(resolve => {
                this.coil = new Coil();
                resolve(this.coil); 
            });
        } else {
            return Promise.all([
                this.centerService.getCenters().then(centers => { this.centers = centers; return centers; }),
                this.coilService.getCoil(this.id)
            ]).then(([centers, coil]) => {
                if (this.mode === 'edit') {
                    // Link to objects coming from list requests to display selected item of drop-down list
                    coil.center = this.centers.find(center => center.id == coil.center.id);
                    this.onSelectCenter(coil.center);
                    coil.manufacturerModel = this.manufModels.find(manuf => manuf.id == coil.manufacturerModel.id);
                }
                if (this.acqEquip) {
                    coil.center = this.acqEquip.center;
                    coil.manufacturerModel = this.acqEquip.manufacturerModel;
                }
                this.coil = coil;
                return coil;
            });
        }
    }
    
    onSelectCenter(center: Center): void {
        this.manufModels = [];
        if (center) center.acquisitionEquipments.map(acqEqu => this.manufModels.push(acqEqu.manufacturerModel));
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
        this.coilForm.valueChanges.subscribe(data => this.onValueChanged(data));
        this.coilForm.statusChanges.subscribe(status => this.footerState.valid = status == 'VALID');
        this.onValueChanged();
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
        this.router.navigate(['/coil/edit/' + this.id]);
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
        this.coilService.update(this.id, this.coil)
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