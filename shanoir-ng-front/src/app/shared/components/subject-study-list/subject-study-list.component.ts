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

import { Study } from '../../../studies/shared/study.model';
import { SubjectStudy } from '../../../subjects/shared/subject-study.model';
import { Subject } from '../../../subjects/shared/subject.model';
import { isDarkColor } from '../../../utils/app.utils';
import { AbstractInput } from '../../form/input.abstract';
import { Option } from '../../select/select.component';
import { Mode } from '../entity/entity.component.abstract';
import { BrowserPaging } from '../table/browser-paging.model';
import { FilterablePageable, Page } from '../table/pageable.model';
import { TableComponent } from '../table/table.component';
import { ColumnDefinition } from '../table/column.definition.type';
import { combineLatest, Subscription } from 'rxjs';
import { Router } from '@angular/router';
import { Subject as RxjsSubject} from 'rxjs';

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
]
})

export class SubjectStudyListComponent extends AbstractInput<SubjectStudy[]> implements OnChanges, OnDestroy {
    
    @Input() mode: Mode;
    @Input() subject: Subject;
    @Input() study: Study;
    @Input() selectableList: Subject[] | Study[];
    public selected: Subject | Study;
    public optionList: Option<Subject | Study>[];
    @Input() displaySubjectType: boolean = true;
    @Input() allowRemove: boolean;
    hasTags: boolean;
    columnDefs: ColumnDefinition[];
    @ViewChild('table') table: TableComponent;
    private subjectOrStudyObs: RxjsSubject <Subject | Study> = new RxjsSubject();
    private subjectStudyListObs: RxjsSubject<SubjectStudy[]> = new RxjsSubject();
    private subscriptions: Subscription[] = [];
    
    constructor(private router: Router) {
        super();
        this.subscriptions.push(
            combineLatest([this.subjectOrStudyObs, this.subjectStudyListObs]).subscribe(() => {
                this.processHasTags();
                this.createColumnDefs();
            })
        );

    }

    get legend(): string {
        return this.compMode == 'study' ? 'Subject' : 'Study';
    }
    
    ngOnChanges(changes: SimpleChanges): void {
        if (changes.selectableList) {
            this.optionList = [];
            if (this.selectableList) {
                for (let item of this.selectableList) {
                    let option: Option<Subject | Study> = new Option(item, item.name);
                    if(this.model && this.model.find(subStu => (this.compMode == 'study' ? subStu.subject.id : subStu.study.id) == option.value.id)) {
                        option.disabled = true;
                    }
                    this.optionList.push(option);
                }
            }
        }
        if (changes.subject || changes.study) {
            this.subjectOrStudyObs.next(changes.subject ? this.subject : this.study);
        }
    }

    ngOnDestroy(): void {
        this.subscriptions.forEach(s => s.unsubscribe());
    }
    
    writeValue(obj: any): void {
        super.writeValue(obj);
        this.subjectStudyListObs.next(obj);
        this.updateDisabled();
    }

    getPage(pageable: FilterablePageable): Promise<Page<SubjectStudy>> {
        return Promise.resolve(new BrowserPaging<SubjectStudy>(this.model, this.columnDefs).getPage(pageable));

    }

