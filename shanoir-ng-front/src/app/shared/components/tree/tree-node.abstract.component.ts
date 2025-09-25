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
import { AfterContentInit, Directive, ElementRef, EventEmitter, Input, Output, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';

import { TaskState } from 'src/app/async-tasks/task.model';
import { ShanoirNode } from 'src/app/tree/tree.model';
import { SuperPromise } from 'src/app/utils/super-promise';

@Directive()
export class TreeNodeAbstractComponent<T extends ShanoirNode> implements AfterContentInit, OnDestroy {

    @Output() nodeInit: EventEmitter<T> = new EventEmitter();
    @Output() selectedChange: EventEmitter<T> = new EventEmitter();
    @Output() nodeSelect: EventEmitter<number> = new EventEmitter();
    protected node: T;
    loading: boolean = false;
    menuOpened: boolean = false;
    showDetails: boolean;
    @Input() hasBox: boolean = false;
    detailsPath: string = "";
    @Input() withMenu: boolean = true;
    protected contentLoaded: SuperPromise<void> = new SuperPromise();
    public downloadState: TaskState = new TaskState();
    protected subscriptions: Subscription[] = [];

    constructor(private elementRef: ElementRef) {}

    ngAfterContentInit(): void {
        // the position is needed to auto scroll to the node
        setTimeout(() => {
            this.node.getTop = () => this.elementRef?.nativeElement?.offsetTop;
        });
    }

    ngOnDestroy() {
        for (let subscribtion of this.subscriptions) {
            subscribtion.unsubscribe();
        }
    }
}
