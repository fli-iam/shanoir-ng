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

import { MrDataset } from '../datasets/dataset/mr/dataset.mr.model';
import { EegDataset } from '../datasets/dataset/eeg/dataset.eeg.model';
import { Dataset } from '../datasets/shared/dataset.model';

// Users http api
const BACKEND_API_USERS_MS_URL: string = process.env.BACKEND_API_USERS_MS_URL;
export const BACKEND_API_USER_URL: string = BACKEND_API_USERS_MS_URL + '/users';
export const BACKEND_API_USER_ACCOUNT_REQUEST_URL: string = BACKEND_API_USERS_MS_URL + '/accountrequest';
export const BACKEND_API_USER_CONFIRM_ACCOUNT_REQUEST_URL: string = '/confirmaccountrequest';
export const BACKEND_API_USER_DENY_ACCOUNT_REQUEST_URL: string = '/denyaccountrequest';
export const BACKEND_API_USER_EXTENSION_REQUEST_URL: string = BACKEND_API_USER_URL + '/extension';
export const BACKEND_API_ROLE_ALL_URL: string = BACKEND_API_USERS_MS_URL + '/roles';

const BACKEND_API_STUDIES_MS_URL: string = process.env.BACKEND_API_STUDIES_MS_URL;
// Centers http api
export const BACKEND_API_CENTER_URL: string = BACKEND_API_STUDIES_MS_URL + '/centers';
export const BACKEND_API_CENTER_NAMES_URL: string = BACKEND_API_CENTER_URL + '/names';

// Studies http api
export const BACKEND_API_STUDY_URL: string = BACKEND_API_STUDIES_MS_URL + '/studies';
export const BACKEND_API_STUDY_ALL_NAMES_URL: string = BACKEND_API_STUDY_URL + '/names';
export const BACKEND_API_STUDY_ALL_NAMES_AND_CENTERS_URL: string = BACKEND_API_STUDY_URL + '/namesAndCenters';
export const BACKEND_API_STUDY_RIGHTS: string = BACKEND_API_STUDY_URL + '/rights';
export const BACKEND_API_STUDY_HAS_ONE_STUDY_TO_IMPORT: string = BACKEND_API_STUDY_URL + '/hasOneStudy';
export const BACKEND_API_STUDY_BIDS_EXPORT_URL: string = BACKEND_API_STUDY_URL + '/exportBIDS';
export const BACKEND_API_STUDY_BIDS_STRUCTURE_URL: string = BACKEND_API_STUDY_URL + '/bidsStructure';

// Subjects http api
export const BACKEND_API_SUBJECT_URL: string = BACKEND_API_STUDIES_MS_URL + '/subjects';
export const BACKEND_API_SUBJECT_NAMES_URL: string = BACKEND_API_SUBJECT_URL + '/names';
export const BACKEND_API_SUBJECT_FILTER_URL: string = BACKEND_API_STUDIES_MS_URL + '/subjects/filter';
export const BACKEND_API_SUBJECT_FIND_BY_IDENTIFIER : string = BACKEND_API_SUBJECT_URL + '/findByIdentifier';

// Subject Study http api
export const BACKEND_API_SUBJECT_STUDY_URL: string = BACKEND_API_STUDIES_MS_URL + '/subjectStudy';

// Centers http api
export const BACKEND_API_COIL_URL: string = BACKEND_API_STUDIES_MS_URL + '/coils';

// Datasets http api
const BACKEND_API_DATASET_MS_URL: string = process.env.BACKEND_API_DATASET_MS_URL;
export const BACKEND_API_DATASET_URL: string = BACKEND_API_DATASET_MS_URL + '/datasets';

// Dataset acquisition http api
export const BACKEND_API_DATASET_ACQUISITION_URL: string = BACKEND_API_DATASET_MS_URL + '/datasetacquisition';

// BIDS http api
export const BACKEND_API_BIDS_URL: string = BACKEND_API_DATASET_MS_URL + '/bids';
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
const BACKEND_API_IMPORT_MS_URL: string = process.env.BACKEND_API_IMPORT_MS_URL;
export const BACKEND_API_UPLOAD_DICOM_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/upload_dicom/';
export const BACKEND_API_IMPORT_DICOM_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/import_dicom/';
export const BACKEND_API_UPLOAD_DICOM_START_IMPORT_JOB_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/start_import_job/';
export const BACKEND_API_UPLOAD_EEG_START_IMPORT_JOB_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/start_import_eeg_job/';
export const BACKEND_API_IMAGE_VIEWER_URL: string = BACKEND_API_IMPORT_MS_URL + '/viewer/ImageViewerServlet';
export const BACKEND_API_QUERY_PACS: string = BACKEND_API_IMPORT_MS_URL + '/importer/query_pacs/';
export const BACKEND_API_STUDY_CARD_URL: string = BACKEND_API_DATASET_MS_URL + '/studycards';
export const BACKEND_API_UPLOAD_EEG_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/upload_eeg/';
export const BACKEND_API_UPLOAD_BIDS_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/importAsBids/';
export const BACKEND_API_IMPORT_EEG_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/import_eeg/';

// Nifti Converter http api
export const BACKEND_API_NIFTI_CONVERTER_URL: string = BACKEND_API_IMPORT_MS_URL + '/niftiConverters';

export function hasUniqueError(error: any, fieldName: string): boolean {
    let hasUniqueError = false;
    if (error.error && error.error.details) {
        let fieldErrors = error.error.details.fieldErrors || '';
        if (fieldErrors[fieldName]) {
            for (let fieldError of fieldErrors[fieldName]) {
                if (fieldError.code == 'unique') {
                    hasUniqueError = true;
                }
            }
        }
    }
    return hasUniqueError;
}

export function browserDownloadFile(blob: Blob, filename: string){
    if (navigator.msSaveBlob) { 
        // IE 10+
        navigator.msSaveBlob(blob, filename);
    } else {
        var link = document.createElement('a');
        // Browsers that support HTML5 download attribute
        if (link.download !== undefined) 
        {
            var url = URL.createObjectURL(blob);
            link.setAttribute('href', url);
            link.setAttribute('download', filename);
            link.style.visibility = 'hidden';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        }
    }
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
export function findLastIndex<T>(array: Array<T>, predicate: (value: T, index: number, obj: T[]) => boolean): number {
    let l = array.length;
    while (l--) {
        if (predicate(array[l], l, array))
            return l;
    }
    return -1;
}


@Pipe({name: 'times'})
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

@Pipe({name: 'getValues'})
export class GetValuesPipe implements PipeTransform {
    transform(map: Map<any, any>): any[] {
        let ret = [];
        map.forEach((val, key) => {
            ret.push({
                key: key,
                val: val
            });
        });
        return ret;
    }
}

export function allOfEnum<T>(enumClass): Array<T> {
    let list: Array<T> = [];
    for (let key in enumClass) {
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

export function isFunction(obj) {
    return !!(obj && obj.constructor && obj.call && obj.apply);
}

export function getDatasetInstance(type: string) { 
    if (type == 'Mr') return new MrDataset();
    if (type == 'Eeg') return new EegDataset();
    else return new MrDataset(); 
}

export function getEntityInstance(entity: Dataset) { 
    return getDatasetInstance(entity.type);

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // TODO : Implement others !!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    // fixes errors with our test dataset (which have no real types)
    // TODO : Throw en exception
}