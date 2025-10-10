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

import { Component, forwardRef, Input } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { Router } from '@angular/router';

import { Mode } from 'src/app/shared/components/entity/entity.component.abstract';
import { BrowserPaging } from 'src/app/shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from 'src/app/shared/components/table/pageable.model';
import { KeycloakService } from 'src/app/shared/keycloak/keycloak.service';
import { SuperPromise } from 'src/app/utils/super-promise';

import { ColumnDefinition } from '../../../../shared/components/table/column.definition.type';
import { SubjectPathology } from '../shared/subjectPathology.model';


@Component({
    selector: 'subject-pathology-list',
    templateUrl: 'subject-pathology-list.component.html',
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => SubjectPathologiesListComponent),
            multi: true,
        }
    ],
    standalone: false
})

export class SubjectPathologiesListComponent implements ControlValueAccessor {

    @Input() mode: Mode;
    protected paging: BrowserPaging<SubjectPathology>;
    protected columnDefs: ColumnDefinition[];
    protected customActionDefs: any[];
    protected options: any = {
        view: false // Specify that we can't view a pathology'
    };
    protected disabled: boolean = false;
    protected propagateChange = (_: any) => { return; };
    protected propagateTouched = () => { return; };
    protected refreshTable: SuperPromise<(number?) => void> = new SuperPromise<(number?) => void>();

    constructor(
        private keycloakService: KeycloakService,
        private router: Router) {

        this.columnDefs = this.getColumnDefs();
        this.paging = new BrowserPaging<SubjectPathology>([], this.columnDefs);
        this.completeCustomActions();
    }

    writeValue(obj: any): void {
        this.paging.setItems(obj ? obj : []);
        this.refreshTable.then(refresh => {
            refresh(1);
        });
    }

    registerOnChange(fn: any): void {
        this.propagateChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.propagateTouched = fn;
    }

    setDisabledState?(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    private getColumnDefs(): ColumnDefinition[] {
        const columnDefs: ColumnDefinition[] = [
            { headerName: "Pathology", field: "pathologyModel.pathology.name" },
            { headerName: "PathologyModel", field: "pathologyModel.name" },
            { headerName: "Location", field: "location.value" },
            { headerName: "Start Date", field: "startDate", type: "date" },
            { headerName: "End Date", field: "endDate", type: "date" },
        ];
        if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
            columnDefs.push({ headerName: "", type: "button", awesome: "fa-regular fa-trash-can", action: (item) => this.removeItem(item) });
        }
        return columnDefs;
    }

    private completeCustomActions(): void {
        if (this.mode != 'view' && this.keycloakService.isUserAdminOrExpert()) {
            this.customActionDefs = [{
                title: "New", awesome: "fa-solid fa-plus", action: () => this.router.navigate(['/preclinical-subject-pathology/create'])
            }];
        } else {
            this.customActionDefs = [];
        }
    }

    protected removeItem(item: SubjectPathology) {
        this.paging.setItems(this.paging.items.filter(p => p !== item));
        this.refreshTable.then(refresh => {
            refresh();
        });
        this.propagateChange(this.paging.items);
        this.propagateTouched();
    }

    protected getPage(pageable: FilterablePageable, _forceRefresh: boolean = false): Promise<Page<SubjectPathology>> {
        return Promise.resolve(this.paging.getPage(pageable));
    }
}
