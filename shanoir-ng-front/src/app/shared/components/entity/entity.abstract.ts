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

import { EntityService } from "./entity.abstract.service";

export abstract class Entity {

    abstract id: number;
    
    abstract service: EntityService<Entity>;

    create(): Promise<Entity> {
        return this.service.create(this);
    }

    update(): Promise<void> {
        return this.service.update(this.id, this);
    }

    delete(): Promise<void> {
        return this.service.delete(this.id);
    }

    protected getIgnoreList() { return ['service', '_links']; }

    protected replacer = (key, value) => {
        if (this.getIgnoreList().indexOf(key) > -1) return undefined;
        else if (this[key] instanceof Date) return this.datePattern(this[key]);
        else return value;
    }

    public stringify() {
        let ret = JSON.stringify(this, this.replacer);
        return ret;
    }

    private datePattern(date: Date): string {
         return date.getFullYear()
         + '-' 
         + ('0' + (date.getMonth() + 1)).slice(-2)
         + '-' 
         + ('0' + date.getDate()).slice(-2);
    }
}

export class EntityRoutes {

    constructor(public routingName: string) {}

    public getRouteToView(id: number): string {
        return '/' + this.routingName + '/details/' + id;
    }

    public getRouteToEdit(id: number): string {
        return '/' + this.routingName + '/edit/' + id;
    }

    public getRouteToCreate(): string {
        return '/' + this.routingName + '/create';
    }

    public getRouteToList(): string {
        return '/' + this.routingName + '/list';
    }

}