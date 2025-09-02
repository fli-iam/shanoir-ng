import {Pipe, PipeTransform} from "@angular/core";
import {BROWSER_LANGUAGE, dateFormat} from "./localDate.abstract";
import { DatePipe } from '@angular/common';

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
