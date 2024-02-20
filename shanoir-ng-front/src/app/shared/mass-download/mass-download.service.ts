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

import { formatDate } from '@angular/common';
import { HttpResponse } from '@angular/common/http';
import { ComponentRef, Injectable } from '@angular/core';
import { Observable, Subscription } from 'rxjs-compat';
import { take } from 'rxjs/operators';
import { Task, TaskState } from 'src/app/async-tasks/task.model';
import { Dataset } from 'src/app/datasets/shared/dataset.model';
import { DatasetService, Format } from 'src/app/datasets/shared/dataset.service';
import { ServiceLocator } from 'src/app/utils/locator.service';
import { SuperPromise } from 'src/app/utils/super-promise';
import { ConfirmDialogService } from '../components/confirm-dialog/confirm-dialog.service';
import { ConsoleService } from '../console/console.service';
import { NotificationsService } from '../notifications/notifications.service';
import { DownloadSetupAltComponent } from './download-setup-alt/download-setup-alt.component';
import { DownloadSetupComponent, DownloadSetupOptions } from './download-setup/download-setup.component';
import { Queue } from './queue.model';
import { ShanoirError } from '../models/error.model';
import { getSizeStr } from 'src/app/utils/app.utils';

declare var JSZip: any;

export type Report = {
    taskId: number,
    folderName: string,
    requestedDatasetIds: number[],
    startTime: number,
    list?: {
        [key: number]: {
            status: 'QUEUED' | 'ERROR' | 'SUCCESS',
            error?: any,
            errorTime?: number
            zipSize?: string,
        }
    }
    nbSuccess?: number;
    nbError?: number;
    duration?: number;
    format: Format;
    nbQueues: number;
    unzip: boolean;
};

@Injectable()
export class MassDownloadService {

    private downloadQueue: Queue = new Queue();
    readonly BROWSER_COMPAT_ERROR_MSG: string = 'browser not compatible';
    readonly REPORT_FILENAME: string = 'downloadReport.json';

    constructor(
        private datasetService: DatasetService,
        private notificationService: NotificationsService,
        private consoleService: ConsoleService,
        private dialogService: ConfirmDialogService) {
    }

    downloadByIds(datasetIds: number[], format?: Format): Promise<void> {
        return this.openModal(format).then(ret => {
            if (ret != 'cancel') {
                return this._downloadByIds(datasetIds, ret);
            } else return Promise.resolve();
        }).catch(error => {
            if (error == this.BROWSER_COMPAT_ERROR_MSG) {
                if (datasetIds.length > this.datasetService.MAX_DATASETS_IN_ZIP_DL) {
                    this.dialogService.error('Too many datasets', 'You are trying to download ' 
                        + datasetIds.length + ' datasets while Shanoir sets a limit to ' + this.datasetService.MAX_DATASETS_IN_ZIP_DL 
                        + ' in a single zip. Please confider using a browser compatible with the Shanoir unlimited download functionality. See link below.',
                        "https://developer.mozilla.org/en-US/docs/Web/API/Window/showDirectoryPicker#browser_compatibility" );
                } else {
                    return this.openAltModal(format).then(ret => {
                        if (ret != 'cancel') {
                            return this._downloadAlt('datasetIds', datasetIds, ret);
                        } else return Promise.resolve();
                    });
                }
            } else throw error;
        });
    }

    downloadAllByStudyId(studyId: number): Promise<void> {
        return this.openModal().then(ret => {
            if (ret != 'cancel') {
                return this.datasetService.getByStudyId(studyId).then(datasets => {
                    this._downloadDatasets(datasets, ret);
                })
            } else return Promise.resolve();
        }).catch(error => {
            if (error == this.BROWSER_COMPAT_ERROR_MSG) {
                this.datasetService.countDatasetsByStudyId(studyId).then(nbDatasets => {
                    if (nbDatasets > this.datasetService.MAX_DATASETS_IN_ZIP_DL) {
                        this.dialogService.error('Too many datasets', 'This study contains ' 
                            + nbDatasets + ' datasets while Shanoir sets a limit to ' + this.datasetService.MAX_DATASETS_IN_ZIP_DL 
                            + ' in a single zip. Please confider using a browser compatible with the Shanoir unlimited download functionality. See link below.',
                            "https://developer.mozilla.org/en-US/docs/Web/API/Window/showDirectoryPicker#browser_compatibility" );
                    } else {
                        return this.openAltModal().then(ret => {
                            if (ret != 'cancel') {
                                return this._downloadAlt('studyId', studyId, ret);
                            } else return Promise.resolve();
                        });
                    }
                });
            } else throw error;
        });
    }

