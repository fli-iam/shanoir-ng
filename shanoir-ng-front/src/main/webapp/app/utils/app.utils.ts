// Local storage keys
export const STORAGE_ACCOUNT_TOKEN:string = 'shanoirApp-account';
export const STORAGE_TOKEN:string = 'shanoirApp-token';
export const STORAGE_TOKEN_TIMEOUT:string = 'shanoirApp-tokenTimeout';
export const STORAGE_REFRESH_TOKEN:string = 'shanoirApp-refreshToken';

// Common http root api
export const BACKEND_API_ROOT_URL:string = 'http://localhost:9901';

// Users http api
const BACKEND_API_USERS_MS_URL:string = '';
export const BACKEND_API_AUTHENTICATE_PATH:string = BACKEND_API_USERS_MS_URL + '/authenticate';
export const BACKEND_API_REFRESH_AUTH_TOKEN_PATH:string = BACKEND_API_AUTHENTICATE_PATH + '/token';
export const BACKEND_API_LOGOUT_PATH:string = BACKEND_API_USERS_MS_URL + '/logout';
export const BACKEND_API_USER_ALL_URL:string = BACKEND_API_USERS_MS_URL + '/user/all';
export const BACKEND_API_USER_URL:string = BACKEND_API_USERS_MS_URL + '/user';
export const BACKEND_API_USER_ACCOUNT_REQUEST_URL:string = '/accountrequest';
export const BACKEND_API_USER_CONFIRM_ACCOUNT_REQUEST_URL:string = '/confirmaccountrequest';
export const BACKEND_API_USER_DENY_ACCOUNT_REQUEST_URL:string = '/denyaccountrequest';
export const BACKEND_API_ROLE_ALL_URL:string = BACKEND_API_USERS_MS_URL + '/role/all';

// Centers http api
// const BACKEND_API_CENTERS_MS_URL: string = '/centers';
const BACKEND_API_CENTERS_MS_URL: string = '';
export const BACKEND_API_CENTER_ALL_URL: string = BACKEND_API_CENTERS_MS_URL + '/center/all';