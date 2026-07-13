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

import { HttpClient, HttpEvent, HttpEventType, HttpParams, HttpProgressEvent, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom, last, map, mergeMap, Observable, shareReplay } from 'rxjs';

import { TaskState, TaskStatus } from 'src/app/async-tasks/task.model';
import { browserDownloadFileFromResponse } from 'src/app/utils/app.utils';

declare let JSZip: any;

@Injectable()
export class DownloadUtilsService {

    constructor(
        private http: HttpClient) {
    }

    downloadBlob(url: string, params?: HttpParams): Promise<Blob> {
        return firstValueFrom(this.http.get(
            url,
            {
                reportProgress: true,
                responseType: 'blob',
                params: params
            }
        )
        .pipe(map(response => {
            return response;
        })));
    }

    downloadWithStatusGET(url: string, params?: HttpParams, state?: TaskState): Observable<TaskState> {
        const obs: Observable<HttpEvent<Blob>> = this.http.get(
            url,
            {
                reportProgress: true,
                observe: 'events',
                responseType: 'blob',
                params: params
            }
        ).pipe(shareReplay());
        obs.pipe(last()).subscribe(response => {
            browserDownloadFileFromResponse(response as HttpResponse<Blob>)
        });
        return obs.pipe(mergeMap(event => {
            return this.extractState(event).then(s => {
                if (state) {
                    state.errors = s.errors;
                    state.progress = s.progress;
                    state.status = s.status;
                }
                return s;
            });
        }));
    }

    downloadWithStatusPOST(url: string, formData: FormData, state?: TaskState): Observable<TaskState> {
        const obs: Observable<HttpEvent<Blob>> = this.http.post(
            url,
            formData,
            {
                reportProgress: true,
                observe: 'events',
                responseType: 'blob'
            }
        ).pipe(shareReplay());
        obs.pipe(last()).subscribe(response => {
            browserDownloadFileFromResponse(response as HttpResponse<Blob>)
        });
        return obs.pipe(mergeMap(event => {
            return this.extractState(event).then(s => {
                if (state) {
                    state.errors = s.errors;
                    state.progress = s.progress;
                    state.status = s.status;
                }
                return s;
            });
        }));
    }

    private extractState(event: HttpEvent<any>): Promise<TaskState> {
        switch (event.type) {
            case HttpEventType.Sent:
            case HttpEventType.ResponseHeader: {
                const task = new TaskState(TaskStatus.QUEUED, 0);
                return Promise.resolve(task);
            }
            case HttpEventType.DownloadProgress: {
                const total: number | undefined = (event as HttpProgressEvent).total;
                const task = new TaskState(TaskStatus.IN_PROGRESS, (event as HttpProgressEvent).loaded);
                if (task.progress == undefined) task.progress = 0;
                if (total) task.progress /= total;
                return Promise.resolve(task);
            }
            case HttpEventType.Response: {
                const task = new TaskState(TaskStatus.DONE);
                const blob: Blob | null = (event as HttpResponse<Blob>).body;
                if (blob && event.headers.get('Content-Type') == 'application/zip') {
                    //report.list[id].zipSize = getSizeStr(blob?.size);
                    // Check ERRORS file in zip
                    const zip: any = new JSZip();
                    return zip.loadAsync(blob).then((dataFiles: any) => {
                        if (dataFiles.files['ERRORS.json']) {
                            return dataFiles.files['ERRORS.json'].async('string').then((content: any) => {
                                const errorsJson: any = JSON.parse(content);
                                task.errors = JSON.stringify(errorsJson, null, 4);
                                task.status = TaskStatus.DONE_BUT_WARNING;
                                return task;
                            });
                        }
                        return task;
                    });
                } else {
                    return Promise.resolve(task);
                }
            }
            default: return Promise.resolve(new TaskState());
        }
    }
}