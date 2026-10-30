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

import { Examination } from './examination.model';
import { ExaminationPipe } from './examination.pipe';
import { SubjectExamination } from './subject-examination.model';
import { SubjectExaminationPipe } from './subject-examination.pipe';

describe('examination labels', () => {

    const date = new Date(2015, 9, 19);

    it('shows the comment between the date and the id', () => {
        const exam = Object.assign(new Examination(), { id: 4648, examinationDate: date, comment: 'THORAX' });
        expect(new ExaminationPipe().transform(exam)).toContain(' , THORAX  ( id = 4648 )');
    });

    it('does not print null for an examination without comment', () => {
        const exam = Object.assign(new Examination(), { id: 4648, examinationDate: date, comment: null });
        const label = new ExaminationPipe().transform(exam);
        expect(label).not.toContain('null');
        expect(label).toContain('( id = 4648 )');
    });

    it('does not print null for a subject examination without comment', () => {
        const exam = Object.assign(new SubjectExamination(), { id: 4648, examinationDate: date, comment: undefined });
        const label = new SubjectExaminationPipe().transform(exam);
        expect(label).not.toContain('null');
        expect(label).not.toContain('undefined');
        expect(label).toContain('( id = 4648 )');
    });

});
