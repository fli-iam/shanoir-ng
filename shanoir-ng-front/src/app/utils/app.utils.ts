// Users http api
const BACKEND_API_USERS_MS_URL:string = process.env.BACKEND_API_USERS_MS_URL;
export const BACKEND_API_USER_ALL_URL:string = BACKEND_API_USERS_MS_URL + '/user/all';
export const BACKEND_API_USER_URL:string = BACKEND_API_USERS_MS_URL + '/user';
export const BACKEND_API_USER_ACCOUNT_REQUEST_URL:string = BACKEND_API_USERS_MS_URL + '/accountrequest';
export const BACKEND_API_USER_CONFIRM_ACCOUNT_REQUEST_URL:string = '/confirmaccountrequest';
export const BACKEND_API_USER_DENY_ACCOUNT_REQUEST_URL:string = '/denyaccountrequest';
export const BACKEND_API_ROLE_ALL_URL:string = BACKEND_API_USERS_MS_URL + '/role/all';

// Centers http api
const BACKEND_API_STUDIES_MS_URL: string = process.env.BACKEND_API_STUDIES_MS_URL;
export const BACKEND_API_CENTER_ALL_URL: string = BACKEND_API_STUDIES_MS_URL + '/center/all';
export const BACKEND_API_CENTER_URL:string = BACKEND_API_STUDIES_MS_URL + '/center';