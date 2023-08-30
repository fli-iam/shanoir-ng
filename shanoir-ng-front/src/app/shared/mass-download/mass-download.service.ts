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
import { ComponentRef, Injectable } from '@angular/core';
import { Observable, Subject, Subscription } from 'rxjs-compat';
import { Task, TaskStatus } from 'src/app/async-tasks/task.model';
import { Dataset } from 'src/app/datasets/shared/dataset.model';
import { DatasetService, Format } from 'src/app/datasets/shared/dataset.service';
import { ServiceLocator } from 'src/app/utils/locator.service';
import { NotificationsService } from '../notifications/notifications.service';
import { DownloadSetupComponent } from './download-setup/download-setup.component';
import { ConfirmDialogService } from '../components/confirm-dialog/confirm-dialog.service';
import { Queue } from './queue.model';
import { take } from 'rxjs/operators';
import { SuperPromise } from 'src/app/utils/super-promise';
import { DownloadSetupAltComponent } from './download-setup-alt/download-setup-alt.component';

declare var JSZip: any;

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
    duration?: number;
};

@Injectable()
export class MassDownloadService {

    private downloadQueue: Queue = new Queue();
    readonly BROWSER_COMPAT_ERROR_MSG: string = 'browser not compatible';

    constructor(
        private datasetService: DatasetService,
        private notificationService: NotificationsService,
        private dialogService: ConfirmDialogService) {
    }

    downloadByIds(datasetIds: number[]): Promise<void> {
        return this.openModal().then(ret => {
            if (ret != 'cancel') {
                return this._downloadByIds(datasetIds, ret.format, ret.nbQueues)
            } else return Promise.resolve();
        }).catch(error => {
            if (error == this.BROWSER_COMPAT_ERROR_MSG) {
                return this.openAltModal().then(ret => {
                    if (ret != 'cancel') {
                        return this._downloadByIdsAlt(datasetIds, ret);
                    } else return Promise.resolve();
                });
            } else throw error;
        });
    }

    downloadDatasets(datasets: Dataset[]): Promise<void> {
        return this.openModal().then(ret => {
            if (ret != 'cancel') {
                return this._downloadDatasets(datasets, ret.format, ret.nbQueues);
            } else return Promise.resolve();
        });
    }

    downloadAllByStudyId(studyId: number): Promise<void> {
        return this.openModal().then(ret => {
            if (ret != 'cancel') {
                return this.datasetService.getByStudyId(studyId).then(datasets => {
                    this._downloadDatasets(datasets, ret.format, ret.nbQueues);
                })
            } else return Promise.resolve();
        });
    }

    downloadAllByStudyIdAndSubjectId(studyId: number, subjectId: number): Promise<void> {
        return this.openModal().then(ret => {
            if (ret != 'cancel') {
                return this.datasetService.getByStudyIdAndSubjectId(studyId, subjectId).then(datasets => {
                    return this._downloadDatasets(datasets, ret.format, ret.nbQueues).then(r => {
                        return r;
                    });
                })
            } else return Promise.resolve();
        });
    }

    private _downloadByIds(datasetIds: number[], format: Format, nbQueues: number = 4): Promise<void> {
        if (datasetIds.length == 0) return;
        return this.getFolderHandle().then(parentFolderHandle => { // ask the user's parent directory
            let task: Task = this.createTask(datasetIds.length);
            return this.downloadQueue.waitForTurn().then(releaseQueue => {
                try {
                    task.status = 2;
                    const start: number = Date.now();
                    let ids = [...datasetIds]; // copy array
                    let report: Report = this.initReport(datasetIds);
                    let promises: Promise<void>[] = [];
                    for (let queueIndex = 0; queueIndex < nbQueues; queueIndex++) { // build the dl queues
                        promises.push(
                            this.recursiveSave(ids.shift(), format, parentFolderHandle, ids, report, task)
                        );
                    }
                    return Promise.all(promises).then(() => {
                        this.handleEnd(task, report, start);
                    }).catch(reason => {
                        task.message = 'download error : ' + reason;
                        this.notificationService.pushLocalTask(task);
                    }).finally(() => {
                        releaseQueue();
                    });
                } catch (error) {
                    releaseQueue();
                    throw error;
                }
            });
        }).catch(error => { /* the user clicked 'cancel' in the choose directory window */ });
    }

