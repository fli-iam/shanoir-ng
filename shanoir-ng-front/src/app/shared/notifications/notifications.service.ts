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
import { Injectable } from '@angular/core';
import { EventSourcePolyfill } from 'ng-event-source';

import { Task } from '../../async-tasks/task.model';
import { TaskService } from '../../async-tasks/task.service';
import { KeycloakService } from '../keycloak/keycloak.service';
import * as AppUtils from '../../utils/app.utils';

@Injectable()
export class NotificationsService {
  
    public nbProcess: number = 0;
    public nbDone: number = 0;
    protected tasks: Task[] = [];
    public tasksDone: Task[] = [];
    public tasksInProgress: Task[] = [];
    protected isLoading = false;
    protected source;

    constructor(private taskService: TaskService, private keycloakService: KeycloakService) {
    }

    refresh(items = []) {
        this.isLoading = true;
        if (items.length == 0) {
            this.taskService.getTasks().then(itemsGot => {
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

    totalProgress(): number {
        let total: number = 0;
        this.tasksInProgress.forEach(task => total += task.progress);
        return total/this.tasksInProgress.length;
    }

}