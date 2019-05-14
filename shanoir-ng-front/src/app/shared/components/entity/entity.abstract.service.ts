import { HttpClient } from '@angular/common/http';

import { ServiceLocator } from '../../../utils/locator.service';
import { Entity } from './entity.abstract';

export abstract class EntityService<T extends Entity> {
    
    abstract API_URL: string;

    abstract getEntityInstance(entity?: T): T;

    protected http: HttpClient = ServiceLocator.injector.get(HttpClient);

    getAll(): Promise<T[]> {
        return this.http.get<T[]>(this.API_URL)
            .map(entities => entities.map((entity) => this.toRealObject(entity)))
            .toPromise();
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(this.API_URL + '/' + id)
            .toPromise();
    }

    get(id: number): Promise<T> {
        return this.http.get<T>(this.API_URL + '/' + id)
        .map((entity) => this.toRealObject(entity))
            .toPromise();
    }

    create(entity: T): Promise<T> {
        return this.http.post<any>(this.API_URL, entity.stringify())
        .map((entity) => this.toRealObject(entity))
            .toPromise();
    }

    update(id: number, entity: T): Promise<void> {
        return this.http.put<any>(this.API_URL + '/' + id, entity.stringify())
            .toPromise();
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