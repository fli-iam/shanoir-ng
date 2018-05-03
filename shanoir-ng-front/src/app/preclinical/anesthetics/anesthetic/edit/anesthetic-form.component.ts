import { Component, OnInit, Output, EventEmitter, OnChanges } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Location } from '@angular/common';

import { Anesthetic }    from '../shared/anesthetic.model';
import { AnestheticService } from '../shared/anesthetic.service';
import { Reference }   from '../../../reference/shared/reference.model';
import { ReferenceService } from '../../../reference/shared/reference.service';
import { AnestheticIngredient }   from '../../ingredients/shared/anestheticIngredient.model';
import { AnestheticIngredientService } from '../../ingredients/shared/anestheticIngredient.service';

import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { AnestheticType } from "../../../shared/enum/anestheticType";
import { EnumUtils } from "../../../shared/enum/enumUtils";
import { Enum } from "../../../../shared/utils/enum";

import { KeycloakService } from "../../../../shared/keycloak/keycloak.service";
import { Mode } from "../../../shared/mode/mode.model";
import { Modes } from "../../../shared/mode/mode.enum";
import { ModesAware } from "../../../shared/mode/mode.decorator";


@Component({
    selector: 'anesthetic-form',
    templateUrl: 'anesthetic-form.component.html',
    styleUrls: ['anesthetic-form.component.css'],
    providers: [AnestheticService, ReferenceService, AnestheticIngredientService]
})
@ModesAware
export class AnestheticFormComponent implements OnInit {

    public anesthetic: Anesthetic = new Anesthetic();
    @Output() closing = new EventEmitter();
    newAnestheticForm: FormGroup;
    private mode: Mode = new Mode();
    private anestheticId: number;
    private canModify: Boolean = false;
    private anestheticTypes: Enum[] = [];
    ingredientsToDelete: AnestheticIngredient[] = [];
    ingredientsToCreate: AnestheticIngredient[] = [];
    private isAnestheticUnique: Boolean = true;

    constructor(
        private anestheticsService: AnestheticService,
        private referenceService: ReferenceService,
        private ingredientsService: AnestheticIngredientService,
        private keycloakService: KeycloakService,
        private enumUtils: EnumUtils,
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private location: Location) {

    }


    getAnesthetic(): void {
        //Must initiate ingredient array
        this.anesthetic.ingredients = [];
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let anestheticId = queryParams['id'];
                let mode = queryParams['mode'];
                if (mode) {
                    this.mode.setModeFromParameter(mode);
                }
                if (anestheticId) {
                    // view or edit mode
                    this.anestheticId = anestheticId;
                    return this.anestheticsService.getAnesthetic(anestheticId);
                } else {
                    // create mode
                    return Observable.of<Anesthetic>();
                }
            })
            .subscribe(anesthetic => {
                if (!this.mode.isCreateMode()) {
                    this.anesthetic = anesthetic;
                    //this.anestheticTypeEnumValue = AnestheticType[this.anesthetic.anestheticType];
                    this.ingredientsService.getIngredients(this.anesthetic).then(ingredients => {
                        if (ingredients) {
                            this.anesthetic.ingredients = ingredients;
                        }
                    });
                }
            });
    }

    ngOnInit(): void {
        //this.loadData();
        this.getEnum();
        this.getAnesthetic();
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
    }

    goToEditPage(): void {
        this.router.navigate(['/preclinical/anesthetic'], { queryParams: { id: this.anestheticId, mode: "edit" } });
    }

    getEnum(): void {
        this.anestheticTypes = this.enumUtils.getEnumArrayFor('AnestheticType');
    }

    buildForm(): void {
        this.newAnestheticForm = this.fb.group({
            'name': [this.anesthetic.name],
            'comment': [this.anesthetic.comment],
            'anestheticType': [this.anesthetic.anestheticType, Validators.required]
        });

        this.newAnestheticForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged();
    }

    onValueChanged(data?: any) {
        if (!this.newAnestheticForm) { return; }
        const form = this.newAnestheticForm;
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
        'anestheticType': ''
    };

    getOut(anesthetic: Anesthetic = null): void {
        if (this.closing.observers.length > 0) {
            this.closing.emit(anesthetic);
            this.location.back();
        } else {
            this.location.back();
        }
    }

    addAnesthetic() {
        if (!this.anesthetic) { return; }
        this.anestheticsService.create(this.anesthetic)
            .subscribe(anesthetic => {
                if (this.ingredientsToCreate) {
                    for (let ingredient of this.ingredientsToCreate) {
                        this.ingredientsService.create(anesthetic, ingredient).subscribe();
                    }
                }
                this.getOut(anesthetic);
            }, (err: String) => {
                console.log('error in update ' + err);
                if (err.indexOf("should be unique") != -1) {
                    this.isAnestheticUnique = false;
                }
            });
    }

    updateAnesthetic(): void {
        this.anestheticsService.update(this.anesthetic)
            .subscribe(anesthetic => {
                if (this.ingredientsToDelete) {
                    for (let ingredient of this.ingredientsToDelete) {
                        this.ingredientsService.delete(this.anesthetic.id, ingredient.id);
                    }
                }
                if (this.ingredientsToCreate) {
                    for (let ingredient of this.ingredientsToCreate) {
                        if (ingredient.id === undefined) {
                            this.ingredientsService.create(this.anesthetic, ingredient).subscribe();
                        }
                    }
                }
                this.getOut(anesthetic);
            }, (err: String) => {
                console.log('error in update ' + err);
                if (err.indexOf("should be unique") != -1) {
                    this.isAnestheticUnique = false;
                }
            });
    }

    onChangeType() {
        let generatedName = '';
        this.refreshName(generatedName);
    }

    refreshName(generatedName: string) {

        if (this.anesthetic && this.anesthetic.anestheticType && generatedName.indexOf(AnestheticType[this.anesthetic.anestheticType]) < 0) {
            generatedName = AnestheticType[this.anesthetic.anestheticType].concat(' ').concat(generatedName);
        }

        if (this.anesthetic && this.anesthetic.ingredients) {
            for (let ingredient of this.anesthetic.ingredients) {
                let strIngredient = '';
                strIngredient = strIngredient.concat(ingredient.name.value.substring(0, 3)).concat('. ');
                if (ingredient.concentration) strIngredient = strIngredient.concat(String(ingredient.concentration));
                if (ingredient.concentration_unit) strIngredient = strIngredient.concat(ingredient.concentration_unit.value);
                strIngredient = strIngredient.concat(' ');
                if (generatedName.indexOf(strIngredient) < 0) {
                    generatedName = generatedName.concat(strIngredient);
                }
            }
        }
        this.anesthetic.name = generatedName;

    }

    addToCreate(ingredientsToCreate: AnestheticIngredient[]) {
        this.ingredientsToCreate = ingredientsToCreate;
        this.anesthetic.ingredients = ingredientsToCreate;
        this.refreshName('');
    }

    addToDelete(ingredientToDelete: AnestheticIngredient) {
        this.ingredientsToDelete.push(ingredientToDelete);
    }

}