    private _downloadByIdsAlt(datasetIds: number[], format: Format): Promise<void> {
        if (datasetIds.length == 0) return;
        let task: Task = this.createTask(datasetIds.length);
        return this.downloadQueue.waitForTurn().then(releaseQueue => {
            try {
                task.status = 2;
                const start: number = Date.now();
                const downloadObs: Observable<{status?: TaskStatus, progress?: number}> = this.datasetService.downloadDatasets(datasetIds, format);
                let endPromise: SuperPromise<void> = new SuperPromise();

                let errorFunction = error => {
                    task.lastUpdate = new Date();
                    task.status = -1;
                    task.message = 'error while downloading : ' + (error?.message || error?.toString() || 'see logs');
                    this.notificationService.pushLocalTask(task);
                    releaseQueue();
                    endPromise.reject(error);
                }

                const flowSubscription: Subscription = downloadObs.subscribe(state => {
                    task.lastUpdate = new Date();
                    task.progress = state?.progress;
                    task.status = state?.status;
                    this.notificationService.pushLocalTask(task);
                }, errorFunction);

                const endSubscription: Subscription = downloadObs.last().subscribe(state => {
                    flowSubscription.unsubscribe();
                    let duration: number = Date.now() - start;
                    task.message = 'download completed in ' + duration + 'ms, ' + datasetIds.length + ' datasets zipped';
                    task.lastUpdate = new Date();
                    task.status = 1;
                    task.progress = 1;
                    this.notificationService.pushLocalTask(task);
                    endPromise.resolve();
                }, errorFunction);

                return endPromise.finally(() => {
                    flowSubscription.unsubscribe();
                    endSubscription.unsubscribe();
                    releaseQueue();
                });
            } catch (error) {
                releaseQueue();
                throw error;
            }
        });
    }

    private _downloadDatasets(datasets: Dataset[], format: Format, nbQueues: number = 4): Promise<void> {
        if (datasets.length == 0) return;
        return this.getFolderHandle().then(parentFolderHandle => { // ask the user's parent directory
            let task: Task = this.createTask(datasets.length);
            return this.downloadQueue.waitForTurn().then(releaseQueue => {
                try {
                    task.status = 2;
                    const start: number = Date.now();
                    let ids = [...datasets.map(ds => ds.id)];
                    let report: Report = this.initReport(datasets.map(ds => ds.id));
                    let promises: Promise<void>[] = [];
                    let j = 0;
                    for (let queueIndex = 0; queueIndex < nbQueues; queueIndex++) { // build the dl queues
                        promises.push(
                            this.recursiveSave(ids.shift(), format, parentFolderHandle, ids, report, task, datasets)
                        );
                    }
                    return Promise.all(promises).then(() => {
                        this.handleEnd(task, report, start);
                    }).catch(reason => {
                        task.message = 'download error : ' + reason;
                        this.notificationService.pushLocalTask(task);
                    }).finally(() => {
                        releaseQueue();
                    });
                } catch (error) {
                    releaseQueue();
                    throw error;
                }
            });
        }).catch(error => { /* the user clicked 'cancel' in the choose directory window */ });
    }

    private handleEnd(task: Task, report: Report, start: number) {
        task.lastUpdate = new Date();
        report.duration = Date.now() - start;
        if (report.nbError > 0) {
            task.status = -1;
            const tab: string = '- ';
            task.message = 'download failed in ' + report.duration + 'ms.\n'
                + tab + report.nbSuccess + ' datasets were successfully downloaded\n'
                + tab + report.nbError + ' datasets were downloaded but are (at least partially) in error and files could be missing.\n'
                + 'errors details :\n'
                + JSON.stringify(report, null, 4);
            JSON.stringify(report);
        } else {
            task.status = task.status == -1 ? -1 : 1;
            task.message = 'download completed in ' + report.duration + 'ms, ' + report.nbSuccess + ' files saved in the selected directory';
        }

        this.notificationService.pushLocalTask(task);
    }

    private recursiveSave(id: number, format: Format, userFolderHandle: FileSystemDirectoryHandle, remainingIds: number[], report: Report, task: Task, datasets?: Dataset[]): Promise<void> {
        if (!id) return Promise.resolve();
        return this.saveDataset(id, format, userFolderHandle, report, task, datasets?.find(ds => ds.id == id)).then(() => {
            if (remainingIds.length > 0) {
                return this.recursiveSave(remainingIds.shift(), format, userFolderHandle, remainingIds, report, task, datasets);
            } else {
                return Promise.resolve();
            }
        });
    }

