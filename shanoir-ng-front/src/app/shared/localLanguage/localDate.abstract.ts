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

export const DATE_FORMAT_DISPLAY: Record<string, string> = {
    'en': 'mm/dd/yyyy',
    'fr': 'jj/mm/aaaa',
    'de': 'tt.mm.jjjj',
    'es': 'dd/mm/aaaa'
};

export const DATE_FORMAT: Record<string, string> = {
    'en': 'MM/dd/yyyy',
    'fr': 'dd/MM/yyyy',
    'de': 'dd/MM/yyyy',
    'es': 'dd/MM/yyyy'
};

export const BROWSER_LANGUAGE = navigator.language.slice(0, 2);

export const dateDisplay = DATE_FORMAT_DISPLAY[BROWSER_LANGUAGE] || 'jj/mm/aaaa';

export const dateFormat = DATE_FORMAT[BROWSER_LANGUAGE] || 'dd/MM/yyyy';
