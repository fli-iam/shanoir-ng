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

import { Pipe, PipeTransform } from '@angular/core';
import { HttpClient, HttpEvent, HttpEventType, HttpParams, HttpProgressEvent, HttpResponse } from '@angular/common/http';
import { Observable, firstValueFrom } from 'rxjs';
import { last, map, mergeMap, shareReplay } from 'rxjs/operators';

import { TaskState, TaskStatus } from '../async-tasks/task.model';

import { ServiceLocator } from './locator.service';


// Base urls
const url = window.location;
export const BACKEND_API_URL = url.protocol + "//" + url.hostname + "/shanoir-ng";
export const KEYCLOAK_BASE_URL = url.protocol + "//" + url.hostname + "/auth";
export const LOGOUT_REDIRECT_URL = url.protocol + "//" + url.hostname + "/shanoir-ng/welcome";
export const LOGIN_REDIRECT_URL = url.protocol + "//" + url.hostname + "/shanoir-ng/index.html";
export const SILENT_CHECK_SSO_URL = url.protocol + "//" + url.hostname + "/shanoir-ng/assets/silent-check-sso.html";


// Users http api
export const BACKEND_API_USERS_MS_URL: string = BACKEND_API_URL + "/users";
export const BACKEND_API_USER_URL: string = BACKEND_API_USERS_MS_URL + '/users';
export const BACKEND_API_USER_EVENTS: string = BACKEND_API_USERS_MS_URL + '/events';
export const BACKEND_API_USER_ACCOUNT_REQUEST_URL: string = BACKEND_API_USERS_MS_URL + '/accountrequest';
export const BACKEND_API_USER_CONFIRM_ACCOUNT_REQUEST_URL: string = '/confirmaccountrequest';
export const BACKEND_API_USER_DENY_ACCOUNT_REQUEST_URL: string = '/denyaccountrequest';
export const BACKEND_API_USER_EXTENSION_REQUEST_URL: string = BACKEND_API_USERS_MS_URL + '/extensionrequest';
export const BACKEND_API_USER_ACCESS_REQUEST: string = BACKEND_API_USERS_MS_URL + '/accessrequest';
export const BACKEND_API_USER_ACCESS_REQUEST_BY_USER: string = BACKEND_API_USERS_MS_URL + '/accessrequest/byUser';
export const BACKEND_API_USER_ACCESS_REQUEST_BY_ADMIN: string = BACKEND_API_USERS_MS_URL + '/accessrequest/byAdmin';
export const BACKEND_API_ACCESS_REQUEST_RESOLVE: string = BACKEND_API_USERS_MS_URL + '/accessrequest/resolve/';




export const BACKEND_API_ROLE_ALL_URL: string = BACKEND_API_USERS_MS_URL + '/roles';

export const BACKEND_API_STUDIES_MS_URL: string = BACKEND_API_URL + '/studies';
// Centers http api
export const BACKEND_API_CENTER_URL: string = BACKEND_API_STUDIES_MS_URL + '/centers';
export const BACKEND_API_CENTER_NAMES_URL: string = BACKEND_API_CENTER_URL + '/names';
export const BACKEND_API_CENTER_STUDY_URL: string = BACKEND_API_CENTER_URL + '/study';


// Studies http api
export const BACKEND_API_STUDY_URL: string = BACKEND_API_STUDIES_MS_URL + '/studies';
export const BACKEND_API_STUDY_STUDIES_LIGHT_URL: string = BACKEND_API_STUDIES_MS_URL + '/studies/light';
export const BACKEND_API_STUDY_ALL_NAMES_URL: string = BACKEND_API_STUDY_URL + '/names';
export const BACKEND_API_STUDY_DELETE_USER: string = BACKEND_API_STUDY_URL + '/studyUser';
export const BACKEND_API_STUDY_RIGHTS: string = BACKEND_API_STUDY_URL + '/rights';
export const BACKEND_API_STUDY_HAS_ONE_STUDY_TO_IMPORT: string = BACKEND_API_STUDY_URL + '/hasOneStudy';
export const BACKEND_API_STUDY_PUBLIC_STUDIES_URL: string = BACKEND_API_STUDY_URL + '/public';
export const BACKEND_API_STUDY_PUBLIC_STUDIES_DATA_URL: string = BACKEND_API_STUDY_URL + '/public/data';
export const BACKEND_API_STUDY_PUBLIC_STUDIES_CONNECTED_URL: string = BACKEND_API_STUDY_URL + '/public/connected';
export const BACKEND_API_STUDY_COPY_DATASETS: string = BACKEND_API_STUDY_URL + '/copyDatasets';


