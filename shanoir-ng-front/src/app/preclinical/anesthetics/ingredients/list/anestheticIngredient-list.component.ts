import {Component, Input, Output, EventEmitter, ViewChild, ViewContainerRef, OnChanges, ChangeDetectionStrategy} from '@angular/core'
import { Router } from '@angular/router'; 

import { ConfirmDialogService } from "../../../../shared/components/confirm-dialog/confirm-dialog.service";
import { KeycloakService } from "../../../../shared/keycloak/keycloak.service";

import { AnestheticIngredient } from '../shared/anestheticIngredient.model';
import { AnestheticIngredientService } from '../shared/anestheticIngredient.service';

import { Anesthetic } from '../../anesthetic/shared/anesthetic.model';
import { AnestheticType } from "../../../shared/enum/anestheticType";

import { Mode } from "../../../shared/mode/mode.model";
import { Modes } from "../../../shared/mode/mode.enum";
import { ModesAware } from "../../../shared/mode/mode.decorator";
import { ImagesUrlUtil } from '../../../../shared/utils/images-url.util';
import { FilterablePageable, Page } from '../../../../shared/components/table/pageable.model';
import { BrowserPaging } from '../../../../shared/components/table/browser-paging.model';
import { TableComponent } from '../../../../shared/components/table/table.component';

@Component({
  selector: 'ingredients-list',
  templateUrl:'anestheticIngredient-list.component.html',
  styleUrls: ['anestheticIngredient-list.component.css'], 
  providers: [AnestheticIngredientService]
})
@ModesAware
export class AnestheticIngredientsListComponent {
  //@Input() ingredients: AnestheticIngredient[];
  
  private ingredientsPromise: Promise<void>  = this.getAnestheticIngredients();
  private browserPaging: BrowserPaging<AnestheticIngredient>;
  public rowClickAction: Object;
  public columnDefs: any[];
  public customActionDefs: any[];
  @Input() mode:Mode = new Mode();
  @Input() canModify: Boolean = false;
  @Input() anesthetic: Anesthetic;
  public toggleFormAI: boolean = false;
  public createAIMode: boolean = false;
  public ingredientSelected : AnestheticIngredient;
  @Output() onIngredientAdded = new EventEmitter();
  @Output() onIngredientDeleted = new EventEmitter();
  @ViewChild('ingredientsTable') table: TableComponent;
    
    
    constructor(
        public ingredientsService: AnestheticIngredientService,
        private keycloakService: KeycloakService,
        public router: Router,
        public confirmDialogService: ConfirmDialogService, 
        private viewContainerRef: ViewContainerRef) 
        {
            this.initiateByCreationMode();   
    }
         
    getPage(pageable: FilterablePageable): Promise<Page<AnestheticIngredient>> {
        return new Promise((resolve) => {
            this.ingredientsPromise.then(() => {
                resolve(this.browserPaging.getPage(pageable));
            });
        });
    }
    
    getAnestheticIngredients(): Promise<void> {
        let ingredientsLoaded : AnestheticIngredient[] = [];
        this.browserPaging = new BrowserPaging(ingredientsLoaded, this.columnDefs);
    	if (this.anesthetic && this.anesthetic.id) {
            return this.ingredientsService.getIngredients(this.anesthetic).then(ingredients => {
            	if (ingredients){
            		this.anesthetic.ingredients = ingredients;
            		ingredientsLoaded = ingredients;
            	}
                this.browserPaging.setItems(ingredientsLoaded);
                this.table.refresh();
            });
        }else{
        	return new Promise<void> ((resolve) => {
        		resolve();
        	})
        }
    }
                    	
    
    
    generateAnestheticName(){
        if(this.anesthetic){
            let generatedName = '';
            if(this.anesthetic.anestheticType) generatedName = generatedName.concat(AnestheticType[this.anesthetic.anestheticType]).concat(' ');
            if(this.anesthetic.ingredients){
                for(let ingredient of this.anesthetic.ingredients){
                    let strIngredient = '';
                    strIngredient = strIngredient.concat(ingredient.name.value.substring(0,3)).concat('. ');
                    if(ingredient.concentration) strIngredient = strIngredient.concat(String(ingredient.concentration));
                    if(ingredient.concentration_unit) strIngredient = strIngredient.concat(ingredient.concentration_unit.value);
                    strIngredient = strIngredient.concat(' ');
                    if(generatedName.indexOf(strIngredient) < 0){
                        generatedName = generatedName.concat(strIngredient);
                    }            
                }
            }
            this.onIngredientAdded.emit(this.anesthetic.ingredients);
            
        }
    }
        
