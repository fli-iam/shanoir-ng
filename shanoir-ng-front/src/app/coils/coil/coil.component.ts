import { Component, OnInit, Input , ViewChild} from '@angular/core';
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

@Component({
    selector: 'coil',
    templateUrl: 'coil.component.html',
    styleUrls: ['coil.component.css'],
})

export class CoilComponent implements OnInit {

    public coilForm: FormGroup
    private coil: Coil = new Coil();
    private coilId: number;
    public mode: "view" | "edit" | "create";
   // private isNameUnique: Boolean = true;
    public canModify: Boolean = false;
    private centers: IdNameObject[];
    private manufModels: IdNameObject[];
    @ViewChild('manufModelModal') manufModelModal: ModalComponent;
    @ViewChild('centerModal') centerModal: ModalComponent;
    private addIconPath: string = ImagesUrlUtil.ADD_ICON_PATH;
    private coilTypes: Enum[] = [];

    constructor(private route: ActivatedRoute, private router: Router,
        private coilService: CoilService, private fb: FormBuilder,
        private centerService: CenterService,
        private manufModelService: ManufacturerModelService,
        private location: Location, private keycloakService: KeycloakService) {

    }

    ngOnInit(): void {
        
        this.getCenters();
        this.getManufModels();
        this.getEnum();
        this.getCoil();
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
    }

    getCoil(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let coilId = queryParams['id'];
                let mode = queryParams['mode'];
                if (mode) {
                    this.mode = mode;
                }
                if (coilId) {
                    // view or edit mode
                    this.coilId = coilId;
                    return this.coilService.getCoil(coilId);
                } else {
                    // create mode
                    return Observable.of<Coil>();
                }
            })
            .subscribe((coil: Coil) => {
                this.coil = coil;
            });
    }

    getCenters(): void {
        this.centerService
            .getCentersNamesForExamination()
            .then(centers => {
                this.centers = centers;

            })
            .catch((error) => {
                // TODO: display error
                console.log("error getting center list!");
            });
    }

    getManufModels(): void {
        this.manufModelService
            .getManufacturerModelsNames()
            .then(manufModels => {
                this.manufModels = manufModels;
            })
            .catch((error) => {
                // TODO: display error
                console.log("error getting manufacturer model list!");
            });
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
        'center':'',
        'manufacturerModel':''
    };

    back(): void {
        this.location.back();
    }

    edit(): void {
        this.router.navigate(['/coil'], { queryParams: { id: this.coilId, mode: "edit" } });
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