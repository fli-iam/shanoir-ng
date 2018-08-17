import { Pageable } from '../shared/components/table/pageable.model';
import { HttpResponse } from '@angular/common/http';

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
export const BACKEND_API_STUDY_WITH_CARDS_BY_USER_EQUIPMENT_URL: string = BACKEND_API_STUDY_URL + '/listwithcards';
export const BACKEND_API_STUDY_ALL_NAMES_URL: string = BACKEND_API_STUDY_URL + '/names';
export const BACKEND_API_STUDY_FIND_MEMBERS_URL: string = '/members';

// Subjects http api
export const BACKEND_API_SUBJECT_URL: string = BACKEND_API_STUDIES_MS_URL + '/subjects';
export const BACKEND_API_SUBJECT_NAMES_URL: string = BACKEND_API_SUBJECT_URL + '/names';
export const BACKEND_API_SUBJECT_FILTER_URL: string = BACKEND_API_STUDIES_MS_URL + '/subjects/filter';

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
export const BACKEND_API_IMPORT_DICOM_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/import_dicom/';
export const BACKEND_API_UPLOAD_DICOM_START_IMPORT_JOB_URL: string = BACKEND_API_IMPORT_MS_URL + '/importer/start_import_job/';
export const BACKEND_API_IMAGE_VIEWER_URL: string = BACKEND_API_IMPORT_MS_URL + '/viewer/ImageViewerServlet';

export function getPageableQuery(pageable: Pageable): string {
    let requestUrl: string = '';
    if (pageable != null) {
        requestUrl += '?page=' + pageable.page + '&size=' + pageable.size
        if (pageable.sortProperty != null) {
            requestUrl += '&sort=' + pageable.sortProperty + ',' + (pageable.asc ? 'asc' : 'desc');
        }
    }
    return requestUrl;
}

export function hasUniqueError(error: any, fieldName: string): boolean {
    let hasUniqueError = false;
    if (error.error.details) {
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