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
