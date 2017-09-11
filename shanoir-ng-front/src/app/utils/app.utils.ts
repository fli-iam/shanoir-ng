// Users http api
const BACKEND_API_USERS_MS_URL:string = process.env.BACKEND_API_USERS_MS_URL;
export const BACKEND_API_USER_ALL_URL:string = BACKEND_API_USERS_MS_URL + '/user/all';
export const BACKEND_API_USER_URL:string = BACKEND_API_USERS_MS_URL + '/user';
export const BACKEND_API_USER_ACCOUNT_REQUEST_URL:string = BACKEND_API_USERS_MS_URL + '/accountrequest';
export const BACKEND_API_USER_CONFIRM_ACCOUNT_REQUEST_URL:string = '/confirmaccountrequest';
export const BACKEND_API_USER_DENY_ACCOUNT_REQUEST_URL:string = '/denyaccountrequest';
export const BACKEND_API_USER_EXTENSION_REQUEST_URL:string = BACKEND_API_USER_URL + '/extension';
export const BACKEND_API_ROLE_ALL_URL:string = BACKEND_API_USERS_MS_URL + '/role/all';

// Centers http api
const BACKEND_API_STUDIES_MS_URL: string = process.env.BACKEND_API_STUDIES_MS_URL;
export const BACKEND_API_CENTER_URL:string = BACKEND_API_STUDIES_MS_URL + '/center';
export const BACKEND_API_CENTER_ALL_URL: string = BACKEND_API_CENTER_URL + '/all';
export const BACKEND_API_CENTER_ALL_NAMES_URL: string = BACKEND_API_CENTER_URL + '/allnames';

// Acquisition equipment http api
export const BACKEND_API_ACQ_EQUIP_URL:string = BACKEND_API_STUDIES_MS_URL + '/acquisitionequipment';
export const BACKEND_API_ACQ_EQUIP_ALL_URL: string = BACKEND_API_ACQ_EQUIP_URL + '/all';

// Manufacturer model http api
export const BACKEND_API_MANUF_MODEL_URL:string = BACKEND_API_STUDIES_MS_URL + '/manufacturermodel';
export const BACKEND_API_MANUF_MODEL_ALL_URL: string = BACKEND_API_MANUF_MODEL_URL + '/all';

// Manufacturer http api
export const BACKEND_API_MANUF_URL:string = BACKEND_API_STUDIES_MS_URL + '/manufacturer';
export const BACKEND_API_MANUF_ALL_URL: string = BACKEND_API_MANUF_URL + '/all';