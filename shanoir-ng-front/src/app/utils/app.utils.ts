// Users http api
const BACKEND_API_USERS_MS_URL:string = process.env.BACKEND_API_USERS_MS_URL;
export const BACKEND_API_USER_URL:string = BACKEND_API_USERS_MS_URL + '/users';
export const BACKEND_API_USER_ACCOUNT_REQUEST_URL:string = BACKEND_API_USERS_MS_URL + '/accountrequest';
export const BACKEND_API_USER_CONFIRM_ACCOUNT_REQUEST_URL:string = '/confirmaccountrequest';
export const BACKEND_API_USER_DENY_ACCOUNT_REQUEST_URL:string = '/denyaccountrequest';
export const BACKEND_API_USER_EXTENSION_REQUEST_URL:string = BACKEND_API_USER_URL + '/extension';
export const BACKEND_API_ROLE_ALL_URL:string = BACKEND_API_USERS_MS_URL + '/roles';

const BACKEND_API_STUDIES_MS_URL: string = process.env.BACKEND_API_STUDIES_MS_URL;
// Centers http api
export const BACKEND_API_CENTER_URL:string = BACKEND_API_STUDIES_MS_URL + '/center';
export const BACKEND_API_CENTER_ALL_URL: string = BACKEND_API_CENTER_URL + '/all';
export const BACKEND_API_CENTER_ALL_NAMES_URL: string = BACKEND_API_CENTER_URL + '/allnames';

// Studies http api
export const BACKEND_API_STUDY_URL:string = BACKEND_API_STUDIES_MS_URL + '/study';
export const BACKEND_API_STUDY_ALL_BY_USER_URL: string = BACKEND_API_STUDY_URL + '/list';
export const BACKEND_API_STUDY_WITH_CARDS_BY_USER_URL: string = BACKEND_API_STUDY_URL + '/listwithcards';

// Subjects http api
export const BACKEND_API_SUBJECT_URL:string = BACKEND_API_STUDIES_MS_URL + '/subject';

// Acquisition equipment http api
export const BACKEND_API_ACQ_EQUIP_URL:string = BACKEND_API_STUDIES_MS_URL + '/acquisitionequipment';
export const BACKEND_API_ACQ_EQUIP_ALL_URL: string = BACKEND_API_ACQ_EQUIP_URL + '/all';

// Manufacturer model http api
export const BACKEND_API_MANUF_MODEL_URL:string = BACKEND_API_STUDIES_MS_URL + '/manufacturermodel';
export const BACKEND_API_MANUF_MODEL_ALL_URL: string = BACKEND_API_MANUF_MODEL_URL + '/all';

// Manufacturer http api
export const BACKEND_API_MANUF_URL:string = BACKEND_API_STUDIES_MS_URL + '/manufacturer';
export const BACKEND_API_MANUF_ALL_URL: string = BACKEND_API_MANUF_URL + '/all';

// Import http api
const BACKEND_API_IMPORT_MS_URL: string = process.env.BACKEND_API_IMPORT_MS_URL; 
export const BACKEND_API_UPLOAD_DICOM_URL:string = BACKEND_API_IMPORT_MS_URL + '/importer/upload_dicom/';
export const BACKEND_API_UPLOAD_DICOM_SELECT_SERIES_URL:string = BACKEND_API_IMPORT_MS_URL + '/importer/select_series/';
export const BACKEND_API_IMAGE_VIEWER_URL:string = BACKEND_API_IMPORT_MS_URL + '/viewer/ImageViewerServlet/';