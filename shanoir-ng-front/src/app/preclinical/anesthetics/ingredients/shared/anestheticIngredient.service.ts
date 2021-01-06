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

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { EntityService } from '../../../../shared/components/entity/entity.abstract.service';

import { AnestheticIngredient } from './anestheticIngredient.model';

import { Anesthetic } from '../../anesthetic/shared/anesthetic.model';

import * as PreclinicalUtils from '../../../utils/preclinical.utils';

@Injectable()
export class AnestheticIngredientService extends EntityService<AnestheticIngredient> {
    API_URL = PreclinicalUtils.PRECLINICAL_API_ANESTHETICS_URL;

    getEntityInstance() { return new AnestheticIngredient(); }    
       

    getIngredients(anesthetic:Anesthetic): Promise<AnestheticIngredient[]>{
        const url = `${PreclinicalUtils.PRECLINICAL_API_ANESTHETICS_URL}/${anesthetic.id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC_INGREDIENT}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return this.http.get<AnestheticIngredient[]>(url)
            .map(entities => entities.map((entity) => this.toRealObject(entity)))
            .toPromise();
    }
    
        
    getIngredient(anesthetic_id:number,ingredient_id: number): Promise<AnestheticIngredient>{
        return this.http.get<AnestheticIngredient>(PreclinicalUtils.PRECLINICAL_API_ANESTHETICS_URL+"/"+anesthetic_id+"/"+PreclinicalUtils.PRECLINICAL_ANESTHETIC_INGREDIENT+"/"+ingredient_id)
            .map((entity) => this.toRealObject(entity))
            .toPromise();
    }
     
    updateAnestheticIngredient(anesthetic_id:number,ingredient: AnestheticIngredient): Observable<AnestheticIngredient> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_ANESTHETICS_URL}/${anesthetic_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC_INGREDIENT}/${ingredient.id}`;
        return this.http
            .put<AnestheticIngredient>(url, JSON.stringify(ingredient))
            .map(response => response);
    }
    
    createAnestheticIngredient(anesthetic_id:number, ingredient: AnestheticIngredient): Observable<AnestheticIngredient> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_ANESTHETICS_URL}/${anesthetic_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC_INGREDIENT}`;
        return this.http
            .post<AnestheticIngredient>(url, JSON.stringify(ingredient))
            .map(res => res);
    }
        
    deleteAnestheticIngredient(anesthetic_id:number,ingredient_id: number): Promise<void> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_ANESTHETICS_URL}/${anesthetic_id}/${PreclinicalUtils.PRECLINICAL_ANESTHETIC_INGREDIENT}/${ingredient_id}`;
        return this.http.delete<void>(url)
            	.toPromise();
    }
    
}