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

import { EntityListComponent } from "./entity-list.component.abstract";
import { Entity } from "./entity.abstract";
import { FilterablePageable, Page } from "../table/pageable.model";
import { BrowserPaging } from "../table/browser-paging.model";
import { OnInit } from "@angular/core";

export abstract class BrowserPaginEntityListComponent<T extends Entity> extends EntityListComponent<T> implements OnInit{

    private entitiesPromise: Promise<void>;
    private browserPaging: BrowserPaging<T>;
    private entities: T[];

    ngOnInit() {
        this.entitiesPromise = this.getEntities().then((entities) => {
            this.entities = entities;
            this.browserPaging = new BrowserPaging(this.entities, this.columnDefs)
        });
        this.manageAfterDelete();
    }

    getPage(pageable: FilterablePageable): Promise<Page<T>> {
        return new Promise((resolve) => {
            this.entitiesPromise.then(() => {
                this.browserPaging.setItems(this.entities);
                resolve(this.browserPaging.getPage(pageable));
            });
        });
    }

    abstract getEntities(): Promise<T[]>;

    private manageAfterDelete() {
        this.subscribtions.push(
            this.onDelete.subscribe(response => {
                if (this.instanceOfEntity(response)) {
                    this.entities = this.entities.filter(item => item.id != response.id);
                }
            })
        );
    }

    private instanceOfEntity(obj: any): boolean {
        return obj.id && obj.create && obj.delete && obj.update;
    }
}