// Profile API
export const BACKEND_API_PROFILE_URL: string = BACKEND_API_STUDIES_MS_URL + '/profiles';
export const BACKEND_API_PROFILE_ALL_PROFILES_URL: string = BACKEND_API_PROFILE_URL + '/all';

// Challenge API
export const BACKEND_API_STUDY_CHALLENGES_URL: string = BACKEND_API_STUDIES_MS_URL + '/challenges';


// Subjects http api
export const BACKEND_API_SUBJECT_URL: string = BACKEND_API_STUDIES_MS_URL + '/subjects';
export const BACKEND_API_SUBJECT_NAMES_URL: string = BACKEND_API_SUBJECT_URL + '/names';
export const BACKEND_API_SUBJECT_FILTER_URL: string = BACKEND_API_SUBJECT_URL + '/filter';
export const BACKEND_API_SUBJECT_FIND_BY_IDENTIFIER: string = BACKEND_API_SUBJECT_URL + '/findByIdentifier';

// Subject Study http api
export const BACKEND_API_SUBJECT_STUDY_URL: string = BACKEND_API_STUDIES_MS_URL + '/subjectStudy';

// Centers http api
export const BACKEND_API_COIL_URL: string = BACKEND_API_STUDIES_MS_URL + '/coils';

// Datasets http api
export const BACKEND_API_DATASET_MS_URL: string = BACKEND_API_URL + '/datasets';
export const BACKEND_API_DATASET_URL: string = BACKEND_API_DATASET_MS_URL + '/datasets';
export const BACKEND_API_PROCESSED_DATASET_URL: string = BACKEND_API_DATASET_URL + '/processedDataset';

// Dataset processing api
export const BACKEND_API_DATASET_PROCESSING_URL: string = BACKEND_API_DATASET_MS_URL + '/datasetProcessing';

// Dataset acquisition http api
export const BACKEND_API_DATASET_ACQUISITION_URL: string = BACKEND_API_DATASET_MS_URL + '/datasetacquisition';

// Solr http api
export const BACKEND_API_SOLR_URL: string = BACKEND_API_DATASET_MS_URL + '/solr';
export const BACKEND_API_SOLR_INDEX_URL: string = BACKEND_API_SOLR_URL + '/index';
export const BACKEND_API_SOLR_FULLTEXT_SEARCH_URL: string = BACKEND_API_SOLR_URL + '/search';
// BIDS http api
export const BACKEND_API_BIDS_URL: string = BACKEND_API_DATASET_MS_URL + '/bids';
export const BACKEND_API_BIDS_EXPORT_URL: string = BACKEND_API_BIDS_URL + '/exportBIDS';
export const BACKEND_API_BIDS_STRUCTURE_URL: string = BACKEND_API_BIDS_URL + '/bidsStructure';
export const BACKEND_API_BIDS_REFRESH_URL: string = BACKEND_API_BIDS_URL + '/refreshBids';

export const BACKEND_API_TASKS_URL: string = BACKEND_API_USERS_MS_URL + '/tasks';
export const BACKEND_API_UPDATE_TASKS_URL: string = BACKEND_API_TASKS_URL + '/updateTasks';

// Examinations http api
export const BACKEND_API_EXAMINATION_URL: string = BACKEND_API_DATASET_MS_URL + '/examinations';
export const BACKEND_API_EXAMINATION_PRECLINICAL_URL: string = BACKEND_API_EXAMINATION_URL + '/preclinical';

// Acquisition equipment http api
export const BACKEND_API_ACQ_EQUIP_URL: string = BACKEND_API_STUDIES_MS_URL + '/acquisitionequipments';

