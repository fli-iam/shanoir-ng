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

import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Task, TaskState } from 'src/app/async-tasks/task.model';

import * as AppUtils from '../../utils/app.utils';
import { NotificationsService } from '../notifications/notifications.service';
import { SessionService } from '../services/session.service';


@Injectable()
export class SingleDownloadService {

    constructor(
        private notificationService: NotificationsService,
        private sessionService: SessionService) {
    }

    /**
     * Handles large files. Download a single file, displaying a loading bar in the side menu if the dl takes more than 5s.
     * 
     * It is impossible to nicely use the browser dl for large files.
     * It's because the browser need a dl through a <a href> to do so, and we can't as we have an auth token to put in a http header.
     * But using js makes the browser dl the file silently, then copying the blob to the dl dir with the nice browser display.
     * So with large files, the silent step is confusing for users. Here we will help her/him by displaying the progress.
     * 
     * @param url 
     * @param params 
     * @param state 
     * @param totalSize total size of the file, if known
     * @returns 
     */
    downloadSingleFile(url: string, params?: HttpParams, state?: TaskState): Observable<TaskState> {
        let obs: Observable<TaskState> = AppUtils.downloadWithStatusGET(url, params, state);

        let task: Task = new Task();
        task.id = Date.now();
        task.creationDate = new Date();
        task.lastUpdate = task.creationDate;
        task.message = 'Downloading ' + (url + '').split('/').pop();
        task.progress = 0;
        task.status = 2;
        task.eventType = 'downloadFile.event';
        task.sessionId = this.sessionService.sessionId;

        let startTs: number = Date.now();
        obs.subscribe(event => {
            let ts: number = Date.now();
            if (ts - startTs > 5000) {
                if (event.progress) {
                    task.progress = event.progress;
                }
                task.status = event.status;
                if (task.status == 1) task.progress = 1;
                this.notificationService.pushLocalTask(task);
            }
        });
    
        return obs;
    }
}