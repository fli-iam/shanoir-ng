import { Component, Input, Output, EventEmitter,OnInit, OnChanges, ChangeDetectionStrategy } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Location } from '@angular/common';
import { IMyOptions, IMyDateModel, IMyInputFieldChanged } from 'mydatepicker';

import { AnestheticIngredient } from '../shared/anestheticIngredient.model';
import { AnestheticIngredientService } from '../shared/anestheticIngredient.service';

import { Anesthetic }   from '../../anesthetic/shared/anesthetic.model';

import { ReferenceService } from '../../../reference/shared/reference.service';
import { Reference }    from '../../../reference/shared/reference.model';
import { KeycloakService } from "../../../../shared/keycloak/keycloak.service";

import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { Mode } from "../../../shared/mode/mode.model";
import { Modes } from "../../../shared/mode/mode.enum";
import { ModesAware } from "../../../shared/mode/mode.decorator";

import { ImagesUrlUtil } from "../../../../shared/utils/images-url.util";


@Component({
  selector: 'anesthetic-ingredient-form',
  templateUrl: 'anestheticIngredient-form.component.html',
  providers: [AnestheticIngredientService,ReferenceService]
})
@ModesAware   
export class AnestheticIngredientFormComponent {
      
  ingredient = new AnestheticIngredient();
  @Input() anesthetic: Anesthetic;
  @Input() mode:Mode = new Mode();
  @Input() canModify: Boolean = false;
  @Input('toggleForm') toggleForm: boolean;
  @Input() ingredientSelected: AnestheticIngredient;
  @Output() onEvent = new EventEmitter();
  @Input() createAIMode: boolean;
  newIngredientForm: FormGroup;
  names: Reference[];
  units: Reference[];
  private addIconPath: string = ImagesUrlUtil.ADD_ICON_PATH;
  
  
  constructor(
        private ingredientsService: AnestheticIngredientService,
        private referenceService: ReferenceService,
        private keycloakService: KeycloakService,
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private location: Location) { }  
    
           
   loadUnits(){
       this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_CAT_UNIT,PreclinicalUtils.PRECLINICAL_UNIT_CONCENTRATION).then(units => this.units = units);
   }
    
   loadNames(){
       this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_ANESTHETIC,PreclinicalUtils.PRECLINICAL_ANESTHETIC_INGREDIENT).then(names => this.names = names);
   }
           
   ngOnInit(): void {
      this.loadUnits();
      this.loadNames();      
      if(this.ingredientSelected){
          this.ingredient = this.ingredientSelected;
      }
      if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
      }
  }
      
  toggleFormAI(creation: boolean): void {
    if(this.toggleForm==false){
        this.toggleForm = true;
    }else if(this.toggleForm==true){
        this.toggleForm = false;
        this.onEvent.emit(false);
    }else{
        this.toggleForm = false;
        this.onEvent.emit(false);
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
    
  buildForm(): void {
        this.newIngredientForm = this.fb.group({
            'name': [this.ingredient.name, Validators.required],
            'concentration': [this.ingredient.concentration, Validators.required],
            'concentration_unit': [this.ingredient.concentration_unit, Validators.required]
        });

        this.newIngredientForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged();
    }

    onValueChanged(data?: any) {
        if (!this.newIngredientForm) { return; }
        const form = this.newIngredientForm;
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
        'name': '',
        'concentration': '',
        'concentration_unit': ''
    };
    
  
  cancelIngredient(){
      this.toggleFormAI(false);
  }
   
  addIngredient() {
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
      this.ingredientsService.update(this.anesthetic.id, this.ingredient)
        .subscribe(ingredient =>{
            if (this.onEvent.observers.length > 0) {
                this.onEvent.emit(this.ingredient);
            }    
        });
      this.toggleForm = false;
      this.ingredient = new AnestheticIngredient();
  }
  
  
  //params should be anesthetic&ingredient or unit&concentration
    goToRefPage(...params: string[]): void {
        let category;
        let reftype;
        if (params && params[0]) category = params[0];
        if (params && params[1]) reftype = params[1];
        if (category && reftype) this.router.navigate(['/preclinical-reference'], { queryParams: { mode: "create", category: category, reftype: reftype} });
        
    }
   
}