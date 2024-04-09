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
import { SuperTimeout } from 'src/app/utils/super-timeout';
import { SessionService } from '../services/session.service';

@Injectable()
export class NotificationsService {

    public nbNew: number = 0;
    public nbNewError: number = 0;
    private tasks: Task[] = [];
    public tasksInProgress: Task[] = [];
    public tasksInWait: Task[] = [];
    public freshCompletedTasks: Task[] = [];
    private source;
    private tasksSubject: BehaviorSubject<Task[]> = new BehaviorSubject<Task[]>([]) ;
    private storageKey: string = KeycloakService.auth.userId + 'downloadTasks';
    private newLocalTasksQueue: Task[] = [];
    private localTasks: Task[] = [];
    private writeLocalStorageConnection;
    private readLocalStorageConnection;
    private lastLocalStorageRead: number = 0;
    readonly writeInterval: number = 500;
    readonly readInterval: number = 1000;
    readonly persistenceTime: number = 1800000;
    private freshTimeouts: SuperTimeout[] = [];
    

    constructor(private taskService: TaskService, private keycloakService: KeycloakService, private sessionService: SessionService) {
        this.connectToServer();
        this.connectReadSessionToLocalStorage();
    }

    private connectWriteSessionToLocalStorage() {
        if (this.writeLocalStorageConnection) clearInterval(this.writeLocalStorageConnection);
        this.writeLocalStorageConnection = setInterval(() => {
            try {
                if (this.newLocalTasksQueue?.length > 0) {
                    this.updateLocalStorage();
                } else {
                    clearInterval(this.writeLocalStorageConnection);
                    this.writeLocalStorageConnection = null;
                }
            } catch (e) {
                clearInterval(this.writeLocalStorageConnection);
                this.writeLocalStorageConnection = null;
                throw e;
            }
        }, this.writeInterval);
    }

    private connectReadSessionToLocalStorage() {
        if (this.readLocalStorageConnection) clearInterval(this.readLocalStorageConnection);
        this.readLocalStorageConnection = setInterval(() => {
            try {
                if ((Date.now() - this.lastLocalStorageRead) >= (this.readInterval - 100)) {
                    this.readLocalTasks();
                    this.updateStatusVars();
                    this.emitTasks();
                }
            } catch (e) {
                clearInterval(this.readLocalStorageConnection);
                this.lastLocalStorageRead = null;
                throw e;
            }
        }, this.readInterval);
    }

    private refresh() {
        this.taskService.getTasks().then(items => {
            if (items) {
                this.tasks = items;
                this.updateStatusVars();
                this.emitTasks();
            }
        });
    }

    updateStatusVars() {
        let tmpTasksInProgress = [];
        let tmpTasksInWait = [];
        for (let task of this.allTasks) {
            if (task.eventType.startsWith("downloadDataset") && (task.status == 2 || task.status == 4 || task.status == 5)) {
                if (!this.sessionService.isActive(task.sessionId)) {
                    task.status = -1;
                    task.message = 'interrupted';
                }
            }
            if (task.status == -1 && task.lastUpdate) {
                let freshError: boolean = !this.freshCompletedTasks?.find(t => t.id == task.id && t.status == -1) && !!this.tasksInProgress.find(tip => task.id == tip.id) || (Date.now() - new Date(task.lastUpdate).getTime()) <= (this.readInterval);
                if (freshError) {
                    this.freshTimeouts[task.id]?.triggerNow();
                    this.pushToFreshError(task);
                }
            } else if (task.status == 1 || task.status == 3) {
                let freshDone: boolean = !this.freshCompletedTasks?.find(t => t.id == task.id && (t.status == 1 || t.status == 3)) && !!this.tasksInProgress.find(tip => task.id == tip.id) || (Date.now() - new Date(task.lastUpdate).getTime()) <= (this.readInterval);
                if (freshDone) {
                    this.freshTimeouts[task.id]?.triggerNow();
                    this.pushToFreshCompleted(task);
                }
            } else if (task.status == 2 || task.status == 5) {
                this.freshTimeouts[task.id]?.triggerNow();
                tmpTasksInProgress.push(task);
            } else if (task.status == 4) {
                this.freshTimeouts[task.id]?.triggerNow();
                tmpTasksInWait.push(task);
            }
        }
        this.tasksInProgress = tmpTasksInProgress;
        this.tasksInWait = tmpTasksInWait;
    }

