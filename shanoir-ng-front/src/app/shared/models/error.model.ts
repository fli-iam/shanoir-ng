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


export class ShanoirError {
    public code: number;
    public details: any;
    public message: string;

    static FILE_PATH_TOO_LONG: string = 'FILE_PATH_TOO_LONG';
    static FILE_TOO_BIG: string = 'FILE_TOO_BIG';
    static UNKNOWN_REASON: string = 'UNKNOWN_REASON';

    constructor(reason: {error: {code?: any, details?: any, message?: any}}) {
        this.code = reason.error.code;
        this.details = reason.error.details;
        this.message = reason.error.message;
    }

    public hasFieldError(field: string, code: string, value?: string): boolean {
        if (this.details && this.details.fieldErrors && this.details.fieldErrors[field]) {
            for (const error of this.details.fieldErrors[field]) {
                if (error.code == code && (!value || error.givenValue == value)) return true;
            }
        }
    }
}

export class RightsError extends Error {
    constructor() {
        super('You don\'t have the required rights to perform this action.');
    }
}
