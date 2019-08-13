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

import { ServiceLocator } from '../../../utils/locator.service';
import { Entity } from './entity.abstract';
import { Page } from '../table/pageable.model';

export abstract class EntityService<T extends Entity> {
    
    abstract API_URL: string;

    abstract getEntityInstance(entity?: T): T;

    protected http: HttpClient = ServiceLocator.injector.get(HttpClient);

    getAll(): Promise<T[]> {
        return this.http.get<T[]>(this.API_URL)
            .map(this.mapEntityList)
            .toPromise();
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(this.API_URL + '/' + id)
            .toPromise();
    }

    get(id: number): Promise<T> {
        return this.http.get<T>(this.API_URL + '/' + id)
            .map(this.mapEntity)
            .toPromise();
    }

    create(entity: T): Promise<T> {
        return this.http.post<any>(this.API_URL, entity.stringify())
            .map(this.mapEntity)
            .toPromise();
    }

    update(id: number, entity: T): Promise<void> {
        return this.http.put<any>(this.API_URL + '/' + id, entity.stringify())
            .toPromise();
    }

    protected mapEntity = (entity: T): T => {
        return this.toRealObject(entity);
    }

    protected mapEntityList = (entities: T[]): T[] => {
        return entities ? entities.map(this.mapEntity) : [];
    }

    protected mapPage = (page: Page<T>): Page<T> => {
            if (page) page.content = page.content.map(this.mapEntity);
            return page;
    }

    private toRealObject(entity: T) {
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
}