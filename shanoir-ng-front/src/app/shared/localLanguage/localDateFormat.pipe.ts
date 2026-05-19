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

import {Pipe, PipeTransform} from "@angular/core";
import { DatePipe } from '@angular/common';

import {BROWSER_LANGUAGE, dateFormat} from "./localDate.abstract";

@Pipe({
    name: 'localDateFormatPipe',
})

export class LocalDateFormatPipe implements PipeTransform{

    private datePipe = new DatePipe(BROWSER_LANGUAGE);

    transform(date: Date, time: boolean = false) {
        if (date) {
            let format = dateFormat
            if (time) {
                format = format + ' HH:mm:ss'
            }
            return this.datePipe.transform(date, format);
        }
        return ""
    }
}
