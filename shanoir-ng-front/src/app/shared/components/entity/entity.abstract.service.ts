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
import { HttpClient } from '@angular/common/http';

import { Page } from '../table/pageable.model';
import { Entity } from './entity.abstract';



export abstract class EntityService<T extends Entity> {
    
    abstract API_URL: string;

    abstract getEntityInstance(entity?: T): T;

    // protected http: HttpClient = ServiceLocator.injector.get(HttpClient);

    constructor(protected http: HttpClient) {}

    getAll(): Promise<T[]> {
        return this.http.get<any[]>(this.API_URL)
            .toPromise()
            .then(this.mapEntityList);
    }

    getAllAdvanced(): { quick: Promise<T[]>, complete: Promise<T[]> } {
        let res = { quick: null, complete: null };
        res.complete = new Promise((resolve, reject) => {
            res.quick = this.http.get<any[]>(this.API_URL)
                .toPromise()
                .then((all) => {
                    let quickRes: T[] = [];
                    let mapPromise = this.mapEntityList(all, quickRes);
                    res.complete = mapPromise
                    resolve(mapPromise);
                    return quickRes;                   
                }).catch(reason => reject(reason));
        });
        return res;
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(this.API_URL + '/' + id)
            .toPromise();
    }

    get(id: number): Promise<T> {
        return this.http.get<any>(this.API_URL + '/' + id)
            .toPromise()
            .then(this.mapEntity);
    }

    create(entity: T): Promise<T> {
        return this.http.post<any>(this.API_URL, this.stringify(entity))
            .toPromise()
            .then(this.mapEntity);
    }

    update(id: number, entity: T): Promise<void> {
        return this.http.put<any>(this.API_URL + '/' + id, this.stringify(entity))
            .toPromise();
    }

    protected mapEntity = (entity: any, quickResult?: T): Promise<T> => {
        return Promise.resolve(this.toRealObject(entity));
    }

    protected mapEntityList = (entities: any[], quickResult?: T[]): Promise<T[]> => {
        return Promise.resolve(entities ? entities.map(entity => this.toRealObject(entity)) : []);
    }

    protected mapPage = (page: Page<T>): Promise<Page<T>> => {
        if (!page) return null;
        return this.mapEntityList(page.content).then(entities => {
            page.content = entities;
            return page;
        });            
    }

    protected toRealObject(entity: T): T {
        let trueObject = Object.assign(this.getEntityInstance(entity), entity);
        Object.keys(entity).forEach(key => {
            let value = entity[key];
            // For Date Object, put the json object to a real Date object
            if (String(key).indexOf("Date") > -1 && value) {
                trueObject[key] = new Date(value);
            } 
        });
        return trueObject;
    }

    public stringify(obj: any) {
        return JSON.stringify(obj, (key, value) => {
            return this.customReplacer(key, value, obj);
        });
    }

    protected getIgnoreList() { return ['_links']; }

    protected customReplacer = (key, value, entity) => {
        if (this.getIgnoreList().indexOf(key) > -1) return undefined;
        else if (entity[key] instanceof Date) return this.datePattern(entity[key]);
        else return value;
    }

    private datePattern(date: Date): string {
         return date.getFullYear()
         + '-' 
         + ('0' + (date.getMonth() + 1)).slice(-2)
         + '-' 
         + ('0' + date.getDate()).slice(-2);
    }
}