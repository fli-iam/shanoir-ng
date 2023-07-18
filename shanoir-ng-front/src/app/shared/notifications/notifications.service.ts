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
import { BehaviorSubject, Observable, Subscriber } from 'rxjs';

@Injectable()
export class NotificationsService {
  
    public nbNew: number = 0;
    public nbNewError: number = 0;
    protected tasks: Task[] = [];
    public tasksInProgress: Task[] = [];
    public freshCompletedTasks: Task[] = [];
    protected isLoading = false;
    protected source;
    private tasksSubject: BehaviorSubject<Task[]> = new BehaviorSubject<Task[]>([]);

    constructor(private taskService: TaskService, private keycloakService: KeycloakService) {
        this.connect();
    }

    private refresh(items: Task[] = []) {
        this.isLoading = true;
        if (items.length == 0) {
            this.taskService.getTasks().then(itemsGot => {
                this.tasksSubject.next(itemsGot);
                if (itemsGot && itemsGot.length > 0) {
                    this.refresh(itemsGot);
                }
            });
            return;
        }
        this.tasks = items;
        this.updateStatusVars();
        this.isLoading = false;
    }

    updateStatusVars() {
        let tmpTasksInProgress = [];
        for (let task of this.tasks) {
            if (task.status == -1) {
                let freshError: Task = this.tasksInProgress.find(tip => tip.status == 2 && task.id == tip.id);
                if (freshError) {
                    this.nbNewError++;
                    this.freshCompletedTasks.push(task);
                    // remove after 30s
                    setTimeout(() => {
                        this.freshCompletedTasks = this.freshCompletedTasks.filter(tip => tip.id != task.id);
                    }, 30000);
                }
            } else if (task.status == 1) {
                let freshDone: Task = this.tasksInProgress.find(tip => tip.status == 2 && task.id == tip.id);
                if (freshDone) {
                    this.nbNew++;
                    this.freshCompletedTasks.push(task);
                    // remove after 30s
                    setTimeout(() => {
                        this.freshCompletedTasks = this.freshCompletedTasks.filter(tip => tip.id != task.id);
                    }, 30000);
                }
            } else if (task.status == 2) {
                tmpTasksInProgress.push(task);
            }
        }
        this.tasksInProgress = tmpTasksInProgress;
    }

    private connect() {
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
            this.refresh();
            this.source.onmessage = (message)=>{
                let n:Notification = JSON.parse(message.data);
            }     
        });
    }

    public getNotifications(): Observable<Task[]> {
        return this.tasksSubject;
    }

    totalProgress(): number {
        let total: number = 0;
        this.tasksInProgress.forEach(task => total += task.progress);
        return total/this.tasksInProgress.length;
    }

}