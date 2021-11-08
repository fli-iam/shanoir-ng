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

export class Pageable {

    constructor(
        public pageNumber: number,
        public pageSize: number,
        public sort?: Sort
    ) {}

    public toParams(): any {
        let params: any = {
            'page': this.pageNumber-1,
            'size': this.pageSize
        };
        if (this.sort && this.sort.orders && this.sort.orders.length > 0) {
            params['sort'] = []
            for (let order of this.sort.orders) {
                params['sort'].push(order.property + ',' + order.direction);
            }
        }
        return params;
    }
}

export class FilterablePageable extends Pageable {
    constructor(
        pageNumber: number,
        pageSize: number,
        sort?: Sort,
        public filter?: Filter
    ) {
        super(pageNumber, pageSize, sort);
    }
}

export class Filter {
    constructor(
        public searchStr: string,
        public searchField: string
    ) { }
}

export class Sort {
    constructor(public orders: Order[] = []) {}
    
    public equals(other: Sort): boolean {
        if (!other || this.orders.length != other.orders.length) return false;
        let result: boolean = true;
        this.orders.forEach((order, index) => {
            if (!order.equals(other.orders[index])) {
                result = false;
                return;
            }
        });
        return result;
    }
}

export class Order {
    constructor(
        public direction: 'ASC' | 'DESC',
        public property: string,
        public ignoreCase: boolean = true,
        public nullHandling: 'NATIVE' | 'NULLS_FIRST' | 'NULLS_LAST' = 'NATIVE'
    ) {}

    public equals(other: Order): boolean {
        return this.direction == other.direction &&
            this.property == other.property &&
            this.ignoreCase == other.ignoreCase &&
            this.nullHandling == other.nullHandling;
    }
}

export class Page<T> {
    public content: T[];
    public number: number;
    public numberOfElements: number;
    public size: number;
    public totalElements: number;
    public totalPages: number;

    static transType<U>(page: Page<any>, content: U[]): Page<U> {
        let newPage: Page<U> = new Page<U>();
        newPage.number = page.number;
        newPage.numberOfElements = page.numberOfElements;
        newPage.size = page.size;
        newPage.totalElements = page.totalElements;
        newPage.totalPages = page.totalPages;
        newPage.content = content;
        return newPage;
    }
}