// Manufacturer model http api
export const BACKEND_API_MANUF_MODEL_URL: string = BACKEND_API_STUDIES_MS_URL + '/manufacturermodels';
export const BACKEND_API_MANUF_MODEL_NAMES_URL: string = BACKEND_API_MANUF_MODEL_URL + '/names';
export const BACKEND_API_CENTER_MANUF_MODEL_NAMES_URL: string = BACKEND_API_MANUF_MODEL_URL + '/centerManuModelsNames';

// Manufacturer http api
export const BACKEND_API_MANUF_URL: string = BACKEND_API_STUDIES_MS_URL + '/manufacturers';

// Import http api
const BACKEND_API_IMPORT_MS_URL: string = BACKEND_API_URL + '/import';
export const BACKEND_API_UPLOAD_DICOM_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/upload_dicom/';
export const BACKEND_API_UPLOAD_MUTIPLE_DICOM_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/upload_multiple_dicom/';
export const BACKEND_API_UPLOAD_PROCESSED_DATASET_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/upload_processed_dataset/';
export const BACKEND_API_IMPORT_DICOM_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/import_dicom/';
export const BACKEND_API_UPLOAD_DICOM_START_IMPORT_JOB_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/start_import_job/';
export const BACKEND_API_UPLOAD_EEG_START_IMPORT_JOB_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/start_import_eeg_job/';
export const BACKEND_API_GET_DICOM_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/get_dicom/';
export const BACKEND_API_QUERY_PACS: string = BACKEND_API_IMPORT_MS_URL + '/importer/query_pacs/';
export const BACKEND_API_STUDY_CARD_URL: string = BACKEND_API_DATASET_MS_URL + '/studycards';
export const BACKEND_API_QUALITY_CARD_URL: string = BACKEND_API_DATASET_MS_URL + '/qualitycards';
export const BACKEND_API_UPLOAD_EEG_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/upload_eeg/';
export const BACKEND_API_ANALYSE_EEG_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/start_analysis_eeg_job/';
export const BACKEND_API_UPLOAD_BIDS_URL: string = BACKEND_API_IMPORT_MS_URL + '/bidsImporter/';
export const BACKEND_API_IMPORT_EEG_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/import_eeg/';

// Nifti Converter http api
export const BACKEND_API_NIFTI_CONVERTER_URL: string = BACKEND_API_IMPORT_MS_URL + '/niftiConverters';

// Preclinical http api
export const BACKEND_API_PRECLINICAL_MS_URL: string = BACKEND_API_URL + '/preclinical';

// vip
export const BACKEND_API_VIP_URL: string = BACKEND_API_DATASET_MS_URL + '/vip';
export const BACKEND_API_VIP_EXEC_URL : string = BACKEND_API_VIP_URL + "/execution";
export const BACKEND_API_VIP_PIPE_URL : string = BACKEND_API_VIP_URL + "/pipeline";

export const BACKEND_API_VIP_EXEC_MONITORING_URL: string = BACKEND_API_DATASET_MS_URL + '/execution-monitoring';

declare let JSZip: any;

export function hasUniqueError(error: any, fieldName: string): boolean {
    let hasUniqueError = false;
    if (error.error && error.error.details) {
        const fieldErrors = error.error.details.fieldErrors || '';
        if (fieldErrors[fieldName]) {
            for (const fieldError of fieldErrors[fieldName]) {
                if (fieldError.code == 'unique') {
                    hasUniqueError = true;
                }
            }
        }
    }
    return hasUniqueError;
}

export function browserDownloadFile(blob: Blob, filename: string) {
    if (window.navigator.msSaveBlob) {
        // IE 10+
        window.navigator.msSaveBlob(blob, filename);
    } else {
        const link = document.createElement('a');
        // Browsers that support HTML5 download attribute
        if (link.download !== undefined) {
            const url = URL.createObjectURL(blob);
            link.setAttribute('href', url);
            link.setAttribute('download', filename);
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        }
    }
}

