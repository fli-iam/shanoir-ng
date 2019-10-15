
// const METADATA = {
//     BACKEND_API_USERS_MS_URL: 'https://users:9901' + '/users',
//     BACKEND_API_STUDIES_MS_URL: 'https://studies:9902' + '/studies',
//     BACKEND_API_DATASET_MS_URL: 'https://datasets:9904' + '/datasets',
//     BACKEND_API_IMPORT_MS_URL: 'https://import:9903' + '/import',
//     BACKEND_API_BOUTIQUES_MS_URL: 'https://boutiques:9000' + '/boutiques',
//     KEYCLOAK_BASE_URL: 'https://keycloak:8080/auth',
//     LOGOUT_REDIRECT_URL: 'https://localhost:4200/shanoir-ng/index.html',
//     ENV: 'development',
// };

// const METADATA = {
//     BACKEND_API_USERS_MS_URL: 'https://localhost:9901' + '/users',
//     BACKEND_API_STUDIES_MS_URL: 'https://localhost:9902' + '/studies',
//     BACKEND_API_DATASET_MS_URL: 'https://localhost:9904' + '/datasets',
//     BACKEND_API_IMPORT_MS_URL: 'https://localhost:9903' + '/import',
//     BACKEND_API_BOUTIQUES_MS_URL: 'https://localhost:9000' + '/boutiques',
//     KEYCLOAK_BASE_URL: 'https://localhost:8080/auth',
//     LOGOUT_REDIRECT_URL: 'https://localhost:4200/shanoir-ng/index.html',
//     ENV: 'development',
// };

const METADATA = {
    // BACKEND_API_USERS_MS_URL: 'https://localhost:9901' + '/users',
    // BACKEND_API_STUDIES_MS_URL: 'https://localhost:9902' + '/studies',
    // BACKEND_API_DATASET_MS_URL: 'https://localhost:9904' + '/datasets',
    // BACKEND_API_IMPORT_MS_URL: 'https://localhost:9903' + '/import',
    // BACKEND_API_BOUTIQUES_MS_URL: 'https://localhost:9000' + '/boutiques',
    // KEYCLOAK_BASE_URL: 'https://localhost:8080/auth',
    // LOGOUT_REDIRECT_URL: 'https://localhost:4200/shanoir-ng/index.html',
    // ENV: 'development',
    BACKEND_API_USERS_MS_URL: 'https://shanoir-ng-nginx/shanoir-ng' + '/users',
    BACKEND_API_STUDIES_MS_URL: 'https://shanoir-ng-nginx/shanoir-ng' + '/studies',
    BACKEND_API_DATASET_MS_URL: 'https://shanoir-ng-nginx/shanoir-ng' + '/datasets',
    BACKEND_API_IMPORT_MS_URL: 'https://shanoir-ng-nginx/shanoir-ng' + '/import',
    BACKEND_API_BOUTIQUES_MS_URL: 'https://shanoir-ng-nginx/shanoir-ng' + '/boutiques',
    KEYCLOAK_BASE_URL: 'https://shanoir-ng-nginx/auth',
    LOGOUT_REDIRECT_URL: 'http://shanoir-ng-nginx/test/index.html',
    ENV: 'development',
};


    

export var process = { env: {
    'ENV': METADATA.ENV,
    'NODE_ENV': METADATA.ENV,
    'BACKEND_API_USERS_MS_URL': METADATA.BACKEND_API_USERS_MS_URL,
    'BACKEND_API_STUDIES_MS_URL': METADATA.BACKEND_API_STUDIES_MS_URL,
    'BACKEND_API_DATASET_MS_URL': METADATA.BACKEND_API_DATASET_MS_URL,
    'BACKEND_API_IMPORT_MS_URL': METADATA.BACKEND_API_IMPORT_MS_URL,
    'BACKEND_API_BOUTIQUES_MS_URL': METADATA.BACKEND_API_BOUTIQUES_MS_URL,
    'LOGOUT_REDIRECT_URL': METADATA.LOGOUT_REDIRECT_URL,
    'KEYCLOAK_BASE_URL': METADATA.KEYCLOAK_BASE_URL,
}};