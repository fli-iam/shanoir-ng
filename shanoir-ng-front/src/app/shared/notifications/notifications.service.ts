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

import { BehaviorSubject, Observable } from 'rxjs';
import { Task } from '../../async-tasks/task.model';
import { TaskService } from '../../async-tasks/task.service';
import * as AppUtils from '../../utils/app.utils';
import { KeycloakService } from '../keycloak/keycloak.service';

@Injectable()
export class NotificationsService {
  
    protected tasks: Task[] = [];
    protected isLoading = false;
    protected source;
    private tasksSubject: BehaviorSubject<Task[]> = new BehaviorSubject<Task[]>([]);
    private clientSideTasks: Task[] = [];


    constructor(private taskService: TaskService, private keycloakService: KeycloakService) {
        this.connect();
    }

    private refresh() {
        this.isLoading = true;
        this.taskService.getTasks().then(items => {
            if (items && items.length > 0) {
                this.tasks = items;
                this.emitTasks();
            }
        }).finally(() => {
            this.isLoading = false;
        });
        return;
        
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

    pushLocalTask(task: Task) {
        this.clientSideTasks.push(task);
        this.emitTasks();
    }

    private emitTasks() {
        let all = this.clientSideTasks.concat(this.tasks)
                .sort((a, b) => new Date(b.creationDate).getTime() - new Date(a.creationDate).getTime());
        this.tasksSubject.next(all);
    }
}