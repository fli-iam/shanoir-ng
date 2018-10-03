import { Component } from '@angular/core';
import { FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { Center } from '../shared/center.model';
import { CenterService } from '../shared/center.service';
import { ShanoirError } from '../../shared/models/error.model';
import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';

@Component({
    selector: 'center-detail',
    templateUrl: 'center.component.html',
    styleUrls: ['center.component.css']
})

export class CenterComponent extends EntityComponent<Center> {

    private isNameUniqueError: boolean = false;
    private phoneNumberPatternError: boolean = false;
    private openAcqEq: boolean = true;

    constructor(
            private route: ActivatedRoute,
            private centerService: CenterService) {

        super(route, 'center');
        this.manageSaveErrors();
    }

    get center(): Center { return this.entity; }
    set center(center: Center) { this.entity = center; }

    initView(): Promise<void> {
        return this.centerService.get(this.id).then(center => {
            this.center = center;
        });
    }

    initEdit(): Promise<void> {
        return this.centerService.get(this.id).then(center => {
            this.center = center;
        });
    }

    initCreate(): Promise<void> {
        this.entity = new Center();
        return Promise.resolve();
    }

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'name': [this.center.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
            'street': [this.center.street],
            'postalCode': [this.center.postalCode],
            'city': [this.center.city],
            'country': [this.center.country],
            'phoneNumber': [this.center.phoneNumber],
            'website': [this.center.website]
        });
    }

    private manageSaveErrors() {
        this.subscribtions.push(
            this.onSave.subscribe(response => {
                if (response && response instanceof ShanoirError && response.code == 422) {
                    this.phoneNumberPatternError = response.hasFieldError('phoneNumber', 'Pattern');
                    this.isNameUniqueError = response.hasFieldError('name', 'unique');   
                }
            })
        );
    }

    private goToAcquisitionEquipment(acqE: AcquisitionEquipment) {
        this.router.navigate(['/acquisition-equipment/details/' + acqE.id]);
    }

}