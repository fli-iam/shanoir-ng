import { Component } from '@angular/core';
import { FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { Center } from '../shared/center.model';
import { CenterService } from '../shared/center.service';

@Component({
    selector: 'center-detail',
    templateUrl: 'center.component.html',
    styleUrls: ['center.component.css']
})

export class CenterComponent extends EntityComponent<Center> {

    private isNameUnique: Boolean = true;
    private phoneNumberPatternError = false;

    constructor(
            private route: ActivatedRoute,
            private centerService: CenterService) {

        super(route, 'center');
    }

    get center(): Center { return this.entity; }
    set center(center: Center) { this.entity = center; }

    initView(): Promise<void> {
        return this.centerService.getCenter(this.id).then(center => {
            this.center = center;
        });
    }

    initEdit(): Promise<void> {
        return this.centerService.getCenter(this.id).then(center => {
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



    // create(): void {
    //     this.center = this.centerForm.value;
    //     this.centerService.create(this.center)
    //         .subscribe((center) => {
    //             this.back();
    //         }, (err: string) => {
    //             this.manageRequestErrors(err);
    //       });
    // }  


    private manageRequestErrors(err: string): void {
        if (err.indexOf("name should be unique") != -1) {
            this.isNameUnique = false;
        }
        if (err.indexOf("phoneNumber should be Pattern") != -1) {
            this.phoneNumberPatternError = true;
        }
    }

    resetNameErrorMsg(): void {
        this.isNameUnique = true;
    }

    resetPhoneNumberErrorMsg(): void {
        this.phoneNumberPatternError = false;
    }

}