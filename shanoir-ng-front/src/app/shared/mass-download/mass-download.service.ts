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

import { HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Task } from 'src/app/async-tasks/task.model';
import { Dataset } from 'src/app/datasets/shared/dataset.model';
import { DatasetService, Format } from 'src/app/datasets/shared/dataset.service';
import { NotificationsService } from '../notifications/notifications.service';

export type Report = {
    requestedDatasetIds: number[],
    startTime: number,
    list?: {
        [key: number]: {
            status: 'QUEUED' | 'ERROR' | 'SUCCESS',
            error?: any,
            errorTime?: number
        }
    }
    nbSuccess?: number;
    nbError?: number;
};

@Injectable()
export class MassDownloadService {

    constructor(
            private datasetService: DatasetService,
            private notificationService: NotificationsService) {}

    download(datasetIds: number[], format: Format, nbQueues: number = 4) {
        if (datasetIds.length == 0) return;
        this.getFolderHandle().then(parentFolderHandle => { // ask the user's parent directory
            let task: Task = this.createTask(datasetIds);
            let ids = [...datasetIds]; // copy array
            let report: Report = this.initReport(datasetIds);
            datasetIds.forEach(id => report.list[id] = { status: 'QUEUED' });
            let promises: Promise<void>[] = []; 
            for (let queueIndex = 0; queueIndex < nbQueues; queueIndex ++) { // build the dl queues
                promises.push(
                    this.recursiveSave(ids.shift(), format, parentFolderHandle, ids, report, task)
                );
            }
            Promise.all(promises).then(() => {
                task.lastUpdate = new Date();
                task.status = 1;
                task.message = 'download completed, files saved in the selected directory';
            });
        }).catch(error => { /* the user clicked 'cancel' in the choose directory window */ });
    }

    private recursiveSave(id: number, format: Format, userFolderHandle: FileSystemDirectoryHandle, remainingIds: number [], report: Report, task: Task): Promise<void> {
        return this.saveDataset(id, format, userFolderHandle, report, task).then(() => {
            if (remainingIds.length > 0) {
                return this.recursiveSave(remainingIds.shift(), format, userFolderHandle, remainingIds, report, task);
            } else {
                return Promise.resolve();
            }
        });
    } 

    private saveDataset(id: number, format: Format, userFolderHandle: FileSystemDirectoryHandle, report: Report, task: Task): Promise<void> {
        const metadataPromise: Promise<Dataset> = this.datasetService.get(id, 'lazy');
        const downloadPromise: Promise<HttpResponse<Blob>> = this.datasetService.downloadToBlob(id, format);
        return Promise.all([metadataPromise, downloadPromise]).then(([dataset, httpResponse]) => {
            const blob: Blob = httpResponse.body;
            const filename: string = this.getFilename(httpResponse);
            const path: string = 
                    dataset.datasetAcquisition?.examination?.comment 
                    + '_' + dataset.datasetAcquisition?.examination?.id
                    + '/'
                    + filename
            return this.writeMyFile(path, blob, userFolderHandle);
        }).then(() => {
            report.list[id].status = 'SUCCESS';
            report.nbSuccess++;
            task.message = '(' + report.nbSuccess + '/' + report.requestedDatasetIds.length + ') dataset n°' + id + ' successfully saved' ;
        }).catch(reason => {
            report.list[id].status = 'ERROR';
            report.list[id].error = reason;
            report.list[id].errorTime = Date.now();
            report.nbError++;
            task.message = 'saving dataset n°' + id + ' failed' ;
            task.status = -1;
        }).finally(() => {
            task.lastUpdate = task.creationDate;
            task.progress = (report.nbSuccess + report.nbError) / report.requestedDatasetIds.length;
        });
    }

    private writeMyFile(path: string, content: any, userFolderHandle: FileSystemDirectoryHandle): Promise<void> {
        path = path.trim();
        if (path.startsWith('/')) path = path.substring(1); // remove 1st '/'
        let splited: string[] = path.split('/');
        const filename = splited.pop(); // separate filename from dir path

        return this.createDirectoriesIn(splited, userFolderHandle).then(lastFolderHandle => { // create the sub directories
            lastFolderHandle.getFileHandle(filename, { create: true } // create the file handle
                ).then(fileHandler => {
                    this.writeFile(fileHandler, content); // write the file
                });
        });
    }

    private async getFolderHandle(): Promise<FileSystemDirectoryHandle> {
        const options = {
            mode: 'readwrite'
        };
        const handle: FileSystemDirectoryHandle = await window.showDirectoryPicker(options);
        return handle;
    }

    private async writeFile(fileHandle: FileSystemFileHandle, contents) {
        // Create a FileSystemWritableFileStream to write to.
        const writable: FileSystemWritableFileStream = await fileHandle.createWritable();
        // Write the contents of the file to the stream.
        await writable.write(contents);
        // Close the file and write the contents to disk.
        await writable.close();
      }
      
    private createDirectoriesIn(dirs: string[], parentFolderHandle: FileSystemDirectoryHandle): Promise<FileSystemDirectoryHandle> {
        const dirToCreate: string = dirs.shift(); // separate the first element
        return parentFolderHandle.getDirectoryHandle(dirToCreate, { create: true})
                .then(handle => {
                    if (dirs.length > 0) {
                        return this.createDirectoriesIn(dirs, handle);
                    } else return handle;
                });
    }

    private getFilename(response: HttpResponse<any>): string {
        const prefix = 'attachment;filename=';
        let contentDispHeader: string = response.headers.get('Content-Disposition');
        return contentDispHeader.slice(contentDispHeader.indexOf(prefix) + prefix.length, contentDispHeader.length).replace('/', '_');
    }

    private initReport(datasetIds: number[]): Report {
        return{
            requestedDatasetIds: datasetIds,
            startTime: Date.now(),
            list: {},
            nbError: 0,
            nbSuccess: 0
        };
    }

    private createTask(datasetIds: number[]): Task {
        let task: Task = new Task();
        task.id = Date.now();
        task.creationDate = new Date();
        task.lastUpdate = task.creationDate;
        task.message = 'Download launched for ' + datasetIds.length + ' datasets';
        task.progress = 0;
        task.status = 2;
        task.eventType = 'downloadDataset.event';
        this.notificationService.pushLocalTask(task);
        return task;
    }
}

