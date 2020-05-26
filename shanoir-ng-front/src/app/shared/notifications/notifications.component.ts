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

import { Component, ElementRef, OnInit } from '@angular/core';

import { menuSlideDown } from '../animations/animations';
import { ImagesUrlUtil } from '../utils/images-url.util';
import { Task } from '../../async-tasks/task.model'
import { TaskService } from '../../async-tasks/task.service'
import * as AppUtils from '../../utils/app.utils'
import { KeycloakService } from '../../shared/keycloak/keycloak.service'

import { EventSourcePolyfill } from 'event-source-polyfill';

@Component({
    selector: 'notifications',
    templateUrl: 'notifications.component.html',
    styleUrls: ['notifications.component.css'],
    animations: [menuSlideDown]
})

export class NotificationsComponent implements OnInit {
  
    protected animate: number = 0;
    protected isOpen: boolean = false;
    protected nbProcess: number = 0;
    protected nbDone: number = 0;
    protected newProcess: boolean = false;
    protected newDones: boolean = true;
    protected ImagesUrlUtil = ImagesUrlUtil;
    protected tasks: Task[] = [];
    protected tasksDone: Task[] = [];
    protected tasksInProgress: Task[] = [];
    protected isLoading = false;
    protected source;

    constructor(public elementRef: ElementRef, private taskService: TaskService, private keycloakService: KeycloakService) {
        document.addEventListener('click', () => {
            if (!elementRef.nativeElement.contains(event.target)) {
                if (this.isOpen) this.close();
            }
        });
    }

    ngOnInit(): void {
        this.refresh();
        this.connect();
    }

    getEntities(): Promise<Task[]> {
        return this.taskService.getTasks();
    }

    private refresh(items = []) {
        this.isLoading = true;
        if (items.length == 0) {
           this.getEntities().then(itemsGot => {
                if (itemsGot && itemsGot.length > 0) {
                    this.refresh(itemsGot);
                }
            });
            return;
        }

        this.tasks = items;
        this.nbProcess = 0;
        this.nbDone = 0;
        this.tasksDone = [];
        this.tasksInProgress = []
        for (let task of this.tasks) {
            if (task.status == 1) {
                this.tasksDone.push(task);
                this.nbDone +=1;
            } else {
                this.tasksInProgress.push(task);
                 this.nbProcess +=1;
            }
        }
        this.isLoading = false;
    }

    connect(): void {
        this.keycloakService.getToken().then(token => {
            this.source = new EventSourcePolyfill(AppUtils.BACKEND_API_UPDATE_TASKS_URL, {
                  headers: {
                    'Authorization': "Bearer " + token
                  }
                });
            this.source.addEventListener('message', message => {
                if (message.data !== "{}") {
                    this.refresh();
                }
            });
            this.source.onmessage = (message)=>{
                let n:Notification = JSON.parse(message.data);
            }     
        })
    }

    private toggle() {
        if (this.isOpen) this.close();
        else {
             this.open();
             this.refresh();
        }
    }

    close() {
        this.isOpen = false;
    }

    open() {
        this.isOpen = true;
    }

}