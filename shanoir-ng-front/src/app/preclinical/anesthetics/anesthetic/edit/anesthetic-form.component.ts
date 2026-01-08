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

import { Component, ViewChild } from '@angular/core';
import { UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { slideDown } from '../../../../shared/animations/animations';
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import { BrowserPaging } from '../../../../shared/components/table/browser-paging.model';
import { ColumnDefinition } from '../../../../shared/components/table/column.definition.type';
import { FilterablePageable, Page } from '../../../../shared/components/table/pageable.model';
import { TableComponent } from '../../../../shared/components/table/table.component';
import { Reference } from '../../../reference/shared/reference.model';
import { ReferenceService } from '../../../reference/shared/reference.service';
import { AnestheticType } from "../../../shared/enum/anestheticType";
import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { AnestheticIngredient } from '../../ingredients/shared/anestheticIngredient.model';
import { AnestheticIngredientService } from '../../ingredients/shared/anestheticIngredient.service';
import { Anesthetic } from '../shared/anesthetic.model';
import { AnestheticService } from '../shared/anesthetic.service';
import { EntityService } from '../../../../shared/components/entity/entity.abstract.service';

@Component({
    selector: 'anesthetic-form',
    templateUrl: 'anesthetic-form.component.html',
    styleUrls: ['anesthetic-form.component.css'],
    animations: [slideDown],
    standalone: false
})
export class AnestheticFormComponent extends EntityComponent<Anesthetic> {

    @ViewChild('ingredientsTable', { static: false }) table: TableComponent;

    AnestheticType = AnestheticType;
    ingredientsToDelete: AnestheticIngredient[] = [];
    ingredientsToCreate: AnestheticIngredient[] = [];
    isAnestheticUnique: boolean = true;
    names: Reference[];
    units: Reference[];

    private browserPaging: BrowserPaging<AnestheticIngredient>;
    public columnDefs: ColumnDefinition[];
    private ingredientsPromise: Promise<any>;

    public toggleFormAI: boolean = false;
    public createAIMode: boolean = false;
    public ingredientSelected : AnestheticIngredient;


    constructor(
        private route: ActivatedRoute,
        private anestheticService: AnestheticService,
        private ingredientService: AnestheticIngredientService,
        private referenceService: ReferenceService) {

        super(route);
    }

    protected getRoutingName(): string {
        return 'preclinical-anesthetic';
        this.manageSaveEntity();
    }

    get anesthetic(): Anesthetic { return this.entity; }
    set anesthetic(anesthetic: Anesthetic) { this.entity = anesthetic; }

    getService(): EntityService<Anesthetic> {
        return this.anestheticService;
    }

    initView(): Promise<void> {
        this.createColumnDefs();
        this.ingredientsPromise = Promise.resolve().then(() => {
            this.browserPaging = new BrowserPaging([], this.columnDefs);
        });
        this.loadUnits();
        this.loadNames();
        this.anesthetic.ingredients = [];
        if (this.anesthetic && this.anesthetic.id){
            this.ingredientService.getIngredients(this.anesthetic).then(ingredients => {
                if (ingredients){
                    this.anesthetic.ingredients = ingredients;
                    this.browserPaging.setItems(ingredients);
                    this.table.refresh();
                }
            });
        }
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        this.createColumnDefs();
        this.ingredientsPromise = Promise.resolve().then(() => {
            this.browserPaging = new BrowserPaging([], this.columnDefs);
        });
        this.loadUnits();
        this.loadNames();
        this.anesthetic.ingredients = [];
        if (this.anesthetic && this.anesthetic.id){
            this.ingredientService.getIngredients(this.anesthetic).then(ingredients => {
                if (ingredients){
                    this.anesthetic.ingredients = ingredients;
                    this.browserPaging.setItems(ingredients);
                    this.table.refresh();
                }
            });
        }
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.createColumnDefs();
        this.entity = new Anesthetic();
        this.anesthetic.ingredients = [];
        this.ingredientsPromise = Promise.resolve().then(() => {
            this.browserPaging = new BrowserPaging([], this.columnDefs);
        });
        this.loadUnits();
        this.loadNames();
        this.createColumnDefs();
        return Promise.resolve();
    }

    buildForm(): UntypedFormGroup {
        return this.formBuilder.group({
            'name': [this.anesthetic.name],
            'comment': [this.anesthetic.comment],
            'anestheticType': [this.anesthetic.anestheticType, Validators.required],
            'ingredientsList': [this.anesthetic.ingredients]
        });
    }

    getPage(pageable: FilterablePageable): Promise<Page<AnestheticIngredient>> {
        return new Promise((resolve) => {
            this.ingredientsPromise.then(() => {
                resolve(this.browserPaging.getPage(pageable));
            });
        });
    }

    private createColumnDefs() {
        this.columnDefs = [
            {headerName: "Name", field: "name.value"},
            {headerName: "Concentration", field: "concentration", type: "number"},
            {headerName: "Concentration Unit", field: "concentrationUnit.value", type: "number"}
        ];

        if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
            this.columnDefs.push({ headerName: "", type: "button", awesome: "fa-regular fa-edit", action: item => this.editIngredient(item) });
        }
        if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
            this.columnDefs.push({ headerName: "", type: "button", awesome: "fa-regular fa-trash-can", action: (item) => this.removeIngredient(item) });
        }
    }

    loadUnits(){
        this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_CAT_UNIT,PreclinicalUtils.PRECLINICAL_UNIT_CONCENTRATION).then(units => this.units = units);
     }

     loadNames(){
        this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_ANESTHETIC,PreclinicalUtils.PRECLINICAL_ANESTHETIC_INGREDIENT).then(names => this.names = names);
     }


    manageSaveEntity(): void {
        this.subscriptions.push(
            this.onSave.subscribe(response => {
                if (this.ingredientsToDelete) {
                    for (const ingredient of this.ingredientsToDelete) {
                        this.ingredientService.deleteAnestheticIngredient(response.id, ingredient.id);
                    }
                }
                if (this.ingredientsToCreate) {
                    for (const ingredient of this.ingredientsToCreate) {
                        this.ingredientService.createAnestheticIngredient(response.id, ingredient).subscribe();
                    }
                }
            })
        );

    }


    onChangeType() {
        const generatedName = '';
        this.refreshName(generatedName);
    }

    refreshName(generatedName: string) {
        if (this.anesthetic && this.anesthetic.anestheticType && generatedName.indexOf(AnestheticType[this.anesthetic.anestheticType]) < 0) {
            generatedName = AnestheticType[this.anesthetic.anestheticType].concat(' ').concat(generatedName);
        }

        if (this.anesthetic && this.anesthetic.ingredients) {
            for (const ingredient of this.anesthetic.ingredients) {
                let strIngredient = '';
                strIngredient = strIngredient.concat(ingredient.name.value.substring(0, 3)).concat('. ');
                if (ingredient.concentration) strIngredient = strIngredient.concat(String(ingredient.concentration));
                if (ingredient.concentrationUnit) strIngredient = strIngredient.concat(ingredient.concentrationUnit.value);
                strIngredient = strIngredient.concat(' ');
                if (generatedName.indexOf(strIngredient) < 0) {
                    generatedName = generatedName.concat(strIngredient);
                }
            }
        }
        this.anesthetic.name = generatedName;
    }

    goToAddIngredient() {
        this.ingredientSelected = new AnestheticIngredient();
        this.createAIMode = true;
        if(this.toggleFormAI==false){
            this.toggleFormAI = true;
        }else if(this.toggleFormAI==true){
            this.toggleFormAI = false;
        }else{
            this.toggleFormAI = true;
        }
        this.form.markAsDirty();
        this.form.updateValueAndValidity();
    }

    private editIngredient = (item: AnestheticIngredient) => {
        this.ingredientSelected = item;
        this.toggleFormAI = true;
        this.createAIMode = false;
    }

    refreshDisplay(ingredient: AnestheticIngredient){
        this.toggleFormAI = false;
        this.createAIMode = false;
        if (ingredient && ingredient != null && !ingredient.id ){
            this.ingredientsToCreate.push(ingredient);
        }
        this.refreshName('');
        this.browserPaging.setItems(this.anesthetic.ingredients);
        this.table.refresh();
    }

    private removeIngredient = (item: AnestheticIngredient) => {
        const index: number = this.anesthetic.ingredients.indexOf(item);
        if (index !== -1) {
            this.anesthetic.ingredients.splice(index, 1);
        }
        this.ingredientsToDelete.push(item);

        const createIndex: number = this.ingredientsToCreate.indexOf(item);
        if (createIndex !== -1) {
            this.ingredientsToCreate.splice(createIndex, 1);
        }

        this.browserPaging.setItems(this.anesthetic.ingredients);
        this.table.refresh();

        this.form.markAsDirty();
        this.form.updateValueAndValidity();
    }

}
