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
import { AngularDeviceInformationService } from 'angular-device-information';
import { Observable, race, Subscription } from 'rxjs';
import { last, map, take } from 'rxjs/operators';
import { Task, TaskState, TaskStatus } from 'src/app/async-tasks/task.model';
import { Dataset } from 'src/app/datasets/shared/dataset.model';
import { DatasetLight, DatasetService, Format } from 'src/app/datasets/shared/dataset.service';
import { getSizeStr, StrictUnion } from 'src/app/utils/app.utils';
import { ServiceLocator } from 'src/app/utils/locator.service';
import { SuperPromise } from 'src/app/utils/super-promise';
import { ConfirmDialogService } from '../components/confirm-dialog/confirm-dialog.service';
import { ConsoleService } from '../console/console.service';
import { ShanoirError } from '../models/error.model';
import { NotificationsService } from '../notifications/notifications.service';
import { SessionService } from '../services/session.service';
import { DownloadSetupAltComponent } from './download-setup-alt/download-setup-alt.component';
import { DownloadSetupComponent } from './download-setup/download-setup.component';
import { Queue } from './queue.model';

declare var JSZip: any;

export type Report = {
    taskId: number,
    folderName?: string,
    requestedDatasetIds?: number[],
    studyId?: number,
    status?: 'QUEUED' | 'ERROR' | 'SUCCESS',
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
    format?: Format;
    converter?: number;
    nbQueues?: number;
    unzip?: boolean;
    folderStructureOptions?: {
        subjectFolders?: boolean,
        examinationFolders?: boolean,
        datasetFolders?: boolean
    }
};

export type DownloadInputIds = StrictUnion<
    {studyId: number}
    | {studyId: number, subjectId: number}
    | {examinationId: number}
    | {acquisitionId: number}
    | {datasetIds: number[]}>;

@Injectable()
export class MassDownloadService {

    private downloadQueue: Queue = new Queue();
    readonly BROWSER_COMPAT_ERROR_MSG: string = 'browser not compatible';
    readonly REPORT_FILENAME: string = 'downloadReport.json';
    winOs: boolean;
    // @ts-ignore
    public advancedDownloadCompat: boolean = !!window.showDirectoryPicker;

    constructor(
        private datasetService: DatasetService,
        private notificationService: NotificationsService,
        private consoleService: ConsoleService,
        private dialogService: ConfirmDialogService,
        private sessionService: SessionService,
        deviceInformationService: AngularDeviceInformationService) {

        this.winOs = deviceInformationService.getDeviceInfo()?.os?.toLocaleLowerCase().includes('windows');
    }

    downloadAllByStudyId(studyId: number, totalSize: number, downloadState?: TaskState) {
        return this.downloadByDatasets({studyId: studyId}, downloadState, totalSize);
    }

    downloadAllByExaminationId(examinationId: number, downloadState?: TaskState): Promise<void> {
        return this.downloadByDatasets({examinationId: examinationId}, downloadState);
    }

    downloadAllByAcquisitionId(acquisitionId: number, downloadState?: TaskState) {
        return this.downloadByDatasets({acquisitionId: acquisitionId}, downloadState);
    }

    downloadAllByStudyIdAndSubjectId(studyId: number, subjectId: number, downloadState?: TaskState): Promise<void> {
        return this.downloadByDatasets({studyId: studyId, subjectId: subjectId}, downloadState);
    }

    downloadByIds(datasetIds: number[], downloadState?: TaskState): Promise<void> {
        return this.downloadByDatasets({datasetIds: datasetIds}, downloadState);
    }

    /**
     * This method is the generic entry to download multiple datasets.
     */
    private downloadByDatasets(inputIds: DownloadInputIds, downloadState?: TaskState, totalSize?: number): Promise<void> {
        return this.openModal(inputIds, totalSize).then(ret => {
            if (ret != 'cancel') {
                return this._downloadDatasets(ret, downloadState);
            } else return Promise.resolve();
        }).catch(error => {
            if (error == this.BROWSER_COMPAT_ERROR_MSG) {
                    return this.openAltModal(inputIds).then(ret => {
                        if (ret != 'cancel' && ret.datasets) {
                            return this._downloadAlt(ret.datasets.map(ds => ds.id), ret.format, ret.converter, downloadState).catch(error => {
                                if (ret.datasets.length > this.datasetService.MAX_DATASETS_IN_ZIP_DL) {
                                    this.dialogService.error('Too many datasets', 'You are trying to download '
                                        + ret.datasets.length + ' datasets while Shanoir sets a limit to ' + this.datasetService.MAX_DATASETS_IN_ZIP_DL
                                        + ' in a single zip. Please confider using a browser compatible with the Shanoir unlimited download functionality. See link below.',
                                        "https://developer.mozilla.org/en-US/docs/Web/API/Window/showDirectoryPicker#browser_compatibility" );
                                }
                            });
                        } else return Promise.resolve();
                    });
            } else throw error;
        });
    }