export function browserDownloadFileFromResponse(response: HttpResponse<any>) {
    if (response.body) {
        browserDownloadFile(response.body, getFilename(response));
    } else {
        throw new Error('can\'t download, server response is empty');
    }
}

export function downloadBlob(url: string, params?: HttpParams): Promise<Blob> {
    const http: HttpClient = ServiceLocator.injector.get(HttpClient);
    return firstValueFrom(http.get(
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

export function downloadWithStatusGET(url: string, params?: HttpParams, state?: TaskState): Observable<TaskState> {
    const http: HttpClient = ServiceLocator.injector.get(HttpClient);
    const obs: Observable<HttpEvent<Blob>> = http.get(
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
        return extractState(event).then(s => {
            if (state) {
                state.errors = s.errors;
                state.progress = s.progress;
                state.status = s.status;
            }
            return s;
        });
    }));
}

export function downloadWithStatusPOST(url: string, formData: FormData, state?: TaskState): Observable<TaskState> {
    const http: HttpClient = ServiceLocator.injector.get(HttpClient);
    const obs: Observable<HttpEvent<Blob>> = http.post(
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
        return extractState(event).then(s => {
            if (state) {
                state.errors = s.errors;
                state.progress = s.progress;
                state.status = s.status;
            }
            return s;
        });
    }));
}