    downloadAllByExaminationId(examinationId: number, format?: Format, options?: DownloadSetupOptions, downloadState?: TaskState): Promise<void> {
        const datasetsPromise: Promise<Dataset[]> = this.datasetService.getByExaminationId(examinationId);
        return this.openModal(format, options).then(ret => {
            if (ret != 'cancel') {
                return datasetsPromise.then(datasets => {
                    this._downloadDatasets(datasets, ret, downloadState);
                })
            } else return Promise.resolve();
        }).catch(error => {
            if (error == this.BROWSER_COMPAT_ERROR_MSG) {
                return datasetsPromise.then(datasets => {
                    return this.openAltModal(format, options).then(ret => {
                        if (ret != 'cancel') {
                            return this._downloadAlt('datasetIds', datasets.map(ds => ds.id), ret, downloadState);
                        } else return Promise.resolve();
                    });
                });
            } else throw error;
        });
    }

    // zip only
    downloadAllByAcquisitionId(acquisitionId: number, options?: DownloadSetupOptions, downloadState?: TaskState) {
        return this.openAltModal(null, options, false).then(ret => {
            if (ret != 'cancel') {
                this.datasetService.downloadDatasetsByAcquisition(acquisitionId, ret, downloadState);
            } else return Promise.resolve();
        });
    }

    // zip only
    downloadDataset(datasetId: number, options: DownloadSetupOptions, downloadState?: TaskState) {
        return this.openAltModal(null, options, false).then(ret => {
            if (ret != 'cancel') {
                this.datasetService.downloadFromId(datasetId, ret, null, downloadState);
            } else return Promise.resolve();
        });
    }

    downloadAllByStudyIdAndSubjectId(studyId: number, subjectId: number): Promise<void> {
        return this.openModal().then(ret => {
            if (ret != 'cancel') {
                return this.datasetService.getByStudyIdAndSubjectId(studyId, subjectId).then(datasets => {
                    return this._downloadDatasets(datasets, ret).then(r => {
                        return r;
                    });
                })
            } else return Promise.resolve();
        });
    }