    refreshDisplay(cancel:boolean){
        this.toggleFormAI = false;
        this.createAIMode = false;
        this.generateAnestheticName();
        if (this.anesthetic.ingredients){
        	this.browserPaging.setItems(this.anesthetic.ingredients);
        }
        this.table.refresh();
    }
    
    
    initiateByCreationMode(): void {
       if(!this.mode.isCreateMode()){
       		this.getAnestheticIngredients();
            this.createColumnDefs();
        }else{
            this.createColumnDefs();
        }
    }
        
    delete(ingredient:AnestheticIngredient): void {      
      this.ingredientsService.delete(this.anesthetic.id, ingredient.id).then((res) => this.getAnestheticIngredient(ingredient));
      let index = this.anesthetic.ingredients.findIndex(i => i.id === ingredient.id); //find index in your array
        this.anesthetic.ingredients.splice(index, 1);
      this.browserPaging.setItems(this.anesthetic.ingredients);
      this.table.refresh();
    }
    
    getAnestheticIngredient(ingredient: AnestheticIngredient): void {
    	this.onIngredientDeleted.emit(ingredient);
      	this.generateAnestheticName();
    }
    
       
    ngOnChanges(){
        this.initiateByCreationMode();
        
    }
    
    viewIngredient = (ingredient: AnestheticIngredient) => {
        this.toggleFormAI = true;
        this.createAIMode = false;
        this.ingredientSelected = ingredient;
        //this.generateAnestheticName();
    }
    
    toggleIngredientForm(){
    	this.ingredientSelected = new AnestheticIngredient();
        this.createAIMode = true;
        if(this.toggleFormAI==false){
            this.toggleFormAI = true;
        }else if(this.toggleFormAI==true){
            this.toggleFormAI = false;
        }else{
            this.toggleFormAI = true;
        }
    }
    
    // Grid columns definition
    private createColumnDefs() {
        function castToString(id: number) {
            return String(id);
        };
        function checkNullValue(value: any) {
            if(value){
                return value;
            }
            return '';
        };
        function checkNullValueReference(reference: any) {
            if(reference){
                return reference.value;
            }
            return '';
        };
       
        this.columnDefs = [
            /*{headerName: "ID", field: "id", type: "id", cellRenderer: function (params: any) {
                return castToString(params.data.id);
            }}, */
            {headerName: "Name", field: "name.value", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.name);
            }},
            {headerName: "Concentration", field: "concentration", type: "number", cellRenderer: function (params: any) {
                return checkNullValue(params.data.concentration);
            }},
            {headerName: "Concentration Unit", field: "concentration_unit.value", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.concentration_unit);
            }}
            //{headerName: "Edit", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, action: this.editIngredient,component:this},
            //{headerName: "Delete", type: "button", img:ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.openDeleteIngredientConfirmDialog, component:this},
            
        ];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push({headerName: "", type: "button", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.openDeleteIngredientConfirmDialog});
            if(this.mode && this.mode.isEditMode()) this.columnDefs.push({headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, action: this.viewIngredient,component:this});
        }
        if (!this.keycloakService.isUserGuest() && this.mode.isViewMode()) {
            this.columnDefs.push({headerName: "", type: "button", img: ImagesUrlUtil.VIEW_ICON_PATH, action: this.viewIngredient,component:this});
        }
        this.customActionDefs = []; 
        this.customActionDefs.push({title: "delete selected", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.deleteAll });
        if (!this.keycloakService.isUserGuest()) {
            this.rowClickAction = { action: this.viewIngredient,component:this};
        }
       
    }
    
    openDeleteIngredientConfirmDialog = (item: AnestheticIngredient) => {
         this.confirmDialogService
                .confirm('Delete ingredient for anesthetic', 'Are you sure you want to delete ingredient ' + item.name.value + '?', 
                    this.viewContainerRef)
                .subscribe(res => {
                    if (res) {
                        this.delete(item);
                    }
                });
    }
    
    deleteAll = () => {
        let ids: number[] = [];
        for (let ingredient of this.anesthetic.ingredients) {
            if (ingredient["isSelectedInTable"]) ids.push(ingredient.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }
}