export function extractState(event: HttpEvent<any>): Promise<TaskState> {
    let task: TaskState;
    switch (event.type) {
        case HttpEventType.Sent:
        case HttpEventType.ResponseHeader: {
            task = new TaskState(TaskStatus.QUEUED, 0);
            return Promise.resolve(task);
        }
        case HttpEventType.DownloadProgress: {
            const total: number = (event as HttpProgressEvent).total;
            task = new TaskState(TaskStatus.IN_PROGRESS, (event as HttpProgressEvent).loaded);
            if (total) task.progress /= total;
            return Promise.resolve(task);
        }
        case HttpEventType.Response: {
            task = new TaskState(TaskStatus.DONE);
            const blob: Blob = (event as HttpResponse<Blob>).body;
            if (blob && event.headers.get('Content-Type') == 'application/zip') {
                //report.list[id].zipSize = getSizeStr(blob?.size);
                // Check ERRORS file in zip
                const zip: any = new JSZip();
                return zip.loadAsync(blob).then(dataFiles => {
                    if (dataFiles.files['ERRORS.json']) {
                        return dataFiles.files['ERRORS.json'].async('string').then(content => {
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
        default: return Promise.resolve(task);
    }
}

export function getFilename(response: HttpResponse<any>): string {
    const prefix = 'attachment;filename=';
    const contentDispHeader: string = response.headers.get('Content-Disposition');
    return contentDispHeader?.slice(contentDispHeader.indexOf(prefix) + prefix.length, contentDispHeader.length);
}

export function pad(n, width, z?): string {
    z = z || '0';
    n = n + '';
    return n.length >= width ? n : new Array(width - n.length + 1).join(z) + n;
}


/**
* Returns the index of the last element in the array where predicate is true, and -1
* otherwise.
* @param array The source array to search in
* @param predicate find calls predicate once for each element of the array, in descending
* order, until it finds one where predicate returns true. If such an element is found,
* findLastIndex immediately returns that element index. Otherwise, findLastIndex returns -1.
*/
export function findLastIndex<T>(array: T[], predicate: (value: T, index: number, obj: T[]) => boolean): number {
    let l = array.length;
    while (l--) {
        if (predicate(array[l], l, array))
            return l;
    }
    return -1;
}


@Pipe({
    name: 'times',
    standalone: false
})
export class TimesPipe implements PipeTransform {
    transform(value: number): any {
        const iterable = {};
        iterable[Symbol.iterator] = function* () {
            let n = 0;
            while (n < value) {
                yield ++n;
            }
        };
        return iterable;
    }
}

@Pipe({
    name: 'getValues',
    standalone: false
})
export class GetValuesPipe implements PipeTransform {
    transform(map: Map<any, any>): any[] {
        const ret = [];
        map.forEach((val, key) => {
            ret.push({
                key: key,
                val: val
            });
        });
        return ret;
    }
}

export function allOfEnum<T>(enumClass): T[] {
    const list: T[] = [];
    for (const key in enumClass) {
        if (!(enumClass[key] instanceof Function)) list.push(enumClass[key]);
    }
    return list;
}

export function capitalizeFirstLetter(str: string) {
    if (!str) return;
    return str.charAt(0).toUpperCase() + str.slice(1);
}

export function capitalsAndUnderscoresToDisplayable(str: string) {
    if (!str) return;
    return capitalizeFirstLetter(str.replace(new RegExp('_', 'g'), ' ').toLowerCase());
}

@Pipe({
    name: 'camel',
    standalone: false
})
export class CamelPipe implements PipeTransform {
    transform(value: string): any {
        return capitalsAndUnderscoresToDisplayable(value);
    }
}

export function camelToSpaces(str: string): string {
    return str
        // insert a space before all caps
        .replace(/([A-Z])/g, ' $1')
        // uppercase the first character
        .replace(/^./, function (str) { return str.toUpperCase(); });
}

export function isFunction(obj) {
    return !!(obj && obj.constructor && obj.call && obj.apply);
}

function deepEquals(x, y) {
    if (x === y) {
        return true; // if both x and y are null or undefined and exactly the same
    } else if (!(x instanceof Object) || !(y instanceof Object)) {
        return false; // if they are not strictly equal, they both need to be Objects
    } else if (x.constructor !== y.constructor) {
        // they must have the exact same prototype chain, the closest we can do is
        // test their constructor.
        return false;
    } else {
        for (const p in x) {
            if (!Object.prototype.hasOwnProperty.call(x, p)) {
                continue; // other properties were tested using x.constructor === y.constructor
            }
            if (!Object.prototype.hasOwnProperty.call(y, p)) {
                return false; // allows to compare x[ p ] and y[ p ] when set to undefined
            }
            if (x[p] === y[p]) {
                continue; // if they have the same strict value or identity then they are equal
            }
            if (typeof (x[p]) !== 'object') {
                return false; // Numbers, Strings, Functions, Booleans must be strictly equal
            }
            if (!deepEquals(x[p], y[p])) {
                return false;
            }
        }
        for (const p in y) {
            if (Object.prototype.hasOwnProperty.call(y, p) && !Object.prototype.hasOwnProperty.call(x, p)) {
                return false;
            }
        }
        return true;
    }
};

export function objectsEqual(value1, value2) {
    if (value1 === value2) return true;
    else if (value1 && value2 && value1.id && value2.id) return value1.id === value2.id;
    else if (value1 && value2 && value1.equals && value2.equals && typeof value1.equals === 'function' && typeof value2.equals === 'function') return value1.equals(value2);
    else return deepEquals(value1, value2);
}

export function arraysEqual(array1: any[], array2: any[]) {
    return array1?.length === array2?.length && array1?.every((value, index) => array2 && objectsEqual(value, array2[index]));
}

export function isDarkColor(colorInp: string): boolean {
    colorInp = colorInp?.replace('#', '');
    const r = parseInt(colorInp.substring(0, 2), 16); // hexToR
    const g = parseInt(colorInp.substring(2, 4), 16); // hexToG
    const b = parseInt(colorInp.substring(4, 6), 16); // hexToB
    return (((r * 0.299) + (g * 0.587) + (b * 0.114)) < 145);
}

export function getSizeStr(size: number): string {
    if (size == null || size == undefined){
        return "";
    }
    const base: number = 1024;
    const units: string[] = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    if (size == 0) {
        return "0 " + units[0];
    }
    const exponent: number = Math.floor(Math.log(size) / Math.log(base));
    const value: number = Math.round(parseFloat((size / Math.pow(base, exponent)).toFixed(2)));
    const unit: string = units[exponent];
    return value + " " + unit;
}

type UnionKeys<T> = T extends T ? keyof T : never;
type StrictUnionHelper<T, TAll> = T extends any ? T & Partial<Record<Exclude<UnionKeys<TAll>, keyof T>, never>> : never;
export type StrictUnion<T> = StrictUnionHelper<T, T>
