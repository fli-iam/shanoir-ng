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
import { Component, forwardRef, Input, OnChanges, OnDestroy, SimpleChanges, ViewChild } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { combineLatest, Subscription , Subject as RxjsSubject} from 'rxjs';
import { Router } from '@angular/router';

import { Study } from '../../../studies/shared/study.model';
import { Subject } from '../../../subjects/shared/subject.model';
import { isDarkColor } from '../../../utils/app.utils';
import { AbstractInput } from '../../form/input.abstract';
import { Option } from '../../select/select.component';
import { Mode } from '../entity/entity.component.abstract';
import { BrowserPaging } from '../table/browser-paging.model';
import { FilterablePageable, Page } from '../table/pageable.model';
import { TableComponent } from '../table/table.component';
import { ColumnDefinition } from '../table/column.definition.type';
import { ConfirmDialogService } from '../confirm-dialog/confirm-dialog.service';


@Component({
    selector: 'subject-study-list',
    templateUrl: 'subject-study-list.component.html',
    styleUrls: ['subject-study-list.component.css'],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => SubjectStudyListComponent),
            multi: true
        }
    ],
    standalone: false
})

export class SubjectStudyListComponent extends AbstractInput<Subject[]> implements OnChanges, OnDestroy {

    @Input() mode: Mode;
    @Input() study: Study;
    @Input() selectableList: Subject[];
    public selected: Subject;
    @Input() displaySubjectType: boolean = true;
    @Input() allowRemove: boolean;
    hasTags: boolean;
    hasQualityTags: boolean;
    columnDefs: ColumnDefinition[];
    @ViewChild('table') table: TableComponent;
    private subjectOrStudyObs: RxjsSubject <Subject[] | Study> = new RxjsSubject();
    private subjectListObs: RxjsSubject<Subject[]> = new RxjsSubject();
    private subscriptions: Subscription[] = [];
    private warningDisplayed: boolean = false;

    constructor(private router: Router,
        private confirmDialogService: ConfirmDialogService) {
        super();
        this.subscriptions.push(
            combineLatest([this.subjectOrStudyObs, this.subjectListObs]).subscribe(() => {
                this.processHasTags();
                this.createColumnDefs();
            })
        );
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.subject || changes.study) {
            this.subjectOrStudyObs.next(changes.subject ? this.study.subjects : this.study);
        }
    }

    ngOnDestroy(): void {
        this.subscriptions.forEach(s => s.unsubscribe());
    }

    writeValue(obj: any): void {
        super.writeValue(obj);
        this.subjectListObs.next(obj);
    }

    getPage(pageable: FilterablePageable): Promise<Page<Subject>> {
        return Promise.resolve(new BrowserPaging<Subject>(this.study.subjects, this.columnDefs).getPage(pageable));
    }

    private createColumnDefs() {
        this.columnDefs = [{ headerName: 'Subject', field: 'name', defaultSortCol: true }];
        if (this.hasTags) {
            this.columnDefs.push(
                { headerName: 'Tags', field: 'tags', editable: true, multi: true,
                    possibleValues: () => {
                        return this.study?.tags?.map(tag => {
                            const opt = new Option(tag, tag.name);
                            if (tag.color) {
                                opt.backgroundColor = tag.color;
                                opt.color = isDarkColor(tag.color) ? 'white' : 'black';
                            }
                            return opt;
                        });
                    }
                }
            );
        }
        if (this.hasQualityTags) {
            this.columnDefs.push(
                { headerName: 'Quality', field: 'qualityTag', editable: false, width: '90px', cellGraphics: (item) => {
                    if (item.qualityTag == 'VALID') return {color: 'green', tag: true, awesome: 'fas fa-check-circle'};
                    else if (item.qualityTag == 'WARNING') return {color: 'chocolate', tag: true, awesome: 'fas fa-exclamation-triangle'};
                    else if (item.qualityTag == 'ERROR') return {color: 'red', tag: true, awesome: 'fas fa-times-circle'};
                }}
            );
        }
        this.columnDefs.push(
            { headerName: 'Subject id for this study', field: 'studyIdentifier', editable: true },
            { headerName: 'Physically Involved', field: 'physicallyInvolved', type: 'boolean', editable: true, width: '54px', disableSorting: true }
        );
        if (this.displaySubjectType) {
            this.columnDefs.push(
                { headerName: 'Subject Type', field: 'subjectType', editable: true, possibleValues: [new Option(null, ''), new Option('HEALTHY_VOLUNTEER', 'Healthy Volunteer'), new Option('PATIENT', 'Patient'), new Option('PHANTOM', 'Phantom')] },
            );
        }
        this.columnDefs.push(
            { headerName: "", type: "button", awesome: "fa-regular fa-eye", action: item => this.goToView(item) }
        );
        if (this.allowRemove) {
            this.columnDefs.push(
                { headerName: "", type: "button", awesome: "fa-regular fa-trash-can", action: (item) => this.removeSubject(item) }
            );
        }
    }

    goToView(item): void {
        this.router.navigate(['/study/details/' + item.study?.id]);
    }

    rowClick(item): string {
        return '/study/details/' + item.study?.id;
    }

    private processHasTags() {
        this.hasTags = (!!this.model && !!(this.model as Subject[]).find(subject => subject.study && subject.study.tags && subject.study.tags.length > 0))
                || this.study?.tags?.length > 0;
        this.hasQualityTags = (!!this.model && !!(this.model as Subject[]).find(subject => !!subject.qualityTag));
    }

    removeSubject(subject: Subject):void {
        if (!this.warningDisplayed) {
            this.confirmDialogService.confirm('Deleting subject',
            'Warning: If this subject is only linked to this study, it will be completely deleted from the database. This means each examination, acquisition and dataset of this subject will be deleted. Are you sure ?')
            .then(userChoice => {
                if (userChoice) {
                    this.removeSubjectOk(subject);
                    this.warningDisplayed = true;
                }
            });
        } else {
            this.removeSubjectOk(subject);
        }
    }

    removeSubjectOk(subject: Subject):void {
        const index: number = this.study.subjects.indexOf(subject);
        if (index > -1) {
            this.study.subjects.splice(index, 1);
            this.propagateChange(this.study);
        }
        this.table.refresh();
    }

    onChange() {
        this.propagateChange(this.model);
        this.propagateTouched();
    }

}