    private saveDataset(id: number, format: Format, userFolderHandle: FileSystemDirectoryHandle, report: Report, task: Task, dataset?: Dataset): Promise<void> {
        const metadataPromise: Promise<Dataset> = (dataset?.id == id && dataset.datasetAcquisition?.examination?.subject) ? Promise.resolve(dataset) : this.datasetService.get(id, 'lazy');
        const downloadPromise: Promise<HttpResponse<Blob>> = this.datasetService.downloadToBlob(id, format);
        return Promise.all([metadataPromise, downloadPromise]).then(([dataset, httpResponse]) => {
            const blob: Blob = httpResponse.body;
            const filename: string = this.getFilename(httpResponse) || 'dataset_' + id;
            const path: string = this.buildDatasetPath(dataset) + filename;

            // Check ERRORS file in zip
            var zip = new JSZip();
            let unzipTs: number = Date.now();
            const errorsCheckPromise: Promise<void> = zip.loadAsync(httpResponse.body).then(dataFiles => {
                if (dataFiles.files['ERRORS.json']) {
                    return dataFiles.files['ERRORS.json'].async('string').then(content => {
                        const errorsJson: any = JSON.parse(content);
                        report.list[id].status = 'ERROR';
                        report.list[id].error = errorsJson;
                        report.list[id].errorTime = Date.now();
                        task.message = 'saving dataset n°' + id + ' failed';
                        task.status = 5;
                    });
                } else {
                    report.list[id].status = 'SUCCESS';
                    task.message = '(' + report.nbSuccess + '/' + report.requestedDatasetIds.length + ') dataset n°' + id + ' successfully saved';
                }
            });

            return Promise.all([errorsCheckPromise, this.writeMyFile(path, blob, userFolderHandle)]).then(() => null);

        }).catch(reason => {
            report.list[id].status = 'ERROR';
            report.list[id].error = reason;
            report.list[id].errorTime = Date.now();
            task.message = 'saving dataset n°' + id + ' failed';
            task.status = 5;
        }).finally(() => {
            if (report.list[id].status == 'SUCCESS') {
                report.nbSuccess++;
            } else if (report.list[id].status == 'ERROR') {
                report.nbError++;
            }
            task.lastUpdate = task.creationDate;
            task.progress = (report.nbSuccess + report.nbError) / report.requestedDatasetIds.length;
            this.notificationService.pushLocalTask(task);
        });
    }

    private buildDatasetPath(dataset: Dataset): string {
        return dataset.datasetAcquisition?.examination?.subject?.name
                + '_' + dataset.datasetAcquisition?.examination?.subject?.id
                + '/'
                + dataset.datasetAcquisition?.examination?.comment
                + '_' + dataset.datasetAcquisition?.examination?.id
                + '/';
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
        // @ts-ignore
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
        return parentFolderHandle.getDirectoryHandle(dirToCreate, { create: true })
            .then(handle => {
                if (dirs.length > 0) {
                    return this.createDirectoriesIn(dirs, handle);
                } else return handle;
            });
    }

    private getFilename(response: HttpResponse<any>): string {
        const prefix = 'attachment;filename=';
        let contentDispHeader: string = response.headers.get('Content-Disposition');
        return contentDispHeader?.slice(contentDispHeader.indexOf(prefix) + prefix.length, contentDispHeader.length).replace('/', '_');
    }

    private initReport(datasetIds: number[]): Report {
        let report: Report = {
            requestedDatasetIds: datasetIds,
            startTime: Date.now(),
            list: {},
            nbError: 0,
            nbSuccess: 0
        };
        datasetIds.forEach(id => report.list[id] = { status: 'QUEUED' });
        return report;
    }

    private createTask(nbDatasets: number): Task {
        let task: Task = new Task();
        task.id = Date.now();
        task.creationDate = new Date();
        task.lastUpdate = task.creationDate;
        task.message = 'Download launched for ' + nbDatasets + ' datasets';
        task.progress = 0;
        task.status = 2;
        task.eventType = 'downloadDataset.event';
        this.notificationService.pushLocalTask(task);
        return task;
    }

    private openModal(): Promise<{format: Format, nbQueues: number} | 'cancel'> {
        // @ts-ignore
        if (window.showDirectoryPicker) { // test compatibility
            let modalRef: ComponentRef<DownloadSetupComponent> = ServiceLocator.rootViewContainerRef.createComponent(DownloadSetupComponent);
            return this.waitForEnd(modalRef);
        } else {
            return Promise.reject(this.BROWSER_COMPAT_ERROR_MSG);
        }
    }

    private openAltModal(): Promise<Format | 'cancel'> {
        let modalRef: ComponentRef<DownloadSetupAltComponent> = ServiceLocator.rootViewContainerRef.createComponent(DownloadSetupAltComponent);
        return this.waitForEnd(modalRef);
    }

    private waitForEnd(modalRef: ComponentRef<any>): Promise<any | 'cancel'> {
        let resPromise: SuperPromise<any | 'cancel'> = new SuperPromise();
        let result: Observable<any> = Observable.race([
            modalRef.instance.go, 
            modalRef.instance.close.map(() => 'cancel')
        ]);
        result.pipe(take(1)).subscribe(ret => {
            modalRef.destroy();
            resPromise.resolve(ret);
        }, error => {
            modalRef.destroy();
            resPromise.reject(error);
        });
        return resPromise;
    }
}

