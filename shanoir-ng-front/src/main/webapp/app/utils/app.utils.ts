// Local storage keys
export const STORAGE_ACCOUNT_TOKEN:string = 'shanoirApp-account';
export const STORAGE_TOKEN:string = 'shanoirApp-token';

// Common http root api
export const BACKEND_API_ROOT_URL:string = 'http://localhost:9901';

// Users http api
const BACKEND_API_USERS_MS_URL:string = '';
export const BACKEND_API_AUTHENTICATE_PATH:string = BACKEND_API_USERS_MS_URL + '/authenticate';
export const BACKEND_API_LOGOUT_PATH:string = BACKEND_API_USERS_MS_URL + '/logout';
export const BACKEND_API_USER_ALL_URL:string = BACKEND_API_USERS_MS_URL + '/user/all';
export const BACKEND_API_CREATE_USER_URL:string = BACKEND_API_USERS_MS_URL + '/user';
export const BACKEND_API_ROLE_ALL_URL:string = BACKEND_API_USERS_MS_URL + '/role/all';

