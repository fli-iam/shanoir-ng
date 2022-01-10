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
import { OnInit, Directive } from "@angular/core";

@Directive()
export abstract class BrowserPaginEntityListComponent<T extends Entity> extends EntityListComponent<T> implements OnInit {

    protected entitiesPromise: Promise<void>;
    protected browserPaging: BrowserPaging<T>;

    ngOnInit() {
        this.loadEntities();
    }
    
    private loadEntities(): Promise<void> {
        this.entitiesPromise = this.getEntities().then((entities) => {
            this.browserPaging = new BrowserPaging(entities, this.columnDefs)
        });
        return this.entitiesPromise;
    }

    getPage(pageable: FilterablePageable, forceRefresh: boolean = false): Promise<Page<T>> {
        return this.entitiesPromise.then(() => {
            if (forceRefresh) {
                return this.loadEntities().then(() => this.browserPaging.getPage(pageable));
            } else {
                return this.browserPaging.getPage(pageable);
            }
        });
    }

    abstract getEntities(): Promise<T[]>;

}