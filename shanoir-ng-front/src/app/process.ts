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

const SHANOIR_NG_URL_BACKEND = 'https://shanoir-ng-nginx';
const SHANOIR_NG_URL_BACKEND_API = SHANOIR_NG_URL_BACKEND + '/shanoir-ng';

const METADATA = {
    BACKEND_API_USERS_MS_URL: SHANOIR_NG_URL_BACKEND_API + '/users',
    BACKEND_API_STUDIES_MS_URL: SHANOIR_NG_URL_BACKEND_API + '/studies',
    BACKEND_API_DATASET_MS_URL: SHANOIR_NG_URL_BACKEND_API + '/datasets',
    BACKEND_API_IMPORT_MS_URL: SHANOIR_NG_URL_BACKEND_API + '/import',
	BACKEND_API_PRECLINICAL_MS_URL: SHANOIR_NG_URL_BACKEND_API + '/preclinical',
    KEYCLOAK_BASE_URL: SHANOIR_NG_URL_BACKEND + '/auth',
    LOGOUT_REDIRECT_URL: SHANOIR_NG_URL_BACKEND + '/test/index.html',
    ENV: 'development',
};

export const process = { env: {
    'ENV': METADATA.ENV,
    'NODE_ENV': METADATA.ENV,
    'BACKEND_API_USERS_MS_URL': METADATA.BACKEND_API_USERS_MS_URL,
    'BACKEND_API_STUDIES_MS_URL': METADATA.BACKEND_API_STUDIES_MS_URL,
    'BACKEND_API_DATASET_MS_URL': METADATA.BACKEND_API_DATASET_MS_URL,
    'BACKEND_API_IMPORT_MS_URL': METADATA.BACKEND_API_IMPORT_MS_URL,
    'BACKEND_API_PRECLINICAL_MS_URL': METADATA.BACKEND_API_PRECLINICAL_MS_URL,
    'LOGOUT_REDIRECT_URL': METADATA.LOGOUT_REDIRECT_URL,
    'KEYCLOAK_BASE_URL': METADATA.KEYCLOAK_BASE_URL,
}};