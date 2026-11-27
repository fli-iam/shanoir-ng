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


import { ShanoirError } from './error.model';

describe('ShanoirError', () => {

    it('lists the field errors, so an error no field shows can be read', () => {
        const error = new ShanoirError({ error: { code: 422, message: 'Bad arguments', details: { fieldErrors: {
            protocolFilePaths: [{ code: 'invalid', message: 'Invalid file name', givenValue: '../a.pdf' }],
            name: [{ code: 'unique' }]
        } } } });

        expect(error.fieldErrorTexts()).toEqual([
            'protocolFilePaths: Invalid file name (../a.pdf)',
            'name: unique'
        ]);
    });

    it('has no field error texts without field errors', () => {
        expect(new ShanoirError({ error: { code: 422, message: 'Invalid extra data file name: ../a' } }).fieldErrorTexts()).toEqual([]);
    });
});