    makeRootSubdirectory(handle: FileSystemDirectoryHandle, nbDatasets: number): Promise<FileSystemDirectoryHandle> {
        const dirName: string = 'Shanoir-download_' + nbDatasets + 'ds_' + formatDate(new Date(), 'dd-MM-YYYY_HH\'h\'mm\'ss', 'en-US');
        return handle.getDirectoryHandle(dirName, { create: true })
    }

    // This method is used to download in
    private _downloadAlt(datasetIds: number[], format: Format, converter? : number, downloadState?: TaskState): Promise<void> {
        let task: Task = this.createTask(datasetIds.length, TaskStatus.QUEUED);
        downloadState = new TaskState();
        downloadState.status = task.status;
        downloadState.progress = 0;

        return this.downloadQueue.waitForTurn().then(releaseQueue => {
            try {
                task.status = 2;
                task.lastUpdate = new Date();
                const start: number = Date.now();
                let downloadObs: Observable<TaskState> = this.datasetService.downloadDatasets(datasetIds, format, converter);

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

                const endSubscription: Subscription = downloadObs.pipe(last()).subscribe(state => {
                    flowSubscription.unsubscribe();
                    let duration: number = Date.now() - start;
                    task.message = 'download completed in ' + duration + 'ms for ' + datasetIds.length + ' datasets';
                    task.lastUpdate = new Date();
                    task.status = state.status;
                    task.progress = 1;
                    task.report = state.errors;
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

    /**
     * This method is the main entrypoint to download initially datasets
     */
    private _downloadDatasets(setup: DownloadSetup, downloadState?: TaskState, task?: Task, report?: Report, parentHandle?: FileSystemDirectoryHandle): Promise<void> {
        if (!setup?.datasets) throw new Error('datasets can\'t be null here');
        if (setup.datasets.length == 0) return;
        let datasetIds = setup.datasets.map(ds => ds.id); // copy array
        let directoryHandlePromise: Promise<FileSystemDirectoryHandle>;
        if (parentHandle) {
            directoryHandlePromise = Promise.resolve(parentHandle);
        } else {
            directoryHandlePromise = this.getFolderHandle()
                // add a subdirectory
                .then(handle => this.makeRootSubdirectory(handle, datasetIds.length));
        }
        return directoryHandlePromise.then(parentFolderHandle => { // ask the user's parent directory
            if (!task) task = this.createTask(datasetIds.length, TaskStatus.QUEUED);
            if (downloadState) downloadState.status = task.status;
            return this.downloadQueue.waitForTurn().then(releaseQueue => {
                try {
                    task.status = 2;
                    task.lastUpdate = new Date();
                    this.notificationService.pushLocalTask(task);
                    const start: number = Date.now();
                    let ids = [...setup.datasets.map(ds => ds.id)];
                    if (!report) report = this.initReport(datasetIds, task.id, parentFolderHandle.name, setup);
                    let promises: Promise<void>[] = [];
                    for (let queueIndex = 0; queueIndex < setup.nbQueues; queueIndex++) { // build the dl queues
                        promises.push(
                            this.recursiveSave(ids.shift(), setup, parentFolderHandle, ids, report, task)
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

    private handleEnd(task: Task, report: Report, start: number) {
        task.lastUpdate = new Date();
        report.duration = Date.now() - start;
        task.report = JSON.stringify(report, null, 4);
        if (report.nbError > 0) {
            task.status = 3;
            task.progress = 1;
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

    private recursiveSave(id: number, setup: DownloadSetup, userFolderHandle: FileSystemDirectoryHandle, remainingIds: number[], report: Report, task: Task, datasets?: Dataset[]): Promise<void> {
        if (!id) return Promise.resolve();
        return this.saveDataset(id, setup, userFolderHandle, report, task, datasets?.find(ds => ds.id == id)).then(() => {
            if (remainingIds.length > 0) {
                return this.recursiveSave(remainingIds.shift(), setup, userFolderHandle, remainingIds, report, task, datasets);
            } else {
                return Promise.resolve();
            }
        });
    }

    private saveDataset(id: number, setup: DownloadSetup, userFolderHandle: FileSystemDirectoryHandle, report: Report, task: Task, dataset?: Dataset): Promise<void> {
        const metadataPromise: Promise<Dataset> = (dataset?.id == id && dataset.datasetAcquisition?.examination?.subject) ? Promise.resolve(dataset) : this.datasetService.get(id, 'lazy');
        const downloadPromise: Promise<HttpResponse<Blob>> = this.datasetService.downloadToBlob(id, setup.format, setup.converter);
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

            if (setup.unzip) {
                return unzipPromise.then(data => {
                    let finalPromise: Promise<void> = Promise.resolve(); // write them sequentially, not in parallel like with promise.all
                    if (data) {
                        let index: number = 0;
                        Object.entries(data.files)?.map(([name, file]) => {
                            finalPromise = finalPromise.then(() => {
                                index++;
                                task.message = 'unzipping file ' + name + ' from dataset n°' + id;
                                this.notificationService.pushLocalTask(task);
                                let type: string;
                                if (name.endsWith('.json') || name.endsWith('.txt')) type = 'string';
                                else type = 'blob';
                                return (file as {async: (string) => Promise<Blob>}).async(type).then(blob => {
                                    task.message = 'saving file ' + name + ' from dataset n°' + id;
                                    this.notificationService.pushLocalTask(task);
                                    let path: string;
                                    if (setup.shortPath) {
                                        path = this.buildShortExtractedFilePath(dataset, index, name, setup);
                                    } else {
                                        path = this.buildExtractedFilePath(dataset, filename, name, setup);
                                    }
                                    return this.writeMyFile(path, blob, userFolderHandle);
                                });
                            });
                        })
                    }
                    return finalPromise;
                });
            } else {
                let path: string;
                if (setup.shortPath) {
                    path = this.buildShortFoldersPath(dataset, setup) + dataset.id + '.' + filename.split('.').pop();
                } else {
                    path = this.buildFoldersPath(dataset, setup) + filename;
                }
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
                task.message = '(' + report.nbSuccess + '/' + Object.keys(report.list).length + ') dataset n°' + id + ' successfully saved';
                report.nbSuccess++;
            } else if (report.list[id].status == 'ERROR') {
                task.message = 'saving dataset n°' + id + ' failed';
                report.nbError++;
            }
            task.report = JSON.stringify(report, null, 4);
            this.writeMyFile(this.REPORT_FILENAME, task.report, userFolderHandle);
            task.lastUpdate = new Date();
            task.progress = (report.nbSuccess + report.nbError) / Object.keys(report.list).length;
            this.notificationService.pushLocalTask(task);
        });
    }

    private buildExtractedFilePath(dataset: Dataset, zipName: string, fileName: string, setup: DownloadSetup): string {
        return this.buildFoldersPath(dataset, setup)
            + (setup.datasetFolders ? zipName.replace('.zip', '') + '/' : '')
            + fileName;
    }

    private buildShortExtractedFilePath(dataset: Dataset, fileIndex: number, fileName: string, setup: DownloadSetup): string {
            let fileNameSplit: string[] = fileName.split('.');
            let extension: string =  fileNameSplit.pop();
            return this.buildShortFoldersPath(dataset, setup)
                + (setup.datasetFolders ? 'ds' + dataset.id + '/' : '')
                + fileIndex + '.' + extension;
    }

    private buildFoldersPath(dataset: Dataset, setup: DownloadSetup): string {
        let str: string = '/';
        if (setup.subjectFolders) {
            str += 'Subject-' + (
                dataset.hasProcessing
                    ? dataset.subject?.name + '_' + dataset.subject?.id
                    : dataset.datasetAcquisition?.examination?.subject?.name + '_' + dataset.datasetAcquisition?.examination?.subject?.id
            ) + '/';
        }
        if (setup.examinationFolders && !dataset.hasProcessing) { // for processed datasets, skip the exam folder
            str += dataset.datasetAcquisition?.examination?.comment
                + '_' + dataset.datasetAcquisition?.examination?.id
                + '/';
        }
        if (setup.acquisitionFolders && !dataset.hasProcessing) { 
            let acqName: string = dataset.datasetAcquisition.protocol?.updatedMetadata?.name 
                || dataset.datasetAcquisition.protocol?.originMetadata?.name 
                || dataset.datasetAcquisition.type + '_acquisition';
            str += dataset.datasetAcquisition.sortingIndex + '_' + acqName
            + '_' + dataset.datasetAcquisition.id
            + '/';
        }
        return str;
    }

    private buildShortFoldersPath(dataset: Dataset, setup: DownloadSetup): string {
        let str: string = '/';
        if (setup.subjectFolders) {
            str += 'subj'+ (
                dataset.hasProcessing
                    ? dataset.subject?.id
                    : dataset.datasetAcquisition?.examination?.subject?.id
            ) + '/';
        }
        if (setup.examinationFolders && !dataset.hasProcessing) {
            str += 'exam' + dataset.datasetAcquisition?.examination?.id + '/';
        }
        if (setup.acquisitionFolders && !dataset.hasProcessing) {
            str += 'acq' + dataset.datasetAcquisition.sortingIndex + '_' + dataset.datasetAcquisition?.id + '/';
        }
        return str;
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
            return writable.write({type: 'write', data: contents}).finally(() => {
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

    private initReport(datasetIds: number[], taskId: number, folderName: string, setup: DownloadSetup): Report {
        let report: Report = {
            taskId: taskId,
            folderName: folderName,
            startTime: Date.now(),
            list: {},
            nbError: 0,
            nbSuccess: 0,
            format : setup.format,
            nbQueues: setup.nbQueues,
            unzip: setup.unzip,
            converter: setup.converter,
            folderStructureOptions: {
                subjectFolders: setup.subjectFolders,
                examinationFolders: setup.examinationFolders,
                datasetFolders: setup.datasetFolders
            }
        };
        datasetIds.forEach(id => report.list[id] = { status: 'QUEUED' });
        return report;
    }

    private createTask(nbDatasets: number, status: TaskStatus = 2): Task {
        return this._createTask('Download launched for ' + nbDatasets + ' datasets', status);
    }

    private _createTask(message: string, status: TaskStatus = 2): Task {
        let task: Task = new Task();
        task.id = Date.now();
        task.creationDate = new Date();
        task.lastUpdate = task.creationDate;
        task.message = message;
        task.progress = 0;
        task.status = status;
        task.eventType = 'downloadDataset.event';
        task.sessionId = this.sessionService.sessionId;
        this.notificationService.pushLocalTask(task);
        return task;
    }

    private openModal(inputIds: DownloadInputIds, totalSize?: number): Promise<DownloadSetup | 'cancel'> {
        // @ts-ignore
        if (window.showDirectoryPicker) { // test compatibility
            let modalRef: ComponentRef<DownloadSetupComponent> = ServiceLocator.rootViewContainerRef.createComponent(DownloadSetupComponent);
            modalRef.instance.inputIds = inputIds;
            modalRef.instance.totalSize = totalSize;
            return this.waitForEnd(modalRef);
        } else {
            return Promise.reject(this.BROWSER_COMPAT_ERROR_MSG);
        }
    }

    private openAltModal(inputIds: DownloadInputIds): Promise<DownloadSetup | 'cancel'> {
        let modalRef: ComponentRef<DownloadSetupAltComponent> = ServiceLocator.rootViewContainerRef.createComponent(DownloadSetupAltComponent);
        modalRef.instance.inputIds = inputIds;
        return this.waitForEnd(modalRef);
    }

    private waitForEnd(modalRef: ComponentRef<any>): Promise<any | 'cancel'> {
        let resPromise: SuperPromise<any | 'cancel'> = new SuperPromise();
        let result: Observable<any> = race([
            modalRef.instance.go,
            modalRef.instance.close.pipe(map(() => 'cancel'))
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
                                    report.nbError = 0;
                                    let noSuccessIds: number[] = Object.keys(report.list).filter(key => report.list[key].status != 'SUCCESS').map(key => parseInt(key));
                                    task.status = 2;
                                    task.sessionId = this.sessionService.sessionId;
                                    this.notificationService.pushLocalTask(task);

                                    this.datasetService.getByIds(new Set(noSuccessIds)).then(datasets =>{
                                        let setup: DownloadSetup = new DownloadSetup(report.format);
                                        setup.nbQueues = report.nbQueues;
                                        setup.converter = report.converter;
                                        setup.unzip = report.unzip;
                                        setup.datasets = datasets;
                                        if (report.folderStructureOptions) { // keep default values if absent, don't set to false
                                            if (report.folderStructureOptions.subjectFolders != undefined) {
                                                setup.subjectFolders = report.folderStructureOptions.subjectFolders;
                                            }
                                            if (report.folderStructureOptions.examinationFolders != undefined) {
                                                setup.examinationFolders = report.folderStructureOptions.examinationFolders;
                                            }
                                            if (report.folderStructureOptions.datasetFolders != undefined) {
                                                setup.datasetFolders = report.folderStructureOptions.datasetFolders;
                                            }
                                        }
                                        this._downloadDatasets(setup, null, task, report, parentFolderHandle)
                                    });
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

export class DownloadSetup {

    constructor(public format: Format) {}

    nbQueues: number = 4;
    unzip?: boolean = false;
    shortPath?: boolean = false;
    subjectFolders: boolean = true;
    examinationFolders: boolean = true;
    acquisitionFolders: boolean = false;
    datasetFolders: boolean = true;
    converter: number;
    datasets: Dataset[] | DatasetLight[] = [];
}
