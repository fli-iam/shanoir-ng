import { Component } from '@angular/core';
import { FormGroup, Validators} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { Reference }   from '../shared/reference.model';
import { ReferenceService } from '../shared/reference.service';

import { slideDown } from '../../../shared/animations/animations';
import { ModesAware } from "../../shared/mode/mode.decorator";
import { EntityComponent } from '../../../shared/components/entity/entity.component.abstract';

@Component({
    selector: 'reference-form',
    templateUrl: 'reference-form.component.html',
    styleUrls: ['reference-form.component.css'],
    providers: [ReferenceService],
    animations: [slideDown]
})
@ModesAware
export class ReferenceFormComponent extends EntityComponent<Reference>{

    categories: string[];
    reftypes: string[];
    private isFreeCategory: boolean = false;
    private isFreeRefType: boolean = false;
    private isEditableCategory: boolean = true;
    private isEditableRefType: boolean = true;

    constructor(
            private route: ActivatedRoute,
            private referenceService: ReferenceService) {

        super(route, 'preclinical-reference');
    }

    get reference(): Reference { return this.entity; }
    set reference(reference: Reference) { this.entity = reference; }

    initView(): Promise<void> {
        return this.referenceService.get(this.id).then(reference => {
            this.reference = reference;
        });
    }

    initEdit(): Promise<void> {
        this.loadCategories();
        return this.referenceService.get(this.id).then(reference => {
            this.reference = reference;
            if (this.reference && this.reference.category) 
                this.loadTypesByCategory(this.reference.category);
        });
    }

    initCreate(): Promise<void> {
        this.entity = new Reference();
        this.loadCategories();
        this.loadSmth();
        return Promise.resolve();
    }

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'category': [this.reference.category,[Validators.required]],
            'reftype': [this.reference.reftype, [Validators.required]],
            'value': [this.reference.value, [Validators.required]]
        });
    }

    loadCategories() {
        this.referenceService.getCategories().then(categories => this.categories = categories);
        this.categories = [];
    }

    loadTypesByCategory(category: string) {
        this.referenceService.getTypesByCategory(category).then(reftypes => {
            this.reftypes = reftypes;

        });
        this.reftypes = [];
    }

    loadSmth() {
        let category = this.route.snapshot.queryParams['category'];
        let reftype = this.route.snapshot.queryParams['reftype'];
        if (category) {
            this.reference.category = category;
            this.isEditableCategory = false;
            if (!this.isValueInArray(category, this.categories)) {
                this.isFreeCategory = true;
                this.isFreeRefType = true;
            } else {
                this.loadTypesByCategory(category);
            }

        }
        if (reftype) {
            this.reference.reftype = reftype;
            this.isEditableRefType = false;
            if (!this.isValueInArray(reftype, this.reftypes)) {
                this.isFreeRefType = true;
            }
        } else {
            this.isFreeRefType = false;
        }
    }


    onChangeCategory() {
        this.loadTypesByCategory(this.reference.category);
        //Reinitialise reference reftype
        this.reference.reftype = undefined;
    }

    

    isValueInArray(value: string, array: string[]): boolean {
        for (let search in array) {
            if (search == value) return true;
        }
        return false;
    }


    switchToCreate(formControlName: string) {
        if (formControlName == "category") {
            //this.mode == createMode();
            this.isFreeCategory = true;
            this.reference.category = undefined;
            this.isFreeRefType = true;
            this.reference.reftype = undefined;
            this.reference.value = undefined;
        } else if (formControlName == "reftype") {
            //this.mode.createMode();
            this.isFreeRefType = true;
            this.reference.reftype = undefined;
            this.reference.value = undefined;
        }
    }

}