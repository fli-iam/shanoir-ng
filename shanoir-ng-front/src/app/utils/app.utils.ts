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
export const BACKEND_API_STUDY_FOR_IMPORT_URL: string = BACKEND_API_STUDY_URL + '/list_for_import';
export const BACKEND_API_STUDY_ALL_NAMES_URL: string = BACKEND_API_STUDY_URL + '/names';

// Subjects http api
export const BACKEND_API_SUBJECT_URL: string = BACKEND_API_STUDIES_MS_URL + '/subjects';
export const BACKEND_API_SUBJECT_NAMES_URL: string = BACKEND_API_SUBJECT_URL + '/names';
export const BACKEND_API_SUBJECT_FIND_BY_IDENTIFIER : string = BACKEND_API_SUBJECT_URL + '/findByIdentifier';

// Subject Study http api
export const BACKEND_API_SUBJECT_STUDY_URL: string = BACKEND_API_STUDIES_MS_URL + '/subjectStudy';

// Centers http api
export const BACKEND_API_COIL_URL: string = BACKEND_API_STUDIES_MS_URL + '/coils';

// Datasets http api
const BACKEND_API_DATASET_MS_URL: string = process.env.BACKEND_API_DATASET_MS_URL;
export const BACKEND_API_DATASET_URL: string = BACKEND_API_DATASET_MS_URL + '/datasets';

// Examinations http api
export const BACKEND_API_EXAMINATION_URL: string = BACKEND_API_DATASET_MS_URL + '/examinations';
export const BACKEND_API_EXAMINATION_COUNT_URL: string = BACKEND_API_EXAMINATION_URL + '/count';

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
export const BACKEND_API_UPLOAD_DICOM_START_IMPORT_JOB_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/start_import_job/';
export const BACKEND_API_IMAGE_VIEWER_URL: string = BACKEND_API_IMPORT_MS_URL + '/viewer/ImageViewerServlet';

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

export function allOfEnum<T>(enumClass): Array<T> {
    let list: Array<T> = [];
    for (let key in enumClass) {
        if (key != 'all' && isNaN(Number(key)))
        list.push(enumClass[key]);
    }
    return list;
}

export function capitalizeFirstLetter(str: string) {
    if (!str) return;
    return str.charAt(0).toUpperCase() + str.slice(1);
}

export function capitalsAndUnderscoresToDisplayable(str: string) {
    if (!str) return;
    return capitalizeFirstLetter(str.replace('_', ' ').toLowerCase());
}

export function getEntityInstance(entity: Dataset) { 
    if (entity.type == 'Mr') return new MrDataset();
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // TODO : Implement others !!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    else return new MrDataset(); // fixes errors with our test dataset (which have no real types)
    // TODO : Throw en exception
}