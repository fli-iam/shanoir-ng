import { EntityListComponent } from "./entity-list.component.abstract";
import { Entity } from "./entity.interface";
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
        return obj.id && obj.create && obj.delete && obj.edit;
    }
}