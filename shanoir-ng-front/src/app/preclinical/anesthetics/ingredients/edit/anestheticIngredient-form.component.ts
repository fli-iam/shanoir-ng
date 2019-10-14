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

import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormGroup,  Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { AnestheticIngredient } from '../shared/anestheticIngredient.model';
import { AnestheticIngredientService } from '../shared/anestheticIngredient.service';

import { Anesthetic }   from '../../anesthetic/shared/anesthetic.model';

import { ReferenceService } from '../../../reference/shared/reference.service';
import { Reference }    from '../../../reference/shared/reference.model';
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import { slideDown } from '../../../../shared/animations/animations';

import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { ModesAware } from "../../../shared/mode/mode.decorator";
import { Step } from '../../../../breadcrumbs/breadcrumbs.service';


@Component({
  selector: 'anesthetic-ingredient-form',
  templateUrl: 'anestheticIngredient-form.component.html',
  providers: [AnestheticIngredientService,ReferenceService],
  animations: [slideDown]
})
@ModesAware   
export class AnestheticIngredientFormComponent extends EntityComponent<AnestheticIngredient>{
      
    @Input() anesthetic: Anesthetic;
    @Input('toggleForm') toggleForm: boolean = true;
    @Input() ingredientSelected: AnestheticIngredient;
    @Output() onEvent = new EventEmitter();
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
    set ingredient(ingredient: AnestheticIngredient) { this.entity = ingredient; }
  
    initView(): Promise<void> {
        this.loadUnits();
        this.loadNames();   
        return this.ingredientsService.get(this.id).then(ingredient => {
            this.ingredient = ingredient;
        });
    }

    initEdit(): Promise<void> {
        this.loadUnits();
        this.loadNames(); 
        return this.ingredientsService.get(this.id).then(ingredient => {
            this.ingredient = ingredient;
        });
    }

    initCreate(): Promise<void> {
        this.entity = new AnestheticIngredient();
        this.loadUnits();
        this.loadNames();  
        return Promise.resolve();
    }

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'name': [this.ingredient.name, Validators.required],
            'concentration': [this.ingredient.concentration, Validators.required],
            'concentration_unit': [this.ingredient.concentration_unit, Validators.required]
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
            this.onEvent.emit(null);
        }else{
            this.toggleForm = false;
            this.onEvent.emit(null);
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
    
    loadIngredientAttributesForSelect(ingredientSelected:AnestheticIngredient){
        this.ingredient = ingredientSelected;
        
        if(this.units){
            for (let unit of this.units) {
                if(ingredientSelected.concentration_unit){
                    if (ingredientSelected.concentration_unit.id == unit.id) {
                        this.ingredient.concentration_unit = unit;
                    }
                }
                }
        }
        if(this.names){   
                for (let name of this.names) { 
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
            console.log('nothing to create');
            return; 
        }
        if(this.anesthetic.ingredients === undefined){
            this.anesthetic.ingredients = [];
        }
        this.anesthetic.ingredients.push(this.ingredient);
        if (this.onEvent.observers.length > 0) {
            this.onEvent.emit(this.ingredient);
        }
        this.toggleForm = false;
        this.ingredient = new AnestheticIngredient();
    }
    
    updateIngredient(): void {
        this.ingredientsService.updateAnestheticIngredient(this.anesthetic.id, this.ingredient)
            .subscribe(ingredient =>{
                if (this.onEvent.observers.length > 0) {
                    this.onEvent.emit(this.ingredient);
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
                    
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/preclinical-reference/create'], { queryParams: { category: category, reftype: reftype} }).then(success => {
            currentStep.waitFor(this.breadcrumbsService.currentStep).subscribe(entity => {
                if (reftype == 'ingredient'){
                    this.names.push(entity as Reference);
                    (currentStep.entity as AnestheticIngredient).name = entity as Reference;
                }else if (reftype == 'concentration'){
                    this.units.push(entity as Reference);
                    (currentStep.entity as AnestheticIngredient).concentration_unit = entity as Reference;
                }
        });
        });
    }
   
}