    private createColumnDefs() {
        if (this.compMode == 'study') {
            this.columnDefs = [{ headerName: 'Subject', field: 'subject.name', defaultSortCol: true }];
        } else if (this.compMode == 'subject') {
            this.columnDefs = [{ headerName: 'Study', field: 'study.name', defaultSortCol: true }];
        }
        if (this.hasTags) {
            this.columnDefs.push(
                { headerName: 'Tags', field: 'tags', editable: true, multi: true, 
                    possibleValues: (subjectStudy: SubjectStudy) => {
                        return subjectStudy?.study?.tags?.map(tag => {
                            let opt = new Option(tag, tag.name);
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
        this.columnDefs.push(
            { headerName: 'Subject id for this study', field: 'subjectStudyIdentifier', editable: true },
            { headerName: 'Physically Involved', field: 'physicallyInvolved', type: 'boolean', editable: true, width: '54px', disableSorting: true }
        );
        if (this.displaySubjectType) {
            this.columnDefs.push(
                { headerName: 'Subject Type', field: 'subjectType', editable: true, possibleValues: [new Option(null, ''), new Option('HEALTHY_VOLUNTEER', 'Healthy Volunteer'), new Option('PATIENT', 'Patient'), new Option('PHANTOM', 'Phantom')] },
            );
        }
        this.columnDefs.push(
            { headerName: "", type: "button", awesome: "fa-regular fa-eye", action: item => this.goToView(item.subject?.id) }
        );
        if (this.allowRemove) {
            this.columnDefs.push(
                { headerName: "", type: "button", awesome: "fa-regular fa-trash-can", action: (item) => this.removeSubjectStudy(item) }
            );
        }
    }

    goToView(id: number): void {
        this.router.navigate(['/subject/details/' + id]);
    }

    private updateDisabled() {
        if (this.selectableList && this.model) {
            if (this.compMode == 'study') {
                for (let option of this.optionList) {
                    if(this.model.find(subStu => subStu.subject.id == option.value.id)) option.disabled = true; 
                }
            } else if (this.compMode == 'subject') {
                for (let option of this.optionList) {
                    if(this.model.find(subStu => subStu.study.id == option.value.id)) option.disabled = true; 
                }
            }
        }
    }

    get compMode(): 'subject' | 'study' { 
        if (this.subject && this.study) throw Error('You cannot set both subject and study');
        if (this.subject) return 'subject';
        if (this.study) return 'study';
        throw Error('You have to set either subject or study');
        
    }

    onAdd() {
        if (!this.selected) return;
        if (this.optionList) {
            let foundOption = this.optionList.find(option => option.value.id == this.selected.id);
            if (foundOption) foundOption.disabled = true;
        }
        let newSubjectStudy: SubjectStudy = new SubjectStudy();
        newSubjectStudy.physicallyInvolved = false;
        newSubjectStudy.tags=[];
        if (this.compMode == "study") {
            let studyCopy: Study = new Study();
            studyCopy.id = this.study.id;
            studyCopy.tags = this.study.tags;
            newSubjectStudy.study = studyCopy;
            newSubjectStudy.subject = this.selected as Subject;
        }
        else if (this.compMode == "subject") {
            let subjectCopy: Subject = new Subject();
            subjectCopy.id = this.subject.id;
            newSubjectStudy.subject = subjectCopy;
            newSubjectStudy.study = this.selected as Study;
        }
        this.selected = undefined;
        this.model.push(newSubjectStudy);
        this.processHasTags();
        this.propagateChange(this.model);
        this.table.refresh();
    }

    private processHasTags() {
        this.hasTags = (!!this.model && !!(this.model as SubjectStudy[]).find(subStu => subStu.study && subStu.study.tags && subStu.study.tags.length > 0))
                || this.study?.tags?.length > 0;
    }

    removeSubjectStudy(subjectStudy: SubjectStudy):void {
        const index: number = this.model.indexOf(subjectStudy);
        if (index > -1) {
            this.model.splice(index, 1);
            this.propagateChange(this.model);
            if (this.compMode == 'study') {
                let option: Option<Subject> = this.optionList.find(opt => opt.value.id == subjectStudy.subject.id) as Option<Subject>;
                if (option) option.disabled = false;
            } else if (this.compMode == 'subject') {
                let option: Option<Study> = this.optionList.find(opt => opt.value.id == subjectStudy.study.id) as Option<Study>;
                if (option) option.disabled = false;
            }
        }
        this.table.refresh();
    }

    onChange() {
        this.propagateChange(this.model);
        this.propagateTouched();
    }

    onTouch() {
        this.propagateTouched();
    }
}