    private _downloadByIds(datasetIds: number[], options: DownloadOptions, task?: Task, report?: Report, parentHandle?: FileSystemDirectoryHandle): Promise<void> {
        if (datasetIds.length == 0) return;
        let directoryHandlePromise: Promise<FileSystemDirectoryHandle>;
        if (parentHandle) {
            directoryHandlePromise = Promise.resolve(parentHandle);
        } else {
            directoryHandlePromise = this.getFolderHandle()
                // add a subdirectory
                .then(handle => this.makeRootSubdirectory(handle, datasetIds.length));
        }
        return directoryHandlePromise.then(parentFolderHandle => { // ask the user's parent directory
            if (!task) task = this.createTask(datasetIds.length);
            return this.downloadQueue.waitForTurn().then(releaseQueue => {
                try {
                    task.status = 2;
                    task.lastUpdate = new Date();
                    this.notificationService.pushLocalTask(task);
                    const start: number = Date.now();
                    let ids = [...datasetIds]; // copy array
                    if (!report) report = this.initReport(datasetIds, task.id, parentFolderHandle.name, options);
                    let promises: Promise<void>[] = [];
                    for (let queueIndex = 0; queueIndex < options.nbQueues; queueIndex++) { // build the dl queues
                        promises.push(
                            this.recursiveSave(ids.shift(), options, parentFolderHandle, ids, report, task)
                        );
                    }
                    return Promise.all(promises).then(() => {
                        this.handleEnd(task, report, start);
                        return this.writeMyFile(this.REPORT_FILENAME, JSON.stringify(report), parentFolderHandle);
                    }).catch(reason => {
                        task.message = 'download error : ' + reason;
                        task.lastUpdate = new Date();
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

    makeRootSubdirectory(handle: FileSystemDirectoryHandle, nbDatasets: number): Promise<FileSystemDirectoryHandle> {
        const dirName: string = 'Shanoir-download_' + nbDatasets + 'ds_' + formatDate(new Date(), 'dd-MM-YYYY_HH\'h\'mm', 'en-US');
        return handle.getDirectoryHandle(dirName, { create: true })
    }

    private _downloadAlt(inputDef: 'studyId' | 'datasetIds', input: number | number[], format: Format, downloadState?: TaskState): any {
        if (!inputDef || !input || (inputDef == 'datasetIds' && !(input as [])?.length)) throw new Error('bad arguments : ' + inputDef + ', ' + input);
        let task: Task;
        if (inputDef == 'studyId') {
            task = this.createStudyTask(input as number);
        } else if (inputDef == 'datasetIds') {
            task = this.createTask((input as number[]).length);
        }
        downloadState = new TaskState();
        downloadState.status = task.status;
        downloadState.progress = 0;

        return this.downloadQueue.waitForTurn().then(releaseQueue => {
            try {
                task.status = 2;
                task.lastUpdate = new Date();
                const start: number = Date.now();
                let downloadObs: Observable<TaskState>;
                if (inputDef == 'studyId') {
                    downloadObs = this.datasetService.downloadDatasetsByStudy(input as number, format);
                } else if (inputDef == 'datasetIds') {
                    downloadObs = this.datasetService.downloadDatasets(input as number[], format);
                }
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
                    if (downloadState) {
                        downloadState.progress = task?.progress;
                    }
                    task.status = state?.status;
                    this.notificationService.pushLocalTask(task);
                }, errorFunction);

                const endSubscription: Subscription = downloadObs.last().subscribe(state => {
                    flowSubscription.unsubscribe();
                    let duration: number = Date.now() - start;
                    if (inputDef == 'studyId') {
                        task.message = 'download completed in ' + duration + 'ms for study ' + input;
                    } else if (inputDef == 'datasetIds') {
                        task.message = 'download completed in ' + duration + 'ms for ' + (input as number[]).length + ' datasets';
                    }
                    task.lastUpdate = new Date();
                    task.status = 1;
                    task.progress = 1;
                    downloadState.progress = task.progress;
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

    private _downloadDatasets(datasets: Dataset[], options, downloadState?: TaskState): Promise<void> {
        if (datasets.length == 0) return;
        return this.getFolderHandle()
        // add a subdirectory
            .then(handle => this.makeRootSubdirectory(handle, datasets.length))
            .then(parentFolderHandle => { // ask the user's parent directory

            let task: Task = this.createTask(datasets.length);
            if (downloadState) downloadState.status = task.status;
            return this.downloadQueue.waitForTurn().then(releaseQueue => {
                try {
                    task.status = 2;
                    task.lastUpdate = new Date();
                    const start: number = Date.now();
                    let ids = [...datasets.map(ds => ds.id)];
                    let report: Report = this.initReport(datasets.map(ds => ds.id), task.id, parentFolderHandle.name, options);
                    let promises: Promise<void>[] = [];
                    let j = 0;
                    for (let queueIndex = 0; queueIndex < options.nbQueues; queueIndex++) { // build the dl queues
                        promises.push(
                            this.recursiveSave(ids.shift(), options, parentFolderHandle, ids, report, task, datasets)
                        );
                    }
                    return Promise.all(promises).then(() => {
                        this.handleEnd(task, report, start);
                        this.writeMyFile(this.REPORT_FILENAME, JSON.stringify(report), parentFolderHandle);
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

    private _downloadFromReport(report: Report, task: Task, parentHandle: FileSystemDirectoryHandle) {
        if (!report) throw new Error('report can\'t be null !');
        const noSuccessIds: number[] = Object.keys(report.list).filter(key => report.list[key].status != 'SUCCESS').map(key => parseInt(key));
        this._downloadByIds(noSuccessIds, report, task, report, parentHandle);
    }

    private handleEnd(task: Task, report: Report, start: number) {
        task.lastUpdate = new Date();
        report.duration = Date.now() - start;
        task.report = JSON.stringify(report, null, 4);
        if (report.nbError > 0) {
            task.status = 3;
            const tab: string = '- ';
            task.message = (report.nbSuccess > 0 ? 'download partially succeed in ' : 'download failed in ') + report.duration + 'ms.\n'
                + tab + report.nbSuccess + ' datasets were successfully downloaded\n'
                + tab + report.nbError + ' datasets are (at least partially) in error and files could be missing.\n';
            JSON.stringify(report);
        } else {
            task.status = task.status == -1 ? -1 : 1;
            task.message = 'download completed in ' + report.duration + 'ms, ' + report.nbSuccess + ' datasets saved in the selected directory';
        }

        this.notificationService.pushLocalTask(task);
    }

    private recursiveSave(id: number, options: DownloadOptions, userFolderHandle: FileSystemDirectoryHandle, remainingIds: number[], report: Report, task: Task, datasets?: Dataset[]): Promise<void> {
        if (!id) return Promise.resolve();
        return this.saveDataset(id, options, userFolderHandle, report, task, datasets?.find(ds => ds.id == id)).then(() => {
            if (remainingIds.length > 0) {
                return this.recursiveSave(remainingIds.shift(), options, userFolderHandle, remainingIds, report, task, datasets);
            } else {
                return Promise.resolve();
            }
        });
    }

    private saveDataset(id: number, options: DownloadOptions, userFolderHandle: FileSystemDirectoryHandle, report: Report, task: Task, dataset?: Dataset): Promise<void> {
        const metadataPromise: Promise<Dataset> = (dataset?.id == id && dataset.datasetAcquisition?.examination?.subject) ? Promise.resolve(dataset) : this.datasetService.get(id, 'lazy');
        const downloadPromise: Promise<HttpResponse<Blob>> = this.datasetService.downloadToBlob(id, options.format);
        return Promise.all([metadataPromise, downloadPromise]).then(([dataset, httpResponse]) => {
            const blob: Blob = httpResponse.body;
            report.list[id].zipSize = getSizeStr(blob?.size);
            const filename: string = this.getFilename(httpResponse) || 'dataset_' + id;
            // Check ERRORS file in zip
            let zip: any = new JSZip();
            const unzipPromise: Promise<any> = zip.loadAsync(blob).then(dataFiles => {
                if (dataFiles.files['ERRORS.json']) {
                    return dataFiles.files['ERRORS.json'].async('string').then(content => {
                        const errorsJson: any = JSON.parse(content);
                        report.list[id].status = 'ERROR';
                        report.list[id].error = errorsJson;
                        report.list[id].errorTime = Date.now();
                        task.lastUpdate = new Date();
                        task.status = 5;
                    });
                } else {
                    report.list[id].status = 'SUCCESS';
                    delete report.list[id].error;
                    delete report.list[id].errorTime;
                    return dataFiles;
                }
            });

            if (options.unzip) {
                return unzipPromise.then(data => {
                    if (data) {
                        let index: number = 1;
                        let finalPromise: Promise<void> = Promise.resolve(); // write them sequentially, not in parallel like with promise.all
                        Object.entries(data.files)?.map(([name, file]) => {
                            task.message = 'unzipping file ' + name + ' from dataset n°' + id;
                            this.notificationService.pushLocalTask(task);
                            index++;
                            let type: string;
                            if (name.endsWith('.json') || name.endsWith('.txt')) type = 'string';
                            else type = 'blob';
                            return finalPromise.then(() => {
                                (file as {async: (string) => Promise<Blob>}).async(type).then(blob => {
                                    task.message = 'saving file ' + name + ' from dataset n°' + id;
                                    this.notificationService.pushLocalTask(task);
                                    let path: string;
                                    if (options.shortPath) {
                                        path = this.buildShortFilePath(dataset, index, name);
                                    } else {
                                        path = this.buildFilePath(dataset, filename, name);
                                    }
                                    return this.writeMyFile(path, blob, userFolderHandle);
                                });
                            });
                        })
                        return finalPromise;
                    }
                });
            } else {
                const path: string = this.buildAcquisitionPath(dataset) + filename;
                task.message = 'saving dataset n°' + id;
                this.notificationService.pushLocalTask(task);
                return unzipPromise.then(() => this.writeMyFile(path, blob, userFolderHandle)).then(() => null);
            }
        }).catch(reason => {
            report.list[id].status = 'ERROR';
            report.list[id].error = reason;
            report.list[id].errorTime = Date.now();
            task.lastUpdate = new Date();
            task.message = 'saving dataset n°' + id + ' failed';
            task.status = 5;
        }).finally(() => {
            if (report.list[id].status == 'SUCCESS') {
                task.lastUpdate = new Date();
                task.message = '(' + report.nbSuccess + '/' + report.requestedDatasetIds.length + ') dataset n°' + id + ' successfully saved';
                report.nbSuccess++;
            } else if (report.list[id].status == 'ERROR') {
                task.message = 'saving dataset n°' + id + ' failed';
                report.nbError++;
            }
            task.report = JSON.stringify(report, null, 4);
            this.writeMyFile(this.REPORT_FILENAME, task.report, userFolderHandle);
            task.lastUpdate = new Date();
            task.progress = (report.nbSuccess + report.nbError) / report.requestedDatasetIds.length;
            this.notificationService.pushLocalTask(task);
        });
    }
    
    private buildFilePath(dataset: Dataset, zipName: string, fileName: string): string {
        return this.buildAcquisitionPath(dataset) + zipName.replace('.zip', '') + '/' + fileName;
    }

    private buildShortFilePath(dataset: Dataset, fileIndex: number, fileName: string): string {
            let fileNameSplit: string[] = fileName.split('.');
            let extension: string =  fileNameSplit.pop();
            return this.buildShortAcquisitionPath(dataset) + 'ds' + dataset.id + '/' + fileIndex + '.' + extension;
    }

    private buildAcquisitionPath(dataset: Dataset): string {
        return dataset.datasetAcquisition?.examination?.subject?.name
                + '_' + dataset.datasetAcquisition?.examination?.subject?.id
                + '/'
                + dataset.datasetAcquisition?.examination?.comment
                + '_' + dataset.datasetAcquisition?.examination?.id
                + '/';
    }

    private buildShortAcquisitionPath(dataset: Dataset): string {
        return 'subj' + dataset.datasetAcquisition?.examination?.subject?.id
            + '/'
            + 'exam' + dataset.datasetAcquisition?.examination?.id
            + '/';
    }

    private writeMyFile(path: string, content: any, userFolderHandle: FileSystemDirectoryHandle): Promise<void> {
        path = path.trim();
        if (path.startsWith('/')) path = path.substring(1); // remove 1st '/'
        let splitted: string[];
        if (path.includes('/')) {
            splitted = path.split('/');
        } else {
            splitted = [path];
        }
        const filename = splitted.pop(); // separate filename from dir path
        if (splitted.length > 0) { // if dirs to create
            return this.createDirectoriesIn(splitted, userFolderHandle).then(lastFolderHandle => { // create the sub directories
                return lastFolderHandle.getFileHandle(filename, { create: true }).then(fileHandler => {
                    return this.writeFile(fileHandler, content); // write the file
                }).catch(error => {
                    this.processFileError(error + '', path);
                });
            }).catch(error => {
                if (error instanceof ShanoirError) { 
                    throw error;
                } else {
                    throw new ShanoirError({error: {code: ShanoirError.FILE_PATH_TOO_LONG, message: 'Probable reason: directory path too long for Windows, max 260 characters (<your chosen directory>/' + path + ')', details: error + ''}});
                }
            });
        } else { // if no dir to create
            return userFolderHandle.getFileHandle(filename, { create: true }).then(fileHandler => {
                return this.writeFile(fileHandler, content);
            }).catch(error => {
                this.processFileError(error + '', path);
            });
        }
    }

    private processFileError(error: string, path: string) {
        if (error?.includes('NotFoundError')) {
            throw new ShanoirError({error: {code: ShanoirError.FILE_PATH_TOO_LONG, message: 'Probable reason: file path too long for Windows, max 260 characters (<your chosen directory>/' + path + ')', details: error + ''}});
        } else if (error?.includes('Failed to create swap file')) {
            throw new ShanoirError({error: {code: ShanoirError.FILE_TOO_BIG, message: 'Probable reason: file too big', details: error + ''}});
        } else {
            throw new ShanoirError({error: {code: ShanoirError.UNKNOWN_REASON, message: 'Writing the file failed with an unexpected error (' + path + ')', details: error + ''}});
        }
    }

    private getFolderHandle(): Promise<FileSystemDirectoryHandle> {
        const options = {
            mode: 'readwrite'
        };
        // @ts-ignore
        return window.showDirectoryPicker(options);
    }

    private writeFile(fileHandle: FileSystemFileHandle, contents): Promise<void> {
        return fileHandle.createWritable().then(writable => {
            return writable.write({type: 'write', data: contents}).then(() => {
                return writable.close();
            });
        });
    }

    private createDirectoriesIn(dirs: string[], parentFolderHandle: FileSystemDirectoryHandle): Promise<FileSystemDirectoryHandle> {
        if (dirs.length == 0) return;
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

    private initReport(datasetIds: number[], taskId: number, folderName: string, options: DownloadOptions): Report {
        let report: Report = {
            taskId: taskId,
            folderName: folderName,
            requestedDatasetIds: datasetIds,
            startTime: Date.now(),
            list: {},
            nbError: 0,
            nbSuccess: 0,
            format : options.format,
            nbQueues: options.nbQueues,
            unzip: options.unzip
        };
        datasetIds.forEach(id => report.list[id] = { status: 'QUEUED' });
        return report;
    }

    private createTask(nbDatasets: number): Task {
        return this._createTask('Download launched for ' + nbDatasets + ' datasets');
    }

     private createStudyTask(studyId: number): Task {
        return this._createTask('Download launched for study ' + studyId);
    }

    private _createTask(message: string): Task {
        let task: Task = new Task();
        task.id = Date.now();
        task.creationDate = new Date();
        task.lastUpdate = task.creationDate;
        task.message = message;
        task.progress = 0;
        task.status = 2;
        task.eventType = 'downloadDataset.event';
        this.notificationService.pushLocalTask(task);
        return task;
    }

    private openModal(format?: Format, options?: DownloadSetupOptions): Promise<DownloadOptions | 'cancel'> {
        // @ts-ignore
        if (window.showDirectoryPicker) { // test compatibility
            let modalRef: ComponentRef<DownloadSetupComponent> = ServiceLocator.rootViewContainerRef.createComponent(DownloadSetupComponent);
            modalRef.instance.format = format;
            modalRef.instance.options = options;
            return this.waitForEnd(modalRef);
        } else {
            return Promise.reject(this.BROWSER_COMPAT_ERROR_MSG);
        }
    }

    private openAltModal(format?: Format, options?: DownloadSetupOptions, compatibilityMsg: boolean = true): Promise<Format | 'cancel'> {
        let modalRef: ComponentRef<DownloadSetupAltComponent> = ServiceLocator.rootViewContainerRef.createComponent(DownloadSetupAltComponent);
        modalRef.instance.format = format;
        modalRef.instance.compatibilityMessage = compatibilityMsg;
        modalRef.instance.options = options;
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

    retry(task: Task): Promise<void> {
        // @ts-ignore
        if (!window.showDirectoryPicker) {
            throw new Error(this.BROWSER_COMPAT_ERROR_MSG);
        }
        let report: Report = this.getReportFromTask(task);
        let msg: string = 'Please now select the directory of the download you want to resume or retry. ';
        if (report) msg += 'Recorded directory name : ' + report.folderName;

        return this.dialogService.confirm('Select data directory', msg)
            .then(agreed => {
                if (agreed) {
                    return this.getFolderHandle().then(parentFolderHandle => {
                        return parentFolderHandle.getFileHandle(this.REPORT_FILENAME).then(fileHandle => {
                            return fileHandle.getFile().then(file => {
                                return file.text().then(text => {
                                    let reportFromFile: Report = JSON.parse(text);
                                    reportFromFile.nbError = 0;
                                    return this._downloadFromReport(reportFromFile, task, parentFolderHandle);
                                });
                            });
                        });
                    });
                }
            });
    }

    private getReportFromTask(task: Task): Report {
        try {
            return JSON.parse(task?.report);
        } catch (e) {
            this.consoleService.log('error', 'Can\'t parse the status from the recorded message', [e, task?.report]);
            return null;
        }
    }
}

export class DownloadOptions {

    constructor(public format: Format) {}

    nbQueues: number = 4;
    unzip?: boolean = false;
    shortPath?: boolean = false;
}