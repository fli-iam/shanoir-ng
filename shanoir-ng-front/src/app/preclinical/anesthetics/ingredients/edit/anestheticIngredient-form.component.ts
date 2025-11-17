/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

import { Component, Input, Output, EventEmitter, OnChanges } from '@angular/core';
import { UntypedFormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

import { AnestheticIngredient } from '../shared/anestheticIngredient.model';
import { AnestheticIngredientService } from '../shared/anestheticIngredient.service';
import { Anesthetic }   from '../../anesthetic/shared/anesthetic.model';
import { ReferenceService } from '../../../reference/shared/reference.service';
import { Reference }    from '../../../reference/shared/reference.model';
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { Step } from '../../../../breadcrumbs/breadcrumbs.service';



@Component({
    selector: 'anesthetic-ingredient-form',
    templateUrl: 'anestheticIngredient-form.component.html',
    imports: [FormsModule, ReactiveFormsModule]
})
export class AnestheticIngredientFormComponent extends EntityComponent<AnestheticIngredient> implements OnChanges {

    @Input() anesthetic: Anesthetic;
    @Input() toggleForm: boolean = true;
    @Input() ingredientSelected: AnestheticIngredient;
    @Output() event = new EventEmitter();
    @Input() createAIMode: boolean;
    names: Reference[];
    units: Reference[];

    constructor(
        private route: ActivatedRoute,
        private ingredientsService: AnestheticIngredientService,
        private referenceService: ReferenceService)
    {

        super(route, 'preclinical-anesthetic-ingredient');
    }

    get ingredient(): AnestheticIngredient { return this.entity; }
    set ingredient(ingredient: AnestheticIngredient) { this.entity = ingredient; }

    getService(): EntityService<AnestheticIngredient> {
        return this.ingredientsService;
    }

    initView(): Promise<void> {
        this.loadUnits();
        this.loadNames();
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        this.loadUnits();
        this.loadNames();
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.entity = new AnestheticIngredient();
        this.loadUnits();
        this.loadNames();
        return Promise.resolve();
    }

    buildForm(): UntypedFormGroup {
        return this.formBuilder.group({
            'name': [this.ingredient.name, Validators.required],
            'concentration': [this.ingredient.concentration, Validators.required],
            'concentrationUnit': [this.ingredient.concentrationUnit, Validators.required]
        });
    }

    loadUnits(){
       this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_CAT_UNIT,PreclinicalUtils.PRECLINICAL_UNIT_CONCENTRATION).then(units => this.units = units);
    }

    loadNames(){
       this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_ANESTHETIC,PreclinicalUtils.PRECLINICAL_ANESTHETIC_INGREDIENT).then(names => this.names = names);
    }


    toggleFormAI(creation: boolean): void {
        if(this.toggleForm==false){
            this.toggleForm = true;
        }else if(this.toggleForm==true){
            this.toggleForm = false;
            this.event.emit(null);
        }else{
            this.toggleForm = false;
            this.event.emit(null);
        }
        this.createAIMode = creation;
    }

    ngOnChanges(){
    if(this.ingredientSelected){
        this.loadIngredientAttributesForSelect(this.ingredientSelected);
        }
        if(this.toggleForm){
            this.buildForm();
        }
    }

    loadIngredientAttributesForSelect(ingredientSelected:AnestheticIngredient) {
        this.ingredient = ingredientSelected;

        if(this.units){
            for (const unit of this.units) {
                if(ingredientSelected.concentrationUnit){
                    if (ingredientSelected.concentrationUnit.id == unit.id) {
                        this.ingredient.concentrationUnit = unit;
                    }
                }
                }
        }
        if(this.names){
                for (const name of this.names) {
                    if(ingredientSelected.name){
                        if (ingredientSelected.name.id == name.id) {
                            this.ingredient.name = name;
                        }
                    }
                }
        }
    }


    cancelIngredient(){
        this.toggleFormAI(false);
    }

    addIngredient(): Promise<void> {
        if (!this.ingredient) {
            return;
        }
        if(this.anesthetic.ingredients === undefined){
            this.anesthetic.ingredients = [];
        }
        this.anesthetic.ingredients.push(this.ingredient);
        if (this.event.observed) {
            this.event.emit(this.ingredient);
        }
        this.toggleForm = false;
        this.ingredient = new AnestheticIngredient();
    }

    updateIngredient(): void {
        this.ingredientsService.updateAnestheticIngredient(this.anesthetic.id, this.ingredient)
            .subscribe(() =>{
                if (this.event.observed) {
                    this.event.emit(this.ingredient);
                }
            });
        this.toggleForm = false;
        this.ingredient = new AnestheticIngredient();
    }

    canUpdateIngredient(): boolean{
        return !this.createAIMode && this.keycloakService.isUserAdminOrExpert && this.mode != 'view';
    }

    canAddIngredient(): boolean{
        return this.createAIMode && this.mode != 'view';
    }

    //params should be anesthetic&ingredient or unit&concentration
    goToRefPage(...params: string[]): void {
        let category;
        let reftype;
        if (params && params[0]) category = params[0];
        if (params && params[1]) reftype = params[1];

        const currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/preclinical-reference/create'], { queryParams: { category: category, reftype: reftype} }).then(() => {
            currentStep.waitFor(this.breadcrumbsService.currentStep).subscribe(entity => {
                if (reftype == 'ingredient'){
                    this.names.push(entity as Reference);
                    this.entity.name = entity as Reference;
                } else if (reftype == 'concentration'){
                    this.units.push(entity as Reference);
                    this.entity.concentrationUnit = entity as Reference;
                }
            });
        });
    }

}
