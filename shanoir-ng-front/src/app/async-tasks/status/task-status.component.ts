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
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { NotificationsService } from '../../shared/notifications/notifications.service';
import { Task } from '../task.model';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';


@Component({
    selector: 'task-status',
    templateUrl: 'task-status.component.html',
    styleUrls: ['task-status.component.css'],
})
export class TaskStatusComponent implements OnInit, OnDestroy {

    importTs: number;
    protected subscribtions: Subscription[] = [];
    currentImportTask: Task;

    constructor(
        private notificationsService: NotificationsService,
        private activatedRoute: ActivatedRoute,
        private breadcrumbsService: BreadcrumbsService
    ) {
        setTimeout(() => {
            breadcrumbsService.currentStepAsMilestone();
            breadcrumbsService.currentStep.label = 'Task Status';
        });

        this.subscribtions.push(
            this.notificationsService.getNotifications().subscribe(tasks => {
                this.currentImportTask = tasks.find(task => task.timestamp == this.importTs);
            })
        );
    }

    ngOnInit(): void {
        this.subscribtions.push(this.activatedRoute.params.subscribe( 
            params => {
                this.importTs = +params['ts'];
            }
        ));
    }

    ngOnDestroy() {
        for (let subscribtion of this.subscribtions) {
            subscribtion.unsubscribe();
        }
    }

}