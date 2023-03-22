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
import { process } from '../process';
import { environment } from '../../environments/environment';


// Base urls
let url = window.location;
const BACKEND_API_URL = url.protocol + "//" + url.hostname + "/shanoir-ng";
export const KEYCLOAK_BASE_URL = url.protocol + "//" + url.hostname + "/auth";
export const LOGOUT_REDIRECT_URL = url.protocol + "//" + url.hostname + "/shanoir-ng/welcome";
export const LOGIN_REDIRECT_URL = url.protocol + "//" + url.hostname + "/shanoir-ng/index.html";
export const SILENT_CHECK_SSO_URL = url.protocol + "//" + url.hostname + "/shanoir-ng/assets/silent-check-sso.html";


// Users http api
export const BACKEND_API_USERS_MS_URL: string = BACKEND_API_URL + "/users";
export const BACKEND_API_USER_URL: string = BACKEND_API_USERS_MS_URL + '/users';
export const BACKEND_API_USER_ACCOUNT_REQUEST_URL: string = BACKEND_API_USERS_MS_URL + '/accountrequest';
export const BACKEND_API_USER_CONFIRM_ACCOUNT_REQUEST_URL: string = '/confirmaccountrequest';
export const BACKEND_API_USER_DENY_ACCOUNT_REQUEST_URL: string = '/denyaccountrequest';
export const BACKEND_API_USER_EXTENSION_REQUEST_URL: string = BACKEND_API_USERS_MS_URL + '/extensionrequest';
export const BACKEND_API_USER_ACCESS_REQUEST: string = BACKEND_API_USERS_MS_URL + '/accessrequest';
export const BACKEND_API_USER_ACCESS_REQUEST_BY_USER: string = BACKEND_API_USERS_MS_URL + '/accessrequest/byUser';
export const BACKEND_API_ACCESS_REQUEST_RESOLVE: string = BACKEND_API_USERS_MS_URL + '/accessrequest/resolve/';




export const BACKEND_API_ROLE_ALL_URL: string = BACKEND_API_USERS_MS_URL + '/roles';

const BACKEND_API_STUDIES_MS_URL: string = BACKEND_API_URL + '/studies';
// Centers http api
export const BACKEND_API_CENTER_URL: string = BACKEND_API_STUDIES_MS_URL + '/centers';
export const BACKEND_API_CENTER_NAMES_URL: string = BACKEND_API_CENTER_URL + '/names';
export const BACKEND_API_CENTER_STUDY_URL: string = BACKEND_API_CENTER_URL + '/study';


// Studies http api
export const BACKEND_API_STUDY_URL: string = BACKEND_API_STUDIES_MS_URL + '/studies';
export const BACKEND_API_STUDY_ALL_NAMES_URL: string = BACKEND_API_STUDY_URL + '/names';
export const BACKEND_API_STUDY_DELETE_USER: string = BACKEND_API_STUDY_URL + '/studyUser';
export const BACKEND_API_STUDY_ALL_NAMES_AND_CENTERS_URL: string = BACKEND_API_STUDY_URL + '/namesAndCenters';
export const BACKEND_API_STUDY_RIGHTS: string = BACKEND_API_STUDY_URL + '/rights';
export const BACKEND_API_STUDY_HAS_ONE_STUDY_TO_IMPORT: string = BACKEND_API_STUDY_URL + '/hasOneStudy';
export const BACKEND_API_STUDY_PUBLIC_STUDIES_URL: string = BACKEND_API_STUDY_URL + '/public';
export const BACKEND_API_STUDY_PUBLIC_STUDIES_DATA_URL: string = BACKEND_API_STUDY_URL + '/public/data';
export const BACKEND_API_STUDY_PUBLIC_STUDIES_CONNECTED_URL: string = BACKEND_API_STUDY_URL + '/public/connected';


// Profile API
export const BACKEND_API_PROFILE_URL: string = BACKEND_API_STUDIES_MS_URL + '/profiles';
export const BACKEND_API_PROFILE_ALL_PROFILES_URL: string = BACKEND_API_PROFILE_URL + '/all';

// Challenge API
export const BACKEND_API_STUDY_CHALLENGES_URL: string = BACKEND_API_STUDIES_MS_URL + '/challenges';


// Subjects http api
export const BACKEND_API_SUBJECT_URL: string = BACKEND_API_STUDIES_MS_URL + '/subjects';
export const BACKEND_API_SUBJECT_NAMES_URL: string = BACKEND_API_SUBJECT_URL + '/names';
export const BACKEND_API_SUBJECT_FILTER_URL: string = BACKEND_API_SUBJECT_URL + '/filter';
export const BACKEND_API_SUBJECT_FIND_BY_IDENTIFIER : string = BACKEND_API_SUBJECT_URL + '/findByIdentifier';

// Subject Study http api
export const BACKEND_API_SUBJECT_STUDY_URL: string = BACKEND_API_STUDIES_MS_URL + '/subjectStudy';

// Centers http api
export const BACKEND_API_COIL_URL: string = BACKEND_API_STUDIES_MS_URL + '/coils';

// Datasets http api
const BACKEND_API_DATASET_MS_URL: string = BACKEND_API_URL + '/datasets';
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

// carmin
export const CARMIN_BASE_URL : string = environment.vipUrl + "/vip/rest";
export const BACKEND_API_CARMIN_DATASET_PROCESSING_URL: string = BACKEND_API_DATASET_MS_URL + '/carminDatasetProcessing';

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
    if (window.navigator.msSaveBlob) {
        // IE 10+
        window.navigator.msSaveBlob(blob, filename);
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
        if (!x.hasOwnProperty(p)) {
          continue; // other properties were tested using x.constructor === y.constructor
        }
        if (!y.hasOwnProperty(p)) {
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
        if (y.hasOwnProperty(p) && !x.hasOwnProperty(p)) {
          return false;
        }
      }
      return true;
    }
};

export function objectsEqual(value1, value2) {
        if (value1 == value2) return true;
        else if (value1 && value2 && value1.id && value2.id) return value1.id == value2.id;
        else if (value1 && value2 && value1.equals && value2.equals && typeof value1.equals == 'function' && typeof value2.equals == 'function') return value1.equals(value2);
        else return deepEquals(value1, value2);
    }

export function arraysEqual(array1: any[], array2: any[]) {
    return array1?.length === array2?.length && array1?.every((value, index) => array2 && objectsEqual(value, array2[index]));
}

export function isDarkColor(colorInp: string): boolean {
  var color = (colorInp.charAt(0) === '#') ? colorInp.substring(1, 7) : colorInp;
  var r = parseInt(color.substring(0, 2), 16); // hexToR
  var g = parseInt(color.substring(2, 4), 16); // hexToG
  var b = parseInt(color.substring(4, 6), 16); // hexToB
  return (((r * 0.299) + (g * 0.587) + (b * 0.114)) < 145);
}
