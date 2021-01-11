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

export abstract class Entity {
    
    abstract id: number;
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