    pushToFreshCompleted(task: Task) {
        if (!this.freshCompletedTasks.find(tip => tip.id == task.id)) {
            this.freshCompletedTasks.push(task);
        }
        this.nbNew++;
        // remove after 30min
        this.freshTimeouts[task.id] = new SuperTimeout(() => {
            this.freshCompletedTasks = this.freshCompletedTasks.filter(tip => tip.id != task.id);
        }, this.persistenceTime);
    }

    pushToFreshError(task: Task) {
        if (!this.freshCompletedTasks.find(tip => tip.id == task.id)) {
            this.freshCompletedTasks.push(task);
        }
        this.nbNewError++;
        // remove after 30min
        this.freshTimeouts[task.id] = new SuperTimeout(() => {
            this.freshCompletedTasks = this.freshCompletedTasks.filter(tip => tip.id != task.id);
        }, this.persistenceTime);
    }

    private connectToServer() {
        this.keycloakService.getToken().then(token => {
            this.source = new EventSourcePolyfill(AppUtils.BACKEND_API_UPDATE_TASKS_URL, {
                    headers: {
                        'Authorization': "Bearer " + token
                    }
                });
            this.source.addEventListener('message', message => {
                if (message.data !== "{}") {
                    let task: Task = this.taskService.toRealObject(JSON.parse(message.data));
                    this.tasks = this.tasks.filter(t => t.completeId != task.completeId);
                    this.tasks.push(task);
                    this.updateStatusVars();
                    this.emitTasks();
                }
            });
            this.refresh();
        });
    }

    public getNotifications(): Observable<Task[]> {
        return this.tasksSubject;
    }

    pushLocalTask(task: Task) {
        this.createOrUpdateTask(task.clone());
    }

    private createOrUpdateTask(task: Task) {
        this.newLocalTasksQueue = this.newLocalTasksQueue.filter(t => t.id != task.id);
        this.newLocalTasksQueue.push(task);
        if (!this.writeLocalStorageConnection) {
            this.connectWriteSessionToLocalStorage();
        }
    }

    private emitTasks() {
        this.tasksSubject.next(this.allTasks);
    }

    private get allTasks(): Task[] {
        return this.localTasks.concat(this.tasks)
            .sort((a, b) => new Date(b.creationDate).getTime() - new Date(a.creationDate).getTime());
    }

    private readLocalTasks() {
        let storageTasksStr: string = localStorage.getItem(this.storageKey);
        this.lastLocalStorageRead = Date.now();
        let storageTasks: Task[] = [];
        if (storageTasksStr) {
            storageTasks = JSON.parse(storageTasksStr).map(task => Object.assign(new Task(), task));
        }
        this.localTasks = storageTasks;
    }

    private updateLocalStorage() {
        this.readLocalTasks();
        let tmpTasks: Task[] = this.localTasks.filter(lt => !this.newLocalTasksQueue.find(nlt => lt.id == nlt.id));
        tmpTasks = tmpTasks.concat(this.newLocalTasksQueue);
        let tmpTasksStr: string = this.serializeTasks(tmpTasks); // also checks the size limit
        localStorage.setItem(this.storageKey, tmpTasksStr);
        this.newLocalTasksQueue = [];
        this.localTasks = tmpTasks;
        this.updateStatusVars();
        this.emitTasks();
    }

    private serializeTasks(tasks: Task[]): string {
        let tasksToStore: Task[] = [].concat(tasks);
        let str: string = '[' + tasksToStore.map(t => t.stringify()).join(',') + ']';
        while (str.length > 5200000) {
            tasksToStore.shift();
            str = '[' + tasksToStore.map(t => t.stringify()).join(',') + ']';
        }
        return str;
    }

    totalProgress(): number {
        let total: number = 0;
        this.tasksInProgress.forEach(task => total += task.progress);
        return total/this.tasksInProgress.length;
    }
}
