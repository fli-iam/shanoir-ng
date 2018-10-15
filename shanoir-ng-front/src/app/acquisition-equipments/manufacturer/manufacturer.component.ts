import { Component } from '@angular/core';
import { FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { Manufacturer } from '../shared/manufacturer.model';
import { ManufacturerService } from '../shared/manufacturer.service';

@Component({
    selector: 'manufacturer-detail',
    templateUrl: 'manufacturer.component.html'
})

export class ManufacturerComponent extends EntityComponent<Manufacturer> {
    
    constructor (
            private route: ActivatedRoute,
            private manufService: ManufacturerService) {

        super(route, 'manufacturer');
    }

    private get manuf(): Manufacturer { return this.entity; }
    private set manuf(manuf: Manufacturer) { this.entity = manuf; }

    initView(): Promise<void> {
        return this.getManufacturer();
    }

    initEdit(): Promise<void> {
        return this.getManufacturer();
    }

    initCreate(): Promise<void> {
        this.entity = new Manufacturer();
        return Promise.resolve();
    }

    getManufacturer(): Promise<void> {
        return this.manufService.get(this.id)
            .then(manuf => {
                this.manuf = manuf;
            });
    }   

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'name': [this.manuf.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200), this.registerOnSubmitValidator('unique', 'name')]]
        });

    }
}